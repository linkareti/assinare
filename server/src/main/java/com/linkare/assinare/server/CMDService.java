package com.linkare.assinare.server;

import static com.linkare.assinare.server.ErrorCode.BAD_OTP;
import static com.linkare.assinare.server.ErrorCode.BAD_USER_PIN;
import static com.linkare.assinare.server.ErrorCode.GENERAL_CMD_ERROR;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.ws.WebServiceException;

import org.apache.commons.lang3.StringUtils;
import org.datacontract.schemas._2004._07.ama_structures.ArrayOfHashStructure;
import org.datacontract.schemas._2004._07.ama_structures.HashStructure;
import org.datacontract.schemas._2004._07.ama_structures.MultipleSignRequest;
import org.datacontract.schemas._2004._07.ama_structures.SignRequest;
import org.datacontract.schemas._2004._07.ama_structures.SignResponse;
import org.datacontract.schemas._2004._07.ama_structures.SignStatus;

import service.authentication.ama.SCMDService;

/**
 *
 * @author bnazare
 */
@Dependent
public class CMDService {

    @Inject
    SCMDService wsDelegate;

    public String getCertificate(UUID applicationId, byte[] userId) throws CMDException {
        try {
            String certificateData = wsDelegate.getCertificate(applicationId, userId);
            if (StringUtils.isNotBlank(certificateData)) {
                return certificateData;
            } else {
                throw new CMDException(BAD_USER_PIN, "CMD returned no certificates");
            }
        } catch (WebServiceException ex) {
            throw new CMDException(GENERAL_CMD_ERROR, ex);
        }
    }

    public String scmdSign(SignRequest request) throws CMDException {
        try {
            SignStatus signStatus = wsDelegate.scmdSign(request);
            validateSignStatus(signStatus);

            return signStatus.getProcessId();
        } catch (WebServiceException ex) {
            throw new CMDException(GENERAL_CMD_ERROR, ex);
        }
    }

    public String scmdMultipleSign(MultipleSignRequest request, ArrayOfHashStructure documents) throws CMDException {
        try {
            SignStatus signStatus = wsDelegate.scmdMultipleSign(request, documents);
            validateSignStatus(signStatus);

            return signStatus.getProcessId();
        } catch (WebServiceException ex) {
            throw new CMDException(GENERAL_CMD_ERROR, ex);
        }
    }

    public Map<String, byte[]> validateOtp(byte[] code, String processId, UUID applicationId) throws CMDException {
        try {
            SignResponse signResponse = wsDelegate.validateOtp(code, processId, applicationId);
            validateSignStatus(signResponse.getStatus());

            return extractSignedHashes(signResponse);
        } catch (WebServiceException ex) {
            throw new CMDException(GENERAL_CMD_ERROR, ex);
        }
    }

    public String forceSMS(String processId, String citizenId, byte[] applicationId) throws CMDException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getCertificateWithPin(byte[] applicationId, String userId, String pin) throws CMDException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void validateSignStatus(SignStatus signStatus) throws CMDException {
        String code = signStatus.getCode();
        String message = signStatus.getMessage();
        CMDStatusCode statusCode = CMDStatusCode.forCode(code);
        
        String errorMessage = "CMD error: " + code + " - " + message;

        switch (statusCode) {
            case SUCCESS:
                // yey!
                break;
            case PIN_MISMATCH:
                throw new CMDException(BAD_USER_PIN, errorMessage);
            case INVALID_OTP:
                throw new CMDException(BAD_OTP, errorMessage);
            case OTP_TIMEOUT:
            case INACTIVE_SIGNATURE:
            case GENERAL_ERROR:
            default:
                throw new CMDException(GENERAL_CMD_ERROR, errorMessage);
        }
    }

    private Map<String, byte[]> extractSignedHashes(final SignResponse signResponse) throws CMDException {
        if (signResponse.getSignature() != null) {
            Map<String, byte[]> signedHash = new HashMap<>();
            signedHash.put(null, signResponse.getSignature());
            return signedHash;
        } else if (signResponse.getArrayOfHashStructure() != null) {
            final List<HashStructure> hashStructList = signResponse.getArrayOfHashStructure().getHashStructure();

            return hashStructList.stream().collect(
                    Collectors.toMap(HashStructure::getName, HashStructure::getHash)
            );
        } else {
            throw new CMDException(GENERAL_CMD_ERROR, "CMD returned no signatures");
        }
    }

}
