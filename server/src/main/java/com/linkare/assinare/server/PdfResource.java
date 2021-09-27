package com.linkare.assinare.server;

import java.io.ByteArrayInputStream;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.server.pojo.GetHashParams;
import com.linkare.assinare.server.pojo.GetHashReturn;
import com.linkare.assinare.server.pojo.PDFSignatureConfiguration;
import com.linkare.assinare.server.pojo.SignHashParams;
import com.linkare.assinare.sign.fileprovider.FileService;
import com.linkare.assinare.sign.model.AssinareDocument;
import com.linkare.assinare.sign.pdf.PdfSigner;

/**
 *
 * @author bnazare
 */
@Path("/data/pdf")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PdfResource {

    static final String DEFAULT_SIG_ALGO_NAME = "SHA384withRSAandMGF1";

    @Inject
    PDFSignatureConfiguration pdfSignatureConfiguration;

    @Inject
    PdfSigner pdfSigner;

    @Inject
    FileService fileService;

    @Inject
    PDFSignatureFieldsHolder reqSignatureFieldsHolder;

    @POST
    @Path("/hash")
    public GetHashReturn getHash(@Valid GetHashParams params) throws GeneralSecurityException, AssinareException, AssinareError {
        reqSignatureFieldsHolder.setPdfSignatureFields(pdfSignatureConfiguration.toPDFSignatureFields());
        ZonedDateTime signingDate = params.getDate();

        final X509Certificate certificate = parseCertificate(params.getCert());
        final AssinareDocument document = fileService.getFile(params.getDocName());

        byte[] hash = pdfSigner.hashPdf(document, new X509Certificate[]{certificate}, DEFAULT_SIG_ALGO_NAME, pdfSignatureConfiguration.toPDFSignatureFields(), signingDate);

        return new GetHashReturn(hash);
    }

    @POST
    @Path("/sign")
    public void signHash(@Valid SignHashParams params) throws GeneralSecurityException, AssinareException, AssinareError {
        reqSignatureFieldsHolder.setPdfSignatureFields(pdfSignatureConfiguration.toPDFSignatureFields());
        ZonedDateTime signingDate = params.getDate();

        final X509Certificate certificate = parseCertificate(params.getCert());
        final AssinareDocument document = fileService.getFile(params.getDocName());

        AssinareDocument signedDocument = pdfSigner.signPdf(document, new X509Certificate[]{certificate}, params.getSignedHash(), DEFAULT_SIG_ALGO_NAME, pdfSignatureConfiguration.toPDFSignatureFields(), signingDate);

        fileService.putFile(params.getDocName(), signedDocument);
    }

    private X509Certificate parseCertificate(byte[] certData) throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X509");
        return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certData));
    }

}
