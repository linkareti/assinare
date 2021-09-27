package com.linkare.assinare.sign.pdf.itext;

import static com.linkare.assinare.sign.AssinareConstants.REASON_PREFIX_DEMO_RELEASE_EN;
import static com.linkare.assinare.sign.pdf.DemoImageUtils.makeDemoImageOverlay;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfSignatureAppearance.RenderingMode;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.CrlClient;
import com.itextpdf.text.pdf.security.CrlClientOnline;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.LtvTimestamp;
import com.itextpdf.text.pdf.security.LtvVerification;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.OcspClient;
import com.itextpdf.text.pdf.security.OcspClientBouncyCastle;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import com.itextpdf.text.pdf.security.ProviderDigest;
import com.itextpdf.text.pdf.security.TSAClient;
import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.commons.utils.ManifestUtils;
import com.linkare.assinare.sign.AssinareConstants;
import com.linkare.assinare.sign.SigningKey;
import com.linkare.assinare.sign.model.AssinareDocument;
import com.linkare.assinare.sign.model.FileDocument;
import com.linkare.assinare.sign.pdf.AbstractPdfSigner;
import com.linkare.assinare.sign.pdf.PDFSignatureFields;

/**
 *
 * @author bnazare
 */
public class ITextPdfSigner extends AbstractPdfSigner {

    private static final int BASE_SIZE_ESTIMATE = 8192;

    @Override
    protected void signPdf(SigningKey signingKey, AssinareDocument srcDoc, AssinareDocument destDoc, PDFSignatureFields signOptions) throws AssinareError, AssinareException {
        ExternalSignature es = new SigningKeyExternalSignature("SHA1", signingKey);
        Certificate[] chain = signingKey.getCertificateChain();
        try {
            PdfReader reader;
            try (InputStream srcStream = srcDoc.openInputStream()) {
                reader = new PdfReader(srcStream);
            }

            if (!signOptions.isArchiving()) {
                try (OutputStream destStream = destDoc.openOutputStream()) {
                    doBaseSignature(reader, destStream, signOptions, es, chain);
                    destStream.flush();
                }
            } else {
                AssinareDocument tmpDoc = FileDocument.createTemporary("assTmp", null);
                try (OutputStream tmpOutStream = tmpDoc.openOutputStream()) {
                    doBaseSignature(reader, tmpOutStream, signOptions, es, chain);
                    tmpOutStream.flush();
                }

                addLtv(tmpDoc, destDoc, signOptions.getTsaUrl());
            }
        } catch (IOException ex) {
            throw new AssinareError(ex);
        }
    }

