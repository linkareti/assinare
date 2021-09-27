package com.linkare.assinare.sign.pdf.dss;

import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Date;

import org.junit.jupiter.api.Test;

import com.linkare.assinare.sign.DocumentParseException;
import com.linkare.assinare.sign.model.AssinareDocument;
import com.linkare.assinare.sign.model.InMemoryDocument;
import com.linkare.assinare.sign.pdf.ParsedPdfInfo;
import com.linkare.assinare.sign.pdf.SignatureFieldInfo;
import com.linkare.assinare.sign.pdf.SignatureInfo;

/**
 *
 * @author bnazare
 */
public class DssPdfParserTest {

    private static final byte[] NOT_A_PDF = new byte[]{1, 2, 3, 4};

    @Test
    public void testTestPdf() throws IOException {
        AssinareDocument doc = new InMemoryDocument("dummy", getClass().getClassLoader().getResourceAsStream("docs/testdoc.pdf"));

        DssPdfParser instance = new DssPdfParser();
        boolean result = instance.testPdf(doc);

        assertTrue(result);
    }

    @Test
    public void testTestPdf_BadData() throws IOException {
        AssinareDocument doc = new InMemoryDocument("dummy", NOT_A_PDF);

        DssPdfParser instance = new DssPdfParser();
        boolean result = instance.testPdf(doc);

        assertFalse(result);
    }

    @Test
    public void testParsePdf() throws Exception {
        AssinareDocument doc = new InMemoryDocument("dummy", getClass().getClassLoader().getResourceAsStream("docs/testdoc-signed-official-cmd.pdf"));

        DssPdfParser instance = new DssPdfParser();
        ParsedPdfInfo result = instance.parsePdf(doc);

        SignatureInfo sigInfo = new SignatureInfo("Signature1", 1, 1,
                0.32843137f, 0.7045455f, 0.29411765933036804, 0.0767676830291748,
                "BRUNO GONÇALO NAZARÉ GONÇALVES",
                new Date(1616001820000L), // Wed Mar 17 17:23:40 WET 2021
                "anywhere", "testing", null, true);
        ParsedPdfInfo expResult = new ParsedPdfInfo(singletonList(sigInfo), EMPTY_LIST);
        assertEquals(expResult, result);
    }

    @Test
    public void testParsePdf_BlankSigFields() throws Exception {
        AssinareDocument doc = new InMemoryDocument("dummy", getClass().getClassLoader().getResourceAsStream("docs/testdoc-blank-sig-fields.pdf"));

        DssPdfParser instance = new DssPdfParser();
        ParsedPdfInfo result = instance.parsePdf(doc);

        SignatureFieldInfo blankSigInfo = new SignatureFieldInfo("Signature2",
                0, 1, 0.054516394f, 0.048458815f, 0.6479661464691162, 0.10242551565170288);
        ParsedPdfInfo expResult = new ParsedPdfInfo(EMPTY_LIST, singletonList(blankSigInfo));
        assertEquals(expResult, result);
    }

    @Test
    public void testParsePdf_NoSignatures() throws Exception {
        AssinareDocument doc = new InMemoryDocument("dummy", getClass().getClassLoader().getResourceAsStream("docs/testdoc.pdf"));

        DssPdfParser instance = new DssPdfParser();
        ParsedPdfInfo result = instance.parsePdf(doc);

        ParsedPdfInfo expResult = new ParsedPdfInfo(EMPTY_LIST, EMPTY_LIST);
        assertEquals(expResult, result);
    }

    @Test
    @SuppressWarnings("ThrowableResultIgnored")
    public void testParsePdf_BadData() throws Exception {
        AssinareDocument doc = new InMemoryDocument("dummy", NOT_A_PDF);

        DssPdfParser instance = new DssPdfParser();
        assertThrows(DocumentParseException.class,
                () -> instance.parsePdf(doc)
        );
    }

}
