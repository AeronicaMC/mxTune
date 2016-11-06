/**
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese a.k.a. Aeronica
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.aeronica.mods.mxtune.mml;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.sound.midi.Instrument;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import net.aeronica.libs.mml.core.MMLLexer;
import net.aeronica.libs.mml.core.MMLParser;
import net.aeronica.libs.mml.core.MMLToMIDI;
import net.aeronica.mods.mxtune.groups.GROUPS;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.bidirectional.StopPlayMessage;
import net.aeronica.mods.mxtune.util.ModLogger;

public class MMLPlayer implements MetaEventListener
{
    private Sequencer sequencer = null;
    private Synthesizer synthesizer = null;
    private Instrument[] instruments;
    private Soundbank defaultSB;
    private static byte[] mmlBuf = null;
    private InputStream is;
    private Integer playID = null;
    private Map<Integer, String> playerMML = new HashMap<Integer, String>();
    private Map<Integer, Integer> playerChannel = new HashMap<Integer, Integer>();
    private boolean closeGUI = true;
    private float fakeVolume;
    private boolean midiException = false;
    
    // private Soundbank sbJammer;
    // private static final ResourceLocation soundFont = new ResourceLocation(
    // ModInfo.ID.toLowerCase(), "synth/MCJammerLT.sf2");

    /**
     * Solo play format "<playerName|groupID>=mml@...;"
     * 
     * Jam play format inserts with a space between each player=MML sequence
     * "<playername1>=MML@...abcd; <playername2>=MML@...efgh; <playername2>=MML@...efgh;"
     * 
     * @param mmml
     * @param groupID
     * @return
     */
    @SuppressWarnings("static-access")
    public boolean mmlPlay(String mmml, Integer groupID, boolean closeGUI, float volumeIn)
    {
        /** Only one playing instance per playID at a time. */
        if (!MMLManager.getInstance().registerThread(this, groupID)) return false;
        this.playID = groupID;
        this.playerMML = GROUPS.deserializeIntStrMap(mmml);
        this.closeGUI = closeGUI;
        this.fakeVolume = volumeIn;
        String mml = new String();

        if (playerMML == null)
        {
            ModLogger.logInfo("playerMML is null! Check for an issue with NBT, networking, threads");
            MMLManager.getInstance().resetMute();
            return false;
        }

        /**
         * Map players to channels and append all the players MML.
         * This mapping is also used to send packets to the close the gui of the musicians
         */
        Set<Integer> keys = playerMML.keySet();
        Iterator<Integer> it = keys.iterator();
        Integer ch = 0;
        while (it.hasNext())
        {
            Integer playerID = it.next();
            playerChannel.put(playerID, ch);
            ch++;
            mml += playerMML.get(playerID);
        }

        try
        {
            mmlBuf = mml.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
            return false;
        }
        is = new java.io.ByteArrayInputStream(mmlBuf);

        /** ANTLR4 MML Parser BEGIN */
        MMLToMIDI mmlTrans = new MMLToMIDI(this.fakeVolume);
        ANTLRInputStream input = null;

        try
        {
            input = new ANTLRInputStream(is);
        } catch (IOException e1)
        {
            e1.printStackTrace();
            return false;
        }
        MMLLexer lexer = new MMLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MMLParser parser = new MMLParser(tokens);
        // parser.removeErrorListeners();
        // parser.addErrorListener(new UnderlineListener());
        parser.setBuildParseTree(true);
        ParseTree tree = parser.band();

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(mmlTrans, tree);
        /** ANTLR4 MML Parser END */

        ModLogger.debug("playerChannel: " + playerChannel);
        ModLogger.debug("Playing for:   " + groupID);

        try
        {
            /** Using the default sequencer and synthesizer */
            synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            defaultSB = synthesizer.getDefaultSoundbank();
            synthesizer.unloadAllInstruments(defaultSB);
            instruments = defaultSB.getInstruments();
            if (instruments.length == 0) throw new Exception("No Instruments Available!");
            for (Integer patch: mmlTrans.getPatches())
            {
                synthesizer.loadInstrument(instruments[patch]);
            }
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequencer.addMetaEventListener(this);
            sequencer.setSequence(mmlTrans.getSequence());
            sequencer.setTickPosition(0L);
            /** Sleep and let the synthesizer stabilize */
            try {Thread.sleep(250);} catch (InterruptedException e) {}
            sequencer.start();
        } catch (Exception e)
        {
            MMLManager.getInstance().deregisterThread(groupID);
            MMLManager.unMuteSounds();
            if (sequencer != null && sequencer.isOpen()) sequencer.close();
            if (synthesizer != null && synthesizer.isOpen()) synthesizer.close();
            ModLogger.logError("MMLPlayer#mmlPlay MIDI failure: " + e.getLocalizedMessage());
            e.printStackTrace();
            midiException = true;
            return false;
        }
        finally
        {
            if (midiException)
            {
                if (sequencer != null) 
                    sequencer.removeMetaEventListener(this);
                return false;
            }
        }
        return true;
    }

    public synchronized void mmlKill(Integer iD, boolean closeGui)
    {

        if (playID == null) return;

        if (!playerChannel.isEmpty() && playerChannel.containsKey(iD))
        {
            /**
             * If this is a JAM, tell the JAMMERs they are done. If you're a
             * member, but did not queue MML then your request to close will be
             * ignored. Solo players force close themselves.
             */
            ModLogger.debug("MMLPlayer#mmlKill: " + iD);
            if (sequencer != null && sequencer.isOpen())
            {
                try
                {
                    sequencer.stop();
                    sequencer.setMicrosecondPosition(0L);
                    sequencer.removeMetaEventListener(this);
                } catch (Exception e) {}
            }

            try
            {
                Thread.sleep(250);
            } catch (InterruptedException e)
            {
            }
            if (sequencer != null && sequencer.isOpen()) sequencer.close();
            if (synthesizer != null && synthesizer.isOpen()) synthesizer.close();

            Set<Integer> keys = playerChannel.keySet();
            if (closeGui && this.closeGUI)
            {
                Iterator<Integer> it = keys.iterator();
                while (it.hasNext())
                {
                    Integer playerID = it.next();
                    PacketDispatcher.sendToServer(new StopPlayMessage(playerID));
                }
            }
            playID = null;
            Integer resultID = GROUPS.getMembersGroupID(iD) != null ? GROUPS.getMembersGroupID(iD) : iD;
            MMLManager.getInstance().deregisterThread(resultID);
        }
    }

    public void mmlAbort(){
        if (sequencer != null && sequencer.isOpen())
        {
            sequencer.stop();
            sequencer.setMicrosecondPosition(0L);
            sequencer.removeMetaEventListener(this);
        }
        try
        {
            Thread.sleep(250);
        } catch (InterruptedException e) {}
        if (sequencer != null && sequencer.isOpen()) sequencer.close();
        if (synthesizer != null && synthesizer.isOpen()) synthesizer.close();
    }
    
    @Override
    public void meta(MetaMessage event)
    {
        if (event.getType() == 47)
        { // end of stream
            sequencer.stop();
            sequencer.setTickPosition(0L);
            sequencer.removeMetaEventListener(this);
            ModLogger.debug("MetaMessage EOS event for: " + playID);
            try
            {
                Thread.sleep(250);
            } catch (InterruptedException e) {}
            if (sequencer != null && sequencer.isOpen()) sequencer.close();
            if (synthesizer != null && synthesizer.isOpen()) synthesizer.close();

            /**
             * If this is a JAM or Solo, tell the player(s) they are done
             */
            MMLManager.getInstance().deregisterThread(playID);
            if (!playerChannel.isEmpty())
            {
                Set<Integer> keys = playerChannel.keySet();
                Iterator<Integer> it = keys.iterator();
                if (closeGUI)
                {
                    while (it.hasNext())
                    {
                        Integer playerID = it.next();
                        PacketDispatcher.sendToServer(new StopPlayMessage(playerID));
                    }
                }
            }
        }
    }
}
