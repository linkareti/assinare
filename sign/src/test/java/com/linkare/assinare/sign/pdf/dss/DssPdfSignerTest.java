package com.linkare.assinare.sign.pdf.dss;

import static com.linkare.assinare.sign.AssinareConstants.PDF_DEFAULT_PAGE;
import static com.linkare.assinare.sign.AssinareConstants.SIGNATURE_DEFAULT_HEIGHT;
import static com.linkare.assinare.sign.AssinareConstants.SIGNATURE_DEFAULT_WIDTH;
import static com.linkare.assinare.sign.AssinareConstants.SIGNATURE_DEFAULT_X_PCT;
import static com.linkare.assinare.sign.AssinareConstants.SIGNATURE_DEFAULT_Y_PCT;
import static com.linkare.assinare.test.CryptoAssertions.assertValidSignatureForCC;
import static java.util.Base64.getDecoder;
import static java.util.Base64.getEncoder;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.commons.ui.AssinarePINGUI;
import com.linkare.assinare.commons.ui.SignatureStage;
import com.linkare.assinare.sign.SignatureRenderingMode;
import com.linkare.assinare.sign.SigningKey;
import com.linkare.assinare.sign.keysupplier.dss.MockSigningKey;
import com.linkare.assinare.sign.model.AssinareDocument;
import com.linkare.assinare.sign.model.InMemoryDocument;
import com.linkare.assinare.sign.pdf.PDFSignatureFields;

/**
 *
 * @author bnazare
 */
public class DssPdfSignerTest {

    private static final String DEFAULT_DIGEST_ALGORITHM = "SHA384";
    private static final String DEFAULT_SIG_ALGO = DEFAULT_DIGEST_ALGORITHM + "withRSA";

    private static final PDFSignatureFields DEFAULT_SIG_OPTS = new PDFSignatureFields(
            "www.linkare.com", "my-location", "my-reason",
            SIGNATURE_DEFAULT_X_PCT, SIGNATURE_DEFAULT_Y_PCT,
            PDF_DEFAULT_PAGE, SIGNATURE_DEFAULT_WIDTH, SIGNATURE_DEFAULT_HEIGHT,
            SignatureRenderingMode.PRE_DEFINED_LOGO, null, "none", false);

    private static final ZonedDateTime DEFAULT_DATE = ZonedDateTime.of(2019, 12, 1, 0, 0, 0, 0, ZoneId.of("Europe/Lisbon"));
    private static final String HASH_FROM_DEFAULTS = "MYHRMBgGCSqGSIb3DQEJAzELBgkqhkiG9w0BBwEwPwYJKoZIhvcNAQkEMTIEMF6T8VRzmETaj4zXfThl1fLW3hoWqeRPimucj2/4mmOOuMyx3OOa6LPbenTwYUvYnzB0BgsqhkiG9w0BCRACLzFlMGMwYTBfMA0GCWCGSAFlAwQCAgUABDCWL75cXG2OlWgk9iEkmfNvoByBGGlMYPMBLEYOu42rttjYo0e5++v3Mm6mXoyy+SwwHDAUpBIwEDEOMAwGA1UEAwwFZHVtbXkCBF3W3BI=";

    @Test
    public void testSign() throws IOException, AssinareException, AssinareError, GeneralSecurityException {
        SigningKey signingKey = new MockSigningKey();
        AssinareDocument target = new InMemoryDocument("dummy", getClass().getClassLoader().getResourceAsStream("docs/testdoc.pdf"));
        AssinarePINGUI pinCallback = new AssinarePINGUI(1);
        PDFSignatureFields signOptions = DEFAULT_SIG_OPTS;

        DssPdfSigner instance = new DssPdfSigner();
        AssinareDocument signedDoc = instance.sign(signingKey, target, pinCallback, signOptions,
                (SignatureStage... chunks) -> {
                } //NOOP
        );

        assertValidSignatureForCC(signedDoc.openInputStream());
    }

    @Test
    public void testSign_BadData() throws IOException, AssinareException, AssinareError, GeneralSecurityException {
        SigningKey signingKey = new MockSigningKey();
        AssinareDocument target = new InMemoryDocument("dummy", new byte[]{1, 2, 3, 4});
        AssinarePINGUI pinCallback = new AssinarePINGUI(1);
        PDFSignatureFields signOptions = DEFAULT_SIG_OPTS;

        DssPdfSigner instance = new DssPdfSigner();
        AssinareError asnError = assertThrows(AssinareError.class,
                () -> instance.sign(signingKey, target, pinCallback, signOptions,
                        (SignatureStage... chunks) -> {
                        } //NOOP
                )
        );

        assertNotNull(asnError.getCause());
        assertEquals(IOException.class, asnError.getCause().getClass());
    }

    @Test
    public void testHashPdf() throws IOException, AssinareException, AssinareError, GeneralSecurityException {
        SigningKey signingKey = new MockSigningKey();
        AssinareDocument target = new InMemoryDocument("dummy", getClass().getClassLoader().getResourceAsStream("docs/testdoc.pdf"));
        PDFSignatureFields signOptions = DEFAULT_SIG_OPTS;

        DssPdfSigner instance = new DssPdfSigner();
        byte[] result = instance.hashPdf(target, signingKey.getCertificateChain(), DEFAULT_SIG_ALGO, signOptions, DEFAULT_DATE);

        assertNotNull(result);
        assertEquals(HASH_FROM_DEFAULTS, getEncoder().encodeToString(result));
    }

    @Test
    public void testSignPdf_6args() throws IOException, AssinareException, AssinareError, GeneralSecurityException {
        SigningKey signingKey = new MockSigningKey();
        AssinareDocument target = new InMemoryDocument("dummy", getClass().getClassLoader().getResourceAsStream("docs/testdoc.pdf"));
        PDFSignatureFields signOptions = DEFAULT_SIG_OPTS;

        DssPdfSigner instance = new DssPdfSigner();
        byte[] hash = getDecoder().decode(HASH_FROM_DEFAULTS);
        byte[] signedHash = signingKey.sign(hash, DEFAULT_DIGEST_ALGORITHM);
        AssinareDocument result = instance.signPdf(target, signingKey.getCertificateChain(), signedHash, DEFAULT_SIG_ALGO, signOptions, DEFAULT_DATE);

        assertNotNull(result);
        assertValidSignatureForCC(result.openInputStream());
    }

}
