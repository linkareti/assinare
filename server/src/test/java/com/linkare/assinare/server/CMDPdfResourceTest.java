package com.linkare.assinare.server;

import static com.linkare.assinare.server.scriptengine.ScriptBackedFileServiceTest.BAD_PDF_DATA;
import static com.linkare.assinare.test.CryptoAssertions.assertValidSignature;
import static io.restassured.RestAssured.given;
import static java.time.ZoneOffset.UTC;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.server.pojo.PDFSignatureConfiguration;
import com.linkare.assinare.server.test.Profiles;
import com.linkare.assinare.server.test.resources.FakeSCMDService;
import com.linkare.assinare.server.test.utils.PdfCryptoUtils;
import com.linkare.assinare.server.test.utils.ScriptEngineTestHelper;
import com.linkare.assinare.sign.pdf.PDFSignatureFields;
import com.linkare.assinare.test.DummyCrypto;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;

@QuarkusTest
@TestProfile(Profiles.Default.class)
public class CMDPdfResourceTest {

    private static final Pattern UUID_PATTERN = Pattern.compile("[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}");

    private static X509Certificate dummyCert;
    private static MessageDigest cmdMessageDigest;
    
    private final FakeSCMDService mockSCMDService = FakeSCMDService.INSTANCE;

    @Inject
    SigningContextCache cache;

    @Inject
    ScriptEngineTestHelper fileHelper;

    @Inject
    CMDConfiguration cmdConfiguration;
    
    @Inject
    PDFSignatureConfiguration pdfSignatureConfiguration;

    @Inject
    PdfCryptoUtils pdfCryptoUtils;
    
    @BeforeAll
    public static void setup() throws IOException {
        try {
            cmdMessageDigest = MessageDigest.getInstance(CMDPdfResource.CMD_DIGEST_ALGORITHM);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }

        dummyCert = DummyCrypto.getUserACert();
    }

    @BeforeEach
    public void init() throws IOException {
        mockSCMDService.clearAll();
    }
    
    @AfterEach
    public void tearDown() {
        fileHelper.deleteAll();
    }

    @Test
    public void testHelloEndpoint() throws GeneralSecurityException {
        given()
                .when().get("/cmd/pdf")
                .then()
                .statusCode(200)
                .body(is("hello"));
    }

    @Test
    public void testInitializeEndpoint() throws IOException, AssinareException, AssinareError {
        mockSCMDService.addKnownApplicationId(cmdConfiguration.applicationId());
        mockSCMDService.addKnownUser("+351 931234567", "1234");

        try (InputStream testdocData = getClass().getClassLoader().getResourceAsStream("docs/testdoc.pdf")) {
            fileHelper.putOriginalFile("testdoc.pdf", testdocData);
            testdocData.reset();

            Map<String, Object> input = new HashMap<>();
            Map<String, String> options = new HashMap<>();

            options.put("reason", "test-reason");

            input.put("docNames", Arrays.asList("testdoc.pdf"));
            input.put("userId", "+351 931234567");
            input.put("userPin", "1234");
            input.put("signatureOptions", options);

            String processId = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(input)
            .when()
                .post("/cmd/pdf/initialize")
            .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .body("processId", matchesPattern(UUID_PATTERN))
                .extract()
                .<String>path("processId");

            SigningContext signingContext = cache.getIfPresent(processId);
            assertNotNull(signingContext);
            assertNotNull(signingContext.getSigningDate());
            assertEquals(Arrays.asList("testdoc.pdf"), signingContext.getDocNames());
            assertEquals(List.of(dummyCert), signingContext.getCertChain());
            assertNotNull(signingContext.getSigFields());
            assertEquals("test-reason", signingContext.getSigFields().getReason());

            Map<String, byte[]> hashes = mockSCMDService.getKnowHashes(processId);
            assertNotNull(hashes);
            assertEquals(1, hashes.size());

            byte[] expectedHash = calculatePrefixedHash(testdocData, signingContext);
            byte[] hash = mockSCMDService.getKnowHash(processId, "testdoc.pdf");
            assertArrayEquals(expectedHash, hash);
        }
    }
    
