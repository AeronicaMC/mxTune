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
package net.aeronica.mods.mxtune.sound;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import javax.sound.sampled.AudioFormat;

import jvst.examples.liquinth.Liquinth;
import jvst.examples.liquinth.Player;
import jvst.examples.liquinth.Sequencer;
import net.aeronica.mods.mxtune.util.ModLogger;
import paulscode.sound.ICodec;
import paulscode.sound.SoundBuffer;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemLogger;

/**
 * WARNING *** HIGHLY EXPERIMENTAL ***
 * The CodeLiquinthc class provides an ICodec interface for reading from PAT
 * files via the Liquinth library. <br>
 * http://www.deedbeef.net </b><br>
 * <br>
 * <b> This software is based on or using the Liquinth library available from
 * https://github.com/martincameron/liquinth </b><br>
 * <br>
 * <br>
 * <b> Liquinth is copyright (c) 2014, Martin Cameron <br>
 * <br>
 * <br>
 * </b>
 */
public class CodecLiquinth implements ICodec
{
    /**
     * Used to return a current value from one of the synchronized
     * boolean-interface methods.
     */
    private static final boolean GET = false;

    /**
     * Used to set the value in one of the synchronized boolean-interface
     * methods.
     */
    private static final boolean SET = true;

    /**
     * Used when a parameter for one of the synchronized boolean-interface
     * methods is not applicable.
     */
    private static final boolean XXX = false;

    /**
     * True if there is no more data to read in.
     */
    private boolean endOfStream = false;

    /**
     * True if the stream has finished initializing.
     */
    private boolean initialized = false;

    /**
     * Format the converted audio will be in.
     */
    private AudioFormat myAudioFormat = null;

    /**
     * True if the using library requires data read by this codec to be
     * reverse-ordered before returning it from methods read() and readAll().
     */
    private boolean reverseBytes = false;

    /**
     * The patch and sequence to be played.
     */
    byte[] thePatchSequence = null;

    /**
     * Liquinth Synthesizer.
     */
    private Liquinth liquinth = null;

    /**
     * Liquinth Sequencer.
     */
    private Sequencer sequencer = null;

    /**
     * Duration of the audio (in ticks). tickLen = Player.SAMPLING_RATE *
     * Player.OVERSAMPLE / 1000
     */
    private int songDurationTicks = 0;

    /**
     * Duration of the audio (in frames).
     */
    // private int songDuration = 0;

    /**
     * Audio read position (in frames).
     */
    private int playPosition = 0;

    /**
     * Processes status messages, warnings, and error messages.
     */
    private SoundSystemLogger logger;

    public CodecLiquinth()
    {
        logger = SoundSystemConfig.getLogger();
    }

    /**
     * Tells this codec when it will need to reverse the byte order of the data
     * before returning it in the read() and readAll() methods. The IBXM library
     * produces audio data in a format that some external audio libraries
     * require to be reversed. Derivatives of the Library and Source classes for
     * audio libraries which require this type of data to be reversed will call
     * the reverseByteOrder() method.
     * 
     * @param b True if the calling audio library requires byte-reversal.
     */
    @Override
    public void reverseByteOrder(boolean b) {reverseBytes = b;}

    @Override
    public boolean initialize(URL url)
    {
        initialized(SET, false);
        cleanup();

        if (liquinth == null) liquinth = new Liquinth(Player.SAMPLING_RATE); // *
                                                                             // Player.OVERSAMPLE);
        if (myAudioFormat == null) myAudioFormat = new AudioFormat(Player.SAMPLING_RATE, 16, 1, true, false);

        if (url == null)
        {
            errorMessage("URL null in method 'initialize'");
            cleanup();
            return false;
        }

        // tickLen = Player.SAMPLING_RATE / 1000 ;

        tickLen = liquinth.getSamplingRate() / 1000;

        int len = 0;
        InputStream is;
        try
        {
            is = url.openStream();
            while (is.read() > 0)
            {
                len++;
            }
            is.close();
            is = url.openStream();
            thePatchSequence = new byte[len];
            is.read(thePatchSequence, 0, thePatchSequence.length);

        } catch (IOException ioe)
        {
            errorMessage("I/O exception reading patch/sequence file in method 'initialize'");
            printStackTrace(ioe);
            return false;
        }

        try
        {
            @SuppressWarnings("unused")
            String sequence = new String(thePatchSequence, "US-ASCII");
        } catch (UnsupportedEncodingException uee)
        {
            errorMessage("Non ASCII characters detected in patch file in method 'initialize'");
            printStackTrace(uee);
            return false;
        }

        try
        {
            songDurationTicks = setPatch(thePatchSequence, tickLen);
        } catch (IllegalArgumentException iae)
        {
            errorMessage("Illegal argument in method 'initialize'");
            printStackTrace(iae);
            return false;
        } catch (IOException ioe)
        {
            errorMessage("I/O exception in method 'initialize'");
            printStackTrace(ioe);
            return false;
        }

        endOfStream(SET, false);
        initialized(SET, true);
        return true;
    }

