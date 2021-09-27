package com.linkare.assinare.server.nativeimage;

import org.bouncycastle.crypto.prng.SP800SecureRandom;
import org.bouncycastle.jcajce.provider.drbg.DRBG;

import com.oracle.svm.core.annotate.AutomaticFeature;

/**
 *
 * @author bnazare
 */
@AutomaticFeature
public class BouncyCastleFeature extends BaseFeature {

    @Override
    public void duringSetup(DuringSetupAccess access) {
        /**
         * These classes obtain (directly or indirectly) an instance of
         * NativePRNG during static initialization. This is not allowed in
         * native images. However, unlike the classes above, we can not simply
         * delay their initialization until runtime as they are used at build
         * time. Thus, we need to set them up so they can be used at build time,
         * discarded and then initialized once more at runtime.
         */
        rerunInitialization(
                "for statically initialized BouncyCastle",
                SP800SecureRandom.class,
                DRBG.Default.class,
                DRBG.NonceAndIV.class
        );
    }

}
