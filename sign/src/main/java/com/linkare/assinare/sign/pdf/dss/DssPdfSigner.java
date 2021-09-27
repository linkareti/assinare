package com.linkare.assinare.sign.pdf.dss;

import static com.linkare.assinare.sign.AssinareConstants.REASON_PREFIX_DEMO_RELEASE_EN;
import static com.linkare.assinare.sign.pdf.DemoImageUtils.makeDemoImageOverlay;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.commons.utils.ManifestUtils;
import com.linkare.assinare.sign.SigningKey;
import com.linkare.assinare.sign.X500NameParser;
import com.linkare.assinare.sign.dss.AssinareTSPSource;
import com.linkare.assinare.sign.dss.CertificateUtils;
import com.linkare.assinare.sign.dss.SigningUtils;
import com.linkare.assinare.sign.dss.TSPDSSException;
import com.linkare.assinare.sign.helpers.RectangleHelper;
import com.linkare.assinare.sign.model.AssinareDocument;
import com.linkare.assinare.sign.pdf.AbstractPdfSigner;
import com.linkare.assinare.sign.pdf.PDFSignatureFields;
import com.linkare.assinare.sign.pdf.itext.TSAAssinareException;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.ImageScaling;
import eu.europa.esig.dss.enumerations.SignatureAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.enumerations.SignerTextHorizontalAlignment;
import eu.europa.esig.dss.enumerations.SignerTextPosition;
import eu.europa.esig.dss.enumerations.TextWrapping;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.pades.DSSFileFont;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.SignatureFieldParameters;
import eu.europa.esig.dss.pades.SignatureImageParameters;
import eu.europa.esig.dss.pades.SignatureImageTextParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.http.commons.TimestampDataLoader;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.service.tsp.OnlineTSPSource;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;

/**
 *
 * @author bnazare
 */
public class DssPdfSigner extends AbstractPdfSigner {

    private static final String CC_SIG_ALGO_NAME = "SHA1withRSA";

    private static final int DEFAULT_SIG_TEXT_SIZE = 3;
    // font file that comes with PDFBox
    private static final String DEFAULT_SIG_FONT_PATH = "org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf";
    // we could use a DSSFileFont instance here, but that is mutable so it's
    // not a great fit for a static field
    private static final InMemoryDocument DEFAULT_SIG_FONT_DATA;

    private static final DateTimeFormatter SIG_LINES_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss z");

