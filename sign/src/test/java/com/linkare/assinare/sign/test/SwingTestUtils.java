package com.linkare.assinare.sign.test;

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 *
 * @author bnazare
 */
public class SwingTestUtils {

    private SwingTestUtils() {
    }

    public static boolean isHeadless() {
        return GraphicsEnvironment.isHeadless();
    }

    /**
     * Waits for all events presently in the EDT queue to be processed. This is
     * done by adding a no-op event to the EDT queue and waiting for it to
     * finish.
     *
     * @throws InterruptedException
     * @throws InvocationTargetException
     */
    public static void waitForEDT() throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(() -> {
        });
    }

    public static Component findComponent(JFrame dialog, String... names) {
        return findComponent(dialog.getContentPane(), names);
    }

    public static Component findComponent(JDialog dialog, String... names) {
        return findComponent(dialog.getContentPane(), names);
    }

    public static Component findComponent(Container ct, String... names) {
        Component c = ct;
        for (String name : names) {
            c = findComponent((Container) c, name);
        }
        return c;
    }

    private static Component findComponent(Container ct, String name) {
        for (Component component : ct.getComponents()) {
            if (name.equals(component.getName())) {
                return component;
            }
        }
        throw new IllegalStateException("Swing component not found");
    }

}
