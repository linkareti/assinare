package com.linkare.assinare.sign.pdf.itext;

import static com.itextpdf.text.pdf.security.TSAClientBouncyCastle.DEFAULTHASHALGORITHM;
import static com.itextpdf.text.pdf.security.TSAClientBouncyCastle.DEFAULTTOKENSIZE;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.List;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;

import com.itextpdf.text.pdf.security.TSAClient;
import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.sign.AssinareConstants;
import com.linkare.assinare.sign.CertChainPatcher;

/**
 *
 * @deprecated Chain patching should not be needed. Use TSL-based validation
 * instead.
 * @author bnazare
 */
@Deprecated
class PtEidTSAClient implements TSAClient {

    private final AssinareTSAClient delegate;
    private int tokenSizeEstimate;

    public PtEidTSAClient() {
        this.delegate = new AssinareTSAClient(AssinareConstants.TS_CARTAODECIDADAO_URL, null,
                null, (int) (DEFAULTTOKENSIZE * 2), DEFAULTHASHALGORITHM);
        this.tokenSizeEstimate = delegate.getTokenSizeEstimate();
    }

    public PtEidTSAClient(String url, String username, String password,
            int tokSzEstimate, String digestAlgorithm) {
        this.delegate = new AssinareTSAClient(url, username, password,
                tokSzEstimate, digestAlgorithm);
        this.tokenSizeEstimate = delegate.getTokenSizeEstimate();
    }

    @Override
    public MessageDigest getMessageDigest() throws GeneralSecurityException {
        return delegate.getMessageDigest();
    }

    @Override
    public int getTokenSizeEstimate() {
        return this.tokenSizeEstimate;
    }

    @Override
    public byte[] getTimeStampToken(byte[] imprint) throws TSAAssinareException, AssinareError {
        byte[] tmpTk = delegate.getTimeStampToken(imprint);

        try {
            CMSSignedData sig = new CMSSignedData(tmpTk);
            Collection<X509CertificateHolder> tokCerts = sig.getCertificates()
                    .getMatches(null);

            X509CertificateHolder issuerCert = tokCerts.iterator().next();
            List<Certificate> certList = CertChainPatcher.buildCertificateChain(
                    new JcaX509CertificateConverter().getCertificate(issuerCert)
            );

            JcaCertStore certStore2 = new JcaCertStore(certList);

            sig = CMSSignedData.replaceCertificatesAndCRLs(sig, certStore2, null,
                    null);

            byte[] encoded = sig.getEncoded();
            this.tokenSizeEstimate = encoded.length + 32;

            return encoded;
        } catch (CMSException | CertificateException | IOException ex) {
            throw new AssinareError(ex);
        }
    }

}