    @Override
    public boolean initialized() {return initialized(GET, XXX);}

    @Override
    public SoundBuffer read()
    {
        if (endOfStream(GET, XXX)) return null;

        if (!initialized)
        {
            errorMessage("CodecLiquinth not initialized in 'read'");
            return null;
        }

        // Check to make sure there is an audio format:
        if (myAudioFormat == null)
        {
            errorMessage("Audio Format null in method 'read'");
            return null;
        }

        int bufferFrameSize = (int) SoundSystemConfig.getStreamingBufferSize() / 64;

        int frames = songDurationTicks - playPosition;
        if (frames > bufferFrameSize) frames = bufferFrameSize;

        if (frames <= 0)
        {
            endOfStream(SET, true);
            return null;
        }
        byte[] outputBuffer = new byte[frames * 2];
        // ModLogger.logInfo("PlayPosition = " + playPosition + ", waiting: " +
        // waiting);
        get_audio(outputBuffer, frames);
        playPosition += frames;
        if (playPosition >= songDurationTicks)
        {
            endOfStream(SET, true);
            frame = 0;
        }

        // Reverse the byte order if necessary:
        if (!reverseBytes) reverseBytes(outputBuffer, 0, frames * 2);

        // Wrap the data into a SoundBuffer:
        SoundBuffer buffer = new SoundBuffer(outputBuffer, myAudioFormat);

        return buffer;
    }

    @Override
    public SoundBuffer readAll()
    {
        if (!initialized)
        {
            errorMessage("CodecLiquinth not initialized in 'readAll'");
            return null;
        }

        // Check to make sure there is an audio format:
        if (myAudioFormat == null)
        {
            errorMessage("Audio Format null in method 'readAll'");
            return null;
        }

        int bufferFrameSize = (int) SoundSystemConfig.getStreamingBufferSize() / 4;
        ModLogger.logInfo("readAll#bufferFrameSize = " + bufferFrameSize);

        InputStream inputStream = new java.io.ByteArrayInputStream(thePatchSequence);

        byte[] outputBuf = new byte[tickLen * 2];
        byte[] fullBuf = null;

        int line = 1;
        int chr = 0;
        try
        {
            chr = inputStream.read();
        } catch (IOException ioe)
        {
            errorMessage("I/O Exception reading patch sequence");
            printStackTrace(ioe);
            return null;
        }
        char[] inputBuf = new char[8];

        int[] audioBuf = new int[tickLen];

        liquinth.allNotesOff(true);
        while (chr > 0)
        {
            while ((chr > 0 && chr <= 32) || chr == '(')
            {
                if (chr == 10) line++;
                if (chr == '(')
                {
                    while (chr > 0 && chr != ')')
                    {
                        if (chr == 10) line++;
                        try
                        {
                            chr = inputStream.read();
                        } catch (IOException ioe)
                        {
                            errorMessage("I/O Exception reading patch sequence");
                            printStackTrace(ioe);
                            return null;
                        }
                    }
                }
                try
                {
                    chr = inputStream.read();
                } catch (IOException ioe)
                {
                    errorMessage("I/O Exception reading patch sequence");
                    printStackTrace(ioe);
                    return null;
                }
            }
            int len = 0;
            while (chr > 32 && chr != '(')
            {
                if (len < inputBuf.length)
                {
                    inputBuf[len++] = (char) chr;
                } else
                {
                    throw new IllegalArgumentException("Error on line " + line + ": Token '" + new String(inputBuf, 0, len) + "' too long.");
                }
                try
                {
                    chr = inputStream.read();
                } catch (IOException ioe)
                {
                    errorMessage("I/O Exception reading patch sequence");
                    printStackTrace(ioe);
                    return null;
                }
            }
            if (len > 0)
            {
                try
                {
                    int ticks = sequencer.runCommand(new String(inputBuf, 0, len));
                    if (outputBuf != null)
                    {
                        for (int tick = 0; tick < ticks; tick++)
                        {
                            liquinth.getAudio(audioBuf, tickLen);
                            for (int outputIdx = 0; outputIdx < outputBuf.length; outputIdx += 2)
                            {
                                int amp = audioBuf[outputIdx >> 1];
                                outputBuf[outputIdx] = (byte) amp;
                                outputBuf[outputIdx + 1] = (byte) (amp >> 8);
                            }
                            fullBuf = appendByteArrays(fullBuf, outputBuf, outputBuf.length);
                        }
                    }
                } catch (IllegalArgumentException exception)
                {
                    throw new IllegalArgumentException("Error on line " + line + ": " + exception.getMessage());
                }
            }
        }
        liquinth.allNotesOff(true);

        endOfStream(SET, true);
        if (!reverseBytes) reverseBytes(fullBuf, 0, fullBuf.length);

        ModLogger.logInfo("SoundSystemConfig.getStreamingBufferSize() = " + SoundSystemConfig.getStreamingBufferSize());
        ModLogger.logInfo("BufferFrameSize = " + SoundSystemConfig.getStreamingBufferSize() / 4);
        ModLogger.logInfo("SoundSystem MaxFileSize = " + SoundSystemConfig.getMaxFileSize());
        ModLogger.logInfo("songDurationTicks = " + songDurationTicks);
        ModLogger.logInfo("readAll completeBuf.length = " + fullBuf.length);
        SoundBuffer buffer = new SoundBuffer(fullBuf, myAudioFormat);
        return buffer;
    }

