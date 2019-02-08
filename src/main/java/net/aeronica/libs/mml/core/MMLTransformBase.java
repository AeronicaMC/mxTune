package net.aeronica.libs.mml.core;

import net.aeronica.libs.mml.core.MMLParser.AnoteContext;
import net.aeronica.libs.mml.core.MMLParser.BandContext;
import net.aeronica.libs.mml.core.MMLParser.InstContext;
import net.aeronica.libs.mml.core.MMLParser.LenContext;
import net.aeronica.mods.mxtune.util.ModLogger;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Transforms MML into a set of data structures that can be used to generate
 * MIDI, or other formats.
 * 
 * @author Paul Boese
 *
 */
public abstract class MMLTransformBase extends MMLBaseListener
{
    private StateInst instState;
    private StatePart partState;
    
    /** annotate parse tree for tied note processing */
    private ParseTreeProperty<Integer> midiNotes = new ParseTreeProperty<>();
    private ParseTreeProperty<Long> noteRestLengths = new ParseTreeProperty<>();
    private ParseTreeProperty<Integer> mmlVolumes = new ParseTreeProperty<>();
    private ParseTreeProperty<Long> startTicks = new ParseTreeProperty<>();

    /** MObject - store the parse results for later conversion to the desired format */
    private List<MObject> mObject = new ArrayList<>();
    
    public MMLTransformBase()
    {
        instState = new StateInst();
        partState = new StatePart();
    }

    @Nullable
    private Integer getMidiNote(ParserRuleContext ctx) {return ctx != null ? midiNotes.get(ctx) : null;}
    private void saveMidiNote(ParserRuleContext ctx, int midiNote) {midiNotes.put(ctx, midiNote);}

    private long getNoteRestLength(ParserRuleContext ctx) {return noteRestLengths.get(ctx);}
    private void saveNoteRestLength(ParserRuleContext ctx, long length) {noteRestLengths.put(ctx, length);}

    private long getStartTicks(ParserRuleContext ctx) {return startTicks.get(ctx);}
    private void saveStartTicks(ParserRuleContext ctx, long length) {startTicks.put(ctx, length);}

    private int getMMLVolume(ParserRuleContext ctx) {return mmlVolumes.get(ctx);}
    private void saveMMLVolume(ParserRuleContext ctx, int volume) {mmlVolumes.put(ctx, volume);}

    private void addMObject(MObject mmo) {mObject.add(mmo);}
    MObject getMObject(int index) {return mObject.get(index);}

    /**
     * <code>
     * long durationTicks(int mmlNoteLength, boolean dottedLEN) {<br>
     * <blockquote>double ppq = 480.0d;<br>
     * double dot = dottedLEN ? 15.0d : 10.0d;<br>
     * return (long) (((4.0d / (double) mmlNoteLength) * <br> 
     * (dot) / 10.0d) * ppq);</blockquote>
     * }</blockquote><br>
     * </code><br>
     * 
     * @param mmlNoteLength
     * @param dottedLEN
     * @return
     */
    public abstract long durationTicks(int mmlNoteLength, boolean dottedLEN);

    /**
     * 
     * <code> void processMObjects(List &lt;IMObjects&gt; mObjects) {<br>
     * <blockquote>IMObjects.Type type;<br>
     * <blockquote> for(int i=0; i < mmlObjects.size(); i++) {<br>
     * <blockquote> System.out.println(getMObject(i));<br>
     * switch (getMMLObject(i).getType()) {<br>
     * case INST_BEGIN: {<br>
     * <blockquote> MmlInstBegin mmo = (MmlInstBegin) getMObject(i);<br>
     * // Nothing to do in reality<br>
     * break;<br>
     * }<br>
     * </blockquote> case TEMPO: {<br>
     * <blockquote> MmlTempo mmo = (MmlTempo) getMObject(i);<br>
     * currentTempo = mmo.getTempo();<br>
     * tracks[0].add(createTempoMetaEvent(currentTempo,<br>
     * mmo.getTicksStart()+ticksOffset));<br>
     * break;<br>
     * }</blockquote></blockquote><br>
     * . . .<br>
     * </blockquote> }</blockquote><br>
     *
     * @param mObjects
     */
    public abstract void processMObjects(List<MObject> mObjects);

    @Override
    public void enterInst(InstContext ctx)
    {
        instState.init();
        partState.init();
        addMObject(new MObject.MObjectBuilder(MObject.Type.INST_BEGIN).build());
    }

    @Override
    public void exitInst(InstContext ctx)
    {
        instState.collectDurationTicks(partState.getRunningTicks());
        addMObject(new MObject.MObjectBuilder(MObject.Type.INST_END).cumulativeTicks(partState.getRunningTicks()).build());
    }

    @Override
    public void enterPart(MMLParser.PartContext ctx)
    {
        instState.collectDurationTicks(partState.getRunningTicks());
        addMObject(new MObject.MObjectBuilder(MObject.Type.PART).cumulativeTicks(partState.getRunningTicks()).build());
        partState.init();
    }

