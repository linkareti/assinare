/**
 * DSS - Digital Signature Services
 * Copyright (C) 2015 European Commission, provided under the CEF programme
 *
 * This file is part of the "DSS - Digital Signature Services" project.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.linkare.assinare.sign.dss.token;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.smartcardio.Card;
import javax.smartcardio.CardTerminal;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.commons.utils.MOCCAUtils;

import at.gv.egiz.smcc.CardNotSupportedException;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.smcc.SignatureCard.KeyboxName;
import at.gv.egiz.smcc.SignatureCardFactory;
import at.gv.egiz.smcc.pin.gui.PINGUI;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.EncryptionAlgorithm;
import eu.europa.esig.dss.enumerations.MaskGenerationFunction;
import eu.europa.esig.dss.enumerations.SignatureAlgorithm;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.Digest;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.spi.DSSASN1Utils;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.SignatureTokenConnection;

/**
 * MOCCA SignatureTokenConnection
 *
 */
@SuppressWarnings("restriction")
public class MOCCASignatureTokenConnection implements SignatureTokenConnection {

    private static final Logger LOG = LoggerFactory.getLogger(MOCCASignatureTokenConnection.class);

    private final PINGUI callback;

    private List<SignatureCard> signatureCards;

    /**
     * Use this constructor when the signature algorithm is not known before the
     * connection is opened. You must set the SignatureAlgorithm property of the
     * key after the connection has been opened (you can get the
     * SignatureAlgorithm name from the key)
     *
     * @param callback provides the PIN
     */
    public MOCCASignatureTokenConnection(PINGUI callback) {
        this.callback = callback;
    }

    /**
     * Sets a signature cards list
     *
     * @param signatureCards a list of {@link SignatureCard}s
     */
    public void setSignatureCards(List<SignatureCard> signatureCards) {
        this.signatureCards = new ArrayList(signatureCards);
    }

    @Override
    public void close() {

        if (signatureCards != null) {
            for (SignatureCard c : signatureCards) {
                c.disconnect(true);
            }
            signatureCards.clear();
            signatureCards = null;
        }
    }

    private List<SignatureCard> getSignatureCards() {

        if (signatureCards == null) {

            signatureCards = new ArrayList<>();
            SignatureCardFactory factory = SignatureCardFactory.getInstance();

            Map<CardTerminal, Card> cardMap;
            try {
                cardMap = MOCCAUtils.getAllAvailableCards();
            } catch (AssinareException ex) {
                // FIXME: just extend DSSException
                throw new DSSException(ex);
            }

            for (Entry<CardTerminal, Card> entry : cardMap.entrySet()) {
                try {
                    signatureCards.add(factory.createSignatureCard(entry.getValue(), entry.getKey()));
                } catch (CardNotSupportedException e) {
                    // just log the error - MOCCA tries to connect to all cards and we may have an MSCAPI or PKCS11 also
                    // inserted.
                    LOG.info(e.getMessage(), e);
                }
            }
        }

        return new ArrayList(signatureCards);
    }

    @Override
    public List<DSSPrivateKeyEntry> getKeys() throws DSSException {

        List<DSSPrivateKeyEntry> list = getKeysSeveralCards();
        if (list.isEmpty()) {
            // FIXME: just extend DSSException
            throw new DSSException(
                    new AssinareException("Não foram encontrados cartões compatíveis com esta aplicação.")
            );
        }
        return list;
    }

    private List<DSSPrivateKeyEntry> getKeysSeveralCards() throws DSSException {

        final List<DSSPrivateKeyEntry> list = new ArrayList<>();
        final List<SignatureCard> cardList = getSignatureCards();
        int index = 0;
        for (SignatureCard sc : cardList) {

            try {
                final byte[] data = sc.getCertificate(KeyboxName.SECURE_SIGNATURE_KEYPAIR, callback);
                final List<byte[]> chain = sc.getCertificates(KeyboxName.SECURE_SIGNATURE_KEYPAIR, callback);
                if (data != null) {
                    list.add(new MOCCAPrivateKeyEntry(data, KeyboxName.SECURE_SIGNATURE_KEYPAIR, index, sc.getCard().getATR().getBytes(), chain));
                }
            } catch (Exception e) {

                LOG.error(e.getMessage(), e);
            }
            /* This code was disabled to avoid duplicates return by the PT driver
             * (at.gv.egiz.smcc.PtEidCard)
             */
//            try {
//                final byte[] data = sc.getCertificate(KeyboxName.CERTIFIED_KEYPAIR, callback);
//                final List<byte[]> chain = sc.getCertificates(KeyboxName.CERTIFIED_KEYPAIR, callback);
//                if (data != null) {
//                    list.add(new MOCCAPrivateKeyEntry(data, KeyboxName.CERTIFIED_KEYPAIR, index, sc.getCard().getATR().getBytes(), chain));
//                }
//            } catch (Exception e) {
//                LOG.error(e.getMessage(), e);
//            }
            index++;
        }
        return list;
    }

