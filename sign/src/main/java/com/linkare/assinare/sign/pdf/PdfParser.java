package com.linkare.assinare.sign.pdf;

import com.linkare.assinare.sign.DocumentParseException;
import com.linkare.assinare.sign.model.AssinareDocument;

/**
 *
 * @author bnazare
 */
public interface PdfParser {

    boolean testPdf(AssinareDocument doc);
    
    ParsedPdfInfo parsePdf(AssinareDocument doc) throws DocumentParseException;
    
}