    @Override
    public boolean endOfStream()
    {
        return endOfStream(GET, XXX);
    }

    @Override
    public void cleanup()
    {
        playPosition = 0;
        waiting = false;
    }

    @Override
    public AudioFormat getAudioFormat() {return myAudioFormat;}

    /**
     * Sets the patch and sequence to be played.
     * 
     * @param tickLength
     * @throws IOException
     */
    private int setPatch(byte[] inputBuf, int tickLength) throws IOException
    {
        if (inputBuf != null)
        {
            sequencer = new Sequencer(liquinth);

            // calculate song duration in frames
            return sequencer.runSequence(new java.io.ByteArrayInputStream(inputBuf), null, tickLength);
        }
        return 0;
    }

    private int tickLen; // Set in the initialize method
    private int frame;
    private int note;
    private long tick;
    private long tock; // 1 ms
    private boolean waiting;

    public void get_audio(byte[] output_buffer, int frames)
    {
        nextCmd();
        int[] audioBuf = new int[frames];
        liquinth.getAudio(audioBuf, frames);
        for (int outputIdx = 0; outputIdx < output_buffer.length; outputIdx += 2)
        {
            nextCmd();
            int amp = audioBuf[outputIdx >> 1];
            output_buffer[outputIdx] = (byte) amp;
            output_buffer[outputIdx + 1] = (byte) (amp >> 8);
        }
    }

    private boolean cmdInit = false;
    InputStream cmdIS = null;
    int cmdLine = 1;

    public void nextCmd()
    {
        nextTick();
        // Test if WAIT(x)ING: dec ticks, tocks at this point.
        // NOT_WAITING:
        // Parse commands and execute commands
        // if WAIT command
        // set/trigger WAIT(x)ING
        // else
        // WAIT(x)ING: X milliseconds: return immediately
        if (waiting) return;

        if (!cmdInit)
        {
            cmdIS = new java.io.ByteArrayInputStream(thePatchSequence);
            cmdInit = true;
        }

        int chr = 0;
        try
        {
            chr = cmdIS.read();
        } catch (IOException ioe)
        {
            errorMessage("I/O Exception reading patch sequence");
            printStackTrace(ioe);
            return;
        }
        char[] inputBuf = new char[8];

        if (chr > 0)
        {
            while ((chr > 0 && chr <= 32) || chr == '(')
            {
                if (chr == 10) cmdLine++;
                if (chr == '(')
                {
                    while (chr > 0 && chr != ')')
                    {
                        if (chr == 10) cmdLine++;
                        try
                        {
                            chr = cmdIS.read();
                        } catch (IOException ioe)
                        {
                            errorMessage("I/O Exception reading patch sequence");
                            printStackTrace(ioe);
                            return;
                        }
                    }
                }
                try
                {
                    chr = cmdIS.read();
                } catch (IOException ioe)
                {
                    errorMessage("I/O Exception reading patch sequence");
                    printStackTrace(ioe);
                    return;
                }
            }
            int len = 0;
            while (chr > 32 && chr != '(')
            {
                if (len < inputBuf.length)
                {
                    inputBuf[len++] = (char) chr;
                } else
                {
                    throw new IllegalArgumentException("Error on line " + cmdLine + ": Token '" + new String(inputBuf, 0, len) + "' too long.");
                }
                try
                {
                    chr = cmdIS.read();
                } catch (IOException ioe)
                {
                    errorMessage("I/O Exception reading patch sequence");
                    printStackTrace(ioe);
                    return;
                }
            }
            if (len > 0)
            {
                try
                {
                    ModLogger.logInfo("CMD: " + new String(inputBuf, 0, len));
                    setWait(sequencer.runCommand(new String(inputBuf, 0, len)));
                } catch (IllegalArgumentException exception)
                {
                    throw new IllegalArgumentException("Error on line " + cmdLine + ": " + exception.getMessage());
                }
            }
        }
    }