    /**
     * Send all possible parameters to test the deserializers fully.
     *
     * @throws IOException
     * @throws AssinareException
     * @throws AssinareError
     */
    @Test
    public void testInitializeEndpoint_AllParams() throws IOException, AssinareException, AssinareError {
        mockSCMDService.addKnownApplicationId(cmdConfiguration.applicationId());
        mockSCMDService.addKnownUser("+351 931234567", "1234");

        try (InputStream testdocData = getClass().getClassLoader().getResourceAsStream("docs/testdoc.pdf")) {
            fileHelper.putOriginalFile("testdoc.pdf", testdocData);
            testdocData.reset();

            Map<String, Object> input = new HashMap<>();
            Map<String, String> options = new HashMap<>();

            options.put("contact", "test-contact");
            options.put("location", "test-location");
            options.put("reason", "test-reason");
            options.put("percentX", "0.1");
            options.put("percentY", "0.1");
            options.put("width", "100");
            options.put("height", "25");
            options.put("pageNumber", "5");
            options.put("sigRenderingMode", "INVISIBLE");
            options.put("logoFileURL", "http://example.org/example.png");
            options.put("fieldName", "test-field");
            options.put("tsaUrl", "none");
            options.put("doLTV", "false");

            input.put("docNames", Arrays.asList("testdoc.pdf"));
            input.put("userId", "+351 931234567");
            input.put("userPin", "1234");
            input.put("signatureOptions", options);
            input.put("docParams", Map.of("optiona", "valuea", "optionb", "valueb"));

            String processId = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(input)
            .when()
                .post("/cmd/pdf/initialize")
            .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .body("processId", matchesPattern(UUID_PATTERN))
                .extract()
                .<String>path("processId");

            assertThat(processId, not(emptyString()));
        }
    }

    @Test
    public void testInitializeEndpointMulti() throws IOException, AssinareException, AssinareError {
        mockSCMDService.addKnownApplicationId(cmdConfiguration.applicationId());
        mockSCMDService.addKnownUser("+351 931234567", "1234");
        
        try (InputStream testdocData = getClass().getClassLoader().getResourceAsStream("docs/testdoc.pdf");
                InputStream testdocData2 = getClass().getClassLoader().getResourceAsStream("docs/testdoc_pdfa.pdf")) {
            fileHelper.putOriginalFile("testdoc.pdf", testdocData);
            testdocData.reset();
            fileHelper.putOriginalFile("testdoc_pdfa.pdf", testdocData2);
            testdocData2.reset();

            Map<String, Object> input = new HashMap<>();
            Map<String, String> options = new HashMap<>();

            options.put("reason", "test-reason");

            input.put("docNames", Arrays.asList("testdoc.pdf", "testdoc_pdfa.pdf"));
            input.put("userId", "+351 931234567");
            input.put("userPin", "1234");
            input.put("signatureOptions", options);

            String processId = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(input)
            .when()
                .post("/cmd/pdf/initialize")
            .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .body("processId", matchesPattern(UUID_PATTERN))
                .extract()
                .<String>path("processId");

            SigningContext signingContext = cache.getIfPresent(processId);
            assertNotNull(signingContext);
            assertNotNull(signingContext.getSigningDate());
            assertEquals(Arrays.asList("testdoc.pdf", "testdoc_pdfa.pdf"), signingContext.getDocNames());
            assertEquals(List.of(dummyCert), signingContext.getCertChain());
            assertNotNull(signingContext.getSigFields());
            assertEquals("test-reason", signingContext.getSigFields().getReason());

            Map<String, byte[]> hashes = mockSCMDService.getKnowHashes(processId);
            assertNotNull(hashes);
            assertEquals(2, hashes.size());

            byte[] expectedHash = calculatePrefixedHash(testdocData, signingContext);
            byte[] hash = mockSCMDService.getKnowHash(processId, "testdoc.pdf");
            assertArrayEquals(expectedHash, hash);

            byte[] expectedHash2 = calculatePrefixedHash(testdocData2, signingContext);
            byte[] hash2 = mockSCMDService.getKnowHash(processId, "testdoc_pdfa.pdf");
            assertArrayEquals(expectedHash2, hash2);
        }
    }