    @Override
    public SignatureValue sign(ToBeSigned toBeSigned, DigestAlgorithm digestAlgorithm, DSSPrivateKeyEntry keyEntry) throws DSSException {
        final InputStream inputStream = new ByteArrayInputStream(toBeSigned.getBytes());
        if (!(keyEntry instanceof MOCCAPrivateKeyEntry)) {
            throw new UnsupportedOperationException("Unsupported DSSPrivateKeyEntry instance " + keyEntry.getClass() + " / Must be MOCCAPrivateKeyEntry.");
        }

        final MOCCAPrivateKeyEntry moccaKey = (MOCCAPrivateKeyEntry) keyEntry;
        if (signatureCards == null) {
            throw new IllegalStateException("The cards have not been initialised");
        }
        // TODO Bob:20130619 This is not completely true, it is true only for the last card. The signing certificate
        // should be checked.
        if (moccaKey.getPos() > (signatureCards.size() - 1)) {
            throw new IllegalStateException("Card was removed or disconnected " + moccaKey.getPos() + " " + signatureCards.size());
        }
        final SignatureCard signatureCard = signatureCards.get(moccaKey.getPos());
        final EncryptionAlgorithm encryptionAlgo = moccaKey.getEncryptionAlgorithm();
        final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.getAlgorithm(encryptionAlgo, digestAlgorithm);

        LOG.info("MOCCA>>>Signature algorithm: {}", signatureAlgorithm.getJCEId());
        try {
            final KeyboxName keyboxName = moccaKey.getKeyboxName();
            byte[] signedData = signatureCard.createSignature(inputStream, keyboxName, callback, signatureAlgorithm.getUri());
            if (EncryptionAlgorithm.ECDSA.equals(encryptionAlgo)) {
                signedData = encode(signedData);
            }

            SignatureValue value = new SignatureValue();
            value.setAlgorithm(signatureAlgorithm);
            value.setValue(signedData);
            return value;

        } catch (Exception e) {
            throw new DSSException(String.format("An error occurred on signing : %s", e.getMessage()), e);
        }
    }

    @Override
    public SignatureValue sign(ToBeSigned toBeSigned, DigestAlgorithm digestAlgorithm, MaskGenerationFunction mgf, DSSPrivateKeyEntry keyEntry)
            throws DSSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SignatureValue sign(ToBeSigned toBeSigned, SignatureAlgorithm signatureAlgorithm, DSSPrivateKeyEntry keyEntry) throws DSSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SignatureValue signDigest(Digest digest, DSSPrivateKeyEntry keyEntry) throws DSSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SignatureValue signDigest(Digest digest, MaskGenerationFunction mgf, DSSPrivateKeyEntry keyEntry)
            throws DSSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SignatureValue signDigest(Digest digest, SignatureAlgorithm signatureAlgorithm, DSSPrivateKeyEntry keyEntry)
            throws DSSException {
        throw new UnsupportedOperationException();
    }

    /**
     * The ECDSA_SIG structure consists of two BIGNUMs for the r and s value of
     * a ECDSA signature (see X9.62 or FIPS 186-2).<br>
     * This encoding is not implemented at the level of MOCCA!
     *
     * @param signedData binaries
     * @return encoded byte array
     */
    private static byte[] encode(byte[] signedData) {
        final int half = signedData.length / 2;
        final byte[] firstPart = new byte[half];
        final byte[] secondPart = new byte[half];

        System.arraycopy(signedData, 0, firstPart, 0, half);
        System.arraycopy(signedData, half, secondPart, 0, half);

        final BigInteger r = new BigInteger(1, firstPart);
        final BigInteger s = new BigInteger(1, secondPart);

        final ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(new ASN1Integer(r));
        v.add(new ASN1Integer(s));

        return DSSASN1Utils.getDEREncoded(new DERSequence(v));
    }

}
