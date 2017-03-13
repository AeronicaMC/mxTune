/**
 * Aeronica's mxTune MOD
 * Copyright {2016-2017} Paul Boese aka Aeronica
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
package net.aeronica.mods.mxtune.sound;

public class SoundCategoryException extends RuntimeException
{

    private static final long serialVersionUID = 4030124026364725855L;

    public SoundCategoryException() { /* empty by design */ }

    public SoundCategoryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SoundCategoryException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public SoundCategoryException(String message)
    {
        super(message);
    }

    public SoundCategoryException(Throwable cause)
    {
        super(cause);
    }
    
}
