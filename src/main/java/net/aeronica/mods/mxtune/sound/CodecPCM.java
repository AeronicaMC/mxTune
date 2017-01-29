package net.aeronica.mods.mxtune.sound;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Random;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import paulscode.sound.ICodec;
import paulscode.sound.SoundBuffer;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemLogger;

public class CodecPCM implements ICodec {
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
	 * The audio stream from the synthesizer
	 */
    AudioInputStream audioInputStream = null;

    /**
     * A dummy stream just to open a handle to the proxy sound file.
     */
    AudioInputStream dummyInputStream = null;

    public static final int SAMPLE_SIZE = 10240;
    
    byte noiseBuffer[] = new byte[SAMPLE_SIZE];
    byte zeroBuffer[] = new byte[SAMPLE_SIZE];
    
    Random randInt;
	
    Integer playID = null;
    
	/**
	 * Processes status messages, warnings, and error messages.
	 */
	private SoundSystemLogger logger;

	public CodecPCM() {
		logger = SoundSystemConfig.getLogger();
        randInt = new java.util.Random(System.currentTimeMillis());
        nextBuffer(SAMPLE_SIZE);
	}
	
    private void nextBuffer(int sampleSize)
    {
        for (int i=0;i<sampleSize; i+=2){
            int x = ((short) (randInt.nextInt()/3)*2);
            noiseBuffer[i] = (byte) x;
            noiseBuffer[i+1]= (byte) (x >> 8);
            zeroBuffer[i] = zeroBuffer[i+1] = 0;
        } 
    }
    
	/**
	 * Tells this CODEC when it will need to reverse the byte order of the data
	 * before returning it in the read() and readAll() methods. The MML2PCM
	 * class produces audio data in a format that some external audio libraries
	 * require to be reversed. Derivatives of the Library and Source classes for
	 * audio libraries which require this type of data to be reversed will call
	 * the reverseByteOrder() method.
	 * 
	 * @param b
	 *            True if the calling audio library requires byte-reversal.
	 */
	@Override
	public void reverseByteOrder(boolean b) {
		reverseBytes = b;
	}
	
    @Override
    public boolean initialize(URL url)
    {
        
        initialized(SET, false);
        if (playID == null)
        {
            if ((playID = ClientAudio.pollPlayIDQueue02()) == null )
            {
                errorMessage("playID not initialized: " + playID);
                return false;
            } else
            {                
                myAudioFormat = ClientAudio.getAudioFormat(playID);
            }
        }
        
        if( url == null )
        {
            errorMessage( "url null in method 'initialize'" );
            cleanup();
            return false;
        }
        try
        {
            dummyInputStream = AudioSystem.getAudioInputStream(
                                  new BufferedInputStream( url.openStream() ) );
        }
        catch( UnsupportedAudioFileException uafe )
        {
            errorMessage( "Unsupported audio format in method 'initialize'" );
            printStackTrace( uafe );
            return false;
        }
        catch( IOException ioe )
        {
            errorMessage( "Error setting up audio input stream in method " +
                          "'initialize'" );
            printStackTrace( ioe );
            return false;
        }

        endOfStream(SET, false);
        initialized(SET, true);

        return true;
    }
    	
	@Override
	public boolean initialized() {
		return initialized(GET, XXX);
	}

	boolean hasStream = false;
	int zeroBufferCount = 0;
	@Override
	public SoundBuffer read() {
		if (!initialized) {
			errorMessage("Not initialized in 'read'");
			return null;
		}

		// Check to make sure there is an audio format:
		if (myAudioFormat == null) {
			errorMessage("Audio Format null in method 'read'");
			return null;
		}
		
		if (endOfStream() | !ClientAudio.isPlaying(playID) | ClientAudio.isPlayIDAudioDataError(playID)) return null;
        if (hasStream == false)
        {
            if (ClientAudio.isPlayIDAudioDataReady(playID))
            {
                audioInputStream = ClientAudio.getAudioInputStream(playID);
                hasStream = true;
            }
        }

        int bufferSize = 0;
        byte readBuffer[] = new byte[SAMPLE_SIZE];
		byte outputBuffer[] = null;
		nextBuffer(SAMPLE_SIZE);
		try
        {	
		    if (hasStream && audioInputStream != null)
		    {
            bufferSize = audioInputStream.read(readBuffer);
            if (bufferSize > 0) outputBuffer = appendByteArrays(outputBuffer, readBuffer, bufferSize);
            if (bufferSize == -1)
                {
                    endOfStream(SET, true);
                    return null;
                }
		    } else
		    {
		        outputBuffer = appendByteArrays(outputBuffer, zeroBuffer, SAMPLE_SIZE);
		        message("  zeroBufferCount: " + zeroBufferCount);
		        if (zeroBufferCount++ > 64) 
		        {
		            errorMessage("  MML to PCM audio prcessiong took too long. Aborting!");
		            endOfStream(SET, true);
		            return null;
		        }
		    }
        } catch (IOException e)
        {
            printStackTrace(e);
        }
				
		// Reverse the byte order if necessary:
		if (!reverseBytes)
			reverseBytes(outputBuffer, 0, outputBuffer.length);

		// Wrap the data into a SoundBuffer:
		SoundBuffer buffer = new SoundBuffer(outputBuffer, myAudioFormat);

		return buffer;
	}

