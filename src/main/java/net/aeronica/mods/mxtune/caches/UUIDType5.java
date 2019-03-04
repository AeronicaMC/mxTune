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

package net.aeronica.mods.mxtune.caches;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.UUID;

/**
 * UUIDType5
 * <p></p>
 * {@link "https://stackoverflow.com/questions/40230276/how-to-make-a-type-5-uuid-in-java#"}
 * {@link "https://stackoverflow.com/questions/10867405/generating-v5-uuid-what-is-name-and-namespace/28776880#28776880"}
 */
public class UUIDType5 {
    private static final Charset UTF8 = StandardCharsets.UTF_8;
    public static final UUID NAMESPACE_DNS = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
    public static final UUID NAMESPACE_URL = UUID.fromString("6ba7b811-9dad-11d1-80b4-00c04fd430c8");
    public static final UUID NAMESPACE_OID = UUID.fromString("6ba7b812-9dad-11d1-80b4-00c04fd430c8");
    public static final UUID NAMESPACE_X500 = UUID.fromString("6ba7b814-9dad-11d1-80b4-00c04fd430c8");
    // mxTune namespaces
    public static final UUID NAMESPACE_SONG = UUID.fromString("1ee01f66-3df3-11e9-b210-d663bd873d93");
    public static final UUID NAMESPACE_LIST = UUID.fromString("1ee02240-3df3-11e9-b210-d663bd873d93");
    public static final UUID NAMESPACE_AREA = UUID.fromString("1ee025e2-3df3-11e9-b210-d663bd873d93");
    public static final UUID NAMESPACE_SPARE1 = UUID.fromString("1ee02754-3df3-11e9-b210-d663bd873d93");
    public static final UUID NAMESPACE_SPARE2 = UUID.fromString("1ee02894-3df3-11e9-b210-d663bd873d93");
    public static final UUID NAMESPACE_SPARE3 = UUID.fromString("1ee029ca-3df3-11e9-b210-d663bd873d93");
    public static final UUID NAMESPACE_SPARE4 = UUID.fromString("1ee02b00-3df3-11e9-b210-d663bd873d93");
    public static final UUID NAMESPACE_SPARE5 = UUID.fromString("1ee02c2c-3df3-11e9-b210-d663bd873d93");
    public static final UUID NAMESPACE_SPARE6 = UUID.fromString("1ee0300a-3df3-11e9-b210-d663bd873d93");
    public static final UUID NAMESPACE_SPARE7 = UUID.fromString("1ee0317c-3df3-11e9-b210-d663bd873d93");

    private UUIDType5() { /* NOP */ }

    public static UUID nameUUIDFromNamespaceAndString(UUID namespace, String name) {
        return nameUUIDFromNamespaceAndBytes(namespace, Objects.requireNonNull(name, "name == null").getBytes(UTF8));
    }

    public static UUID nameUUIDFromNamespaceAndBytes(UUID namespace, byte[] name) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException nsae) {
            throw new InternalError("SHA-1 not supported");
        }
        md.update(toBytes(Objects.requireNonNull(namespace, "namespace is null")));
        md.update(Objects.requireNonNull(name, "name is null"));
        byte[] sha1Bytes = md.digest();
        sha1Bytes[6] &= 0x0f;  /* clear version        */
        sha1Bytes[6] |= 0x50;  /* set to version 5     */
        sha1Bytes[8] &= 0x3f;  /* clear variant        */
        sha1Bytes[8] |= 0x80;  /* set to IETF variant  */
        return fromBytes(sha1Bytes);
    }

    private static UUID fromBytes(byte[] data) {
        // Based on the private UUID(bytes[]) constructor
        long msb = 0;
        long lsb = 0;
        assert data.length >= 16;
        for (int i = 0; i < 8; i++)
            msb = (msb << 8) | (data[i] & 0xff);
        for (int i = 8; i < 16; i++)
            lsb = (lsb << 8) | (data[i] & 0xff);
        return new UUID(msb, lsb);
    }

    private static byte[] toBytes(UUID uuid) {
        // inverted logic of fromBytes()
        byte[] out = new byte[16];
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        for (int i = 0; i < 8; i++)
            out[i] = (byte) ((msb >> ((7 - i) * 8)) & 0xff);
        for (int i = 8; i < 16; i++)
            out[i] = (byte) ((lsb >> ((15 - i) * 8)) & 0xff);
        return out;
    }
}