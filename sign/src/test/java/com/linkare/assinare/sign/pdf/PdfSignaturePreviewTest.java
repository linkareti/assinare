package com.linkare.assinare.sign.pdf;

import static com.linkare.assinare.sign.AssinareConstants.PDF_DEFAULT_PAGE;
import static com.linkare.assinare.sign.AssinareConstants.SIGNATURE_DEFAULT_HEIGHT;
import static com.linkare.assinare.sign.AssinareConstants.SIGNATURE_DEFAULT_WIDTH;
import static com.linkare.assinare.sign.AssinareConstants.SIGNATURE_DEFAULT_X_PCT;
import static com.linkare.assinare.sign.AssinareConstants.SIGNATURE_DEFAULT_Y_PCT;
import static com.linkare.assinare.sign.AssinareConstants.VALID_EXISTING_SIGNATURES_COLOR;
import static com.linkare.assinare.sign.test.SwingTestUtils.findComponent;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.junit.jupiter.api.Test;

import com.linkare.assinare.sign.SignatureRenderingMode;
import com.linkare.assinare.sign.model.AssinareDocument;
import com.linkare.assinare.sign.model.InMemoryDocument;
import com.linkare.assinare.sign.test.LocalizationAwareTest;

/**
 * Tests {@link PdfSignaturePreview}. This test is WIP.<br>
 * TODO: test method {@link PdfSignaturePreview#changeToPage(int)}
 *
 * @author bnazare
 */
public class PdfSignaturePreviewTest extends LocalizationAwareTest {

    private static final PDFSignatureFields DEFAULT_SIG_OPTS = new PDFSignatureFields(
            "www.linkare.com", "my-location", "my-reason",
            SIGNATURE_DEFAULT_X_PCT, SIGNATURE_DEFAULT_Y_PCT,
            PDF_DEFAULT_PAGE, SIGNATURE_DEFAULT_WIDTH, SIGNATURE_DEFAULT_HEIGHT,
            SignatureRenderingMode.PRE_DEFINED_LOGO, null, "none", false);

    @Test
    public void testInit_2args() throws IOException {
        try (PdfSignaturePreview pdfSignaturePreview = new PdfSignaturePreview()) {
            AssinareDocument doc = new InMemoryDocument("dummy", getClass().getClassLoader().getResourceAsStream("docs/testdoc.pdf"));
            pdfSignaturePreview.init(doc, DEFAULT_SIG_OPTS);

//            throws NPE. intended?
//            assertEquals(1, pdfSignaturePreview.getPageCount());
            assertEquals(1, pdfSignaturePreview.getCurrentPage());
            assertEquals(0, pdfSignaturePreview.getPercentX());
            assertEquals(0, pdfSignaturePreview.getPercentY());
            assertEquals(0, pdfSignaturePreview.getSignatureWidth());
            assertEquals(0, pdfSignaturePreview.getSignatureHeight());

            assertTrue(pdfSignaturePreview.isShowSigTarget());
            pdfSignaturePreview.setShowSigTarget(false);
            assertFalse(pdfSignaturePreview.isShowSigTarget());
        }
    }

    @Test
    public void testInit_3args() throws IOException {
        try (PdfSignaturePreview pdfSignaturePreview = new PdfSignaturePreview()) {
            AssinareDocument doc = new InMemoryDocument("dummy", getClass().getClassLoader().getResourceAsStream("docs/testdoc.pdf"));
            SignatureInfo sigInfo = new SignatureInfo("dummy-field", 1, 1, 0, 0, 10, 10,
                    "TEST SIGNER NAME", new Date(1617911287000L), // 08-04-2021 20:48:07 WEST
                    "test location", "test reason", "test contact info", true);
            List<SignatureInfo> sigInfos = Collections.singletonList(sigInfo);
            pdfSignaturePreview.init(doc, DEFAULT_SIG_OPTS, new ParsedPdfInfo(sigInfos, Collections.EMPTY_LIST));

            assertEquals(1, pdfSignaturePreview.getPageCount());
            assertEquals(1, pdfSignaturePreview.getCurrentPage());
            assertEquals(SIGNATURE_DEFAULT_X_PCT, pdfSignaturePreview.getPercentX());
            assertEquals(SIGNATURE_DEFAULT_Y_PCT, pdfSignaturePreview.getPercentY());
            assertEquals(SIGNATURE_DEFAULT_WIDTH, pdfSignaturePreview.getSignatureWidth());
            assertEquals(SIGNATURE_DEFAULT_HEIGHT, pdfSignaturePreview.getSignatureHeight());

            JPanel pdfPreview = (JPanel) findComponent(pdfSignaturePreview, "scaledPanel", "pdfPreview");
            List<JLabel> existingSigLabels = new ArrayList();
            for (Component comp : pdfPreview.getComponents()) {
                if (comp instanceof JLabel && !"lblSignature".equals(comp.getName())) {
                    existingSigLabels.add((JLabel) comp);
                }
            }

            assertEquals(1, existingSigLabels.size());

            JLabel firstSigLabel = existingSigLabels.get(0);
            assertEquals("1", firstSigLabel.getText());
            assertEquals(VALID_EXISTING_SIGNATURES_COLOR, firstSigLabel.getBackground());
            assertEquals(
                    "<html>TEST SIGNER NAME<br/>  test location, 08-04-2021 20:48:07<br/>  Razão: test reason<br/>  Contacto: test contact info</html>",
                    firstSigLabel.getToolTipText());
        }
    }

}
