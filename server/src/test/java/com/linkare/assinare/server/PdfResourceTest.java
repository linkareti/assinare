package com.linkare.assinare.server;

import static com.linkare.assinare.sign.AssinareConstants.PDF_DEFAULT_PAGE;
import static com.linkare.assinare.sign.AssinareConstants.SIGNATURE_DEFAULT_HEIGHT;
import static com.linkare.assinare.sign.AssinareConstants.SIGNATURE_DEFAULT_WIDTH;
import static com.linkare.assinare.sign.AssinareConstants.SIGNATURE_DEFAULT_X_PCT;
import static com.linkare.assinare.sign.AssinareConstants.SIGNATURE_DEFAULT_Y_PCT;
import static com.linkare.assinare.sign.SignatureRenderingMode.TEXT_ONLY;
import static com.linkare.assinare.test.CryptoAssertions.assertValidSignature;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.server.test.Profiles;
import com.linkare.assinare.server.test.utils.PdfCryptoUtils;
import com.linkare.assinare.server.test.utils.ScriptEngineTestHelper;
import com.linkare.assinare.sign.pdf.PDFSignatureFields;
import com.linkare.assinare.test.DummyCrypto;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;

/**
 *
 * @author bnazare
 */
@QuarkusTest
@TestProfile(Profiles.Default.class)
public class PdfResourceTest {

    private static final Base64.Encoder B64_ENCODER = Base64.getEncoder();
    private static final Base64.Decoder B64_DECODER = Base64.getDecoder();

    private static final String SIG_ALGO = "SHA384withRSAandMGF1";
    private static final ZoneId TZ = ZoneId.of("Europe/Lisbon");

    private static final String TC1_DOC = "docs/testdoc.pdf";
    private static final ZonedDateTime TC1_DATE = ZonedDateTime.of(2019, 12, 1, 0, 0, 0, 0, TZ);
    private static final X509Certificate TC1_CERT = DummyCrypto.getUserACert();
    private static final PDFSignatureFields TC1_FIELDS = new PDFSignatureFields(
            null, null, "my-reason",
            SIGNATURE_DEFAULT_X_PCT, SIGNATURE_DEFAULT_Y_PCT,
            PDF_DEFAULT_PAGE, SIGNATURE_DEFAULT_WIDTH, SIGNATURE_DEFAULT_HEIGHT,
            TEXT_ONLY, null, null, null, null, null);
    private static final String TC1_EXPECTED_HASH = "MYHRMBgGCSqGSIb3DQEJAzELBgkqhkiG9w0BBwEwPwYJKoZIhvcNAQkEMTIEMPEMjiLO/WQHEFOtv1h2O0P0eS+eRp3Vec4K7zQ2DGfM5pI7QPbW5C7pEeL+pSzRADB0BgsqhkiG9w0BCRACLzFlMGMwYTBfMA0GCWCGSAFlAwQCAgUABDCWL75cXG2OlWgk9iEkmfNvoByBGGlMYPMBLEYOu42rttjYo0e5++v3Mm6mXoyy+SwwHDAUpBIwEDEOMAwGA1UEAwwFZHVtbXkCBF3W3BI=";

    private static Signature sig;

    @Inject
    ScriptEngineTestHelper fileHelper;

    @BeforeAll
    public static void setUpClass() {
        try {
            sig = Signature.getInstance(SIG_ALGO);
            sig.initSign(DummyCrypto.getUserAPrivKey());
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @AfterEach
    public void tearDown() {
        fileHelper.deleteAll();
    }

    @Test
    public void testGetHash() throws IOException, AssinareException, AssinareError, CertificateEncodingException {
        try (InputStream testdocData = getClass().getClassLoader().getResourceAsStream(TC1_DOC)) {
            fileHelper.putOriginalFile("testdoc.pdf", testdocData);
        }

        Map<String, Object> input = new HashMap<>();

        input.put("docName", "testdoc.pdf");
        input.put("cert", B64_ENCODER.encodeToString(TC1_CERT.getEncoded()));
        input.put("date", TC1_DATE);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(input)
                .when()
                .post("/data/pdf/hash")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .body("hash", is(TC1_EXPECTED_HASH));
    }

    @Test
    public void testSignHash() throws Exception {
        try (InputStream testdocData = getClass().getClassLoader().getResourceAsStream(TC1_DOC)) {
            fileHelper.putOriginalFile("testdoc.pdf", testdocData);
        }

        byte[] hash = B64_DECODER.decode(TC1_EXPECTED_HASH);
        sig.update(hash);
        byte[] signedHash = sig.sign();

        Map<String, Object> input = new HashMap<>();

        input.put("docName", "testdoc.pdf");
        input.put("cert", B64_ENCODER.encodeToString(TC1_CERT.getEncoded()));
        input.put("date", TC1_DATE);
        input.put("signedHash", B64_ENCODER.encodeToString(signedHash));

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(input)
                .when()
                .post("/data/pdf/sign")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode())
                .contentType("")
                .body(is(""));

        File signedFile = fileHelper.getSignedFile("testdoc.pdf");

        assertNotNull(signedFile);
        assertValidSignature(signedFile);
    }

    /**
     * Utility method to re-generate the expected hashes.
     *
     * @param args
     * @throws IOException
     * @throws AssinareException
     * @throws AssinareError
     */
    public static void main(String[] args) throws IOException, AssinareException, AssinareError {
        try (InputStream docData = PdfResourceTest.class.getClassLoader().getResourceAsStream(TC1_DOC)) {
            byte[] hash = new PdfCryptoUtils().calculateHash(docData, List.of(TC1_CERT), SIG_ALGO, TC1_FIELDS, TC1_DATE);

            System.out.println("TC1_EXPECTED_HASH = \"" + B64_ENCODER.encodeToString(hash) + "\"");
        }
    }

}
