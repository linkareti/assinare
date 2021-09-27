package com.linkare.assinare.server.test.mocks;

import com.linkare.assinare.server.CMDCryptoHelper;

import static java.util.Base64.getDecoder;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;

import javax.crypto.Cipher;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.prng.FixedSecureRandom;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import io.quarkus.test.Mock;

/**
 *
 * @author bnazare
 */
@Mock
public class MockCMDCryptoHelper extends CMDCryptoHelper {

    public static final byte[] SEED_BYTES = getDecoder().decode(StringUtils.repeat('Z', (int) Math.ceil(255 * 4 / 3d)));

    private static final BouncyCastleProvider BC_PROVIDER = new BouncyCastleProvider();

    public MockCMDCryptoHelper() {
        super();

        try {
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", BC_PROVIDER);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public byte[] encrypt(byte[] data) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, CMD_CERT, new FixedSecureRandom(SEED_BYTES));
            return super.encrypt(data);
        } catch (InvalidKeyException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

}