	/** load the entire buffer at once */
	@Override
	public SoundBuffer readAll() {
		if (!initialized) {
			errorMessage("Not initialized in 'readAll'");
			return null;
		}

		// Check to make sure there is an audio format:
		if (myAudioFormat == null) {
			errorMessage("Audio Format null in method 'readAll'");
			return null;
		}
        if (endOfStream() | !ClientAudio.hasPlayID(playID) | ClientAudio.isPlayIDAudioDataError(playID)) return null;
        
        int bufferSize = 0;
        byte outputBuffer[] = null;
        byte readBuffer[] = new byte[SAMPLE_SIZE];
        while (bufferSize != -1)
        {
            if (endOfStream() | ClientAudio.isPlayIDAudioDataError(playID)) return null;
            if (hasStream == false)
            {
                if (ClientAudio.isPlayIDAudioDataReady(playID))
                {
                    audioInputStream = ClientAudio.getAudioInputStream(playID);
                    hasStream = true;
                }
            }
            try
            {
                if (hasStream && audioInputStream != null)
                {
                    bufferSize = audioInputStream.read(readBuffer);
                    if (bufferSize > 0) outputBuffer = appendByteArrays(outputBuffer, readBuffer, bufferSize);
                } else
                {
                    nextBuffer(SAMPLE_SIZE);
                    outputBuffer = appendByteArrays(outputBuffer, noiseBuffer, SAMPLE_SIZE);
                    message("  zeroBufferCount: " + ++zeroBufferCount);
                    if (zeroBufferCount > 64) 
                    {
                        errorMessage("  MML to PCM audio prcessiong took too long. Aborting!");
                        endOfStream(SET, true);
                        return null;
                    }
                }
            } catch (IOException e)
            {
                printStackTrace(e);
            }
        }
        
       // Reverse the byte order if necessary:
        if (!reverseBytes)
            reverseBytes(outputBuffer, 0, outputBuffer.length);

        // Wrap the data into a SoundBuffer:
        SoundBuffer buffer = new SoundBuffer(outputBuffer, myAudioFormat);

        return buffer;
	}

	@Override
	public boolean endOfStream() {
		return endOfStream(GET, XXX);
	}

	@Override
    public void cleanup()
    {
	    message("cleanup");
	    if (audioInputStream != null)
        try
        {
            audioInputStream.close();
        } catch (IOException e)
        {
            printStackTrace(e);
        }
        audioInputStream = null;
        ClientAudio.removeEntityAudioData(playID);
        
        if( dummyInputStream != null )
            try
            {
                dummyInputStream.close();
            }
            catch( Exception e )
            {}
        dummyInputStream = null;
    }

	@Override
	public AudioFormat getAudioFormat() {
		return myAudioFormat;
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
	private synchronized boolean initialized(boolean action, boolean value) {
		if (action == SET)
			initialized = value;
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
	private synchronized boolean endOfStream(boolean action, boolean value) {
		if (action == SET)
			endOfStream = value;
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
	private static byte[] trimArray(byte[] array, int maxLength) {
		byte[] trimmedArray = null;
		if (array != null && array.length > maxLength) {
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
	public static void reverseBytes(byte[] buffer) {
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
	public static void reverseBytes(byte[] buffer, int offset, int size) {

		byte b;
		for (int i = offset; i < (offset + size); i += 2) {
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
	private static byte[] convertAudioBytes(byte[] audio_bytes, boolean two_bytes_data) {
		ByteBuffer dest = ByteBuffer.allocateDirect(audio_bytes.length);
		dest.order(ByteOrder.nativeOrder());
		ByteBuffer src = ByteBuffer.wrap(audio_bytes);
		src.order(ByteOrder.LITTLE_ENDIAN);
		if (two_bytes_data) {
			ShortBuffer dest_short = dest.asShortBuffer();
			ShortBuffer src_short = src.asShortBuffer();
			while (src_short.hasRemaining()) {
				dest_short.put(src_short.get());
			}
		} else {
			while (src.hasRemaining()) {
				dest.put(src.get());
			}
		}
		dest.rewind();

		if (!dest.hasArray()) {
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
	private static byte[] appendByteArrays(byte[] arrayOne, byte[] arrayTwo, int length) {
		byte[] newArray;
		if (arrayOne == null && arrayTwo == null) {
			// no data, just return
			return null;
		} else if (arrayOne == null) {
			// create the new array, same length as arrayTwo:
			newArray = new byte[length];
			// fill the new array with the contents of arrayTwo:
			System.arraycopy(arrayTwo, 0, newArray, 0, length);
			arrayTwo = null;
		} else if (arrayTwo == null) {
			// create the new array, same length as arrayOne:
			newArray = new byte[arrayOne.length];
			// fill the new array with the contents of arrayOne:
			System.arraycopy(arrayOne, 0, newArray, 0, arrayOne.length);
			arrayOne = null;
		} else {
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
	private void errorMessage(String message) {
		logger.errorMessage("CodecPCM", message, 0);
	}

	/**
	 * Prints an exception's error message followed by the stack trace.
	 * 
	 * @param e
	 *            Exception containing the information to print.
	 */
	private void printStackTrace(Exception e) {
		logger.printStackTrace(e, 1);
	}

	private void message(String message)
	{
	    logger.message(message, 0);
	}
	
}
