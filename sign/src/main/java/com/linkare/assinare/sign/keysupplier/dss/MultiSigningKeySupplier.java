package com.linkare.assinare.sign.keysupplier.dss;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore.PasswordProtection;
import java.security.Security;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import com.linkare.assinare.sign.KeySupplier;
import com.linkare.assinare.sign.SigningKey;
import com.linkare.assinare.sign.dss.token.AppleSignatureToken;
import com.linkare.assinare.sign.dss.token.MOCCASignatureTokenConnection;

import at.gv.egiz.smcc.pin.gui.PINGUI;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.token.JKSSignatureToken;
import eu.europa.esig.dss.token.MSCAPISignatureToken;
import eu.europa.esig.dss.token.SignatureTokenConnection;

/**
 *
 * @author bnazare
 */
public class MultiSigningKeySupplier implements KeySupplier {

    private static final Logger LOG = Logger.getLogger(MultiSigningKeySupplier.class.getName());

    private final PINGUI pinCallback;
    private final List<SignatureTokenConnection> tokens;

    public MultiSigningKeySupplier(PINGUI pinCallback) {
        this.pinCallback = pinCallback;
        this.tokens = new LinkedList<>();
    }

    @Override
    public List<SigningKey> getKeys() {
        List<SigningKey> keys = new LinkedList<>();

        MOCCASignatureTokenConnection moccaToken = new MOCCASignatureTokenConnection(pinCallback);
        tokens.add(moccaToken);
        keys.addAll(extractSigningKeys(moccaToken));

        if (Security.getProvider("SunMSCAPI") != null) {
            MSCAPISignatureToken mscapiToken = new MSCAPISignatureToken();
            tokens.add(mscapiToken);
            keys.addAll(extractSigningKeys(mscapiToken));
        } else if (Security.getProvider("Apple") != null) {
            AppleSignatureToken appleToken = new AppleSignatureToken();
            tokens.add(appleToken);
            keys.addAll(extractSigningKeys(appleToken));
        } else {
            keys.addAll(getJKSTokenKeys());
        }

        return keys;
    }

    private List<PreloadedSigningKey> extractSigningKeys(SignatureTokenConnection token) {
        try {
            return token.getKeys().stream().map(
                    privateKey -> new PreloadedSigningKey(token, privateKey)
            ).collect(Collectors.toList());
        } catch (DSSException ex) {
            // this is not critical, so we just log it an return zero keys
            LOG.log(Level.WARNING, null, ex);
            return Collections.EMPTY_LIST;
        }
    }

    private List<PreloadedSigningKey> getJKSTokenKeys() {
        String userDirectory = FileUtils.getUserDirectoryPath();
        File assinareJKS = new File(userDirectory, ".assinare.jks");
        if (assinareJKS.exists()) {
            try {
                char[] pwd = new char[]{'2', 's', 's', '1', 'n', '2', 'r', '3'};
                JKSSignatureToken jksToken = new JKSSignatureToken(assinareJKS, new PasswordProtection(pwd));
                tokens.add(jksToken);
                return extractSigningKeys(jksToken);
            } catch (IOException ex) {
                // this is not critical, so we just log it an return zero keys
                LOG.log(Level.WARNING, null, ex);
                return Collections.EMPTY_LIST;
            }
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public void close() {
        tokens.forEach((suppliedKey) -> {
            suppliedKey.close();
        });
        tokens.clear();
    }

}
