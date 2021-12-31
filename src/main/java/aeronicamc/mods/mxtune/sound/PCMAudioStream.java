package aeronicamc.mods.mxtune.sound;

import net.minecraft.client.audio.IAudioStream;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.BufferUtils;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class PCMAudioStream implements IAudioStream
{
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    private static final int SAMPLE_SIZE = 19200;
    private final AudioData audioData;
    private AudioInputStream audioInputStream = null;
    private ByteBuffer zeroBuffer = BufferUtils.createByteBuffer(SAMPLE_SIZE);
    private boolean hasStream = false;
    private int zeroBufferCount = 0;

    public PCMAudioStream(AudioData audioData)
    {
        this.audioData = audioData;
        nextZeroBuffer();
        LOGGER.debug("PCMAudioStream invoked! PlayID {}", audioData.getPlayId());
    }

    private void nextZeroBuffer()
    {
        for (int i = 0; i < SAMPLE_SIZE; i += 2)
        {
            zeroBuffer.put((byte)0);
            zeroBuffer.put((byte)0);
        }
        zeroBuffer.flip();
    }

    @Override
    public AudioFormat getFormat()
    {
        return audioData.getAudioFormat();
    }

    /*
     * streamRead(int bufferSize) - for streaming audio
     */
    @Nullable
    @Override
    public ByteBuffer read(int size) throws IOException
    {
        if (hasInputStreamError())
            return null;
        notifyOnInputStreamAvailable();

        int bufferSize;
        byte[] readBuffer = new byte[size];
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(size);
        try
        {
            if (hasStream && (audioInputStream != null))
            {
                bufferSize = audioInputStream.read(readBuffer);

                if (bufferSize > 0)
                    byteBuffer.put(readBuffer);

                if (bufferSize == -1)
                {
                    audioDataSetStatus(ClientAudio.Status.DONE);
                    return null;
                }
            }
            else
            {
                nextZeroBuffer();
                byteBuffer.put(zeroBuffer);
                if (zeroBufferCount++ > 256)
                {
                    LOGGER.error("MML to PCM audio processing took too long. Aborting!");
                    audioDataSetStatus(ClientAudio.Status.ERROR);
                    return null;
                }
            }
        } catch (IOException e)
        {
            audioDataSetStatus(ClientAudio.Status.ERROR);
            throw new IOException(e);
        }
        zeroBuffer.flip();
        byteBuffer.flip();
        return byteBuffer;
    }

    private void notifyOnInputStreamAvailable()
    {
        if (!hasStream && (audioData.getStatus() == ClientAudio.Status.READY))
        {
            audioInputStream = audioData.getAudioStream();
            try
            {
                if (audioInputStream.available() > 0)
                    hasStream = true;
            }
            catch (IOException e)
            {
                LOGGER.error("audioInputStream error");
                audioDataSetStatus(ClientAudio.Status.ERROR);
            }
        }
    }

    private void audioDataSetStatus(ClientAudio.Status status)
    {
        if (audioData != null) audioData.setStatus(status);
    }

    private boolean hasInputStreamError()
    {
        if (audioData == null || audioData.getStatus() == ClientAudio.Status.ERROR)
        {
            LOGGER.error("Not initialized in 'read'");
            return true;
        }

        return false;
    }

    @Override
    public void close() throws IOException
    {
        if (audioInputStream != null)
            audioInputStream.close();
    }
}
