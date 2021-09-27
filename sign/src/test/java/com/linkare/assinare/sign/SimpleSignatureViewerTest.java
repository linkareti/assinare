package com.linkare.assinare.sign;

import static com.linkare.assinare.sign.test.SwingTestUtils.findComponent;
import static com.linkare.assinare.sign.test.SwingTestUtils.waitForEDT;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import com.linkare.assinare.commons.ui.SignatureStage;
import com.linkare.assinare.sign.keysupplier.dss.MockSigningKey;
import com.linkare.assinare.sign.pdf.PDFSignatureFields;
import com.linkare.assinare.sign.swing.DeckCardsPanel;
import com.linkare.assinare.sign.test.LocalizationAwareTest;

/**
 * Tests {@link SimpleSignatureViewer}. This test is WIP.<br>
 * TODO: test methods {@link SimpleSignatureViewer#getSignatureFields()},
 * {@link SimpleSignatureViewer#getSigningKey()},
 * {@link SimpleSignatureViewer#publicPublish(SignatureStage...)} and
 * {@link SimpleSignatureViewer#signatureDone()}.
 *
 * @author bnazare
 */
@DisabledIf(
        value = "com.linkare.assinare.sign.test.SwingTestUtils#isHeadless",
        disabledReason = "Tests will error out in headless environment"
)
public class SimpleSignatureViewerTest extends LocalizationAwareTest {

    @Test
    public void testKeyLoading() throws IOException, InterruptedException, InvocationTargetException {
        MockKeySupplier mockKeySupplier = new MockKeySupplier();

        SimpleSignatureViewer simpleSignatureViewer = new SimpleSignatureViewer(false, new PDFSignatureFields(), mockKeySupplier);
        try {
            assertNotNull(simpleSignatureViewer.getGlassPane());
            assertTrue(simpleSignatureViewer.getGlassPane().isVisible());

            simpleSignatureViewer.dataReady(null);
            waitForEDT();
            assertFalse(simpleSignatureViewer.getGlassPane().isVisible());

            JComboBox sigKeyComboBox = (JComboBox) findComponent(simpleSignatureViewer, "sigKeyComboBox");
            JButton reloadButton = (JButton) findComponent(simpleSignatureViewer, "reloadButton");
            DeckCardsPanel deckCards = (DeckCardsPanel) findComponent(simpleSignatureViewer, "deckCards");

            assertNotNull(sigKeyComboBox);
            assertNotNull(reloadButton);
            assertNotNull(deckCards);

            assertTrue(sigKeyComboBox.isEnabled());
            assertEquals(1, sigKeyComboBox.getItemCount());
            assertTrue(deckCards.isEnabled());

            mockKeySupplier.setKeyCount(0);
            reloadButton.doClick();

            // Swing workers are nearly impossible to wait for directly, so sleep a little
            Thread.sleep(100);

            assertFalse(sigKeyComboBox.isEnabled());
            assertNotNull(sigKeyComboBox.getModel());
            assertEquals(0, sigKeyComboBox.getItemCount());
            assertFalse(deckCards.isEnabled());

            mockKeySupplier.setKeyCount(2);
            reloadButton.doClick();

            // Sleep some more
            Thread.sleep(100);

            assertTrue(sigKeyComboBox.isEnabled());
            assertNotNull(sigKeyComboBox.getModel());
            assertEquals(2, sigKeyComboBox.getItemCount());
            assertTrue(deckCards.isEnabled());
        } finally {
            simpleSignatureViewer.dispose();
        }
    }

    private static class MockKeySupplier implements KeySupplier {

        private final List<SigningKey> keys = new ArrayList();

        public MockKeySupplier() {
            setKeyCount(1);
        }

        @Override
        public List<SigningKey> getKeys() {
            return keys;
        }

        @Override
        public void close() throws Exception {
            // NOOP
        }

        public void setKeyCount(int keyCount) {
            keys.clear();
            for (int i = 0; i < keyCount; i++) {
                SigningKey key;
                try {
                    key = new MockSigningKey();
                } catch (IOException | GeneralSecurityException ex) {
                    // should never happen
                    throw new IllegalStateException(ex);
                }
                keys.add(key);
            }
        }
    }

}