    public void nextTick()
    {
        if (waiting)
        {
            if ((++tick % tickLen) == 0)
            {
                if (tock-- <= 0)
                {
                    waiting = false;
                }
            }
        }
    }

    public void setWait(int ms)
    {
        if (ms <= 0) return;
        // ModLogger.logInfo("setWait = " + ms + ", tickLen = " + tickLen);
        tock = ms;
        tick = 0;
        waiting = true;
    }

    // private static final int
    // CTRL_OVERDRIVE = 0,
    // CTRL_REVERB_TIME = 1,
    // CTRL_FILTER_CUTOFF = 2,
    // CTRL_FILTER_RESONANCE = 3,
    // CTRL_FILTER_DETUNE = 4,
    // CTRL_FILTER_ATTACK = 5,
    // CTRL_FILTER_SUSTAIN = 6,
    // CTRL_FILTER_DECAY = 7,
    // CTRL_FILTER_MODULATION = 8,
    // CTRL_PORTAMENTO = 9,
    // CTRL_WAVEFORM = 10,
    // CTRL_VOLUME_ATTACK = 11,
    // CTRL_VOLUME_RELEASE = 12,
    // CTRL_OSCILLATOR_DETUNE = 13,
    // CTRL_VIBRATO_SPEED = 14,
    // CTRL_VIBRATO_DEPTH = 15,
    // CTRL_PULSE_WIDTH = 16,
    // CTRL_PULSE_WIDTH_MODULATION = 17,
    // CTRL_SUB_OSCILLATOR = 18,
    // CTRL_TIMBRE = 19,
    // NUM_CONTROLLERS = 20,
    // NUM_VOICES = 16;

    void randomNote()
    {
        switch (frame++)
        {
        case 0:
            note = (int) (Math.random() * 12) + 72;
            liquinth.setController(0, 44);
            liquinth.setController(2, 127);
            liquinth.setController(11, 6);
            liquinth.setController(12, 127);
            liquinth.setController(4, 2);
            liquinth.noteOn(note, 127);
        case 1:
            break;
        case 2:
            liquinth.noteOff(note);
            break;
        case 3:
            break;
        case 4:
            break;
        case 5:
        case 6:
        case 7:
            break;
        case 8:
        case 9:
        case 10:
        case 11:
        case 12:
            break;
        default:
            liquinth.allNotesOff(true);
            frame = -1;
        }
    }

    /**
     * Internal method for synchronizing access to the boolean 'initialized'.
     * 
     * @param action
     *            GET or SET.
     * @param value
     *            New value if action == SET, or XXX if action == GET.
     * @return True if steam is initialized.
     */
    private synchronized boolean initialized(boolean action, boolean value)
    {
        if (action == SET) initialized = value;
        return initialized;
    }

    /**
     * Internal method for synchronizing access to the boolean 'endOfStream'.
     * 
     * @param action
     *            GET or SET.
     * @param value
     *            New value if action == SET, or XXX if action == GET.
     * @return True if end of stream was reached.
     */
    private synchronized boolean endOfStream(boolean action, boolean value)
    {
        if (action == SET) endOfStream = value;
        return endOfStream;
    }

