package com.linkare.assinare.sign.pdf.dss;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;

import com.linkare.assinare.sign.DocumentParseException;
import com.linkare.assinare.sign.dss.DssTSLUtils;
import com.linkare.assinare.sign.model.AssinareDocument;
import com.linkare.assinare.sign.pdf.ParsedPdfInfo;
import com.linkare.assinare.sign.pdf.PdfParser;
import com.linkare.assinare.sign.pdf.SignatureFieldInfo;
import com.linkare.assinare.sign.pdf.SignatureInfo;

import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.pades.validation.PDFDocumentValidator;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.simplereport.SimpleReport;
import eu.europa.esig.dss.validation.AdvancedSignature;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.reports.Reports;

/**
 *
 * @author bnazare
 */
public class DssPdfParser implements PdfParser {

    @Override
    public boolean testPdf(AssinareDocument doc) {
        try (final InputStream docStream = doc.openInputStream();
                final PDDocument pdDoc = PDDocument.load(docStream)) {
            pdDoc.getVersion();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public ParsedPdfInfo parsePdf(AssinareDocument doc) throws DocumentParseException {
        try {
            try (final InputStream docStream = doc.openInputStream();
                    final PDDocument pdDoc = PDDocument.load(docStream)) {

                List<PDSignatureField> signatureFields = pdDoc.getSignatureFields();
                List<SignatureInfo> existingSignatures = listExistingSignatures(pdDoc, signatureFields, doc);
                List<SignatureFieldInfo> blankSigFields = getBlankSigFields(pdDoc, signatureFields);
                return new ParsedPdfInfo(existingSignatures, blankSigFields);
            }
        } catch (IOException e) {
            throw new DocumentParseException("Erro ao abrir PDF", e);
        }
    }

    private static List<SignatureInfo> listExistingSignatures(PDDocument pdDoc, List<PDSignatureField> signatureFields, AssinareDocument doc) throws IOException {
        DSSDocument dssDocument;
        try (final InputStream docStream = doc.openInputStream()) {
            dssDocument = new InMemoryDocument(docStream);
        }
        PDFDocumentValidator validator = new PDFDocumentValidator(dssDocument);
        CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();
        commonCertificateVerifier.setTrustedCertSources(DssTSLUtils.buildTSLCertificateSource());
        commonCertificateVerifier.setCrlSource(new OnlineCRLSource());
        commonCertificateVerifier.setOcspSource(new OnlineOCSPSource());
        validator.setCertificateVerifier(commonCertificateVerifier);
        List<AdvancedSignature> signatures = validator.getSignatures();
        Map<Date, String> sigSigners = new HashMap<>(signatures.size());
        Map<Date, Boolean> sigValidaties = new HashMap<>(signatures.size());
        final Reports reports = validator.validateDocument();
        final SimpleReport report = reports.getSimpleReport();
        report.getSignatureIdList().forEach((String sigId) -> {
            Date signingTime = report.getSigningTime(sigId);
            sigSigners.put(signingTime, report.getSignedBy(sigId));
            sigValidaties.put(signingTime, report.isValid(sigId));
        });
        int sigCount = 0;
        List<SignatureInfo> sigs = new LinkedList<>();
        for (PDSignatureField field : signatureFields) {
            final PDSignature signature = field.getValue();
            final PDAnnotationWidget widget = field.getWidgets().get(0);
            final PDRectangle rectangle = widget.getRectangle();
            final PDPage page = widget.getPage();
            final PDRectangle cropBox = page.getCropBox();
            if (signature != null) { // means empty signature field
                Calendar signDate = signature.getSignDate();
                if (signDate != null) { // this condition fails for document timestamp signatures (at least for the ones made with iText)
                    String fieldName = field.getFullyQualifiedName();
                    Date date = signDate.getTime();
                    String name = sigSigners.get(date);
                    int revision = ++sigCount; // FIXME: we should get the revision from the field
                    int pageNum = pdDoc.getPages().indexOf(page) + 1;
                    float percentX = (rectangle.getLowerLeftX() - cropBox.getLowerLeftX()) / cropBox.getWidth();
                    float percentY = 1 - (rectangle.getUpperRightY() - cropBox.getLowerLeftY()) / cropBox.getHeight();
                    double percentWidth = rectangle.getWidth() / cropBox.getWidth();
                    double percentHeight = rectangle.getHeight() / cropBox.getHeight();
                    String location = signature.getLocation();
                    String reason = signature.getReason();
                    String contactDetails = signature.getContactInfo();
                    boolean valid = sigValidaties.get(date);
                    sigs.add(new SignatureInfo(fieldName, revision, pageNum, percentX, percentY, percentWidth, percentHeight, name, date, location, reason, contactDetails, valid));
                }
            }
        }
        return sigs;
    }

    private static List<SignatureFieldInfo> getBlankSigFields(PDDocument doc, List<PDSignatureField> signatureFields) {
        List<SignatureFieldInfo> blankSigs = new LinkedList<>();
        signatureFields.forEach((PDSignatureField field) -> {
            final PDSignature signature = field.getValue();
            final PDAnnotationWidget widget = field.getWidgets().get(0);
            final PDRectangle rectangle = widget.getRectangle();
            final PDPage page = widget.getPage();
            final PDRectangle cropBox = page.getCropBox();
            if (signature == null) {
                // means empty signature field
                String fieldName = field.getFullyQualifiedName();
                int revision = 0; // FIXME: we should get the revision from the field
                int pageNum = doc.getPages().indexOf(page) + 1;
                float percentX = (rectangle.getLowerLeftX() - cropBox.getLowerLeftX()) / cropBox.getWidth();
                float percentY = 1 - (rectangle.getUpperRightY() - cropBox.getLowerLeftY()) / cropBox.getHeight();
                double percentWidth = rectangle.getWidth() / cropBox.getWidth();
                double percentHeight = rectangle.getHeight() / cropBox.getHeight();
                blankSigs.add(new SignatureFieldInfo(fieldName, revision, pageNum, percentX, percentY, percentWidth, percentHeight));
            }
        });
        return blankSigs;
    }

}
