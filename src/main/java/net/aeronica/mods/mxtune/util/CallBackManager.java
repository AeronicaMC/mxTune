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
    private static Map<UUID, CallBackData>  callbacks = new HashMap<>();
    private static Map<UUID, TimerTask> tasks = new HashMap<>();

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
    }

    public static UUID register(CallBack callBack, @Nullable Enum<?> xEnum)
    {
        return register(callBack, xEnum, 30);
    }

    public static UUID register(CallBack callback, @Nullable Enum<?> xEnum, int timeout)
    {
        UUID uuid = UUID.randomUUID();
        if (timer != null)
        {
            CallBackData callBackData = new CallBackData(callback, xEnum);
            callbacks.put(uuid, callBackData);
            scheduleTimeout(uuid, callBackData, timeout);
        }
        return uuid;
    }

    public static synchronized void cancel(UUID uuid)
    {
        callbacks.remove(uuid);
        tasks.get(uuid).cancel();
        tasks.remove(uuid);
    }

    private static void scheduleTimeout(UUID uuid, CallBackData callBackData, int timeout)
    {
        if (timer != null)
        {
            TimerTask task;
            task = new TimerTask()
            {
                @Override
                public void run()
                {
                    timedOut(uuid, callBackData, timeout);
                }
            };
            tasks.put(uuid, task);
            long delay = timeout * 1000L;
            timer.schedule(task, delay);
        }
    }

    @Nullable
    public static synchronized CallBackData getCaller(UUID uuid)
    {
        if (callbacks.containsKey(uuid))
        {
            CallBackData callBackData = callbacks.get(uuid);
            cancel(uuid);
            return callBackData;
        }
        return null;
    }

    private static synchronized void timedOut(UUID uuid, CallBackData callBackData, int timeout)
    {
        callbacks.remove(uuid);
        tasks.remove(uuid);
        callBackData.callBack.onFailure(new TextComponentTranslation("mxtune.error.network_timeout", timeout));
    }

    public static final class CallBackData
    {
        public CallBack callBack;
        public Enum<?> xEnum;

        CallBackData(CallBack callBack, @Nullable Enum<?> xEnum)
        {
            this.callBack = callBack;
            this.xEnum = xEnum;
        }
    }
}
