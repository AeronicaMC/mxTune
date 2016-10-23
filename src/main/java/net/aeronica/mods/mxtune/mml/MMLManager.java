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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.aeronica.mods.mxtune.groups.GROUPS;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.util.SoundCategory;

/**
 * Creates a thread pool of 20 to keep the MIDI system initializations and MML
 * parsing out of the game loop to keep graphic updates as smooth as silk.
 * 
 * @author Aeronica
 * 
 */
public class MMLManager
{
    private static final int NTHREDS = 20; // XXX This is probably too many and wasteful
    private static ExecutorService executor;
    private static volatile Map<Integer, MMLPlayer> mmlThreads = new HashMap<Integer, MMLPlayer>();

    private static Minecraft mc;

    /**
     * Counted mute status Since there can be multiple playing instances we only
     * want to mute sounds once. New play instances increment the count. As each
     * instance completes or is cancelled the count is decremented.
     */
    private static int muted = 0;

    /** A temporary storage for saved sound category levels */
    private static Map<SoundCategory, String> savedSoundLevels = new HashMap<SoundCategory, String>();

    /** Don't allow any other class to instantiate the MMLManager */
    private MMLManager()
    {
        executor = Executors.newFixedThreadPool(NTHREDS);
        mc = Minecraft.getMinecraft();
    }

    /**
     * This Class is loaded on the first execution of Class.getClass() or the
     * first access to ClassHolder.INSTANCE, not before. ref:
     * http://en.wikipedia.org/wiki/Singleton_pattern
     */
    private static class MMLManagerHolder {public static final MMLManager INSTANCE = new MMLManager();}

    public static MMLManager getInstance() {return MMLManagerHolder.INSTANCE;}

    public void mmlPlay(String mml, Integer playerID, boolean closeGUI, float volumeIn)
    {
        Runnable worker = new ThreadedmmlPlay(mml, playerID, closeGUI, volumeIn);
        executor.execute(worker);
    }

    /** TODO: Make a real initialization */
    public void mmlInit()
    {
        this.mmlPlay("-Init-=MML@t240v0l64crrrbeadgcf,,;", -1, false, 0F);
    }

    public static void resetMute()
    {
        if (muted > 0)
        {
            muted = 0;
            ModLogger.debug("Un-mute and RESET sound levels");
            loadLevelsAndUnMute();
        }
    }

    public void abortAll()
    {
        resetMute();
        if (mmlThreads != null)
        {
            Set<Integer> set = mmlThreads.keySet();
            for (Integer resultID : set)
            {
                try
                {
                    MMLPlayer pInstance = mmlThreads.get(resultID);
                    if (pInstance != null)
                    {
                        Runnable worker = new ThreadedmmlAbort(pInstance);
                        executor.execute(worker);
                    }
                } catch (IllegalArgumentException e)
                {
                    ModLogger.logError(e.getLocalizedMessage());
                    e.printStackTrace();
                } catch (Exception e)
                {
                    ModLogger.logError(e.getLocalizedMessage());
                    e.printStackTrace();
                }
                ModLogger.debug("MMLManager.mmlAbort(): " + resultID);
            }
        }
        ModLogger.debug("MMLManager playerLogOff or forced abort!");
    }
    
    public static void muteSounds()
    {
        if (muted++ > 0)
        {
            ModLogger.debug("Mute count: " + muted);
            return;
        } else
        {
            ModLogger.debug("Mute sounds");
            saveLevelsAndMute();
        }
    }

    public static void unMuteSounds()
    {
        --muted;
        if (muted > 0)
        {
            ModLogger.debug("Un-mute count: " + muted);
        } else
        {
            muted = 0;
            ModLogger.debug("Un-mute sounds");
            loadLevelsAndUnMute();
        }
    }

    // XXX  - Test muting MUSIC only!
    private static void saveLevelsAndMute()
    {
        Float v = mc.gameSettings.getSoundLevel(SoundCategory.MUSIC);
        savedSoundLevels.put(SoundCategory.MUSIC, v.toString());
        mc.gameSettings.setSoundLevel(SoundCategory.MUSIC, muteLevel(SoundCategory.MUSIC, v));
        mc.gameSettings.setSoundLevel(SoundCategory.MUSIC, muteLevel(SoundCategory.MUSIC, v));
        mc.gameSettings.saveOptions();
    }

    // XXX - Test muting MUSIC only!
    private static void loadLevelsAndUnMute()
    {
        restoreLevel(SoundCategory.MUSIC, Float.valueOf(savedSoundLevels.get(SoundCategory.MUSIC)));
        mc.gameSettings.saveOptions();
    }

    // XXX - Test muting MUSIC only
    /** Sound levels are totally hosed so set to 50% */
    public static void fixLevels()
    {
        restoreLevel(SoundCategory.MUSIC, 0.33F);
        mc.gameSettings.saveOptions();
    }
    
