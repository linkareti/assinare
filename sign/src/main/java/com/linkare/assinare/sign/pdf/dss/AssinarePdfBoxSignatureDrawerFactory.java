package com.linkare.assinare.sign.pdf.dss;

import eu.europa.esig.dss.pades.SignatureImageParameters;
import eu.europa.esig.dss.pdf.pdfbox.visible.PdfBoxSignatureDrawer;
import eu.europa.esig.dss.pdf.pdfbox.visible.PdfBoxSignatureDrawerFactory;
import eu.europa.esig.dss.pdf.pdfbox.visible.nativedrawer.NativePdfBoxVisibleSignatureDrawer;

public class AssinarePdfBoxSignatureDrawerFactory implements PdfBoxSignatureDrawerFactory {

    @Override
    public PdfBoxSignatureDrawer getSignatureDrawer(SignatureImageParameters imageParameters) {
        return new NativePdfBoxVisibleSignatureDrawer();
    }

}
