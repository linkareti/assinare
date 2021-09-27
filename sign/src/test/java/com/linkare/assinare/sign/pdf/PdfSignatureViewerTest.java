package com.linkare.assinare.sign.pdf;

import static com.linkare.assinare.sign.test.SwingTestUtils.findComponent;
import static com.linkare.assinare.sign.test.SwingTestUtils.waitForEDT;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.MenuElement;
import javax.swing.plaf.basic.ComboPopup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import com.linkare.assinare.commons.ui.SignatureStage;
import com.linkare.assinare.sign.keysupplier.dss.LazyMOCCASigningKeySupplier;
import com.linkare.assinare.sign.model.AssinareDocument;
import com.linkare.assinare.sign.model.InMemoryDocument;
import com.linkare.assinare.sign.test.LocalizationAwareTest;

/**
 * Tests {@link PdfSignatureViewer}. This test is WIP.<br>
 * TODO: test methods {@link PdfSignatureViewer#getSignatureFields()},
 * {@link PdfSignatureViewer#getSigningKey()},
 * {@link PdfSignatureViewer#publicPublish(SignatureStage...)} and
 * {@link PdfSignatureViewer#signatureDone()}.<br>
 * TODO: test page changing more thoroughly.
 *
 * @author bnazare
 */
@DisabledIf(
        value = "com.linkare.assinare.sign.test.SwingTestUtils#isHeadless",
        disabledReason = "Tests will error out in headless environment"
)
public class PdfSignatureViewerTest extends LocalizationAwareTest {

    private static final String VALID_SIG_ICON_URL = PdfSignatureViewerTest.class.getResource("/icons/validState.png").toString();

    @Test
    public void testDataReady_Signed() throws IOException, InterruptedException, InvocationTargetException {
        AssinareDocument doc = new InMemoryDocument("dummy", getClass().getClassLoader().getResourceAsStream("docs/testdoc-signed-official-cmd.pdf"));

        PdfSignatureViewer pdfSignatureViewer = new PdfSignatureViewer(false, new PDFSignatureFields(), new LazyMOCCASigningKeySupplier(null));
        try {
            pdfSignatureViewer.setVisible(true);
            assertNotNull(pdfSignatureViewer.getGlassPane());
            assertTrue(pdfSignatureViewer.getGlassPane().isVisible());

            pdfSignatureViewer.dataReady(doc);
            waitForEDT();
            assertFalse(pdfSignatureViewer.getGlassPane().isVisible());

            JLabel jLabelSignsState = (JLabel) findComponent(pdfSignatureViewer, "jPanelBottom", "jLabelSignsState");

            assertNotNull(jLabelSignsState.getIcon());
            assertEquals(VALID_SIG_ICON_URL, jLabelSignsState.getIcon().toString());

            jLabelSignsState.dispatchEvent(new MouseEvent(jLabelSignsState, MouseEvent.MOUSE_CLICKED, 1, 0, 0, 0, 0, false));

            JLayeredPane layeredPane = pdfSignatureViewer.getRootPane().getLayeredPane();
            Component[] popupComps = ((Container) layeredPane.getComponentsInLayer(JLayeredPane.POPUP_LAYER)[0]).getComponents();
            JPopupMenu popup = (JPopupMenu) popupComps[0];
            List<String> signatureDescs = new ArrayList();
            for (MenuElement subElement : popup.getSubElements()) {
                JMenuItem menuItem = (JMenuItem) subElement;
                signatureDescs.add(menuItem.getText());
            }

            List<String> expectedSignatureDescs = Arrays.asList(
                    "<html>1: BRUNO GONÇALO NAZARÉ GONÇALVES<br/><small>17-03-2021 17:23:40 - testing</small></html>"
            );
            assertEquals(expectedSignatureDescs, signatureDescs);

            JButton jButtonIncPage = (JButton) findComponent(pdfSignatureViewer, "jPanelBottom", "jButtonIncPage");
            jButtonIncPage.doClick();
        } finally {
            pdfSignatureViewer.dispose();
        }
    }

    @Test
    public void testDataReady_BlankSigFields() throws IOException, InterruptedException, InvocationTargetException {
        AssinareDocument doc = new InMemoryDocument("dummy", getClass().getClassLoader().getResourceAsStream("docs/testdoc-blank-sig-fields.pdf"));

        PdfSignatureViewer pdfSignatureViewer = new PdfSignatureViewer(false, new PDFSignatureFields(), new LazyMOCCASigningKeySupplier(null));
        try {
            assertNotNull(pdfSignatureViewer.getGlassPane());
            assertTrue(pdfSignatureViewer.getGlassPane().isVisible());

            pdfSignatureViewer.dataReady(doc);
            waitForEDT();
            assertFalse(pdfSignatureViewer.getGlassPane().isVisible());

            JComboBox<SignatureFieldInfo> jComboBoxSigField = (JComboBox) findComponent(pdfSignatureViewer, "jPanelBottom", "jComboBoxSigField");

            List<SignatureFieldInfo> blankSigInfos = new ArrayList();
            for (int i = 0; i < jComboBoxSigField.getItemCount(); i++) {
                SignatureFieldInfo item = jComboBoxSigField.getItemAt(i);
                blankSigInfos.add(item);
            }

            ComboPopup comboPopup = (ComboPopup) jComboBoxSigField.getAccessibleContext().getAccessibleChild(0);
            JList list = comboPopup.getList();
            ListModel model = list.getModel();
            ListCellRenderer cr = list.getCellRenderer();
            List<String> optionNames = new ArrayList();
            for (int i = 0; i < model.getSize(); i++) {
                Object value = model.getElementAt(i);
                JLabel rendererLabel = (JLabel) cr.getListCellRendererComponent(list, value, -1, false, false);
                optionNames.add(rendererLabel.getText());
            }

            List<SignatureFieldInfo> expectedBlankSigInfos = Arrays.asList(
                    new SignatureFieldInfo(null, -1, -1, -1, -1, -1, -1),
                    new SignatureFieldInfo("Signature2", 0, 1, 0.054516394f, 0.048458815f, 0.6479661464691162f, 0.10242551565170288f));
            assertEquals(expectedBlankSigInfos, blankSigInfos);

            List<String> expectedOptionNames = Arrays.asList("-Custom-", "Signature2 (Page 1)");
            assertEquals(expectedOptionNames, optionNames);
        } finally {
            pdfSignatureViewer.dispose();
        }
    }

    @Test
    public void testDataReady_BadData() throws IOException, InterruptedException, InvocationTargetException {
        AssinareDocument doc = new InMemoryDocument("dummy", new byte[]{1, 2, 3, 4});

        PdfSignatureViewer pdfSignatureViewer = new PdfSignatureViewer(false, new PDFSignatureFields(), new LazyMOCCASigningKeySupplier(null));
        try {
            assertNotNull(pdfSignatureViewer.getGlassPane());
            assertTrue(pdfSignatureViewer.getGlassPane().isVisible());

            pdfSignatureViewer.dataReady(doc);
            waitForEDT();
            assertFalse(pdfSignatureViewer.getGlassPane().isVisible());

            // TODO: test correct panel is shown
        } finally {
            pdfSignatureViewer.dispose();
        }
    }

}