    @Test
    public void testFinalizeEndpoint() throws IOException, AssinareException, AssinareError {
        mockSCMDService.addKnownApplicationId(cmdConfiguration.applicationId());

        final String testProcessId = "11111111-1111-1111-1111-111111111111";
        final String docName = "testdoc.pdf";
        final List<String> docNames = Arrays.asList(docName);
        final ZonedDateTime signingDate = ZonedDateTime.of(2019, 12, 1, 0, 0, 0, 0, UTC);
        final List<X509Certificate> certChain = List.of(dummyCert);
        final PDFSignatureFields sigFields = pdfSignatureConfiguration.toPDFSignatureFields();

        try (InputStream testdocData = getClass().getClassLoader().getResourceAsStream("docs/testdoc.pdf")) {
            fileHelper.putOriginalFile("testdoc.pdf", testdocData);
            testdocData.reset();

            byte[] prefixedHash = calculatePrefixedHash(testdocData, certChain, sigFields, signingDate);
            mockSCMDService.putKnowHash(testProcessId, docName, prefixedHash);
        }

        mockSCMDService.addKnownOTP(testProcessId, "123456");
        cache.put(testProcessId, new SigningContext(signingDate, certChain, docNames, sigFields));

        Map<String, String> input = new HashMap<>();
        input.put("processId", testProcessId);
        input.put("userOtp", "123456");

        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(input)
        .when()
            .post("/cmd/pdf/finalize")
        .then()
            .statusCode(Status.NO_CONTENT.getStatusCode())
            .body(is(""));

        assertValidSignature(fileHelper.getSignedFile(docName));
    }

    @Test
    public void testFinalizeEndpointMulti() throws IOException, AssinareException, AssinareError {
        mockSCMDService.addKnownApplicationId(cmdConfiguration.applicationId());

        final String testProcessId = "11111111-1111-1111-1111-111111111112";
        final String docName = "testdoc.pdf";
        final String docName2 = "testdoc_pdfa.pdf";
        final List<String> docNames = Arrays.asList(docName, docName2);
        final ZonedDateTime signingDate = ZonedDateTime.of(2019, 12, 1, 0, 0, 0, 0, UTC);
        final List<X509Certificate> certChain = List.of(dummyCert);
        final PDFSignatureFields sigFields = pdfSignatureConfiguration.toPDFSignatureFields();

        try (InputStream testdocData = getClass().getClassLoader().getResourceAsStream("docs/testdoc.pdf");
                InputStream testdocData2 = getClass().getClassLoader().getResourceAsStream("docs/testdoc_pdfa.pdf")) {
            fileHelper.putOriginalFile("testdoc.pdf", testdocData);
            testdocData.reset();
            fileHelper.putOriginalFile("testdoc_pdfa.pdf", testdocData2);
            testdocData2.reset();

            byte[] knownHash = calculatePrefixedHash(testdocData, certChain, sigFields, signingDate);
            mockSCMDService.putKnowHash(testProcessId, docName, knownHash);

            byte[] knownHash2 = calculatePrefixedHash(testdocData2, certChain, sigFields, signingDate);
            mockSCMDService.putKnowHash(testProcessId, docName2, knownHash2);
        }

        mockSCMDService.addKnownOTP(testProcessId, "567890");
        cache.put(testProcessId, new SigningContext(signingDate, certChain, docNames, sigFields));

        Map<String, String> input = new HashMap<>();
        input.put("processId", testProcessId);
        input.put("userOtp", "567890");

        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(input)
        .when()
            .post("/cmd/pdf/finalize")
        .then()
            .statusCode(Status.NO_CONTENT.getStatusCode())
            .body(is(""));

        assertValidSignature(fileHelper.getSignedFile(docName));
        assertValidSignature(fileHelper.getSignedFile(docName2));
    }
    
    @Test
    public void testInitializeEndpoint_BadUserPin() throws IOException, AssinareException, AssinareError {
        mockSCMDService.addKnownApplicationId(cmdConfiguration.applicationId());
        mockSCMDService.addKnownUser("+351 931234567", "1234");

        try (InputStream testdocData = getClass().getClassLoader().getResourceAsStream("docs/testdoc.pdf")) {
            fileHelper.putOriginalFile("testdoc.pdf", testdocData);
        }
        
        Map<String, Object> input = new HashMap<>();

        input.put("docNames", Arrays.asList("testdoc.pdf"));
        input.put("userId", "+351 000000000");
        input.put("userPin", "0000");

        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(input)
        .when()
            .post("/cmd/pdf/initialize")
        .then()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .contentType(ContentType.JSON)
            .body("code", is(ErrorCode.BAD_USER_PIN.name()))
            .body("message", not(emptyString()));
    }
    