    /**
     * Trims down the size of the array if it is larger than the specified
     * maximum length.
     * 
     * @param array
     *            Array containing audio data.
     * @param maxLength
     *            Maximum size this array may be.
     * @return New array.
     */
    @SuppressWarnings("unused") // Forge
    private static byte[] trimArray(byte[] array, int maxLength)
    {
        byte[] trimmedArray = null;
        if (array != null && array.length > maxLength)
        {
            trimmedArray = new byte[maxLength];
            System.arraycopy(array, 0, trimmedArray, 0, maxLength);
        }
        return trimmedArray;
    }

    /**
     * Reverse-orders all bytes contained in the specified array.
     * 
     * @param buffer
     *            Array containing audio data.
     */
    public static void reverseBytes(byte[] buffer)
    {
        reverseBytes(buffer, 0, buffer.length);
    }

    /**
     * Reverse-orders the specified range of bytes contained in the specified
     * array.
     * 
     * @param buffer
     *            Array containing audio data.
     * @param offset
     *            Array index to begin.
     * @param size
     *            number of bytes to reverse-order.
     */
    public static void reverseBytes(byte[] buffer, int offset, int size)
    {
        byte b;
        for (int i = offset; i < (offset + size); i += 2)
        {
            b = buffer[i];
            buffer[i] = buffer[i + 1];
            buffer[i + 1] = b;
        }
    }

    /**
     * Converts sound bytes to little-endian format.
     * 
     * @param audio_bytes
     *            The original wave data
     * @param two_bytes_data
     *            For stereo sounds.
     * @return byte array containing the converted data.
     */
    @SuppressWarnings("unused") // Forge
    private static byte[] convertAudioBytes(byte[] audio_bytes, boolean two_bytes_data)
    {
        ByteBuffer dest = ByteBuffer.allocateDirect(audio_bytes.length);
        dest.order(ByteOrder.nativeOrder());
        ByteBuffer src = ByteBuffer.wrap(audio_bytes);
        src.order(ByteOrder.LITTLE_ENDIAN);
        if (two_bytes_data)
        {
            ShortBuffer dest_short = dest.asShortBuffer();
            ShortBuffer src_short = src.asShortBuffer();
            while (src_short.hasRemaining())
            {
                dest_short.put(src_short.get());
            }
        } else
        {
            while (src.hasRemaining())
            {
                dest.put(src.get());
            }
        }
        dest.rewind();

        if (!dest.hasArray())
        {
            byte[] arrayBackedBuffer = new byte[dest.capacity()];
            dest.get(arrayBackedBuffer);
            dest.clear();

            return arrayBackedBuffer;
        }

        return dest.array();
    }

    /**
     * Creates a new array with the second array appended to the end of the
     * first array.
     * 
     * @param arrayOne
     *            The first array.
     * @param arrayTwo
     *            The second array.
     * @param length
     *            How many bytes to append from the second array.
     * @return Byte array containing information from both arrays.
     */
    private static byte[] appendByteArrays(byte[] arrayOne, byte[] arrayTwo, int length)
    {
        byte[] newArray;
        if (arrayOne == null && arrayTwo == null)
        {
            // no data, just return
            return null;
        } else if (arrayOne == null)
        {
            // create the new array, same length as arrayTwo:
            newArray = new byte[length];
            // fill the new array with the contents of arrayTwo:
            System.arraycopy(arrayTwo, 0, newArray, 0, length);
            arrayTwo = null;
        } else if (arrayTwo == null)
        {
            // create the new array, same length as arrayOne:
            newArray = new byte[arrayOne.length];
            // fill the new array with the contents of arrayOne:
            System.arraycopy(arrayOne, 0, newArray, 0, arrayOne.length);
            arrayOne = null;
        } else
        {
            // create the new array large enough to hold both
            // arrays:
            newArray = new byte[arrayOne.length + length];
            System.arraycopy(arrayOne, 0, newArray, 0, arrayOne.length);
            // fill the new array with the contents of both arrays:
            System.arraycopy(arrayTwo, 0, newArray, arrayOne.length, length);
            arrayOne = null;
            arrayTwo = null;
        }
        return newArray;
    }

    /**
     * Prints an error message.
     * 
     * @param message
     *            Message to print.
     */
    private void errorMessage(String message)
    {
        logger.errorMessage("CodecLiquinth", message, 0);
    }

    /**
     * Prints an exception's error message followed by the stack trace.
     * 
     * @param e
     *            Exception containing the information to print.
     */
    private void printStackTrace(Exception e)
    {
        logger.printStackTrace(e, 1);
    }
}
