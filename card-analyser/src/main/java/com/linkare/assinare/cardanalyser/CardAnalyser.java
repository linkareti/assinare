package com.linkare.assinare.cardanalyser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.utils.MOCCAUtils;

import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.CardNotSupportedException;
import at.gv.egiz.smcc.PinInfo;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.SignatureCardFactory;
import at.gv.egiz.smcc.pin.gui.PINGUI;
import at.gv.egiz.smcc.util.SMCCHelper;

/**
 *
 * @author bnazare
 */
public class CardAnalyser {

    private static final byte[] GEMSAFE_PTEID_APPLET_AID = new byte[]{(byte) 0x60, (byte) 0x46, (byte) 0x32, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x02};
    // TODO: change to int[] and test with a card
    private static final byte[] TUC_READ_DAT = new byte[]{(byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x0C};

    private static final String XMLDSIG_RSA_SHA1_ALGO_ID = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";

    private static final PrintWriter writer;

    static {
        Console console = System.console();
        if (console != null) {
            writer = console.writer();
        } else {
            writer = new PrintWriter(System.out, true);
        }
    }

    public static void main(String[] args) throws AssinareError, CardException, IOException {
        MOCCAUtils.loadPCSCLibrary();

        testTerminals();

        writer.println("Press the ENTER key to leave ...");
        System.in.read();
    }

    private static void testTerminals() throws CardException {
        TerminalFactory tf = TerminalFactory.getDefault();

        List<CardTerminal> terminals;
        try {
            terminals = tf.terminals().list();
            writer.println(terminals.size() + " terminals found.");
        } catch (CardException cex) {
            writer.println("No terminals found.");
            writer.println("");
            return;
        }

        terminals.forEach(terminal -> writer.println(terminal.getName()));
        writer.println("");

        for (CardTerminal terminal : terminals) {
            testTerminal(terminal);
            writer.println("");
        }
    }

    private static void testTerminal(CardTerminal terminal) throws CardException {
        boolean cardPresent = terminal.isCardPresent();
        writer.println(terminal.getName() + " has card: " + cardPresent);

        if (cardPresent) {
            Card card = terminal.connect("*");
            ATR atr = card.getATR();
            writer.println(terminal.getName() + " card: " + prettyHex(atr.getBytes()));

            testCard(card);
            writer.println("");

            testCardWithMocca(terminal, card);
        }
    }

    private static void testCard(Card card) throws CardException {
        CardChannel channel = card.getBasicChannel();

        ResponseAPDU response = channel.transmit(buildCommandAPDU());
        writer.println("response: " + prettyHex(response.getBytes()));
        writer.println("response status: " + Integer.toHexString(response.getSW()));
        writer.println("response data size: " + response.getNr());
        writer.println("response data: " + prettyHex(response.getData()));

        boolean isGemsafe = (response.getBytes().length == 2) && (response.getSW1() == 0x90) && (response.getSW2() == 0x00);
        writer.println("card is GemSafe: " + isGemsafe);
    }

    private static CommandAPDU buildCommandAPDU() {
        byte cla = TUC_READ_DAT[0];
        byte ins = TUC_READ_DAT[1];
        byte p1 = TUC_READ_DAT[2];
        byte p2 = TUC_READ_DAT[3];

        return new CommandAPDU(cla, ins, p1, p2, GEMSAFE_PTEID_APPLET_AID);
    }

    private static String prettyHex(byte[] bytes) {
        return SMCCHelper.toString(bytes);
    }

    private static void testCardWithMocca(CardTerminal terminal, Card card) {
        SignatureCardFactory signatureCardFactory = SignatureCardFactory.getInstance();
        try {
            SignatureCard signatureCard = signatureCardFactory.createSignatureCard(card, terminal);
            writer.println("MOCCA recognized card as: " + signatureCard);

            testCertificate(signatureCard);
        } catch (CardNotSupportedException ex) {
            writer.println("MOCCA did not recognize the card");
        }
    }

    private static void testCertificate(SignatureCard signatureCard) {
        try {
            byte[] certBytes = signatureCard.getCertificate(null, new ConsolePINGUI());
            writer.println("Obtained certificate");
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            X509Certificate certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certBytes));
            writer.println("Obtained certificate for: " + certificate.getSubjectX500Principal());

            testSignature(signatureCard, certificate);
        } catch (SignatureCardException | InterruptedException ex) {
            writer.println("Error obtaining certificate");
        } catch (CertificateException ex) {
            writer.println("Error parsing certificate");
        }
    }

    private static void testSignature(SignatureCard signatureCard, X509Certificate certificate) {
        try {
            byte[] inData = "1234567890".getBytes();
            byte[] signedData = signatureCard.createSignature(new ByteArrayInputStream(inData), SignatureCard.KeyboxName.SECURE_SIGNATURE_KEYPAIR, new ConsolePINGUI(), XMLDSIG_RSA_SHA1_ALGO_ID);
            writer.println("Created signature successfully");

            Signature sig = Signature.getInstance("SHA1WithRSA");
            sig.initVerify(certificate.getPublicKey());
            sig.update(inData);
            boolean verify = sig.verify(signedData);
            if (verify) {
                writer.println("Signature is correct");
            } else {
                writer.println("Signature is incorrect");
            }
        } catch (SignatureCardException | InterruptedException | IOException ex) {
            writer.println("Error creating signature");
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException ex) {
            writer.println("Error verifying signature");
        }
    }

    public static class ConsolePINGUI implements PINGUI {

        @Override
        public void allKeysCleared() {
            // no-op
        }

        @Override
        public void correctionButtonPressed() {
            // no-op

        }

        @Override
        public void enterPIN(PinInfo spec, int retries) throws CancelledException,
                InterruptedException {
            // no-op

        }

        @Override
        public void enterPINDirect(PinInfo spec, int retries)
                throws CancelledException, InterruptedException {
            // no-op

        }

        @Override
        public void validKeyPressed() {
            // no-op
        }

        @Override
        public char[] providePIN(PinInfo pinSpec, int retries)
                throws CancelledException, InterruptedException {
            if (System.console() != null) {
                char[] pin = System.console().readPassword("Enter %s: ", pinSpec.getLocalizedName());

                if (pin == null || pin.length == 0) {
                    throw new CancelledException();
                }

                return pin;
            } else {
                writer.println("Enter " + pinSpec.getLocalizedName() + ":");
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

                String pin;
                try {
                    pin = in.readLine();
                } catch (IOException e) {
                    throw new CancelledException(e);
                }

                if (pin == null || pin.length() == 0) {
                    throw new CancelledException();
                }

                return pin.toCharArray();
            }
        }

    }

}
