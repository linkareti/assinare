package com.linkare.assinare.server.test.resources;

import static com.linkare.assinare.server.CMDStatusCode.GENERAL_ERROR;
import static com.linkare.assinare.server.CMDStatusCode.INVALID_OTP;
import static com.linkare.assinare.server.CMDStatusCode.PIN_MISMATCH;
import static com.linkare.assinare.server.CMDStatusCode.SUCCESS;
import static java.nio.ByteBuffer.wrap;
import static java.util.Base64.getDecoder;
import static java.util.Base64.getEncoder;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.jws.WebService;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.datacontract.schemas._2004._07.ama_structures.ArrayOfHashStructure;
import org.datacontract.schemas._2004._07.ama_structures.HashStructure;
import org.datacontract.schemas._2004._07.ama_structures.MultipleSignRequest;
import org.datacontract.schemas._2004._07.ama_structures.SignRequest;
import org.datacontract.schemas._2004._07.ama_structures.SignResponse;
import org.datacontract.schemas._2004._07.ama_structures.SignStatus;

import com.linkare.assinare.server.CMDStatusCode;
import com.linkare.assinare.server.test.mocks.MockCMDCryptoHelper;
import com.linkare.assinare.test.DummyCrypto;

import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.DSSUtils;
import service.authentication.ama.SCMDService;

/**
 *
 * @author bnazare
 */
@WebService(
        serviceName = "SCMDService",
        portName = "BasicHttpBinding_SCMDService",
        targetNamespace = "http://Ama.Authentication.Service/",
        wsdlLocation = "classpath:wsdl/SCMDService.wsdl",
        endpointInterface = "service.authentication.ama.SCMDService")
public class FakeSCMDService implements SCMDService {

    private static final Map<String, String> KNOWN_SIGNATURES = Map.of(
            "+351 931234567", "MUHbH6YPTP4+YBV9R4Y0FjPuTtfXFwuwDlYm8NCUkC6tjnh0MNelMcaVUUKP4pgnjpQKCdQhXwIqhHB/TgBhh6wS8IM3tZPI2+g+5u3Fu1QhwAd/hChZI5oGoizlj6955DzZF6JPWg91EaoWPlwAiLYn2Th2SLGLoz5wPBW9ijsZZTlGRm8ZTUsmC9Lk4jhgiT98+KPAwH3ApyC/BD/oy/X86YBmvxqRjoYMbatF9SsvPzW6q0aS1xt4z4EXRKaRJPRDJG4nTLSxm6BBYs90Lzl7tJECRfVmTLDMsftiXgPgYXp2bfjZBAQnoQItjhp+0fWm/tCucWtAqDsz8RLljA==",
            "1234", "Z7E2NU468/0d2nlY4mOn0e4voji7nZubn4bjA53UscvHFStdP24wRf0VHE1lU3RpiSZJdVhC4fxpcIyPELk3eldpwQHuigeWaW+/wlEZuQ0neCA/zYecmqwnPP/j9KHK2PWNWxUFRX92hT1xLXwa2Ty72GKrsYiSi8ez+j0j4dwqfewjEO9aaeVtdzNjqT/LhzMYVnoer1f+qQ9+2izAwszqnlEM0OGy9CvL6QYSLDW7GltiY+tcF6kBZ4E9zhKdXTnCxN4fhuuTOtUfnD/FaSHWj+coS2MyYO1moL9Ybhny7fKh8Aom+wBl4M/ehm5kyXr/inc4cNoikw/bdeGvhA==",
            "123456", "Xw57V71fBU0zpMmVNkvScIAipDmRj/0Ur+DPJ0r5WcceXkXfdU7m1NBchiOlZ2k3I0jgxi/vcKfkq/al33QE/HgfdPrjalhDu/tCN9GWs/+M22vhxNx2rOANrU7MPxRYkebEa03jF80XZQHWTeqcIH8TaqlWuSijcw7VaGzT1nFHEffn/GoyEgpz7enOoSL+SCC9Io0yCqmwaZPyUNOedYhY0IBAnOnmyC1LQt4cL4wE1Y5Ggznf/P9iIZUDQTw+J/V+dVoAVwHJZHtWTEAth4AZs3FpRn9fOCm0SszLvR4+jvV0yhOgJSDyjazr/CMcQU9PM0cdgQF33u53XT/hEQ==",
            "567890", "uHoFV1rN17M8Y9R/HtB6h+9la8rHebWVxAe0c3wR8xHsZkdP75zwiEKq/G3bfk327sofuhQ9qNod3SfW959oiujqOkxRWhgicOjlvCSqSR/lAZlg4HJtddyUHsK42BnrJhEhVPTOUtP0xFz6gNeq8HiT9ZUW2fzIpAGrSqk080+rEDGUAd7tmmQYKmyzpn6VpfXkYcFCpRzloLcnryuYCnJnBrO3e/csTulT/sNRAFa288mzUBeMdsmwSifmKalfaAV/OAPfT+pCW9+HqrlPjs3JDDrX+kQcfAjwQFFZpFjUJGVK7p3b6H+h//2UhnejAxECgq9Zf2ZAR4xDbbpRHg=="
    );
    // we use ByteBuffer because byte[] doesn't implement hashCode() properly
    private static final Map<ByteBuffer, String> RAINBOW_TABLE = new HashMap();

