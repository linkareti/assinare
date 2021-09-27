package com.linkare.assinare.cmd.bindings;

import java.util.Base64;
import java.util.UUID;

/**
 *
 * @author bnazare
 */
public final class UUIDXmlAdapter {

    private static final Base64.Encoder B64_ENCODER = Base64.getEncoder();
    private static final Base64.Decoder B64_DECODER = Base64.getDecoder();

    private UUIDXmlAdapter() {
    }

    public static UUID parseUUIDBinary(String b64) {
        return UUID.fromString(new String(B64_DECODER.decode(b64)));
    }

    public static String readUUIDBinary(UUID uuid) {
        return B64_ENCODER.encodeToString(uuid.toString().getBytes());
    }

}
