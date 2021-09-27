package com.linkare.assinare.sign.pdf.itext;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.security.CertificateInfo;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import com.linkare.assinare.sign.DocumentParseException;
import com.linkare.assinare.sign.helpers.RectangleHelper;
import com.linkare.assinare.sign.model.AssinareDocument;
import com.linkare.assinare.sign.pdf.ParsedPdfInfo;
import com.linkare.assinare.sign.pdf.PdfParser;
import com.linkare.assinare.sign.pdf.SignatureFieldInfo;
import com.linkare.assinare.sign.pdf.SignatureInfo;

/**
 *
 * @author bnazare
 */
public class ITextPdfParser implements PdfParser {

    private static final SimpleDateFormat PDF_DATE_FORMAT = new SimpleDateFormat("'D:'yyyyMMddHHmmssX");

    private static final BouncyCastleProvider PROVIDER = new BouncyCastleProvider();

    @Override
    public boolean testPdf(AssinareDocument doc) {
        try (InputStream docStream = doc.openInputStream()) {
            new PdfReader(docStream).getAcroFields();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public ParsedPdfInfo parsePdf(AssinareDocument doc) throws DocumentParseException {
        try (InputStream docStream = doc.openInputStream()) {
            // PdfReader is closing the stream even though the Javadoc says it shouldn't
            final PdfReader pdfReader = new PdfReader(docStream);
            final AcroFields acroFields = pdfReader.getAcroFields();

            List<SignatureInfo> existingSignatures = listExistingSignatures(pdfReader, acroFields);
            List<SignatureFieldInfo> blankSigFields = getBlankSigFields(pdfReader, acroFields);

            return new ParsedPdfInfo(existingSignatures, blankSigFields);
        } catch (IOException e) {
            throw new DocumentParseException("Erro ao abrir PDF", e);
        }
    }

    private static PdfPKCS7 getPdfPKCS7(AcroFields fields, String name) {
        Security.addProvider(PROVIDER);
        return fields.verifySignature(name);
    }

    private static List<SignatureInfo> listExistingSignatures(PdfReader reader, AcroFields acroFields) {
        List<SignatureInfo> sigs = new ArrayList<>(acroFields.getSignatureNames().size());
        for (String sigName : acroFields.getSignatureNames()) {
            SignatureInfo si = extractSignatureInfo(reader, acroFields, sigName);
            sigs.add(si);
        }
        return sigs;
    }

    private static List<SignatureFieldInfo> getBlankSigFields(PdfReader reader, AcroFields acroFields) {
        List<String> blankSignatureNames = acroFields.getBlankSignatureNames();
        Collections.sort(blankSignatureNames);

        List<SignatureFieldInfo> blankSignatureInfos = new ArrayList<>();
        for (String blankSignatureName : blankSignatureNames) {
            blankSignatureInfos.add(extractBlankSignatureInfo(reader, acroFields, blankSignatureName));
        }

        return blankSignatureInfos;
    }

    private static SignatureFieldInfo extractBlankSignatureInfo(PdfReader pdfReader, AcroFields acroFields, String sigName) {
        int revision = acroFields.getRevision(sigName);

        AcroFields.FieldPosition fieldPosition = acroFields.getFieldPositions(sigName).get(0);
        int page = fieldPosition.page;

        float percentX = fieldPosition.position.getLeft() / pdfReader.getPageSize(page).getWidth();
        float percentY = 1 - fieldPosition.position.getTop() / pdfReader.getPageSize(page).getHeight();

        double percentWidth = fieldPosition.position.getWidth() / pdfReader.getPageSize(page).getWidth();
        double percentHeight = fieldPosition.position.getHeight() / pdfReader.getPageSize(page).getHeight();

        return new SignatureFieldInfo(sigName, revision, page, percentX, percentY, percentWidth, percentHeight);
    }

    private static SignatureInfo extractSignatureInfo(PdfReader pdfReader, AcroFields acroFields, String sigName) {
        int revision = acroFields.getRevision(sigName);

        PdfPKCS7 pkcs7 = getPdfPKCS7(acroFields, sigName);
        boolean tmpValid = false;
        try {
            tmpValid = pkcs7.verifyTimestampImprint() && pkcs7.verify();
        } catch (GeneralSecurityException ex) {
            // signature is invalid
        }
        boolean valid = tmpValid;
        String name = extractSubjectName(pkcs7.getSigningCertificate());

        final PdfDictionary sigDict = acroFields.getSignatureDictionary(sigName);

        Date date = null;
        String reason = null;
        String location = null;
        String contactDetails = null;
        if (!pkcs7.isTsp()) {
            Date tmpDate = null;
            try {
                tmpDate = PDF_DATE_FORMAT.parse(sigDict.getAsString(PdfName.M).toString());
            } catch (ParseException ex) {
                Logger.getLogger(SignatureInfo.class.getName()).log(Level.WARNING, null, ex);
            }
            date = tmpDate;

            reason = safeGetAsString(sigDict, PdfName.REASON);
            location = safeGetAsString(sigDict, PdfName.LOCATION);
            contactDetails = safeGetAsString(sigDict, PdfName.CONTACTINFO);
        } else {
            date = pkcs7.getTimeStampDate().getTime();
        }

        AcroFields.FieldPosition fieldPosition = acroFields.getFieldPositions(sigName).get(0);
        int page = fieldPosition.page;

        Rectangle cropBox = getRotatedCropBox(pdfReader, page);

        float percentX = (fieldPosition.position.getLeft() - cropBox.getLeft()) / cropBox.getWidth();
        float percentY = 1 - (fieldPosition.position.getTop() - cropBox.getBottom()) / cropBox.getHeight();

        double percentWidth = fieldPosition.position.getWidth() / cropBox.getWidth();
        double percentHeight = fieldPosition.position.getHeight() / cropBox.getHeight();

        return new SignatureInfo(sigName, revision, page, percentX, percentY, percentWidth, percentHeight, name, date, location, reason, contactDetails, valid);
    }

    private static String safeGetAsString(final PdfDictionary sigDict, final PdfName key) {
        PdfString pdfString = sigDict.getAsString(key);
        if (pdfString != null) {
            return pdfString.toString();
        } else {
            return null;
        }
    }

    /**
     * returns cn field or if null all other non null fields
     */
    private static String extractSubjectName(X509Certificate cert) {
        String cn = CertificateInfo.getSubjectFields(cert).getField("CN");

        if (cn == null || cn.isEmpty()) {
            return cert.getSubjectX500Principal().toString();
        } else {
            return cn;
        }
    }

    /**
     * Get the size of the CropBox of the given page, ajusted according to page
     * rotation. Rotation is applied in 90 degrees increments, only.
     * <br>
     * NOTE: has NOT been tested with pages with asymmetric crop margins,
     * meaning: left != right or top != bottom
     *
     * @param pdfReader the PdfReader to read data from
     * @param page the page number. The first page is 1
     * @return a Rectangle representing the rotated CropBox
     */
    static Rectangle getRotatedCropBox(final PdfReader pdfReader, final int page) {
        Rectangle cropBox = pdfReader.getCropBox(page);
        int rotations = RectangleHelper.get90Rotations(pdfReader.getPageRotation(page));

        for (int i = 0; i < rotations; i++) {
            cropBox = cropBox.rotate();
        }

        return cropBox;
    }

}