    static {
        for (Map.Entry<String, String> entry : KNOWN_SIGNATURES.entrySet()) {
            // takes the Base64 string, decodes it and wraps the result into a ByteBuffer
            ByteBuffer bb = wrap(getDecoder().decode(entry.getValue()));
            RAINBOW_TABLE.put(bb, entry.getKey());
        }
    }

    public static final FakeSCMDService INSTANCE = new FakeSCMDService();

    private final Map<String, Map<String, byte[]>> KNOWN_HASHES = new HashMap<>();
    private final Set<UUID> KNOWN_APPLICATION_IDS = new HashSet<>();
    private final Set<Pair<String, String>> KNOWN_USERS = new HashSet<>();
    private final Set<Pair<String, String>> KNOWN_OTPS = new HashSet<>();

    private final String certPem;
    private final Signature sig;

    public FakeSCMDService() {
        try {
            certPem = DSSUtils.convertToPEM(new CertificateToken(DummyCrypto.getUserACert()));

            sig = Signature.getInstance("NONEwithRSA");
            sig.initSign(DummyCrypto.getUserAPrivKey());
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public String getCertificate(UUID applicationId, byte[] userId) {
        if (!checkApplicationId(applicationId)) {
            return null;
        } else if (!checkUser(userId)) {
            return null;
        } else {
            return certPem;
        }
    }

    @Override
    public SignStatus scmdSign(SignRequest request) {
        if (!checkApplicationId(request.getApplicationId())) {
            return buildErrorSignStatus(GENERAL_ERROR);
        } else if (!checkUser(request.getUserId(), request.getPin())) {
            return buildErrorSignStatus(PIN_MISMATCH);
        } else {
            final String processId = UUID.randomUUID().toString();
            final String docName = request.getDocName().getValue();
            putKnowHash(processId, docName, request.getHash());

            SignStatus signStatus = buildSuccessSignStatus(processId);

            return signStatus;
        }
    }

    @Override
    public SignResponse validateOtp(byte[] code, String processId, UUID applicationId) {
        if (!checkApplicationId(applicationId)) {
            return buildErrorSignResponse(GENERAL_ERROR);
        } else if (!checkOTP(processId, code)) {
            return buildErrorSignResponse(INVALID_OTP);
        } else {
            List<HashStructure> hashStructures = new ArrayList<>();
            for (Map.Entry<String, byte[]> knowHash : getKnowHashes(processId).entrySet()) {
                byte[] signedHash;
                try {
                    sig.update(knowHash.getValue());
                    signedHash = sig.sign();
                } catch (SignatureException ex) {
                    return buildErrorSignResponse(GENERAL_ERROR);
                }

                final HashStructure hashStructure = new HashStructure();
                hashStructure.setName(knowHash.getKey());
                hashStructure.setHash(signedHash);

                hashStructures.add(hashStructure);
            }

            SignStatus signStatus = buildSuccessSignStatus(processId);

            final SignResponse signResponse = new SignResponse();
            signResponse.setStatus(signStatus);

            if (hashStructures.size() == 1) {
                signResponse.setSignature(hashStructures.get(0).getHash());
            } else {
                ArrayOfHashStructure arrayOfHashStructure = new ArrayOfHashStructure();
                arrayOfHashStructure.getHashStructure().addAll(hashStructures);

                signResponse.setArrayOfHashStructure(arrayOfHashStructure);
            }

            return signResponse;
        }
    }

    @Override
    public SignStatus scmdMultipleSign(MultipleSignRequest request, ArrayOfHashStructure documents) {
        if (!checkApplicationId(request.getApplicationId())) {
            return buildErrorSignStatus(GENERAL_ERROR);
        } else if (!checkUser(request.getUserId(), request.getPin())) {
            return buildErrorSignStatus(PIN_MISMATCH);
        } else {
            final String processId = UUID.randomUUID().toString();
            documents.getHashStructure().forEach((hashStructure) -> {
                putKnowHash(processId, hashStructure.getName(), hashStructure.getHash());
            });

            SignStatus signStatus = buildSuccessSignStatus(processId);

            return signStatus;
        }
    }

    @Override
    public SignStatus getCertificateWithPin(byte[] applicationId, String userId, String pin) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SignStatus forceSMS(String processId, String citizenId, byte[] applicationId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private SignResponse buildErrorSignResponse(CMDStatusCode statusCode) {
        final SignStatus signStatus = buildErrorSignStatus(statusCode);

        final SignResponse signResponse = new SignResponse();
        signResponse.setStatus(signStatus);

        return signResponse;
    }

    private SignStatus buildErrorSignStatus(CMDStatusCode statusCode) {
        final SignStatus signStatus = new SignStatus();
        signStatus.setCode(statusCode.getCode());

        return signStatus;
    }

    private SignStatus buildSuccessSignStatus(String processId) {
        final SignStatus signStatus = new SignStatus();
        signStatus.setCode(SUCCESS.getCode());
        signStatus.setProcessId(processId);

        return signStatus;
    }

    public void putKnowHash(String processId, String docName, byte[] value) {
        Map<String, byte[]> processHashes = KNOWN_HASHES.computeIfAbsent(processId, k -> new HashMap<>());
        processHashes.put(docName, value);
    }

    public byte[] getKnowHash(String processId, String docName) {
        Map<String, byte[]> processHashes = KNOWN_HASHES.get(processId);
        if (processHashes != null) {
            return processHashes.get(docName);
        } else {
            return null;
        }
    }

    public Map<String, byte[]> getKnowHashes(String processId) {
        return KNOWN_HASHES.get(processId);
    }

    public void removeKnowHash(String processId, String docName) {
        Map<String, byte[]> processHashes = KNOWN_HASHES.get(processId);
        if (processHashes != null) {
            processHashes.remove(docName);
            if (processHashes.isEmpty()) {
                KNOWN_HASHES.remove(processId);
            }
        }
    }

    public void clearKnowHashes() {
        KNOWN_HASHES.clear();
    }

    public void addKnownApplicationId(String applicationId) {
        addKnownApplicationId(UUID.fromString(applicationId));
    }

    public void addKnownApplicationId(UUID applicationId) {
        KNOWN_APPLICATION_IDS.add(applicationId);
    }

    public void clearKnownApplicationIds() {
        KNOWN_APPLICATION_IDS.clear();
    }

    private boolean checkApplicationId(UUID applicationId) {
        return KNOWN_APPLICATION_IDS.contains(applicationId);
    }

    public void addKnownUser(String userId, String userPin) {
        KNOWN_USERS.add(new ImmutablePair<>(userId, userPin));
    }

    public void clearKnownUsers() {
        KNOWN_USERS.clear();
    }

    private boolean checkUser(byte[] userId) {
        String userIdString = RAINBOW_TABLE.get(wrap(userId));
        return checkUser(userIdString);
    }

    private boolean checkUser(String userId) {
        return KNOWN_USERS.stream().anyMatch(
                pair -> pair.getLeft().equals(userId)
        );
    }

    private boolean checkUser(byte[] userId, byte[] userPin) {
        String userIdString = RAINBOW_TABLE.get(wrap(userId));
        String userPinString = RAINBOW_TABLE.get(wrap(userPin));
        return checkUser(userIdString, userPinString);
    }

    private boolean checkUser(String userId, String userPin) {
        return KNOWN_USERS.contains(new ImmutablePair<>(userId, userPin));
    }

    public void addKnownOTP(String processId, String otp) {
        KNOWN_OTPS.add(new ImmutablePair<>(processId, otp));
    }

    public void clearKnownOTPs() {
        KNOWN_OTPS.clear();
    }

    private boolean checkOTP(String processId, byte[] otp) {
        String otpString = RAINBOW_TABLE.get(wrap(otp));
        return checkOTP(processId, otpString);
    }

    private boolean checkOTP(String processId, String otp) {
        return KNOWN_OTPS.contains(new ImmutablePair<>(processId, otp));
    }

    public void clearAll() {
        clearKnowHashes();
        clearKnownApplicationIds();
        clearKnownUsers();
        clearKnownOTPs();
    }

    /**
     * Utility method to re-generate the expected hashes.
     *
     * @param args
     */
    public static void main(String[] args) {
        MockCMDCryptoHelper cmdCryptoHelper = new MockCMDCryptoHelper();
        for (String s : new TreeSet<>(KNOWN_SIGNATURES.keySet())) {
            System.out.format("\"%s\", \"%s\",\n", s, getEncoder().encodeToString(cmdCryptoHelper.encrypt(s)));
        }
    }
}
