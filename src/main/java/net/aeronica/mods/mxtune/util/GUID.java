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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/*
 * A better name for this class would be SHA2 since it's main purpose is to create unique 64 character
 * hexadecimal strings. It's Useful for use as file names. It's less useful than java.util.UUID in many respects, but
 * I've noted at least one UUID collision which prompted me to make this class.
 */
public class GUID implements java.io.Serializable, Comparable<GUID>
{
    private static final long serialVersionUID = -4856826361193249489L;

    private final long ddddSigBits;
    private final long ccccSigBits;
    private final long bbbbSigBits;
    private final long aaaaSigBits;

    // Constructors and Factories

    /*
     * Private constructor which uses a byte array to construct the new GUID.
     * Requires exactly 32 bytes. The perfect size to hold all 256 bits needed to store a SHA2 hash.
     */
    private GUID(byte[] data)
    {
        long dsb = 0;
        long csb = 0;
        long bsb = 0;
        long asb = 0;
        assert data.length == 32 : "data must be 32 bytes in length";
        for (int i=0; i<8; i++)
            dsb = (dsb << 8) | (data[i] & 0xff);
        for (int i=8; i<16; i++)
            csb = (csb << 8) | (data[i] & 0xff);
        for (int i=16; i<24; i++)
            bsb = (bsb << 8) | (data[i] & 0xff);
        for (int i=24; i<32; i++)
            asb = (asb << 8) | (data[i] & 0xff);

        this.ddddSigBits = dsb;
        this.ccccSigBits = csb;
        this.bbbbSigBits = bsb;
        this.aaaaSigBits = asb;
    }

    /**
     * Constructs a new {@code GUID} using the specified data.  {@code
     * ddddSigBits} is used for the most significant 64 bits of the {@code
     * GUID} and {@code aaaaSigBits} becomes the least significant 64 bits of
     * the {@code GUID}.
     *
     * @param  ddddSigBits
     *         The most significant bits of the {@code GUID}
     * @param ccccSigBits
     *         The less significant bits of the {@code GUID}
     * @param bbbbSigBits
     *         The lesser significant bits of the {@code GUID}
     * @param  aaaaSigBits
     *         The least significant bits of the {@code GUID}
     */
    public GUID(long ddddSigBits, long ccccSigBits, long bbbbSigBits, long aaaaSigBits) {
        this.ddddSigBits = ddddSigBits;
        this.ccccSigBits = ccccSigBits;
        this.bbbbSigBits = bbbbSigBits;
        this.aaaaSigBits = aaaaSigBits;
    }

    /**
     * Creates a {@code GUID} from the string standard representation as
     * described in the {@link #toString} method.
     *
     * @param  name
     *         A string that specifies a {@code GUID}
     *
     * @return  A {@code GUID} with the specified value
     *
     * @throws  IllegalArgumentException
     *          If name does not conform to the string representation as
     *          described in {@link #toString}
     *
     */
    public static GUID fromString(String name)
    {
        return new GUID(hexStringToByteArray(name));
    }

    /**
     * Generates a SHA256 hash for the entered phrase/string and stores if in the GUID
     * @param phrase to hash.
     * @return A GUID that represents the SHA256 hash for phrase.
     */
    public static GUID stringToSHA2Hash(String phrase)
    {
        MessageDigest md;
        try
        {
            md = MessageDigest.getInstance("SHA-256");
            md.update(phrase.getBytes());
            return new GUID(md.digest());
        }
        catch (NoSuchAlgorithmException e)
        {
            ModLogger.error(e);
            throw new MXTuneRuntimeException("What's wrong with this JVM installation? No SHA-256 message digest? Please review and FIX your JAVA JVM installation!");
        }
    }

    // Getters and toString

    /**
     * Returns the Aaaa (LSB) significant 64 bits of this GUID's 256 bit value.
     *
     * @return  The Aaaa (LSB) significant 64 bits of this GUID's 256 bit value
     */
    public long getAaaaSignificantBits() {
        return aaaaSigBits;
    }

    /**
     * Returns the Bbbb significant 64 bits of this GUID's 256 bit value.
     *
     * @return  The Bbbb significant 64 bits of this GUID's 256 bit value
     */
    public long getBbbbSignificantBits() {
        return bbbbSigBits;
    }

    /**
     * Returns the Cccc significant 64 bits of this GUID's 256 bit value.
     *
     * @return  The Cccc significant 64 bits of this GUID's 256 bit value
     */
    public long getCcccSignificantBits() {
        return ccccSigBits;
    }

    /**
     * Returns the Dddd (MSB) significant 64 bits of this GUID's 256 bit value.
     *
     * @return  The Dddd (MSB) significant 64 bits of this GUID's 256 bit value
     */
    public long getDdddSignificantBits() {
        return ddddSigBits;
    }

    /**
     * Returns a {@code String} object representing this {@code GUID}.
     * @return A 64 character (256 bit) hexadecimal string representation of this {@code GUID}
     */
    @Override
    public String toString()
    {
        return bytesToHex(longsToBytes(this.ddddSigBits, this.ccccSigBits, this.bbbbSigBits, this.aaaaSigBits));
    }

    // Internal helper code

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte byt : bytes) result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }

    private byte[] longsToBytes(long ddddSigBits, long ccccSigBits, long bbbbSigBits, long aaaaSigBits)
    {
        byte[] bytes = new byte[32];
        System.arraycopy(fastLongToBytes(ddddSigBits), 0, bytes, 0, 8);
        System.arraycopy(fastLongToBytes(ccccSigBits), 0, bytes, 8, 8);
        System.arraycopy(fastLongToBytes(bbbbSigBits), 0, bytes, 16, 8);
        System.arraycopy(fastLongToBytes(aaaaSigBits), 0, bytes, 24, 8);
        return bytes;
    }

    private byte[] fastLongToBytes(long lg)
    {
        return new byte[]{
                (byte) (lg >>> 56),
                (byte) (lg >>> 48),
                (byte) (lg >>> 40),
                (byte) (lg >>> 32),
                (byte) (lg >>> 24),
                (byte) (lg >>> 16),
                (byte) (lg >>> 8),
                (byte) lg
        };
    }

    private static byte[] hexStringToByteArray(String s)
    {
        int len = s.length();
        assert len != 64 : "data must be 64 hex characters in length";
        byte[] data = new byte[32];
        for (int i = 0; i < 64; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                          + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    // equals hashCode and comparable

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GUID guid = (GUID) o;
        return ddddSigBits == guid.ddddSigBits &&
                ccccSigBits == guid.ccccSigBits &&
                bbbbSigBits == guid.bbbbSigBits &&
                aaaaSigBits == guid.aaaaSigBits;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ddddSigBits, ccccSigBits, bbbbSigBits, aaaaSigBits);
    }

    @Override
    @SuppressWarnings("all")
    public int compareTo(GUID val)
    {
        /*
         * The ordering is intentionally set up so that the UUIDs
         * can simply be numerically compared as two numbers
         */
        return ((this.ddddSigBits < val.ddddSigBits) ? -1 :
                ((this.ddddSigBits > val.ddddSigBits) ? 1 :
                 ((this.ccccSigBits < val.ccccSigBits) ? -1 :
                  ((this.ccccSigBits > val.ccccSigBits) ? 1 :
                   ((this.bbbbSigBits < val.bbbbSigBits) ? -1 :
                    ((this.bbbbSigBits > val.bbbbSigBits) ? 1 :
                     ((this.aaaaSigBits < val.aaaaSigBits) ? -1 :
                      ((this.aaaaSigBits > val.aaaaSigBits) ? 1 :
                       0))))))));
    }
}
