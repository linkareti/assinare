package com.linkare.assinare.sign.asic;

import static com.linkare.assinare.sign.test.SwingTestUtils.waitForEDT;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import com.linkare.assinare.commons.ui.SignatureStage;
import com.linkare.assinare.sign.keysupplier.dss.LazyMOCCASigningKeySupplier;
import com.linkare.assinare.sign.test.LocalizationAwareTest;

/**
 * Tests {@link ContainerSignatureViewer}. This test is WIP.<br>
 * TODO: test methods {@link ContainerSignatureViewer#getSignatureFields()},
 * {@link ContainerSignatureViewer#getSigningKey()},
 * {@link ContainerSignatureViewer#publicPublish(SignatureStage...)} and
 * {@link ContainerSignatureViewer#signatureDone()}.<br>
 * TODO: test page changing more thoroughly.
 *
 * @author bnazare
 */
@DisabledIf(
        value = "com.linkare.assinare.sign.test.SwingTestUtils#isHeadless",
        disabledReason = "Tests will error out in headless environment"
)
public class ContainerSignatureViewerTest extends LocalizationAwareTest {

    @Test
    public void testDataReady() throws IOException, InterruptedException, InvocationTargetException {
        ContainerSignatureViewer containerSignatureViewer = new ContainerSignatureViewer(false, new ASiCSignatureFields(), new LazyMOCCASigningKeySupplier(null));
        try {
            assertNotNull(containerSignatureViewer.getGlassPane());
            assertTrue(containerSignatureViewer.getGlassPane().isVisible());

            containerSignatureViewer.dataReady(null);
            waitForEDT();
            assertFalse(containerSignatureViewer.getGlassPane().isVisible());
        } finally {
            containerSignatureViewer.dispose();
        }
    }

}