    @Test
    public void testInitializeEndpoint_FileNotFound() throws IOException, AssinareException, AssinareError {
        mockSCMDService.addKnownApplicationId(cmdConfiguration.applicationId());
        mockSCMDService.addKnownUser("+351 931234567", "1234");

        Map<String, Object> input = new HashMap<>();

        input.put("docNames", Arrays.asList("testdoc.pdf"));
        input.put("userId", "+351 931234567");
        input.put("userPin", "1234");

        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(input)
        .when()
            .post("/cmd/pdf/initialize")
        .then()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .contentType(ContentType.JSON)
            .body("code", is(ErrorCode.FILE_NOT_FOUND.name()))
            .body("message", not(emptyString()));
    }
    
    // simulate a signing error by passing dummy data as the document
    @Test
    public void testInitializeEndpoint_GeneralError() throws IOException, AssinareException, AssinareError {
        mockSCMDService.addKnownApplicationId(cmdConfiguration.applicationId());
        mockSCMDService.addKnownUser("+351 931234567", "1234");

        fileHelper.putOriginalFile("testdoc.pdf", new ByteArrayInputStream(BAD_PDF_DATA));

        Map<String, Object> input = new HashMap<>();

        input.put("docNames", Arrays.asList("testdoc.pdf"));
        input.put("userId", "+351 931234567");
        input.put("userPin", "1234");

        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(input)
        .when()
            .post("/cmd/pdf/initialize")
        .then()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .contentType(ContentType.JSON)
            .body("code", is(ErrorCode.GENERAL_ERROR.name()))
            .body("message", not(emptyString()));
    }
    
    @Test
    public void testFinalizeEndpoint_BadApplication() throws IOException, AssinareException, AssinareError {
        mockSCMDService.addKnownApplicationId(UUID.fromString("00000000-0000-0000-0000-000000000000"));

        final String testProcessId = "11111111-1111-1111-1111-111111111111";
        final String docName = "testdoc.pdf";
        final List<String> docNames = Arrays.asList(docName);
        final ZonedDateTime signingDate = ZonedDateTime.of(2019, 12, 1, 0, 0, 0, 0, UTC);
        final List<X509Certificate> certChain = List.of(dummyCert);
        final PDFSignatureFields sigFields = pdfSignatureConfiguration.toPDFSignatureFields();

        try (InputStream testdocData = getClass().getClassLoader().getResourceAsStream("docs/testdoc.pdf")) {
            fileHelper.putOriginalFile("testdoc.pdf", testdocData);
            testdocData.reset();

            byte[] prefixedHash = calculatePrefixedHash(testdocData, certChain, sigFields, signingDate);
            mockSCMDService.putKnowHash(testProcessId, docName, prefixedHash);
        }

        mockSCMDService.addKnownOTP(testProcessId, "123456");
        cache.put(testProcessId, new SigningContext(signingDate, certChain, docNames, sigFields));

        Map<String, String> input = new HashMap<>();
        input.put("processId", testProcessId);
        input.put("userOtp", "123456");

        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(input)
        .when()
            .post("/cmd/pdf/finalize")
        .then()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .body("code", is(ErrorCode.GENERAL_CMD_ERROR.name()))
            .body("message", not(emptyString()));
    }
    
    @Test
    public void testFinalizeEndpoint_BadOTP() throws IOException, AssinareException, AssinareError {
        mockSCMDService.addKnownApplicationId(cmdConfiguration.applicationId());

        final String testProcessId = "11111111-1111-1111-1111-111111111111";
        final String docName = "testdoc.pdf";
        final List<String> docNames = Arrays.asList(docName);
        final ZonedDateTime signingDate = ZonedDateTime.of(2019, 12, 1, 0, 0, 0, 0, UTC);
        final List<X509Certificate> certChain = List.of(dummyCert);
        final PDFSignatureFields sigFields = pdfSignatureConfiguration.toPDFSignatureFields();

        try (InputStream testdocData = getClass().getClassLoader().getResourceAsStream("docs/testdoc.pdf")) {
            fileHelper.putOriginalFile("testdoc.pdf", testdocData);
            testdocData.reset();

            byte[] prefixedHash = calculatePrefixedHash(testdocData, certChain, sigFields, signingDate);
            mockSCMDService.putKnowHash(testProcessId, docName, prefixedHash);
        }

        mockSCMDService.addKnownOTP(testProcessId, "123456");
        cache.put(testProcessId, new SigningContext(signingDate, certChain, docNames, sigFields));

        Map<String, String> input = new HashMap<>();
        input.put("processId", testProcessId);
        input.put("userOtp", "000000");

        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(input)
        .when()
            .post("/cmd/pdf/finalize")
        .then()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .body("code", is(ErrorCode.BAD_OTP.name()))
            .body("message", not(emptyString()));
    }
    
