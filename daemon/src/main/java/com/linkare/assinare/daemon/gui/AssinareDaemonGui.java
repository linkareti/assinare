package com.linkare.assinare.daemon.gui;

import java.awt.AWTException;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.linkare.assinare.daemon.AssinareDaemon;

public final class AssinareDaemonGui {

    private static final Logger LOG = Logger.getLogger(AssinareDaemonGui.class.getName());

    public static void createDaemonGui(AssinareDaemon ad) throws HeadlessException {
        if (SystemTray.isSupported()) {
            createAssinareDaemonInSystemTray(ad);
        } else {
            createShutdownDialog(ad);
        }
    }

    private static void createShutdownDialog(final AssinareDaemon ad) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane optionPane = new JOptionPane("<html>O Assinare Daemon foi iniciado.<br/>" + "Clique no botão para terminar.</html>",
                    JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION);

            JDialog dialog = optionPane.createDialog("Assinare Daemon");
            dialog.setIconImage(new ImageIcon(AssinareDaemonGui.class.getResource("/icons/assinareIconHeader.png")).getImage());
            /*
            * This particular modality type will prevent this dialog from
            * blocking unrelated windows while still blocking execution on
            * setVisible(true) below.
             */
            dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
            dialog.setVisible(true); // blocking

            dialog.dispose();
            ad.shutdown();
        });
    }

    private static void createAssinareDaemonInSystemTray(final AssinareDaemon ad) {
        final PopupMenu popup = new PopupMenu();

        final TrayIcon trayIcon = new TrayIcon(createImage("/icons/assinare-icon1.png", "tray icon"));
        final SystemTray tray = SystemTray.getSystemTray();

        // Create a pop-up menu components
        MenuItem stopItem = new MenuItem("Stop");
        MenuItem aboutItem = new MenuItem("About");

        stopItem.addActionListener(evt -> {
            tray.remove(trayIcon);
            ad.shutdown();
        });

        // Add components to pop-up menu
        popup.add(stopItem);
        popup.addSeparator();
        popup.add(aboutItem);

        trayIcon.setPopupMenu(popup);
        trayIcon.setToolTip("Assinare Daemon");
        trayIcon.setImageAutoSize(true);

        try {
            tray.add(trayIcon);
        } catch (AWTException awtex) {
            LOG.log(Level.SEVERE, "TrayIcon could not be added.", awtex);
        }
    }

    // Obtain the image URL
    private static Image createImage(String path, String description) {
        URL imageURL = AssinareDaemon.class.getResource(path);

        if (imageURL == null) {
            LOG.log(Level.SEVERE, "Resource not found: {0}", path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }

    public static void closeGui() {
        if (SystemTray.isSupported()) {
            final SystemTray systemTray = SystemTray.getSystemTray();
            for (TrayIcon trayIcon : systemTray.getTrayIcons()) {
                systemTray.remove(trayIcon);
            }
        } else {
            for (Frame frame : Frame.getFrames()) {
                frame.dispose();
            }
        }
    }

    private AssinareDaemonGui() {
    }
}
