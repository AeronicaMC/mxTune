package net.aeronica.libs.mml.core;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import net.aeronica.libs.mml.core.MMLParser.AnoteContext;
import net.aeronica.libs.mml.core.MMLParser.BandContext;
import net.aeronica.libs.mml.core.MMLParser.InstContext;
import net.aeronica.libs.mml.core.MMLParser.LenContext;

/**
 * Transforms MML into a set of data structures that can be used to generate
 * MIDI, or other formats.
 * 
 * @author Paul Boese
 *
 */
public abstract class MMLTransformBase extends MMLBaseListener
{
    StateInst instState;
    StatePart partState;
    ParseTreeProperty<Integer> midiNotes = new ParseTreeProperty<Integer>();
    ParseTreeProperty<Long> noteRestLengths = new ParseTreeProperty<Long>();
    ParseTreeProperty<Integer> mmlVolumes = new ParseTreeProperty<Integer>();
    ParseTreeProperty<Long> startTicks = new ParseTreeProperty<Long>();

    int getMidiNote(ParserRuleContext ctx) {return ctx != null ? midiNotes.get(ctx) : null;}

    void saveMidiNote(ParserRuleContext ctx, int midiNote) {midiNotes.put(ctx, midiNote);}

    long getNoteRestLength(ParserRuleContext ctx) {return noteRestLengths.get(ctx);};

    void saveNoteRestLength(ParserRuleContext ctx, long length) {noteRestLengths.put(ctx, length);}

    long getStartTicks(ParserRuleContext ctx) {return startTicks.get(ctx);}

    void saveStartTicks(ParserRuleContext ctx, long length) {startTicks.put(ctx, length);}

    int getMMLVolume(ParserRuleContext ctx) {return mmlVolumes.get(ctx);}

    void saveMMLVolume(ParserRuleContext ctx, int volume) {mmlVolumes.put(ctx, volume);}

    List<IMObjects> mObjects = new ArrayList<IMObjects>();

    IMObjects getMObject(int index) {return mObjects.get(index);}

    void saveMObject(IMObjects mObject) {mObjects.add(mObject);}

    public MMLTransformBase()
    {
        partState = new StatePart();
        instState = new StateInst();
    }

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
    public abstract void processMObjects(List<IMObjects> mObjects);

    @Override
    public void enterInst(InstContext ctx)
    {
        instState.init();
        partState.init();
        MOInstBegin mmo = new MOInstBegin();
        saveMObject(mmo);
    }

    @Override
    public void exitInst(InstContext ctx)
    {
        instState.collectDurationTicks(partState.getRunningTicks());
        MOInstEnd mmo = new MOInstEnd(partState.getRunningTicks());
        saveMObject(mmo);
    }

    @Override
    public void enterPart(MMLParser.PartContext ctx)
    {
        instState.collectDurationTicks(partState.getRunningTicks());
        MOPart mmo = new MOPart(partState.getRunningTicks());
        saveMObject(mmo);
        partState.init();
    }

    @Override
    public void exitBand(BandContext ctx)
    {
        MODone mmo = new MODone(instState.getLongestDurationTicks());
        saveMObject(mmo);
        processMObjects(mObjects);
    }

    @Override
    public void enterOctave(MMLParser.OctaveContext ctx)
    {
        if (ctx.OCTAVE() != null)
        {
            char c = (char) ctx.OCTAVE().getText().charAt(0);
            switch (c)
            {
            case '<':
                partState.downOctave();
                break;
            case '>':
                partState.upOctave();
                break;
            }
        }
    }

    @Override
    public void enterTied(MMLParser.TiedContext ctx) {partState.setTied(true);}

