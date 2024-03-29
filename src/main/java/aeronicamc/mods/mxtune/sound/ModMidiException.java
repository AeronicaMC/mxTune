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

package aeronicamc.mods.mxtune.sound;

public class ModMidiException extends Exception
{
    static final long serialVersionUID = -1L;
    
    public ModMidiException()
    {
        super();
    }

    public ModMidiException(String message)
    {
        super(message);
    }

    public ModMidiException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ModMidiException(Throwable cause)
    {
        super(cause);
    }

    protected ModMidiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