    static {
        try (InputStream is = PDDocument.class.getClassLoader().getResourceAsStream(DEFAULT_SIG_FONT_PATH)) {
            DEFAULT_SIG_FONT_DATA = new InMemoryDocument(is);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private final Function<PDFSignatureFields, PAdESService> padesServiceSupplier;

    public DssPdfSigner() {
        this(null);
    }

    public DssPdfSigner(Function<PDFSignatureFields, PAdESService> padesServiceSupplier) {
        this.padesServiceSupplier = padesServiceSupplier;
    }

    @Override
    protected void signPdf(SigningKey signingKey, AssinareDocument target, AssinareDocument dest, PDFSignatureFields signOptions) throws AssinareError, AssinareException {
        try {
            DSSDocument toSignDocument;
            try (InputStream targetStream = target.openInputStream()) {
                toSignDocument = new InMemoryDocument(targetStream);
            }

            PAdESSignatureParameters parameters;
            try (InputStream targetStream = target.openInputStream();
                    PDDocument pdDocument = PDDocument.load(targetStream)) {
                parameters = buildsignatureParameters(signOptions, signingKey.getCertificate(),
                        signingKey.getCertificateChain(), pdDocument, CC_SIG_ALGO_NAME, ZonedDateTime.now());
            }

            PAdESService service = buildService(signOptions);

            // Get the SignedInfo segment that need to be signed.
            ToBeSigned dataToSign = service.getDataToSign(toSignDocument, parameters);

            // This function obtains the signature value for signed information using the
            // private key and specified algorithm
            DigestAlgorithm digestAlgorithm = parameters.getDigestAlgorithm();
            SignatureValue signatureValue = SigningUtils.sign(signingKey, dataToSign, digestAlgorithm);

            // We invoke the padesService to sign the document with the signature value obtained in
            // the previous step.
            DSSDocument signedDocument = service.signDocument(toSignDocument, parameters, signatureValue);

            try (OutputStream destStream = dest.openOutputStream()) {
                Utils.write(DSSUtils.toByteArray(signedDocument), destStream);
            }
        } catch (TSPDSSException ex) {
            throw new TSAAssinareException(ex.getMessage(), ex);
        } catch (IOException | DSSException ex) {
            throw new AssinareError(ex);
        }

    }

    @Override
    public byte[] hashPdf(AssinareDocument target, X509Certificate[] certChain, String signatureAlgorithmName, PDFSignatureFields signOptions, ZonedDateTime signingDate) throws AssinareException, AssinareError {
        try {
            DSSDocument toSignDocument;
            try (InputStream targetStream = target.openInputStream()) {
                toSignDocument = new InMemoryDocument(targetStream);
            }

            PAdESSignatureParameters parameters;
//            TODO: make stream reopenable, if possible
//            try (InputStream targetStream = target.openInputStream()) {
            try (InputStream targetStream = toSignDocument.openStream();
                    PDDocument pdDocument = PDDocument.load(targetStream)) {
                parameters = buildsignatureParameters(signOptions, certChain[0], certChain, pdDocument, signatureAlgorithmName, signingDate);
            }

            PAdESService service = buildService(signOptions);

            // Get the SignedInfo segment that need to be signed.
            ToBeSigned dataToSign = service.getDataToSign(toSignDocument, parameters);

            return dataToSign.getBytes();
        } catch (IOException | DSSException ex) {
            throw new AssinareError(ex);
        }

    }

    @Override
    public AssinareDocument signPdf(AssinareDocument target, X509Certificate[] certChain, byte[] signedHash, String signatureAlgorithmName, PDFSignatureFields signOptions, ZonedDateTime signingDate) throws AssinareException, AssinareError {
        try {
            DSSDocument toSignDocument;
            try (InputStream targetStream = target.openInputStream()) {
                toSignDocument = new InMemoryDocument(targetStream);
            }

            PAdESSignatureParameters parameters;
//            TODO: make stream reopenable, if possible
//            try (InputStream targetStream = target.openInputStream()) {
            try (InputStream targetStream = toSignDocument.openStream();
                    PDDocument pdDocument = PDDocument.load(targetStream)) {
                parameters = buildsignatureParameters(signOptions, certChain[0], certChain, pdDocument, signatureAlgorithmName, signingDate);
            }

            PAdESService service = buildService(signOptions);

            SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.forJAVA(signatureAlgorithmName);
            SignatureValue signatureValue = new SignatureValue(signatureAlgorithm, signedHash);

            // We invoke the padesService to sign the document with the signature value obtained in
            // the previous step.
            DSSDocument signedDocument = service.signDocument(toSignDocument, parameters, signatureValue);

            // this should be an InMemoryDocument 100% of the time and so
            // we should attemp to reuse the existing byte array instead of copying it
            if (signedDocument instanceof InMemoryDocument) {
                InMemoryDocument inMemoryDocument = (InMemoryDocument) signedDocument;
                return new com.linkare.assinare.sign.model.InMemoryDocument(target.getName(), inMemoryDocument.getBytes());
            } else {
                try (@SuppressWarnings("null") InputStream signedStream = signedDocument.openStream()) {
                    return new com.linkare.assinare.sign.model.InMemoryDocument(target.getName(), signedStream);
                }
            }
        } catch (IOException | DSSException ex) {
            throw new AssinareError(ex);
        }

    }

    private PAdESSignatureParameters buildsignatureParameters(PDFSignatureFields signOptions, X509Certificate signingCertificate, X509Certificate[] certChain, PDDocument pdDocument, String signatureAlgorithmName, ZonedDateTime signingDate) throws AssinareException, AssinareError {
        // Preparing parameters for the PAdES signature
        PAdESSignatureParameters parameters = new PAdESSignatureParameters();
        // We choose the level of the signature (-B, -T, -LT, -LTA).
        if (!signOptions.isUseTsa()) {
            parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
        } else if (signOptions.isArchiving()) {
            parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_LTA);
        } else {
            parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_T);
        }
        // We choose the type of the signature packaging (ENVELOPING, DETACHED).
        parameters.setSignaturePackaging(SignaturePackaging.DETACHED);

        // We set the digest algorithm to use with the signature algorithm. You must use the
        // same parameter when you invoke the method sign on the token. The default value is
        // SHA256
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.forJAVA(signatureAlgorithmName);
        parameters.setDigestAlgorithm(signatureAlgorithm.getDigestAlgorithm());
        parameters.setEncryptionAlgorithm(signatureAlgorithm.getEncryptionAlgorithm());
        parameters.setMaskGenerationFunction(signatureAlgorithm.getMaskGenerationFunction());

        if (certChain.length > 2) { // the first two certificates usually fit in the default size
            int signatureSize = parameters.getContentSize(); // get the default value
            for (int i = 2; i < certChain.length; i++) {
                final X509Certificate cert = certChain[i];
                try {
                    // not sure why the doubling is needed but it's not enough otherwise
                    signatureSize += cert.getEncoded().length * 2;
                } catch (CertificateEncodingException ex) {
                    throw new AssinareError(ex);
                }
            }
            parameters.setContentSize(signatureSize);
        }
        Date sigDate = Date.from(signingDate.toInstant());
        parameters.bLevel().setSigningDate(sigDate);
        TimeZone timeZone = TimeZone.getTimeZone(signingDate.getZone());
        parameters.setSigningTimeZone(timeZone);

        // We set the signing certificate
        parameters.setSigningCertificate(new CertificateToken(signingCertificate));
        // We set the certificate chain
        parameters.setCertificateChain(CertificateUtils.convertCertificates(certChain));

        parameters.setLocation(signOptions.getLocation());
        if (isApplyWatermark()) {
            parameters.setReason(REASON_PREFIX_DEMO_RELEASE_EN + signOptions.getReason());
        } else {
            parameters.setReason(signOptions.getReason());
        }
        parameters.setContactInfo(signOptions.getContact());
        if (signOptions.isShowSignature()) {
            makeSignatureAppearance(signOptions, pdDocument, parameters, signingDate);
        }

        return parameters;
    }