    @Override
    public void exitTied(MMLParser.TiedContext ctx)
    {
        partState.setTied(false);

        List<StructTiedNotes> tiedNotes = new ArrayList<StructTiedNotes>();
        StructTiedNotes tiedNote = null;

        boolean isTied = false;
        long lengthTicks = 0;
        int noteLeft = 0;
        int noteRight = 0;
        AnoteContext ctxL = null;
        AnoteContext ctxR = null;

        List<AnoteContext> listAnotes = ctx.anote();
        int count = listAnotes.size();

        // LEFT
        for (int i = 1; i < count; i++)
        {
            ctxL = ctx.anote(i - 1);
            ctxR = ctx.anote(i);

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

                MONote mmo = new MONote(tiedNote.midiNote, tiedNote.startingTicks, lengthTicks, tiedNote.volume);
                mmo.setText(ctxL.getText());
                saveMObject(mmo);
                isTied = false;
            }
        }
        // LAST RIGHT
        if (isTied)
        {
            lengthTicks += getNoteRestLength(ctxR);

            tiedNote.lengthTicks = lengthTicks;
            tiedNotes.add(tiedNote);
            MONote mmo = new MONote(tiedNote.midiNote, tiedNote.startingTicks, lengthTicks, tiedNote.volume);
            mmo.setText(ctx.getText());
            saveMObject(mmo);
        } else
        {
            // LAST LONELY RIGHT NOTE
            tiedNote = new StructTiedNotes();
            tiedNote.startingTicks = getStartTicks(ctxR);
            lengthTicks = getNoteRestLength(ctxR);
            tiedNote.volume = getMMLVolume(ctxR);
            tiedNote.midiNote = noteRight;
            tiedNote.lengthTicks = lengthTicks;
            tiedNotes.add(tiedNote);

            MONote mmo = new MONote(tiedNote.midiNote, tiedNote.startingTicks, lengthTicks, tiedNote.volume);
            mmo.setText(ctxR.getText());
            saveMObject(mmo);
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

        int rawNote = Integer.valueOf(ctx.NOTE().getText().toUpperCase().charAt(0));
        int midiNote = MMLUtil.getMIDINote(rawNote, partState.getOctave());
        if (ctx.ACC() != null)
        {
            char c = (char) ctx.ACC().getText().charAt(0);
            switch (c)
            {
            case '+':
            case '#':
                midiNote++;
                break;
            case '-':
                midiNote--;
                break;
            }
        }
        if (ctx.INT() != null)
        {
            mmlDuration = Integer.valueOf(ctx.INT().getText());
            dot = false;
        }
        if (!ctx.DOT().isEmpty())
        {
            dot = true;
        }
        long length = durationTicks(mmlDuration, dot);

        if (!tied)
        {
            MONote mmo = new MONote(midiNote, startingTicks, length, volume);
            mmo.setText(ctx.getText());
            saveMObject(mmo);
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
            midiNote = Integer.valueOf(ctx.INT().getText());
        }
        long lengthTicks = durationTicks(mmlLength, dot);

        if (!tied)
        {
            MONote mmo = new MONote(midiNote, startingTicks, lengthTicks, volume);
            mmo.setText(ctx.getText());
            saveMObject(mmo);
        }

        partState.accumulateTicks(lengthTicks);

        /** annotate parse tree for tied note processing */
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
            mmlLength = Integer.valueOf(ctx.INT().getText());
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

        MORest mmo = new MORest(startingTicks, lengthTicks);
        saveMObject(mmo);
    }

    @Override
    public void enterCmd(MMLParser.CmdContext ctx)
    {
        if (ctx.INT() == null) return;
        char c = (char) ctx.CMD().getText().toUpperCase().charAt(0);
        int value = Integer.valueOf(ctx.INT().getText());
        switch (c)
        {
        case 'I':
        {
            instState.setInstrument(value);
            MOInst mmo = new MOInst(instState.getInstrument(), partState.getRunningTicks());
            saveMObject(mmo);
        }
            break;
        case 'O':
            partState.setOctave(value);
            break;
        case 'T':
        {
            instState.setTempo(value);
            MOTempo mmo = new MOTempo(instState.getTempo(), partState.getRunningTicks());
            saveMObject(mmo);
        }
            break;
        case 'V':
            partState.setVolume(value);
            break;
        }

    }

    @Override
    public void enterLen(LenContext ctx)
    {
        if (ctx.INT() == null) return;
        boolean dotted = false;
        // char c = (char) ctx.LEN().getText().toUpperCase().charAt(0);
        int value = Integer.valueOf(ctx.INT().getText());
        if (!ctx.DOT().isEmpty())
        {
            dotted = true;
        }
        partState.setMMLLength(value, dotted);
    }
}
