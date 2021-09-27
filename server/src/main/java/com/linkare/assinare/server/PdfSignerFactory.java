package com.linkare.assinare.server;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;

import com.linkare.assinare.sign.dss.AssinareTSPSource;
import com.linkare.assinare.sign.pdf.PDFSignatureFields;
import com.linkare.assinare.sign.pdf.PdfSigner;
import com.linkare.assinare.sign.pdf.dss.AssinarePdfObjFactory;
import com.linkare.assinare.sign.pdf.dss.DssPdfSigner;

import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.PAdESTimestampParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.pdf.IPdfObjFactory;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.OCSPDataLoader;
import eu.europa.esig.dss.service.http.commons.TimestampDataLoader;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.service.tsp.OnlineTSPSource;
import eu.europa.esig.dss.signature.DocumentSignatureService;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;

/**
 *
 * @author bnazare
 */
public class PdfSignerFactory {

    @Produces
    @RequestScoped
    public PdfSigner getPdfSigner(DocumentSignatureService<PAdESSignatureParameters, PAdESTimestampParameters> padesService) {
        return new DssPdfSigner(a -> (PAdESService) padesService);
    }

    @Produces
    @Dependent
    public DocumentSignatureService<PAdESSignatureParameters, PAdESTimestampParameters> getPAdESService(PDFSignatureFields signOptions, CertificateVerifier certificateVerifier,
            IPdfObjFactory pdfObjFactory, TSPSource tspSource) {
        PAdESService service = new PAdESService(certificateVerifier);
        service.setPdfObjFactory(pdfObjFactory);

        if (signOptions.isUseTsa()) {
            service.setTspSource(tspSource);
        }

        return service;
    }

    @Produces
    @Dependent
    public CertificateVerifier getCertificateVerifier(PDFSignatureFields signOptions) {
        CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();
        if (signOptions.isArchiving()) {
            OCSPDataLoader ocspDataLoader = new OCSPDataLoader();
            ocspDataLoader.setUseSystemProperties(true);
            OnlineOCSPSource onlineOCSPSource = new OnlineOCSPSource(ocspDataLoader);

            CommonsDataLoader crlDataLoader = new CommonsDataLoader();
            crlDataLoader.setUseSystemProperties(true);
            OnlineCRLSource onlineCRLSource = new OnlineCRLSource(crlDataLoader);

            commonCertificateVerifier.setOcspSource(onlineOCSPSource);
            commonCertificateVerifier.setCrlSource(onlineCRLSource);
        }

        return commonCertificateVerifier;
    }

    @Produces
    @Dependent
    public IPdfObjFactory getPdfObjFactory(PDFSignatureFields signOptions) {
        return new AssinarePdfObjFactory();
    }

    @Produces
    @Dependent
    public TSPSource getTSPSource(PDFSignatureFields signOptions) {
        TimestampDataLoader timestampDataLoader = new TimestampDataLoader();
        timestampDataLoader.setUseSystemProperties(true);
        OnlineTSPSource tspSource = new AssinareTSPSource(signOptions.getTsaUrl(), timestampDataLoader);

        return tspSource;
    }

}
