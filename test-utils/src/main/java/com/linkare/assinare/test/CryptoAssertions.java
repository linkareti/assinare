package com.linkare.assinare.test;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.junit.platform.commons.JUnitException;
import org.xml.sax.SAXException;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.jaxb.object.Message;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.pades.validation.PDFDocumentValidator;
import eu.europa.esig.dss.policy.ValidationPolicy;
import eu.europa.esig.dss.policy.ValidationPolicyFacade;
import eu.europa.esig.dss.policy.jaxb.Algo;
import eu.europa.esig.dss.policy.jaxb.AlgoExpirationDate;
import eu.europa.esig.dss.simplereport.SimpleReport;
import eu.europa.esig.dss.spi.x509.CertificateSource;
import eu.europa.esig.dss.spi.x509.CommonTrustedCertificateSource;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.reports.Reports;

/**
 *
 * @author bnazare
 */
public final class CryptoAssertions {

    private static final CertificateToken DUMMY_CERT = new CertificateToken(DummyCrypto.getUserACert());
    private static final CertificateToken DUMMY_TSA_CERT = new CertificateToken(DummyCrypto.getTsaCert());

    private CryptoAssertions() {
    }

    private static void assertValidSignature(InputStream signedFile, ValidationPolicy policy) {
        DSSDocument dssDocument = new InMemoryDocument(signedFile);
        PDFDocumentValidator validator = new PDFDocumentValidator(dssDocument);

        CertificateSource certSource = new CommonTrustedCertificateSource();
        certSource.addCertificate(DUMMY_CERT);
        certSource.addCertificate(DUMMY_TSA_CERT);

        CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();
        commonCertificateVerifier.setTrustedCertSources(certSource);
//        commonCertificateVerifier.setCrlSource(new OnlineCRLSource());
//        commonCertificateVerifier.setOcspSource(new OnlineOCSPSource());
        validator.setCertificateVerifier(commonCertificateVerifier);

        final Reports reports;
        if (policy == null) {
            reports = validator.validateDocument();
        } else {
            reports = validator.validateDocument(policy);
        }
        final SimpleReport simpleReport = reports.getSimpleReport();
        final String signatureId = simpleReport.getFirstSignatureId();
        final boolean validSignature = simpleReport.isValid(signatureId);
        final List<Message> errors = simpleReport.getAdESValidationErrors(signatureId);
        final List<Message> warnings = simpleReport.getAdESValidationWarnings(signatureId);

        if (!errors.isEmpty()) {
            System.out.println("Errors: " + errors.stream().map(Message::getValue).collect(toList()));
        }
        if (!warnings.isEmpty()) {
            System.out.println("Warnings: " + warnings.stream().map(Message::getValue).collect(toList()));
        }

        if (!validSignature) {
            fail("Produced signature is invalid");
        }
    }

    public static void assertValidSignature(InputStream signedFile) {
        assertValidSignature(signedFile, null);
    }

    public static void assertValidSignature(File signedFile) throws IOException {
        try (InputStream fis = new FileInputStream(signedFile)) {
            assertValidSignature(fis);
        }
    }

    public static void assertValidSignatureForCC(InputStream signedFile) {
        assertValidSignature(signedFile, getPatchedDefaultPolicyForCC());
    }

    public static void assertValidSignatureForCC(File signedFile) throws IOException {
        try (InputStream fis = new FileInputStream(signedFile)) {
            assertValidSignatureForCC(fis);
        }
    }

    private static ValidationPolicy getPatchedDefaultPolicyForCC() throws JUnitException {
        try {
            ValidationPolicy validationPolicy = ValidationPolicyFacade.newFacade().getDefaultValidationPolicy();
            AlgoExpirationDate expirationDates = validationPolicy.getCryptographic().getAlgoExpirationDate();
            // reset expiration date for SHA-1 (2009)
            for (Algo algo : expirationDates.getAlgos()) {
                if (algo.getValue().equals(DigestAlgorithm.SHA1.getName())) {
                    algo.setDate(null);
                }
            }

            return validationPolicy;
        } catch (JAXBException | XMLStreamException | IOException | SAXException ex) {
            throw new JUnitException("Bad policy data", ex);
        }
    }
}
