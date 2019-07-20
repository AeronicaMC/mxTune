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
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.*;

public class CallBackManager
{
    private static Timer timer;
    private static Map<UUID, CallBack>  callbacks = new HashMap<>();
    private static Map<UUID, TimerTask> tasks = new HashMap<>();
    private static Map<UUID, Notify>    notified = new HashMap<>();

    private CallBackManager() { /* NOP */ }

    public static void start()
    {
        if (timer == null)
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
        notified.clear();
    }

    public static UUID register(CallBack callBack)
    {
        return register(callBack, 30);
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

    public static UUID register(CallBack callBack, Notify notify)
    {
        UUID uuid = register(callBack, 30);
        notified.put(uuid, notify);
        return uuid;
    }

    public static synchronized void cancel(UUID uuid)
    {
        callbacks.remove(uuid);
        tasks.get(uuid).cancel();
        tasks.remove(uuid);
    }

    private static void scheduleTimeout(UUID uuid, CallBack callBack, int timeout)
    {
        if (timer != null)
        {
            TimerTask task;
            task = new TimerTask()
            {
                @Override
                public void run()
                {
                    timedOut(uuid, callBack, timeout);
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
            CallBack callBack = callbacks.get(uuid);
            cancel(uuid);
            return callBack;
        }
        return null;
    }

    @Nullable
    public static synchronized Notify getNotified(UUID uuid)
    {
        Notify notify = notified.get(uuid);
        notified.remove(uuid);
        return notify;
    }

    private static synchronized void timedOut(UUID uuid, CallBack callBack, int timeout)
    {
        callbacks.remove(uuid);
        notified.remove(uuid);
        tasks.remove(uuid);
        callBack.onFailure(new TranslationTextComponent("mxtune.error.network_timeout", timeout));
    }
}
