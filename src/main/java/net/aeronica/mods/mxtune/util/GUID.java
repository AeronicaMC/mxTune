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
import java.security.SecureRandom;
import java.util.Objects;

public class GUID implements java.io.Serializable, Comparable<GUID>
{
    private static final long serialVersionUID = -4856826361193249489L;

    private final long mostSigBits;
    private final long lessSigBits;
    private final long lesserSigBits;
    private final long leastSigBits;

    /*
     * The random number generator used by this class to create random
     * based UUIDs. In a holder class to defer initialization until needed.
     */
    private static class Holder {
        static final SecureRandom numberGenerator = new SecureRandom();
    }

    // Constructors and Factories

    /*
     * Private constructor which uses a byte array to construct the new UUID.
     */
    private GUID(byte[] data)
    {
        long msb = 0;
        long nsb = 0;
        long osb = 0;
        long lsb = 0;
        assert data.length == 32 : "data must be 32 bytes in length";
        for (int i=0; i<8; i++)
            msb = (msb << 8) | (data[i] & 0xff);
        for (int i=8; i<16; i++)
            nsb = (nsb << 8) | (data[i] & 0xff);
        for (int i=16; i<24; i++)
            osb = (osb << 8) | (data[i] & 0xff);
        for (int i=24; i<32; i++)
            lsb = (lsb << 8) | (data[i] & 0xff);

        this.mostSigBits = msb;
        this.lessSigBits = nsb;
        this.lesserSigBits = osb;
        this.leastSigBits = lsb;
    }

    /**
     * Constructs a new {@code GUID} using the specified data.  {@code
     * mostSigBits} is used for the most significant 64 bits of the {@code
     * GUID} and {@code leastSigBits} becomes the least significant 64 bits of
     * the {@code GUID}.
     *
     * @param  mostSigBits
     *         The most significant bits of the {@code GUID}
     * @param lessSigBits
     *         The less significant bits of the {@code GUID}
     * @param lesserSigBits
     *         The lesser significant bits of the {@code GUID}
     * @param  leastSigBits
     *         The least significant bits of the {@code GUID}
     */
    public GUID(long mostSigBits, long lessSigBits, long lesserSigBits, long leastSigBits) {
        this.mostSigBits = mostSigBits;
        this.lessSigBits = lessSigBits;
        this.lesserSigBits = lesserSigBits;
        this.leastSigBits = leastSigBits;
    }

    public static GUID fromString(String name) throws NoSuchAlgorithmException
    {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(name.getBytes());
        return new GUID(md.digest());
    }

    /**
     * Returns the least significant 64 bits of this GUID's 256 bit value.
     *
     * @return  The least significant 64 bits of this GUID's 256 bit value
     */
    public long getLeastSignificantBits() {
        return leastSigBits;
    }

    /**
     * Returns the less significant 64 bits of this GUID's 256 bit value.
     *
     * @return  The less significant 64 bits of this GUID's 256 bit value
     */
    public long getLessSignificantBits() {
        return lessSigBits;
    }

    /**
     * Returns the lesser significant 64 bits of this GUID's 256 bit value.
     *
     * @return  The lesser significant 64 bits of this GUID's 256 bit value
     */
    public long getLesersSignificantBits() {
        return lesserSigBits;
    }

    /**
     * Returns the most significant 64 bits of this GUID's 256 bit value.
     *
     * @return  The most significant 64 bits of this GUID's 256 bit value
     */
    public long getMostSignificantBits() {
        return mostSigBits;
    }

    /**
     * Returns a {@code String} object representing this {@code GUID}.
     * @return A hexadecimal string representation of this {@code GUID}
     */
    @Override
    public String toString()
    {
        return bytesToHex(SHA256(this.mostSigBits, this.lessSigBits, this.lesserSigBits, this.leastSigBits));
    }


    // Internal helper code

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte byt : bytes) result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }

    private byte[] SHA256(long mostSigBits, long lessSigBits, long lesserSigBits, long leastSigBits )
    {
        long msb = mostSigBits;
        long nsb = lessSigBits;
        long osb = lesserSigBits;
        long lsb = leastSigBits;
        byte[] bytes = new byte[32];

        System.arraycopy(fastLongToBytes(mostSigBits), 0, bytes, 0, 8);
        System.arraycopy(fastLongToBytes(lessSigBits), 0, bytes, 8, 8);
        System.arraycopy(fastLongToBytes(lesserSigBits), 0, bytes, 16, 8);
        System.arraycopy(fastLongToBytes(leastSigBits), 0, bytes, 24, 8);

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

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GUID guid = (GUID) o;
        return mostSigBits == guid.mostSigBits &&
                lessSigBits == guid.lessSigBits &&
                lesserSigBits == guid.lesserSigBits &&
                leastSigBits == guid.leastSigBits;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(mostSigBits, lessSigBits, lesserSigBits, leastSigBits);
    }

    @Override
    public int compareTo(GUID val)
    {
        /*
        The ordering is intentionally set up so that the UUIDs
        can simply be numerically compared as two numbers
        */
        return (this.mostSigBits < val.mostSigBits ? -1 :
                (this.mostSigBits > val.mostSigBits ? 1 :
                 (this.lessSigBits < val.lessSigBits ? -1 :
                  (this.lessSigBits > val.lessSigBits ? 1 :
                   (this.lesserSigBits < val.lesserSigBits ? -1 :
                    (this.lesserSigBits > val.lesserSigBits ? 1 :
                 (this.leastSigBits < val.leastSigBits ? -1 :
                  (this.leastSigBits > val.leastSigBits ? 1 :
                   0))))))));
    }
}
