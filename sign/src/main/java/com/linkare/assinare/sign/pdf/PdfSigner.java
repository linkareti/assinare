package com.linkare.assinare.sign.pdf;

import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;

import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.sign.Signer;
import com.linkare.assinare.sign.model.AssinareDocument;

/**
 *
 * @author bnazare
 */
public interface PdfSigner extends Signer<AssinareDocument, PDFSignatureFields> {

    byte[] hashPdf(AssinareDocument target, X509Certificate[] certChain, String signatureAlgorithmName, PDFSignatureFields signOptions, ZonedDateTime signingDate) throws AssinareException, AssinareError;

    AssinareDocument signPdf(AssinareDocument target, X509Certificate[] certChain, byte[] signedHash, String signatureAlgorithmName, PDFSignatureFields signOptions, ZonedDateTime signingDate) throws AssinareException, AssinareError;

}
