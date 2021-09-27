package com.linkare.assinare.sign.pdf.dss;

import eu.europa.esig.dss.pdf.IPdfObjFactory;
import eu.europa.esig.dss.pdf.PDFServiceMode;
import eu.europa.esig.dss.pdf.PDFSignatureService;
import eu.europa.esig.dss.pdf.pdfbox.PdfBoxSignatureService;

public class AssinarePdfObjFactory implements IPdfObjFactory {

    @Override
    public PDFSignatureService newPAdESSignatureService() {
        return new PdfBoxSignatureService(PDFServiceMode.SIGNATURE, new AssinarePdfBoxSignatureDrawerFactory());
    }

    @Override
    public PDFSignatureService newContentTimestampService() {
        return new PdfBoxSignatureService(PDFServiceMode.CONTENT_TIMESTAMP, new AssinarePdfBoxSignatureDrawerFactory());
    }

    @Override
    public PDFSignatureService newSignatureTimestampService() {
        return new PdfBoxSignatureService(PDFServiceMode.SIGNATURE_TIMESTAMP, new AssinarePdfBoxSignatureDrawerFactory());
    }

    @Override
    public PDFSignatureService newArchiveTimestampService() {
        return new PdfBoxSignatureService(PDFServiceMode.ARCHIVE_TIMESTAMP, new AssinarePdfBoxSignatureDrawerFactory());
    }

}
