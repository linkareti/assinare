package com.linkare.assinare.sign.keysupplier.dss;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.commons.ui.AssinarePINGUI;
import com.linkare.assinare.sign.dss.token.MOCCASignatureTokenConnection;

import at.gv.egiz.smcc.pin.gui.PINGUI;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.token.PasswordInputCallback;

/**
 * Helper to sign with ASiC-E-BASELINE-B/L/LT/LTA
 *
 * @author bnazare
 */
class LazyMOCCASigningKey extends AbstractDSSSigningKey {

    private static final Logger LOG = Logger.getLogger(LazyMOCCASigningKey.class.getName());

    private final PINGUI pinCallback;

    private LazyMOCCASigningKey() {
        super();
        this.pinCallback = new AssinarePINGUI(1);
    }

    LazyMOCCASigningKey(PINGUI pinCallback) {
        super();
        this.pinCallback = pinCallback;
    }

    public void closeToken() {
        if (signingToken != null) {
            signingToken.close();
        }
        privateKey = null;
        signingToken = null;
    }

    /**
     * This method sets the common parameters.
     *
     * @throws com.linkare.assinare.commons.AssinareException
     */
    @Override
    public void lazyInit() throws AssinareException {
        try {
            signingToken = new MOCCASignatureTokenConnection(pinCallback);
            privateKey = signingToken.getKeys().get(0);
        } catch (DSSException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof AssinareException) {
                throw new AssinareException(cause.getMessage(), ex);
            } else {
                throw new AssinareException("Cartão não detectado.", ex);
            }
        }
    }

    private static class ConsolePasswordInputCallback implements PasswordInputCallback {

        private static final Logger LOG = Logger.getLogger(ConsolePasswordInputCallback.class.getName());

        @Override
        public char[] getPassword() {
            try {
                System.out.print("Enter pin:");
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                String pin;
                pin = in.readLine();
                if (pin == null || pin.length() == 0) {
                    throw new RuntimeException("Problems with the pin inserted");
                }
                return pin.toCharArray();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
                throw new RuntimeException("Problems with the pin inserted");
            }
        }
    }

}