    @Override
    public byte[] hashPdf(AssinareDocument target, X509Certificate[] certChain, String signatureAlgorithmName, PDFSignatureFields signOptions, ZonedDateTime signingDate) throws AssinareException, AssinareError {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AssinareDocument signPdf(AssinareDocument target, X509Certificate[] certChain, byte[] signedHash, String signatureAlgorithmName, PDFSignatureFields signOptions, ZonedDateTime signingDate) throws AssinareException, AssinareError {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void doBaseSignature(PdfReader reader, final OutputStream destStream, PDFSignatureFields signOptions, ExternalSignature es, Certificate[] chain) throws AssinareException, AssinareError {
        try {
            PdfStamper stamper = PdfStamper.createSignature(reader, destStream, '\0', null, true);

            PdfSignatureAppearance appearance = makeSignatureAppearance(stamper, reader, signOptions);

            ExternalDigest digest = new ProviderDigest("SunMSCAPI");
            TSAClient tsa = null;
            int sizeEstimate = BASE_SIZE_ESTIMATE;
            if (signOptions.isUseTsa()) {
                tsa = new AssinareTSAClient(signOptions.getTsaUrl());
                sizeEstimate += tsa.getTokenSizeEstimate();
            }

            MakeSignature.signDetached(appearance, digest, es, chain, null, null, tsa, sizeEstimate, MakeSignature.CryptoStandard.CMS);
        } catch (IOException | DocumentException ex) {
            throw new AssinareError(ex);
        } catch (GeneralSecurityException ex) {
            if (ex.getCause() instanceof AssinareException) {
                throw (AssinareException) ex.getCause();
            }
            throw new AssinareError(ex);
        } catch (RuntimeException ex) {
            /* MakeSignature.signDetached() throws a subclass of RuntimeException
             * when it encounters some errors, including the ones thrown by our classes
             * when obtaining trusted timestamps. Here we unpack that stuff.
             */
            if (ex.getCause() instanceof AssinareException) {
                throw (AssinareException) ex.getCause();
            } else if (ex.getCause() instanceof AssinareError) {
                throw (AssinareError) ex.getCause();
            } else {
                throw new AssinareError(ex);
            }
        }
    }

    private static PdfSignatureAppearance makeSignatureAppearance(PdfStamper stamper, PdfReader reader, PDFSignatureFields signOptions) throws DocumentException, IOException {
        final boolean applyWatermark = isApplyWatermark();
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();

        if (applyWatermark) {
            appearance.setReason(REASON_PREFIX_DEMO_RELEASE_EN + signOptions.getReason());
        } else {
            appearance.setReason(signOptions.getReason());
        }
        appearance.setLocation(signOptions.getLocation());
        appearance.setContact(signOptions.getContact());

        if (signOptions.isShowSignature()) {
            if (signOptions.isShowLogo()) {
                appearance.setRenderingMode(RenderingMode.GRAPHIC_AND_DESCRIPTION);

                Image signatureGraphic;
                if (applyWatermark) {
                    BufferedImage bi = makeDemoImageOverlay(signOptions.getLogoFileURL());
                    signatureGraphic = Image.getInstance(bi, null);
                } else {
                    signatureGraphic = Image.getInstance(signOptions.getLogoFileURL());
                }
                appearance.setSignatureGraphic(signatureGraphic);
            } else {
                appearance.setRenderingMode(RenderingMode.NAME_AND_DESCRIPTION);
            }

            setSignaturePosition(appearance, signOptions, reader);
        }

        return appearance;
    }

    private static boolean isApplyWatermark() {
        return ManifestUtils.isCodebaseWildcard();
    }

    private static void setSignaturePosition(PdfSignatureAppearance appearance, PDFSignatureFields signOptions, PdfReader reader) {
        String sigFieldName = signOptions.getFieldName();

        if (sigFieldName == null) {
            int pageNumber = signOptions.getPageNumber();
            Rectangle r = ITextPdfParser.getRotatedCropBox(reader, pageNumber);
            String signNameID = nextSignatureFieldID(reader, AssinareConstants.SIGNATURE_NAME);
            appearance.setVisibleSignature(createRectangle(r, signOptions), pageNumber, signNameID);
        } else {
            appearance.setVisibleSignature(sigFieldName);
        }
    }

    static String nextSignatureFieldID(PdfReader pdfReader, String signatureBaseName) {
        AcroFields fields = pdfReader.getAcroFields();

        ArrayList<Integer> signIds = new ArrayList<>();
        fields.getSignatureNames().stream().filter(
                sigName -> (sigName.startsWith(signatureBaseName))
        ).forEachOrdered(
                sigName -> {
                    try {
                        Integer id = Integer.valueOf(sigName.substring(signatureBaseName.length()));
                        signIds.add(id);
                    } catch (NumberFormatException ex) {
                        Logger.getLogger(ITextPdfSigner.class.getName()).log(Level.WARNING, null, ex);
                    }
                }
        );

        if (!signIds.isEmpty()) {
            return signatureBaseName + (Collections.max(signIds) + 1);
        } else {
            return signatureBaseName + 1;
        }

    }

    private static Rectangle createRectangle(Rectangle rIn, PDFSignatureFields signOptions) {
        int signHeigth = signOptions.getHeight();
        int signWidth = signOptions.getWidth();
        int left = (int) (rIn.getLeft() + (rIn.getWidth() - signWidth) * signOptions.getPercentX());
        int bottom = (int) (rIn.getBottom() + (rIn.getHeight() - signHeigth) * (1 - signOptions.getPercentY()));
        return new Rectangle(left, bottom, left + signWidth, bottom + signHeigth);
    }

    private static void addLtv(AssinareDocument src, AssinareDocument dest, String tsaUrl) throws AssinareError {
        OcspClient ocsp = new OcspClientBouncyCastle();
        CrlClient crl = new CrlClientOnline();
        TSAClient tsa = new AssinareTSAClient(tsaUrl);

        PdfReader pdfReader;
        try (InputStream srcStream = src.openInputStream();
                OutputStream destStream = dest.openOutputStream();) {
            pdfReader = new PdfReader(srcStream);
            PdfStamper stp = PdfStamper.createSignature(pdfReader, destStream, '\0', null, true);

            LtvVerification v = stp.getLtvVerification();
            AcroFields fields = stp.getAcroFields();
            List<String> names = fields.getSignatureNames();

            String sigName = names.get(names.size() - 1);
            PdfPKCS7 pkcs7 = fields.verifySignature(sigName);
            if (pkcs7.isTsp()) {
                v.addVerification(sigName, ocsp, crl,
                        LtvVerification.CertificateOption.SIGNING_CERTIFICATE,
                        LtvVerification.Level.OCSP_OPTIONAL_CRL,
                        LtvVerification.CertificateInclusion.NO);
            } else {
                for (String name : names) {
                    v.addVerification(name, ocsp, crl,
                            LtvVerification.CertificateOption.WHOLE_CHAIN,
                            LtvVerification.Level.OCSP_OPTIONAL_CRL,
                            LtvVerification.CertificateInclusion.NO);
                }
            }

            PdfSignatureAppearance sap = stp.getSignatureAppearance();
            String signNameID = nextSignatureFieldID(pdfReader, AssinareConstants.SIGNATURE_LTV_NAME);
            LtvTimestamp.timestamp(sap, tsa, signNameID);

            destStream.flush();
        } catch (GeneralSecurityException | DocumentException | IOException gsex) {
            throw new AssinareError(gsex);
        }
    }

}