    @Override
    public void exitBand(BandContext ctx)
    {
        addMObject(new MObject.MObjectBuilder(MObject.Type.DONE).longestPartTicks(instState.getLongestDurationTicks())
                           .minVolume(instState.getMinVolume())
                           .maxVolume(instState.getMaxVolume())
                           .build());
        processMObjects(mObject);
    }

    @Override
    public void enterOctave(MMLParser.OctaveContext ctx)
    {
        if (ctx.OCTAVE() != null)
        {
            char c = ctx.OCTAVE().getText().charAt(0);
            if ('<' == c)
                partState.downOctave();
            else if ('>' == c)
                partState.upOctave();
            else
                ModLogger.error("MMLTransformBase.enterOctave: \'<\' or \'>\' expected but \'%c\' was parsed!", c);
        }
    }

    @Override
    public void enterTied(MMLParser.TiedContext ctx) {partState.setTied(true);}

    @Override
    public void exitTied(MMLParser.TiedContext ctx)
    {
        partState.setTied(false);

        List<StructTiedNotes> tiedNotes = new ArrayList<>();
        StructTiedNotes tiedNote = null;

        boolean isTied = false;
        long lengthTicks = 0;
        int noteLeft;
        int noteRight = 0;
        @Nullable
        AnoteContext ctxL;
        @Nullable
        AnoteContext ctxR = null;

        List<AnoteContext> listAnotes = ctx.anote();

        int count = listAnotes.size();

        // LEFT
        for (int i = 1; i < count; i++)
        {
            ctxL = ctx.anote(i - 1);
            ctxR = ctx.anote(i);

            if ((ctxL == null) || (ctxR == null)) continue;
            noteLeft = getMidiNote(ctxL);
            noteRight = getMidiNote(ctxR);

            // Initial LEFT
            if (!isTied)
            {
                tiedNote = new StructTiedNotes();
                lengthTicks = 0;

                tiedNote.volume = getMMLVolume(ctxL);
                tiedNote.startingTicks = getStartTicks(ctxL);
                tiedNote.midiNote = noteLeft;
            }
            if (noteLeft == noteRight)
            {
                // sum LEFTS
                lengthTicks += getNoteRestLength(ctxL);
                isTied = true;
            } else
            {
                lengthTicks += getNoteRestLength(ctxL);
                tiedNote.lengthTicks = lengthTicks;
                tiedNote.volume = getMMLVolume(ctxL);
                tiedNotes.add(tiedNote);

                addMObject(new MObject.MObjectBuilder(MObject.Type.NOTE)
                        .midiNote(tiedNote.midiNote)
                        .startingTicks(tiedNote.startingTicks)
                        .lengthTicks(lengthTicks)
                        .volume(tiedNote.volume)
                        .text(ctxL.getText())
                        .build());
                isTied = false;
            }
        }
        // LAST RIGHT
        if (isTied)
        {
            lengthTicks += getNoteRestLength(ctxR);

            tiedNote.lengthTicks = lengthTicks;
            tiedNotes.add(tiedNote);
            addMObject(new MObject.MObjectBuilder(MObject.Type.NOTE)
                    .midiNote(tiedNote.midiNote)
                    .startingTicks(tiedNote.startingTicks)
                    .lengthTicks(lengthTicks)
                    .volume(tiedNote.volume)
                    .text(ctx.getText())
                    .build());
        } else
        {
            // LAST LONELY RIGHT NOTE
            if (ctxR == null) return;

            tiedNote = new StructTiedNotes();
            tiedNote.startingTicks = getStartTicks(ctxR);
            lengthTicks = getNoteRestLength(ctxR);
            tiedNote.volume = getMMLVolume(ctxR);
            tiedNote.midiNote = noteRight;
            tiedNote.lengthTicks = lengthTicks;
            tiedNotes.add(tiedNote);

            addMObject(new MObject.MObjectBuilder(MObject.Type.NOTE)
                    .midiNote(tiedNote.midiNote)
                    .startingTicks(tiedNote.startingTicks)
                    .lengthTicks(lengthTicks)
                    .volume(tiedNote.volume)
                    .text(ctxR.getText())
                    .build());
        }
    }