    private PAdESService buildService(PDFSignatureFields signOptions) {
        if (padesServiceSupplier != null) {
            return padesServiceSupplier.apply(signOptions);
        } else {
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

    private static void makeSignatureAppearance(PDFSignatureFields signOptions, PDDocument document, PAdESSignatureParameters sigParams, ZonedDateTime signingDate) throws AssinareException {
        SignatureImageParameters imageParameters = sigParams.getImageParameters();
        SignatureImageTextParameters textParameters = imageParameters.getTextParameters();

        if (signOptions.isShowSignature()) {
            if (signOptions.isShowLogo()) {
                try {
                    final URL logoFileURL = signOptions.getLogoFileURL();

                    InMemoryDocument logoDocument;
                    if (isApplyWatermark()) {
                        logoDocument = makeDemoImageOverlayAsDSSDocument(logoFileURL);
                    } else {
                        logoDocument = new InMemoryDocument(logoFileURL.openStream());
                    }

                    imageParameters.setImage(logoDocument);
                    imageParameters.setImageScaling(ImageScaling.ZOOM_AND_CENTER);
                } catch (IOException ex) {
                    Logger.getLogger(DssPdfSigner.class.getName()).log(Level.SEVERE, null, ex);
                }
                textParameters.setSignerTextPosition(SignerTextPosition.RIGHT);
            }

            setSignatureText(sigParams, signingDate);
            textParameters.setSignerTextHorizontalAlignment(SignerTextHorizontalAlignment.LEFT);
            textParameters.setTextWrapping(TextWrapping.FILL_BOX_AND_LINEBREAK);

            String sigFieldName = signOptions.getFieldName();
            if (sigFieldName == null) {
                setSignaturePosition(imageParameters, signOptions, document);
            } else {
                imageParameters.getFieldParameters().setFieldId(sigFieldName);
            }
        }
    }

    private static void setSignatureText(PAdESSignatureParameters sigParams, ZonedDateTime signingDate) throws AssinareException {
        SignatureImageTextParameters textParameters = sigParams.getImageParameters().getTextParameters();
        String[] lines = getLines(sigParams, signingDate);

        // negative font size means auto-fit
        // the absolute value of the font size is used when auto-fit fails
        textParameters.setFont(new DSSFileFont(DEFAULT_SIG_FONT_DATA, DEFAULT_SIG_TEXT_SIZE));
        textParameters.setText(String.join("\n", lines));
    }

    private static String[] getLines(PAdESSignatureParameters sigParams, ZonedDateTime signingDate) {
        List<String> lines = new ArrayList<>(4);
        lines.add("Digitally signed by " + extractSubjectCN(sigParams.getSigningCertificate()));
        lines.add("Date: " + signingDate.format(SIG_LINES_DATE_TIME_FORMATTER));
        if (StringUtils.isNotBlank(sigParams.getReason())) {
            lines.add("Reason: " + sigParams.getReason());
        }
        if (StringUtils.isNotBlank(sigParams.getLocation())) {
            lines.add("Location: " + sigParams.getLocation());
        }
        return lines.toArray(new String[lines.size()]);
    }

    private static String extractSubjectCN(CertificateToken certToken) {
        X500NameParser subject = new X500NameParser(certToken.getSubject().getPrincipal());
        return subject.getCommonName();
    }

    private static InMemoryDocument makeDemoImageOverlayAsDSSDocument(final URL logoFileURL) throws IOException {
        BufferedImage bi = makeDemoImageOverlay(logoFileURL);
        return new InMemoryDocument(toByteArray(bi));
    }

    private static byte[] toByteArray(BufferedImage bi) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(bi, "png", baos);
            return baos.toByteArray();
        }
    }