    private static float muteLevel(SoundCategory sc, float level)
    {
        switch (sc)
        {
        case MASTER:
            return level;

        case MUSIC:
            return 0f;

        case RECORDS:
            return 0f;

        case BLOCKS:
            return 0f;

        case HOSTILE:
            return 0.1f;

        case NEUTRAL:
            return 0.0f;

        case PLAYERS:
            return 0.1f;

        case AMBIENT:
            return 0f;

        case WEATHER:
            return 0f;

        case VOICE:
            return 0.1f;

        default:
            return 0.1f;
        }
    }

    private static void restoreLevel(SoundCategory sc, float level)
    {
        mc.gameSettings.setSoundLevel(sc, level);
        mc.gameSettings.setSoundLevel(sc, level);
    }

    /**
     * 
     * @param mmlPlayer
     * @param groupID
     * @return false if ID is already registered, or true is successfully
     *         registered
     */
    public boolean registerThread(MMLPlayer mmlPlayer, Integer groupID)
    {
        boolean result = false;
        try
        {
            if (mmlThreads != null)
            {
                if (!mmlThreads.containsKey(groupID))
                {
                    mmlThreads.put(groupID, mmlPlayer);
                    muteSounds();
                    result = true;
                }
            }
        } catch (IllegalArgumentException e)
        {
            ModLogger.logError(e.getLocalizedMessage());
            e.printStackTrace();
        } catch (Exception e)
        {
            ModLogger.logError(e.getLocalizedMessage());
            e.printStackTrace();
        }
        ModLogger.debug("registerThread: " + groupID + " " + result);
        return result;
    }

    /**
     * de-register the playing thread.
     * 
     * @param resultID
     */
    public void deregisterThread(Integer resultID)
    {
        if (mmlThreads != null)
        {
            if (mmlThreads.containsKey(resultID))
            {
                try
                {
                    mmlThreads.remove(resultID);
                } catch (IllegalArgumentException e)
                {
                    ModLogger.logError(e.getLocalizedMessage());
                    e.printStackTrace();
                } catch (Exception e)
                {
                    ModLogger.logError(e.getLocalizedMessage());
                    e.printStackTrace();
                }
                unMuteSounds();
                ModLogger.debug("deregisterThread: " + resultID);
            }
        }
    }

    public void mmlKill(Integer playID, boolean closeGUI)
    {
        /**
         * Quite a mess, but here we get the group ID if there is one associated
         * with the player. We use that to get the instance.
         */
        Integer resultID = GROUPS.getMembersGroupID(playID) != null ? GROUPS.getMembersGroupID(playID) : playID;
        if (mmlThreads != null)
        {
            if (mmlThreads.containsKey(resultID))
            {
                try
                {
                    MMLPlayer pInstance = mmlThreads.get(resultID);
                    if (pInstance != null)
                    {
                        Runnable worker = new ThreadedmmlKill(pInstance, playID, closeGUI);
                        executor.execute(worker);
                    }
                } catch (IllegalArgumentException e)
                {
                    ModLogger.logError(e.getLocalizedMessage());
                    e.printStackTrace();
                } catch (Exception e)
                {
                    ModLogger.logError(e.getLocalizedMessage());
                    e.printStackTrace();
                }
                ModLogger.debug("MMLManager.mmlKill(): " + resultID + ", playID: " + playID);
            }
        }
    }

    private class ThreadedmmlPlay implements Runnable
    {
        private final String mml;
        private final Integer groupID;
        private boolean closeGUI;
        private float volumeIn;

        public ThreadedmmlPlay(String mml, Integer playerID, boolean closeGUI, float volumeIn)
        {
            this.mml = mml;
            this.groupID = playerID;
            this.closeGUI = closeGUI;
            this.volumeIn = volumeIn;
        }

        @Override
        public void run()
        {
            MMLPlayer mp = new MMLPlayer();
            ModLogger.debug("ThreadedmmlPlay mml: " + mml.substring(0, mml.length() > 25 ? 25 : mml.length()));
            ModLogger.debug("ThreadedmmlPlay groupID: " + groupID);
            mp.mmlPlay(mml, groupID, closeGUI, volumeIn);
        }
    }

    private class ThreadedmmlKill implements Runnable
    {
        private final MMLPlayer pinstance;
        private final Integer ID;
        private final boolean closeGUI;

        public ThreadedmmlKill(MMLPlayer pInstance, Integer playID, boolean closeGUI)
        {
            this.pinstance = pInstance;
            this.ID = playID;
            this.closeGUI = closeGUI;
        }

        @Override
        public void run()
        {
            pinstance.mmlKill(ID, closeGUI);
        }
    }
    
    private class ThreadedmmlAbort implements Runnable
    {
        private final MMLPlayer pinstance;
        
        public ThreadedmmlAbort(MMLPlayer pInstance)
        {
            this.pinstance = pInstance;
        }
        
        @Override
        public void run()
        {
            pinstance.mmlAbort();
        }
    }
}
