package com.linkare.assinare.server;

import static com.linkare.assinare.server.ErrorCode.FILE_NOT_FOUND;
import static com.linkare.assinare.server.ErrorCode.GENERAL_ERROR;
import static java.nio.charset.StandardCharsets.US_ASCII;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.datacontract.schemas._2004._07.ama_structures.ArrayOfHashStructure;
import org.datacontract.schemas._2004._07.ama_structures.HashStructure;
import org.datacontract.schemas._2004._07.ama_structures.MultipleSignRequest;
import org.datacontract.schemas._2004._07.ama_structures.ObjectFactory;
import org.datacontract.schemas._2004._07.ama_structures.SignRequest;

import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.server.pojo.FinalizeRequest;
import com.linkare.assinare.server.pojo.InitializeRequest;
import com.linkare.assinare.server.pojo.InitializeResponse;
import com.linkare.assinare.server.pojo.PDFSignatureConfiguration;
import com.linkare.assinare.server.pojo.ProcessingError;
import com.linkare.assinare.server.scriptengine.ScriptFileNotFoundException;
import com.linkare.assinare.sign.fileprovider.FileService;
import com.linkare.assinare.sign.model.AssinareDocument;
import com.linkare.assinare.sign.pdf.PDFSignatureFields;
import com.linkare.assinare.sign.pdf.PdfSigner;

