/*
 * MIT License
 *
 * Copyright (c) 2020 Paul Boese a.k.a. Aeronica
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package aeronicamc.libs.mml.parser;

import aeronicamc.libs.mml.util.DataByteBuffer;
import aeronicamc.libs.mml.util.IndexBuffer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static aeronicamc.libs.mml.parser.ElementTypes.*;
import static aeronicamc.libs.mml.parser.MMLUtil.PPQ;
import static aeronicamc.libs.mml.parser.MMLUtil.getMIDINote;

public class MMLParser
{
    // State Collection
    private final InstState instState = new InstState();
    private final PartState partState = new PartState();
    private final TempState tempState = new TempState();
    private long longestRunningTicks = 0;
    // Collect Notes, Rests, etc.
    private final List<MMLObject> mmlObjects = new ArrayList<>(1000);
    private final StringBuilder sb = new StringBuilder();
    private final String mmlString;

    public MMLParser(String mmlString)
    {
        this.mmlString = mmlString;
        parse();
    }

    public List<MMLObject> getMmlObjects()
    {
        return mmlObjects;
    }

    private void parse()
    {
        DataByteBuffer dataBuffer = new DataByteBuffer(mmlString.getBytes(StandardCharsets.US_ASCII));
        IndexBuffer elementBuffer = new IndexBuffer(dataBuffer.getLength(), true);

        MMLLexer parser = new MMLLexer();
        parser.parse(dataBuffer, elementBuffer);

        MMLNavigator navigator = new MMLNavigator(dataBuffer, elementBuffer);
        if (!navigator.hasNext()) return;
        do
        {
            switch (navigator.type())
            {
                case MML_INSTRUMENT:
                case MML_OCTAVE:
                case MML_PERFORM:
                case MML_SUSTAIN:
                case MML_TEMPO:
                case MML_VOLUME: doCommand(navigator);
                break;
                case MML_LENGTH: doLength(navigator);
                break;
                case MML_OCTAVE_UP:
                case MML_OCTAVE_DOWN: doOctaveUpDown(navigator);
                break;
                case MML_MIDI: doMIDI(navigator);
                break;
                case MML_NOTE: doNote(navigator);
                break;
                case MML_TIE: doTie(navigator);
                break;
                case MML_REST: doRest(navigator);
                break;
                case MML_BEGIN: doBegin(navigator);
                break;
                case MML_CHORD: doChord(navigator);
                break;
                case MML_END: doEnd(navigator);
                break;
                default: /* Because SonarCloud says I need a default: */
                    navigator.next();
            }
        } while (navigator.hasNext());

        addMMLObj(new MMLObject.Builder(MMLObject.Type.DONE)
                          .longestPartTicks(getLongestRunningTicks())
                          .minVolume(instState.getMinVolume())
                          .maxVolume(instState.getMaxVolume())
                          .text("EOF")
                          .build());

        processTiedNotes();
    }

    /**
     * This is the second pass in tied note processing. The first pass identifies the NEXT notes in a tied run.
     * The second pass starts at the end of the list and disables the noteOff and or noteOn message for
     * each note in a tied run depending if it's the first, middle or last note(s) of the tie.
     * <br><br/><p>
     * i.e. first(noteOn), &GT middle(none) &LT, last(NoteOff)</p>
     */
    private void processTiedNotes()
    {
        boolean lastTied = false;
        for (int idx = mmlObjects.size() - 1; idx > 0; idx--)
        {
            MMLObject mo = mmlObjects.get(idx);
            if (mo.getType() == MMLObject.Type.PART || mo.getType() == MMLObject.Type.STOP || mo.getType() == MMLObject.Type.REST)
                lastTied = false;
            if (mo.getType() == MMLObject.Type.NOTE)
            {
                if (mo.isTied() && !lastTied) // End of tie
                {
                    lastTied = true;
                    mo.setDoNoteOn(false);
                }
                else if (mo.isTied() && lastTied) // Mid tie
                {
                    mo.setDoNoteOn(false);
                    mo.setDoNoteOff(false);
                }
                else if (!mo.isTied() && lastTied) // Begin tie
                {
                    lastTied = false;
                    mo.setDoNoteOff(false);
                }
            }
        }
    }

    private void doBegin(MMLNavigator nav)
    {
        collectDataToText(nav.asString());
        instState.init();
        partState.init();
        addMMLObj(new MMLObject.Builder(MMLObject.Type.INIT).startingTicks(partState.getRunningTicks()).text(nav.asString()).build());
        clearText();
        if (nav.hasNext())
            nav.next();
    }

    private void doChord(MMLNavigator nav)
    {
        collectDataToText(nav.anyChar());
        instState.collectDurationTicks(partState.getRunningTicks());
        addMMLObj(new MMLObject.Builder(MMLObject.Type.PART)
                          .text(getText())
                          .startingTicks(0)
                          .build());

        clearText();
        partState.init();
        if (nav.hasNext())
            nav.next();
    }

    private void doEnd(MMLNavigator nav)
    {
        collectDataToText(nav.anyChar());
        instState.collectDurationTicks(partState.getRunningTicks());
        addMMLObj(new MMLObject.Builder(MMLObject.Type.STOP)
                          .cumulativeTicks(partState.getRunningTicks())
                          .startingTicks(partState.getRunningTicks())
                          .text(getText())
                          .build());

        setLongestRunningTicks(instState.getLongestDurationTicks());
        clearText();
        if (nav.hasNext())
            nav.next();
    }

    private void doCommand(MMLNavigator nav)
    {
        char anyChar = nav.anyChar();
        byte type = nav.type();
        if (nav.hasNext())
        {
            nav.next();
            if (nav.type() == MML_NUMBER && nav.asInt() >= 0)
            {
                int value = nav.asInt();
                collectDataToText(anyChar);
                collectDataToText(value);
                switch (type)
                {
                    case MML_INSTRUMENT:
                        instState.setInstrument(nav.asInt());
                        addMMLObj(new MMLObject.Builder(MMLObject.Type.INST)
                                          .instrument(instState.getInstrument())
                                          .startingTicks(partState.getRunningTicks())
                                          .text(getText())
                                          .build());
                        clearText();
                        break;
                    case MML_OCTAVE:
                        partState.setOctave(value);
                        break;
                    case MML_PERFORM:
                        partState.setPerform(value);
                        break;
                    case MML_SUSTAIN:
                        partState.setSustain(value);
                        addMMLObj(new MMLObject.Builder(MMLObject.Type.SUSTAIN)
                                          .sustain(partState.getSustain())
                                          .startingTicks(partState.getRunningTicks())
                                          .text(getText())
                                          .build());
                        break;
                    case MML_TEMPO:
                        instState.setTempo(value);
                        addMMLObj(new MMLObject.Builder(MMLObject.Type.TEMPO)
                                          .tempo(instState.getTempo())
                                          .startingTicks(partState.getRunningTicks())
                                          .text(getText())
                                          .build());
                        break;
                    case MML_VOLUME:
                        partState.setVolume(value);
                        break;
                    default: /* NOP */
                }
            }
        }
    }

    private void doLength(MMLNavigator nav)
    {
        collectDataToText(nav.anyChar());
        if (nav.hasNext())
        {
            nav.next();
            if (nav.type() == MML_NUMBER && nav.asInt() >= 0)
            {
                int value = (nav.asInt());
                collectDataToText(value);
                partState.setMMLLength(value, false);
                if (nav.hasNext())
                    nav.next();
                if (nav.type() == MML_DOT)
                {
                    collectDataToText(nav.anyChar());
                    partState.setMMLLength(value, true);
                    if (nav.hasNext())
                        nav.next();
                }
            }
        }
    }

    private void doTie(MMLNavigator nav)
    {
        collectDataToText(nav.anyChar());
        // Only tie if the next element is a NOTE/MIDI
        byte peekValue = peekNextType(nav);
        if (peekValue == MML_NOTE || peekValue == MML_MIDI)
        {
            partState.setTied(true);
        }
        if (nav.hasNext())
            nav.next();
    }

    private void doOctaveUpDown(MMLNavigator nav)
    {
        collectDataToText(nav.anyChar());
        if (nav.type() == MML_OCTAVE_UP)
            partState.upOctave();
        else if (nav.type() == MML_OCTAVE_DOWN)
            partState.downOctave();
        if (nav.hasNext())
            nav.next();
    }

    /**
     * Process Notes 'CDEFGAB' or MIDI 'N'
     *
     * @param nav navigator reference
     */
    private void doNote(MMLNavigator nav)
    {
        int prevPitch = partState.getPrevPitch();
        tempState.init();
        collectDataToText(nav.asChar());
        tempState.setPitch(getMIDINote(nav.asChar(), partState.getOctave()));
        tempState.setDuration(partState.getMMLLength());
        tempState.setDotted(partState.isDotted());
        int nextType;
        do // handle a crazy ass run on accidental sequence +-+---++++ as seen in some whack MML.
        {
            nextType = peekNextType(nav);
            if (nextType == MML_SHARP || nextType == MML_FLAT)
            {
                nav.next();
                collectDataToText(nav.anyChar());
                tempState.setAccidental(nav.type());
                nextType = peekNextType(nav);
            }
        }
        while (nextType == MML_SHARP || nextType == MML_FLAT);

        if (nextType == MML_NUMBER)
        {
            nav.next();
            collectDataToText(nav.asInt());
            tempState.setDuration(nav.asInt());
            tempState.setDotted(false);
            nextType = peekNextType(nav);
        }
        if (nextType == MML_DOT)
        {
            nav.next();
            collectDataToText(nav.anyChar());
            tempState.setDotted(true);
        }

        long lengthTicks = durationTicks(tempState.getDuration(), tempState.isDotted());
        boolean tiedNote = (tempState.getPitch() == prevPitch && partState.isTied());

        addMMLObj(new MMLObject.Builder(MMLObject.Type.NOTE)
                          .midiNote(tempState.getPitch())
                          .startingTicks(partState.getRunningTicks())
                          .lengthTicks(lengthTicks)
                          .volume(partState.getVolume())
                          .tied(tiedNote)
                          .text(getText())
                          .build());

        partState.accumulateTicks(lengthTicks);
        partState.setPrevPitch(tempState.getPitch());
        partState.setTied(false);

        clearText();
        if (nav.hasNext())
            nav.next();
    }

    /**
     * Process manual entry MIDI note 'N'
     *
     * @param nav navigator reference
     */
    private void doMIDI(MMLNavigator nav)
    {
        int prevPitch = partState.getPrevPitch();
        tempState.init();
        collectDataToText(nav.anyChar());
        tempState.setPitch(-3); // invalid note
        tempState.setDuration(partState.getMMLLength());
        tempState.setDotted(partState.isDotted());
        int nextType;

        do // handle a crazy ass run on accidental sequence +-+---++++ as seen in some whack MML.
        {
            nextType = peekNextType(nav);
            if (nextType == MML_SHARP || nextType == MML_FLAT)
            {
                nav.next();
                // Do nothing bu eat accidentals
                nextType = peekNextType(nav);
            }
        }
        while (nextType == MML_SHARP || nextType == MML_FLAT);

        if (nextType == MML_NUMBER)
        {
            nav.next();
            collectDataToText(nav.asInt());
            tempState.setPitch(nav.asInt() + 12); //MIDI Note
            nextType = peekNextType(nav);
        }
        if (nextType == MML_DOT)
        {
            // We do nothing with dots if found.
            nav.next();
            collectDataToText(nav.anyChar());
        }

        long lengthTicks = durationTicks(tempState.getDuration(), tempState.isDotted());
        boolean tiedNote = (tempState.getPitch() == prevPitch && partState.isTied());

        if (tempState.getPitch() >= 0)
            addMMLObj(new MMLObject.Builder(MMLObject.Type.NOTE)
                              .midiNote(tempState.getPitch())
                              .startingTicks(partState.getRunningTicks())
                              .lengthTicks(lengthTicks)
                              .volume(partState.getVolume())
                              .tied(tiedNote)
                              .text(getText())
                              .build());

        partState.accumulateTicks(lengthTicks);
        partState.setPrevPitch(tempState.getPitch());
        partState.setTied(false);

        clearText();
        if (nav.hasNext())
            nav.next();
    }

    private void doRest(MMLNavigator nav)
    {
        // REST breaks ties between notes
        partState.setTied(false);
        partState.setPrevPitch(-2);

        collectDataToText(nav.anyChar());
        tempState.init();
        tempState.setDuration(partState.getMMLLength());
        tempState.setDotted(partState.isDotted());
        int nextType;
        // RESTs don't really need these, but I've seen MML where people treat them like notes. example: r&r+2.
        //  I'm guessing they simply silence notes that way for testing.
        do // handle a crazy ass run on accidental sequence +-+---++++ as seen in some whack MML.
        {
            nextType = peekNextType(nav);
            if (nextType == MML_SHARP || nextType == MML_FLAT)
            {
                nav.next();
                collectDataToText(nav.anyChar());
                nextType = peekNextType(nav);
            }
        }
        while (nextType == MML_SHARP || nextType == MML_FLAT);

        if (nextType == MML_NUMBER)
        {
            nav.next();
            collectDataToText(nav.asInt());
            tempState.setDuration(nav.asInt());
            tempState.setDotted(false);
            nextType = peekNextType(nav);
        }
        if (nextType == MML_DOT)
        {
            nav.next();
            collectDataToText(nav.anyChar());
            tempState.setDotted(true);
        }


        // Do rest Processing HERE ****
        long lengthTicks = durationTicks(tempState.getDuration(), tempState.isDotted());

        addMMLObj(new MMLObject.Builder(MMLObject.Type.REST)
                          .startingTicks(partState.getRunningTicks())
                          .lengthTicks(lengthTicks)
                          .text(getText())
                          .build());

        partState.accumulateTicks(lengthTicks);
        clearText();
        if (nav.hasNext())
            nav.next();
    }

    private byte peekNextType(MMLNavigator nav)
    {
        byte elementType = EOF;
        if (nav.hasNext())
        {
            nav.next();
            elementType = nav.type();
            nav.previous();
        }
        return elementType;
    }

    private void addMMLObj(MMLObject mmlObject)
    {
        mmlObjects.add(mmlObject);
    }

    private long durationTicks(int mmlNoteLength, boolean dottedLEN)
    {
        double dot = dottedLEN ? 15.0d : 10.0d;
        return (long) (((4.0d / (double) mmlNoteLength) * dot / 10.0d) * PPQ);
    }

    // Tick Collection
    public long getLongestRunningTicks()
    {
        return longestRunningTicks;
    }

    public void setLongestRunningTicks(long longestRunningTicks)
    {
        if (longestRunningTicks > this.longestRunningTicks) this.longestRunningTicks = longestRunningTicks;
    }

    // debugging
    private void collectDataToText(char c)
    {
        sb.append(c);
    }

    private void collectDataToText(String s)
    {
        sb.append(s);
    }

    private void collectDataToText(int number)
    {
        sb.append(number);
    }

    private String getText()
    {
        return sb.toString();
    }

    private void clearText()
    {
        try {sb.delete(0, Math.max(sb.length(), 0));} catch (Exception e) {/* NOP */}
    }
}
