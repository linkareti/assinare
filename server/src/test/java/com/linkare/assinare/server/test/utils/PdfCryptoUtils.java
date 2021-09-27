package com.linkare.assinare.server.test.utils;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.sign.dss.AssinareTSPSource;
import com.linkare.assinare.sign.model.InMemoryDocument;
import com.linkare.assinare.sign.pdf.PDFSignatureFields;
import com.linkare.assinare.sign.pdf.PdfSigner;
import com.linkare.assinare.sign.pdf.dss.AssinarePdfObjFactory;
import com.linkare.assinare.sign.pdf.dss.DssPdfSigner;

import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.http.commons.TimestampDataLoader;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.service.tsp.OnlineTSPSource;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;

/**
 *
 * @author bnazare
 */
@ApplicationScoped
public class PdfCryptoUtils {

    public PdfCryptoUtils() {
    }

    public byte[] calculateHash(InputStream docData, final List<X509Certificate> certChain, final String sigAlgoName, final PDFSignatureFields sigFields, final ZonedDateTime signingDate) throws AssinareException, AssinareError, IOException {
        final PdfSigner pdfSigner = new DssPdfSigner(this::buildService);
        final X509Certificate[] certChainArray = certChain.toArray(new X509Certificate[certChain.size()]);
        // the hash value changes between JVM versions so we can't use a fixed value
        return pdfSigner.hashPdf(new InMemoryDocument(null, docData), certChainArray, sigAlgoName, sigFields, signingDate);
    }

    private PAdESService buildService(PDFSignatureFields signOptions) {
        // Create common certificate verifier
        CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();
        if (signOptions.isArchiving()) {
            commonCertificateVerifier.setOcspSource(new OnlineOCSPSource());
            commonCertificateVerifier.setCrlSource(new OnlineCRLSource());
        }

        // Create PAdES padesService for signature
        PAdESService service = new PAdESService(commonCertificateVerifier);
        service.setPdfObjFactory(new AssinarePdfObjFactory());

        if (signOptions.isUseTsa()) {
            OnlineTSPSource tspSource = new AssinareTSPSource(signOptions.getTsaUrl());
            tspSource.setDataLoader(new TimestampDataLoader());

            service.setTspSource(tspSource);
        }

        return service;
    }

}