@Path("/cmd/pdf")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CMDPdfResource {

    private static final Logger LOG = Logger.getLogger(CMDPdfResource.class.getName());

    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    private static final MessageDigest CMD_MESSAGE_DIGEST;

    static final byte[] SHA_256_SIG_PREFIX = {0x30, 0x31, 0x30, 0x0d, 0x06, 0x09,
        0x60, (byte) 0x86, 0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x01,
        0x05, 0x00, 0x04, 0x20};

    static final String CMD_DIGEST_ALGORITHM = "SHA-256";
    static final String CMD_SIG_ALGO_NAME = "SHA256withRSA";

    static {
        try {
            CMD_MESSAGE_DIGEST = MessageDigest.getInstance(CMD_DIGEST_ALGORITHM);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Inject
    SigningContextCache cache;

    @Inject
    CMDConfiguration cmdConfiguration;

    @Inject
    PDFSignatureConfiguration pdfSignatureConfiguration;

    @Inject
    PdfSigner pdfSigner;

    @Inject
    FileService fileService;

    @Inject
    CMDService cmdService;

    @Inject
    CMDCryptoHelper cmdCryptoHelper;

    @Inject
    PDFSignatureFieldsHolder reqSignatureFieldsHolder;

    @Context
    HttpHeaders headers;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "hello";
    }

    @POST
    @Path("/initialize")
    public InitializeResponse initializeSignature(@Valid final InitializeRequest req) {
        final Locale requestLanguage = getRequestLanguage();
        final ZonedDateTime signingDate = ZonedDateTime.now();

        final List<String> docNames = req.getDocNames();
        final Map<String, String> docParams = req.getDocParams();
        final byte[] cipheredUserId = cmdCryptoHelper.encrypt(req.getUserId());
        final byte[] cipheredUserPin = cmdCryptoHelper.encrypt(req.getUserPin());
        final PDFSignatureFields sigFields;
        if (req.getSignatureOptions() != null) {
            sigFields = pdfSignatureConfiguration.toPDFSignatureFields().merge(req.getSignatureOptions().toPDFSignatureFields().sanitize());
        } else {
            sigFields = pdfSignatureConfiguration.toPDFSignatureFields();
        }

        reqSignatureFieldsHolder.setPdfSignatureFields(sigFields);

        try {
            final List<X509Certificate> certificates = getUserCertificates(cipheredUserId);

            final List<byte[]> hashes = new ArrayList<>(docNames.size());
            for (String docName : docNames) {
                hashes.add(calculateDocumentHash(docName, certificates, signingDate, sigFields, docParams));
            }

            final String processId = requestSignatures(docNames, hashes, cipheredUserPin, cipheredUserId);

            cache.put(processId, new SigningContext(signingDate, certificates, docNames, sigFields));

            return new InitializeResponse(processId);
        } catch (CMDException ex) {
            LOG.log(Level.SEVERE, null, ex);
            throw buildInternalServerError(ex.getErrorCode(), requestLanguage);
        } catch (AssinareException | AssinareError ex) {
            LOG.log(Level.SEVERE, null, ex);
            checkFileNotFound(ex, requestLanguage);
            throw buildInternalServerError(GENERAL_ERROR, requestLanguage);
        }
    }

    @POST
    @Path("/finalize")
    public void finalizeSignature(@Valid final FinalizeRequest req) {
        final Locale requestLanguage = getRequestLanguage();
        final byte[] cipheredOtp = cmdCryptoHelper.encrypt(req.getUserOtp());

        final SigningContext sigCtx = cache.getIfPresent(req.getProcessId());
        final List<String> docNames = sigCtx.getDocNames();
        reqSignatureFieldsHolder.setPdfSignatureFields(sigCtx.getSigFields());

        try {
            final Map<String, byte[]> signedHashes = getSignedHashes(req.getProcessId(), cipheredOtp);

            validateSignedHashes(signedHashes, docNames);

            finishSignatures(sigCtx, signedHashes);
        } catch (CMDException ex) {
            LOG.log(Level.SEVERE, null, ex);
            throw buildInternalServerError(ex.getErrorCode(), requestLanguage);
        } catch (AssinareException | AssinareError ex) {
            LOG.log(Level.SEVERE, null, ex);
            checkFileNotFound(ex, requestLanguage);
            throw buildInternalServerError(GENERAL_ERROR, requestLanguage);
        }
    }

    private void checkFileNotFound(final Exception ex, final Locale requestLanguage) throws InternalServerErrorException {
        // this is not a very elegant way of doing this, but the file is only accessed
        // very very lazilly
        if (ex.getCause() != null
                && ex.getCause().getCause() instanceof ScriptFileNotFoundException) {
            throw buildInternalServerError(FILE_NOT_FOUND, requestLanguage);
        }
    }

    private Locale getRequestLanguage() {
        // some versions of the API say this list should never be empty but in
        // practice it happens, so ...
        if (headers.getAcceptableLanguages().isEmpty()) {
            return null;
        } else {
            return headers.getAcceptableLanguages().get(0);
        }
    }

    private InternalServerErrorException buildInternalServerError(ErrorCode errorCode) {
        ProcessingError error = new ProcessingError(errorCode);
        return new InternalServerErrorException(Response.serverError().entity(error).build());
    }

    private InternalServerErrorException buildInternalServerError(ErrorCode errorCode, Locale locale) {
        if (locale != null) {
            ProcessingError error = new ProcessingError(errorCode, locale);
            return new InternalServerErrorException(Response.serverError().entity(error).build());
        } else {
            return buildInternalServerError(errorCode);
        }
    }

    private void validateSignedHashes(final Map<String, byte[]> signedHashes, final List<String> docNames) {
        if (docNames.size() == 1) {
            if (signedHashes.size() == 1 && signedHashes.containsKey(null)) {
                // move the hash from key "null" to key "doc name"
                byte[] hash = signedHashes.remove(null);
                signedHashes.put(docNames.get(0), hash);
                return;
            }
        } else {
            if (CollectionUtils.isEqualCollection(docNames, signedHashes.keySet())) {
                return;
            }
        }

        throw new IllegalStateException("Resulting hash struct does not match expected docs");
    }

    private List<X509Certificate> getUserCertificates(final byte[] cipheredUserId) throws CMDException {
        final String certificatesPEM = cmdService.getCertificate(cmdConfiguration.applicationId(), cipheredUserId);

        return getCertsFromPEM(certificatesPEM);
    }

    private byte[] calculateDocumentHash(final String docName, final List<X509Certificate> certChain, final ZonedDateTime signingDate, final PDFSignatureFields sigFields, final Map<String, String> docParams) throws AssinareException, AssinareError {
        final AssinareDocument document = fileService.getFile(docName, docParams);
        final X509Certificate[] certChainArray = certChain.toArray(new X509Certificate[certChain.size()]);

        return pdfSigner.hashPdf(document, certChainArray, CMD_SIG_ALGO_NAME, sigFields, signingDate);
    }

    private String requestSignatures(final List<String> docNames, final List<byte[]> hashes, final byte[] cipheredUserPin, final byte[] cipheredUserId) throws CMDException {
        if (docNames.size() == 1) {
            final String docName = docNames.get(0);
            final byte[] hash = hashes.get(0);
            final byte[] prefixedHash = prefixHash(hash);

            SignRequest signRequest = OBJECT_FACTORY.createSignRequest();
            signRequest.setApplicationId(cmdConfiguration.applicationId());
            signRequest.setDocName(OBJECT_FACTORY.createSignRequestDocName(docName));
            signRequest.setHash(prefixedHash);
            signRequest.setPin(cipheredUserPin);
            signRequest.setUserId(cipheredUserId);

            return cmdService.scmdSign(signRequest);
        } else {
            MultipleSignRequest multipleSignRequest = OBJECT_FACTORY.createMultipleSignRequest();
            multipleSignRequest.setApplicationId(cmdConfiguration.applicationId());
            multipleSignRequest.setPin(cipheredUserPin);
            multipleSignRequest.setUserId(cipheredUserId);

            ArrayOfHashStructure arrayOfHash = OBJECT_FACTORY.createArrayOfHashStructure();
            List<HashStructure> hashList = arrayOfHash.getHashStructure();
            for (int i = 0; i < docNames.size(); i++) {
                String docName = docNames.get(i);
                byte[] prefixedHash = prefixHash(hashes.get(i));

                HashStructure hashStructure = OBJECT_FACTORY.createHashStructure();
                hashStructure.setName(docName);
                hashStructure.setHash(prefixedHash);
                hashList.add(hashStructure);
            }

            return cmdService.scmdMultipleSign(multipleSignRequest, arrayOfHash);
        }
    }

    private byte[] prefixHash(final byte[] hash) {
        return ArrayUtils.addAll(SHA_256_SIG_PREFIX, CMD_MESSAGE_DIGEST.digest(hash));
    }

    private Map<String, byte[]> getSignedHashes(final String processId, final byte[] cipheredOtp) throws CMDException {
        return cmdService.validateOtp(cipheredOtp, processId, cmdConfiguration.applicationId());
    }

    private void finishSignatures(final SigningContext signingContext, final Map<String, byte[]> signedHashes) throws AssinareException, AssinareError {
        final ZonedDateTime signingDate = signingContext.getSigningDate();
        final List<X509Certificate> certChain = signingContext.getCertChain();
        final X509Certificate[] certChainArray = certChain.toArray(new X509Certificate[certChain.size()]);
        final PDFSignatureFields sigFields = signingContext.getSigFields();

        for (Map.Entry<String, byte[]> signedHashEntry : signedHashes.entrySet()) {
            final String docName = signedHashEntry.getKey();
            final AssinareDocument originDoc = fileService.getFile(docName);
            final byte[] signedHash = signedHashEntry.getValue();

            AssinareDocument signedDoc = pdfSigner.signPdf(originDoc, certChainArray, signedHash, CMD_SIG_ALGO_NAME, sigFields, signingDate);

            fileService.putFile(docName, signedDoc);
        }
    }

    private List<X509Certificate> getCertsFromPEM(final String certificatesPEM) {
        try {
            final CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            final InputStream certStream = new ByteArrayInputStream(certificatesPEM.getBytes(US_ASCII));
            Collection<X509Certificate> certs = (Collection<X509Certificate>) certFactory.generateCertificates(certStream);
            List<X509Certificate> certArray = new ArrayList<>(certs);

            for (X509Certificate cert : certArray) {
                LOG.info(() -> cert.getSubjectX500Principal().toString());
            }

            return certArray;
        } catch (CertificateException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