    @Override
    public void enterNote(MMLParser.NoteContext ctx)
    {
        int mmlDuration = partState.getMMLLength();
        int volume = partState.getVolume();
        long startingTicks = partState.getRunningTicks();
        boolean dot = partState.isDotted();
        boolean tied = partState.isTied();

        int rawNote = ctx.NOTE().getText().toUpperCase(Locale.US).charAt(0);
        int midiNote = MMLUtil.getMIDINote(rawNote, partState.getOctave());
        if (ctx.ACC() != null)
        {
            char c = ctx.ACC().getText().charAt(0);
            switch (c)
            {
            case '+':
            case '#':
                midiNote++;
                break;
            case '-':
                midiNote--;
                break;
            default:
                // NOP
            }
        }
        if (ctx.INT() != null)
        {
            mmlDuration = intFromString(ctx.INT().getText());
            dot = false;
        }
        if (!ctx.DOT().isEmpty())
        {
            dot = true;
        }
        long length = durationTicks(mmlDuration, dot);

        if (!tied)
        {
            addMObject(new MObject.MObjectBuilder(MObject.Type.NOTE)
                    .midiNote(midiNote)
                    .startingTicks(startingTicks)
                    .lengthTicks(length)
                    .volume(volume)
                    .text(ctx.getText())
                    .build());
        }
        partState.accumulateTicks(length);

        /** annotate parse tree for tied note processing */
        saveStartTicks(ctx.getParent(), startingTicks);
        saveNoteRestLength(ctx.getParent(), length);
        saveMidiNote(ctx.getParent(), midiNote);
        saveMMLVolume(ctx.getParent(), volume);
    }

    @Override
    public void enterMidi(MMLParser.MidiContext ctx)
    {

        int mmlLength = partState.getMMLLength();
        int volume = partState.getVolume();
        long startingTicks = partState.getRunningTicks();
        boolean dot = partState.isDotted();
        boolean tied = partState.isTied();

        int midiNote = 0;
        if (ctx.INT() != null)
        {
            // "n#" format is not played correctly. One octave too low. CF Issue #6. Add 12 to fix.
            midiNote = intFromString(ctx.INT().getText()) + 12;
        }
        long lengthTicks = durationTicks(mmlLength, dot);

        if (!tied)
        {
            addMObject(new MObject.MObjectBuilder(MObject.Type.NOTE)
                    .midiNote(midiNote)
                    .startingTicks(startingTicks)
                    .lengthTicks(lengthTicks)
                    .volume(volume)
                    .text(ctx.getText())
                    .build());
        }

        partState.accumulateTicks(lengthTicks);

        // annotate parse tree for tied note processing
        saveStartTicks(ctx.getParent(), startingTicks);
        saveNoteRestLength(ctx.getParent(), lengthTicks);
        saveMidiNote(ctx.getParent(), midiNote);
        saveMMLVolume(ctx.getParent(), volume);
    }

    @Override
    public void enterRest(MMLParser.RestContext ctx)
    {
        int mmlLength = partState.getMMLLength();
        long startingTicks = partState.getRunningTicks();
        boolean dot = partState.isDotted();
        if (ctx.INT() != null)
        {
            mmlLength = intFromString(ctx.INT().getText());
            dot = false;
        }
        if (!ctx.DOT().isEmpty())
        {
            dot = true;
        }
        long lengthTicks = durationTicks(mmlLength, dot);
        partState.accumulateTicks(lengthTicks);

        saveStartTicks(ctx.getParent(), startingTicks);
        saveNoteRestLength(ctx, lengthTicks);
        saveMMLVolume(ctx, partState.getVolume());

        addMObject(new MObject.MObjectBuilder(MObject.Type.REST)
                .startingTicks(startingTicks)
                .lengthTicks(lengthTicks)
                .build());
    }

    @Override
    public void enterCmd(MMLParser.CmdContext ctx)
    {
        if (ctx.INT() == null)
            return;
        char c = ctx.CMD().getText().toUpperCase(Locale.US).charAt(0);
        int value = intFromString(ctx.INT().getText());
        switch (c)
        {
            case 'I':
            {
                instState.setInstrument(value);
                addMObject(new MObject.MObjectBuilder(MObject.Type.INST)
                                   .instrument(instState.getInstrument())
                                   .startingTicks(partState.getRunningTicks())
                                   .build());
            }
            break;
            case 'O':
                partState.setOctave(value);
                break;
            case 'P':
                // TODO: Add perform code [1|5] review
                break;
            case 'S':
                // TODO: Add sustain code [0|1]
                break;
            case 'T':
            {
                instState.setTempo(value);
                addMObject(new MObject.MObjectBuilder(MObject.Type.TEMPO)
                                   .tempo(instState.getTempo())
                                   .startingTicks(partState.getRunningTicks())
                                   .build());
            }
            break;
            case 'V':
                partState.setVolume(value);
                instState.collectVolume(value);
                break;
            default:
                // NOP
        }
    }

    @Override
    public void enterLen(LenContext ctx)
    {
        if (ctx.INT() == null)
            return;
        boolean dotted = false;
        int value = intFromString(ctx.INT().getText());
        if (!ctx.DOT().isEmpty())
        {
            dotted = true;
        }
        partState.setMMLLength(value, dotted);
    }

    private int intFromString(String s)
    {
        int length = s.length() > 8 ? 8 : s.length();
        s = s.substring(0, length);
        return Integer.parseInt(s);
    }
}
