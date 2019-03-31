/*
 * Aeronica's mxTune MOD
 * Copyright 2019, Paul Boese a.k.a. Aeronica
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package net.aeronica.mods.mxtune.util;

import net.aeronica.mods.mxtune.Reference;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.*;

public class CallBackManager
{
    private static Timer timer;
    private static Map<UUID, CallBack>  callbacks = new HashMap<>();
    private static Map<UUID, TimerTask> tasks = new HashMap<>();

    private CallBackManager() { /* NOP */ }

    public static void start()
    {
        timer = new Timer(Reference.MOD_NAME + " CallBackManager Timer");
    }

    public static void shutdown()
    {
        if (timer != null)
        {
            timer.purge();
            timer.cancel();
            timer = null;
        }
        callbacks.clear();
        tasks.clear();
    }

    public static UUID register(CallBack callback)
    {
        return register(callback, 30);
    }

    public static UUID register(CallBack callback, int timeout)
    {
        UUID uuid = UUID.randomUUID();
        if (timer != null)
        {
            callbacks.put(uuid, callback);
            scheduleTimeout(uuid, callback, timeout);
        }
        return uuid;
    }

    public static synchronized void cancel(UUID uuid)
    {
        callbacks.remove(uuid);
        tasks.get(uuid).cancel();
        tasks.remove(uuid);
    }

    private static void scheduleTimeout(UUID uuid, CallBack callback, int timeout)
    {
        if (timer != null)
        {
            TimerTask task;
            task = new TimerTask()
            {
                @Override
                public void run()
                {
                    timedOut(uuid, callback, timeout);
                }
            };
            tasks.put(uuid, task);
            long delay = timeout * 1000L;
            timer.schedule(task, delay);
        }
    }

    @Nullable
    public static synchronized CallBack getCaller(UUID uuid)
    {
        if (callbacks.containsKey(uuid))
        {
            CallBack callback = callbacks.get(uuid);
            cancel(uuid);
            return callback;
        }
        return null;
    }

    private static synchronized void timedOut(UUID uuid, CallBack callback, int timeout)
    {
        callbacks.remove(uuid);
        tasks.remove(uuid);
        callback.onFailure(new ResultMessage(true, new TextComponentTranslation("mxtune.error.network_timeout", timeout)));
    }
}
