package com.linkare.assinare.server;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;

import com.linkare.assinare.sign.pdf.PDFSignatureFields;

/**
 *
 * @author bnazare
 */
@RequestScoped
public class PDFSignatureFieldsHolder {

    private PDFSignatureFields pdfSignatureFields;

    @Produces
    @RequestScoped
    public PDFSignatureFields getPdfSignatureFields() {
        return pdfSignatureFields;
    }

    public void setPdfSignatureFields(PDFSignatureFields pdfSignatureFields) {
        this.pdfSignatureFields = pdfSignatureFields;
    }

}
