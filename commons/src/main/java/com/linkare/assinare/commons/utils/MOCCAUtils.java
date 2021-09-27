package com.linkare.assinare.commons.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

import javax.smartcardio.Card;
import javax.smartcardio.CardTerminal;

import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;

import at.gv.egiz.smcc.util.LinuxLibraryFinder;
import at.gv.egiz.smcc.util.SmartCardIO;

/**
 *
 * @author bnazare
 */
public class MOCCAUtils {

    private MOCCAUtils() {
    }

    public static void loadPCSCLibrary() throws AssinareError {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Linux")) {
            File libFile;
            try {
                libFile = LinuxLibraryFinder.getLibraryPath("pcsclite", "1");
                System.setProperty("sun.security.smartcardio.library", libFile.getAbsolutePath());
            } catch (FileNotFoundException e) {
                throw new AssinareError("PC/SC library not found", e);
            }
        }
    }

    public static Map<CardTerminal, Card> getAllAvailableCards() throws AssinareException {
        SmartCardIO smartCardIO = new SmartCardIO();
        if (smartCardIO.isPCSCSupported()) {
            if (smartCardIO.isTerminalPresent()) {
                return smartCardIO.getCards();
            } else {
                throw new AssinareException("Não existem leitores de cartão ligados ao sistema.");
            }
        } else {
            throw new AssinareException("A leitura de dados do cartão não é suportada neste sistema.");
        }
    }

}
