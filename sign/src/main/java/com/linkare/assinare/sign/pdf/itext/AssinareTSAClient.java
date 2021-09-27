package com.linkare.assinare.sign.pdf.itext;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;

import org.bouncycastle.tsp.TSPException;

import com.itextpdf.text.pdf.security.TSAClient;
import com.itextpdf.text.pdf.security.TSAClientBouncyCastle;

/**
 *
 * @author bnazare
 */
public class AssinareTSAClient implements TSAClient {

    private final TSAClientBouncyCastle delegate;

    public AssinareTSAClient(String url) {
        this.delegate = new TSAClientBouncyCastle(url);
    }

    public AssinareTSAClient(String url, String username, String password) {
        this.delegate = new TSAClientBouncyCastle(url, username, password);
    }

    public AssinareTSAClient(String url, String username, String password, int tokSzEstimate, String digestAlgorithm) {
        this.delegate = new TSAClientBouncyCastle(url, username, password, tokSzEstimate, digestAlgorithm);
    }

    @Override
    public byte[] getTimeStampToken(byte[] imprint) throws TSAAssinareException {
        try {
            return delegate.getTimeStampToken(imprint);
        } catch (IOException | TSPException ex) {
            throw new TSAAssinareException("Erro de comunicação com a TSA", ex);
        }
    }

    @Override
    public int getTokenSizeEstimate() {
        return delegate.getTokenSizeEstimate();
    }

    @Override
    public MessageDigest getMessageDigest() throws GeneralSecurityException {
        return delegate.getMessageDigest();
    }

}