    @Test
    public void testFinalizeEndpoint_FileNotFound() throws IOException, AssinareException, AssinareError {
        mockSCMDService.addKnownApplicationId(cmdConfiguration.applicationId());

        final String testProcessId = "11111111-1111-1111-1111-111111111111";
        final String docName = "testdoc.pdf";
        final List<String> docNames = Arrays.asList(docName);
        final ZonedDateTime signingDate = ZonedDateTime.of(2019, 12, 1, 0, 0, 0, 0, UTC);
        final List<X509Certificate> certChain = List.of(dummyCert);
        final PDFSignatureFields sigFields = pdfSignatureConfiguration.toPDFSignatureFields();

        try (InputStream testdocData = getClass().getClassLoader().getResourceAsStream("docs/testdoc.pdf")) {
//            fileHelper.putOriginalFile("testdoc.pdf", testdocData);
//            testdocData.reset();

            byte[] prefixedHash = calculatePrefixedHash(testdocData, certChain, sigFields, signingDate);
            mockSCMDService.putKnowHash(testProcessId, docName, prefixedHash);
        }

        mockSCMDService.addKnownOTP(testProcessId, "123456");
        cache.put(testProcessId, new SigningContext(signingDate, certChain, docNames, sigFields));

        Map<String, String> input = new HashMap<>();
        input.put("processId", testProcessId);
        input.put("userOtp", "123456");

        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(input)
        .when()
            .post("/cmd/pdf/finalize")
        .then()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .contentType(ContentType.JSON)
            .body("code", is(ErrorCode.FILE_NOT_FOUND.name()))
            .body("message", not(emptyString()));
    }
    
    // simulate a signing error by passing dummy data as the document
    @Test
    public void testFinalizeEndpoint_GeneralError() throws IOException, AssinareException, AssinareError {
        mockSCMDService.addKnownApplicationId(cmdConfiguration.applicationId());

        final String testProcessId = "11111111-1111-1111-1111-111111111111";
        final String docName = "testdoc.pdf";
        final List<String> docNames = Arrays.asList(docName);
        final ZonedDateTime signingDate = ZonedDateTime.of(2019, 12, 1, 0, 0, 0, 0, UTC);
        final List<X509Certificate> certChain = List.of(dummyCert);
        final PDFSignatureFields sigFields = pdfSignatureConfiguration.toPDFSignatureFields();

        fileHelper.putOriginalFile(docName, new ByteArrayInputStream(BAD_PDF_DATA));
        try (InputStream testdocData = getClass().getClassLoader().getResourceAsStream("docs/testdoc.pdf")) {
            // TODO: try passing garbage
            byte[] prefixedHash = calculatePrefixedHash(testdocData, certChain, sigFields, signingDate);
            mockSCMDService.putKnowHash(testProcessId, docName, prefixedHash);
        }

        mockSCMDService.addKnownOTP(testProcessId, "123456");
        cache.put(testProcessId, new SigningContext(signingDate, certChain, docNames, sigFields));

        Map<String, String> input = new HashMap<>();
        input.put("processId", testProcessId);
        input.put("userOtp", "123456");

        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(input)
        .when()
            .post("/cmd/pdf/finalize")
        .then()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .contentType(ContentType.JSON)
            .body("code", is(ErrorCode.GENERAL_ERROR.name()))
            .body("message", not(emptyString()));
    }

    private byte[] calculatePrefixedHash(InputStream docData, final SigningContext signingContext) throws AssinareException, AssinareError, IOException {
        return calculatePrefixedHash(docData, signingContext.getCertChain(), signingContext.getSigFields(), signingContext.getSigningDate());
    }

    private byte[] calculatePrefixedHash(InputStream docData, final List<X509Certificate> certChain, final PDFSignatureFields sigFields, final ZonedDateTime signingDate) throws AssinareException, AssinareError, IOException {
        byte[] hash = pdfCryptoUtils.calculateHash(docData, certChain, CMDPdfResource.CMD_SIG_ALGO_NAME, sigFields, signingDate);
        return ArrayUtils.addAll(CMDPdfResource.SHA_256_SIG_PREFIX, cmdMessageDigest.digest(hash));
    }

}
