package com.linkare.assinare.sign.swing;

import static com.linkare.assinare.sign.test.SwingTestUtils.waitForEDT;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Window;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link AboutDialog}. This test is WIP.
 *
 * @author bnazare
 */
public class AboutDialogTest {

    @Test
    public void testAboutDialog() throws InterruptedException, InvocationTargetException {
        // all our Swing screens extend from JDialog, so we use that
        JDialog parentDialog = new JDialog();
        try {
            // showModal() is a blocking call so if we call invokeAndWait() here,
            // the test will get stuck
            SwingUtilities.invokeLater(() -> {
                AboutDialog.showModal(parentDialog, null);
            });

            waitForEDT();

            Window[] ownedWindows = parentDialog.getOwnedWindows();
            assertEquals(1, ownedWindows.length);
            assertEquals(AboutDialog.class, ownedWindows[0].getClass());
        } finally {
            parentDialog.dispose();
        }
    }

}