    private static boolean isApplyWatermark() {
        return ManifestUtils.isCodebaseWildcard();
    }

    private static void setSignaturePosition(SignatureImageParameters imageParameters, PDFSignatureFields signOptions, PDDocument document) {
        int pageNumber = signOptions.getPageNumber();
        SignatureFieldParameters fieldParameters = imageParameters.getFieldParameters();
        fieldParameters.setPage(pageNumber);
        PDRectangle r = RectangleHelper.getRotatedCropBox(document.getPage(pageNumber - 1));
//        String signNameID = nextSignAuthorNameID(reader, AssinareConstants.SIGNATURE_NAME);
        PDRectangle sigRect = createRectangle(r, signOptions);
        fieldParameters.setOriginX(sigRect.getLowerLeftX());
        // this should work but doesn't
//        imageParameters.setAlignmentVertical(SignatureImageParameters.VisualSignatureAlignmentVertical.BOTTON);
//        imageParameters.setyAxis(sigRect.getLowerLeftY());
        fieldParameters.setOriginY(r.getHeight() - sigRect.getUpperRightY());
        fieldParameters.setWidth((int) sigRect.getWidth());
        fieldParameters.setHeight((int) sigRect.getHeight());
    }

    private static PDRectangle createRectangle(PDRectangle rIn, PDFSignatureFields signOptions) {
        int signHeigth = signOptions.getHeight();
        int signWidth = signOptions.getWidth();
        int left = (int) (rIn.getLowerLeftX() + (rIn.getWidth() - signWidth) * signOptions.getPercentX());
        int bottom = (int) (rIn.getLowerLeftY() + (rIn.getHeight() - signHeigth) * (1 - signOptions.getPercentY()));
        return new PDRectangle(left, bottom, signWidth, signHeigth);
    }

}
