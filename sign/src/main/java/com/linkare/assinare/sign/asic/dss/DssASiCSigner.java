package com.linkare.assinare.sign.asic.dss;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.sign.SigningKey;
import com.linkare.assinare.sign.asic.ASiCSignatureFields;
import com.linkare.assinare.sign.asic.AbstractASiCSigner;
import com.linkare.assinare.sign.asic.CommitmentType;
import com.linkare.assinare.sign.dss.AssinareTSPSource;
import com.linkare.assinare.sign.dss.CertificateUtils;
import com.linkare.assinare.sign.dss.DssTSLUtils;
import com.linkare.assinare.sign.dss.SigningUtils;
import com.linkare.assinare.sign.dss.TSPDSSException;
import com.linkare.assinare.sign.model.AssinareDocument;
import com.linkare.assinare.sign.pdf.itext.TSAAssinareException;

import eu.europa.esig.dss.asic.xades.ASiCWithXAdESSignatureParameters;
import eu.europa.esig.dss.asic.xades.signature.ASiCWithXAdESService;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.CommitmentTypeEnum;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.model.BLevelParameters;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.MimeType;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.SignerLocation;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.http.commons.TimestampDataLoader;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.service.tsp.OnlineTSPSource;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;

/**
 *
 * @author bnazare
 */
public class DssASiCSigner extends AbstractASiCSigner {

    private static final DigestAlgorithm DEFAULT_DIGEST_ALGORITHM = DigestAlgorithm.SHA1;
    private static final ASiCContainerType DEFAULT_CONTAINER_TYPE = ASiCContainerType.ASiC_E;
    private static final MimeType DEFAULT_MIMETYPE = MimeType.ASICE;
    private static final SignatureLevel NO_TIMESTAMP_SIGNATURE_LEVEL = SignatureLevel.XAdES_BASELINE_B;
    private static final SignatureLevel DEFAULT_SIGNATURE_LEVEL = SignatureLevel.XAdES_BASELINE_T;
    private static final SignatureLevel ARCHIVING_SIGNATURE_LEVEL = SignatureLevel.XAdES_BASELINE_LTA;

    public DssASiCSigner() {
        this(false);
    }

    public DssASiCSigner(boolean reSign) {
        super(reSign);
    }

    @Override
    public byte[] signDocuments(SigningKey signingKey, List<AssinareDocument> docs, ASiCSignatureFields asicSigFields) throws IOException, AssinareException, AssinareError {
        return DSSUtils.toByteArray(signDocumentsInternal(signingKey, docs, asicSigFields));
    }

    @Override
    public byte[] reSignContainer(SigningKey signingKey, AssinareDocument container, ASiCSignatureFields asicSigFields) throws IOException, AssinareException, AssinareError {
        InMemoryDocument dssDocument;
        try (InputStream containerStream = container.openInputStream()) {
            // we assume this was tested with isReSignable() which asserts the MimeType
            dssDocument = new InMemoryDocument(containerStream, null, DEFAULT_MIMETYPE);
        }

        return DSSUtils.toByteArray(reSignContainer(signingKey, dssDocument, asicSigFields));
    }

    private DSSDocument signDocumentsInternal(SigningKey signingKey, List<AssinareDocument> files, ASiCSignatureFields asicSigFields) throws IOException, AssinareException, AssinareError {
        List<DSSDocument> toSignDocument = buildDocumentList(files);

        return signDocumentChain(signingKey, toSignDocument, asicSigFields);
    }

    private DSSDocument reSignContainer(SigningKey signingKey, DSSDocument signedDocument, ASiCSignatureFields asicSigFields) throws AssinareException, AssinareError {
        return signDocumentChain(signingKey, Arrays.asList(signedDocument), asicSigFields);
    }

    private DSSDocument signDocumentChain(SigningKey signingKey, List<DSSDocument> documents, ASiCSignatureFields asicSigFields) throws AssinareException, AssinareError {
        ASiCWithXAdESSignatureParameters parameters = buildSignatureParameters(signingKey, asicSigFields);

        ASiCWithXAdESService service = buildSignatureService(parameters.getSignatureLevel(), asicSigFields);

        ToBeSigned dataToSign = service.getDataToSign(documents, parameters);

        try {
            SignatureValue signatureValue = SigningUtils.sign(signingKey, dataToSign, parameters.getDigestAlgorithm());

            return service.signDocument(documents, parameters, signatureValue);
        } catch (TSPDSSException ex) {
            throw new TSAAssinareException(ex.getMessage(), ex);
        } catch (DSSException ex) {
            throw new AssinareError(ex);
        }
    }

    private ASiCWithXAdESSignatureParameters buildSignatureParameters(SigningKey signingKey, ASiCSignatureFields asicSigFields) {
        ASiCWithXAdESSignatureParameters parameters = new ASiCWithXAdESSignatureParameters();
        if (!asicSigFields.isUseTsa()) {
            parameters.setSignatureLevel(NO_TIMESTAMP_SIGNATURE_LEVEL);
        } else if (asicSigFields.isArchiving()) {
            parameters.setSignatureLevel(ARCHIVING_SIGNATURE_LEVEL);
        } else {
            parameters.setSignatureLevel(DEFAULT_SIGNATURE_LEVEL);
        }
        parameters.aSiC().setContainerType(DEFAULT_CONTAINER_TYPE);
        parameters.setDigestAlgorithm(DEFAULT_DIGEST_ALGORITHM);
        parameters.setSigningCertificate(new CertificateToken(signingKey.getCertificate()));
        parameters.setCertificateChain(CertificateUtils.convertCertificates(signingKey.getCertificateChain()));

        BLevelParameters bLevelParameters = buildBLevelParams(asicSigFields);
        parameters.setBLevelParams(bLevelParameters);

        return parameters;
    }

    private BLevelParameters buildBLevelParams(ASiCSignatureFields asicSigFields) {
        BLevelParameters bLevelParameters = new BLevelParameters();

        if (asicSigFields.getLocation() != null) {
            SignerLocation signerLocation = new SignerLocation();
            signerLocation.setLocality(asicSigFields.getLocation());
            bLevelParameters.setSignerLocation(signerLocation);
        }

        if (asicSigFields.getClaimedRoles() != null) {
            for (String claimedRole : asicSigFields.getClaimedRoles()) {
                bLevelParameters.setClaimedSignerRoles(Collections.singletonList(claimedRole));
            }
        }

        if (asicSigFields.getCommitmentTypes() != null
                && asicSigFields.getCommitmentTypes().length > 0) {
            List<eu.europa.esig.dss.enumerations.CommitmentType> commitmentTypeIndications = new ArrayList<>();
            for (CommitmentType commitmentTypeIndication : asicSigFields.getCommitmentTypes()) {
                for (CommitmentTypeEnum commType : CommitmentTypeEnum.values()) {
                    if (commType.getUri().equals(commitmentTypeIndication.getUriId())) {
                        commitmentTypeIndications.add(commType);
                        break;
                    }
                }
            }
            bLevelParameters.setCommitmentTypeIndications(commitmentTypeIndications);
        }

        return bLevelParameters;
    }

    private static TSPSource buildTSPSource(String tsaUrl) {
        OnlineTSPSource tspSource = new AssinareTSPSource(tsaUrl);
        tspSource.setDataLoader(new TimestampDataLoader());
        return tspSource;
    }

    // this method is unused but important for future reference
    // adding an annotation makes Sonar silent
    @SuppressWarnings("unused")
    private static DSSDocument extendSignature(DSSDocument signedDocument, ASiCContainerType containerType, SignatureLevel signatureLevel, ASiCSignatureFields asicSigFields) {
        ASiCWithXAdESSignatureParameters extensionParameters = buildExtensionParameter(signatureLevel, containerType);

        ASiCWithXAdESService service = buildSignatureService(signatureLevel, asicSigFields);

        return service.extendDocument(signedDocument, extensionParameters);
    }

    private static ASiCWithXAdESService buildSignatureService(SignatureLevel signatureLevel, ASiCSignatureFields asicSigFields) {
        CertificateVerifier certificateVerifier = buildCertificateVerifier(signatureLevel);

        ASiCWithXAdESService service = new ASiCWithXAdESService(certificateVerifier);

        if (isTimestamped(signatureLevel)) {
            TSPSource tspSource = buildTSPSource(asicSigFields.getTsaUrl());
            service.setTspSource(tspSource);
        }

        return service;
    }

    private static ASiCWithXAdESSignatureParameters buildExtensionParameter(SignatureLevel signatureLevel, ASiCContainerType containerType) {
        ASiCWithXAdESSignatureParameters extensionParameters = new ASiCWithXAdESSignatureParameters();
        extensionParameters.setSignatureLevel(signatureLevel);
        extensionParameters.aSiC().setContainerType(containerType);
        return extensionParameters;
    }

    private static CertificateVerifier buildCertificateVerifier(SignatureLevel signatureLevel) {
        CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();

        if (isLongTerm(signatureLevel)) {
            TrustedListsCertificateSource tslCertificateSource = DssTSLUtils.buildTSLCertificateSource();
            commonCertificateVerifier.setTrustedCertSources(tslCertificateSource);

            OnlineCRLSource onlineCRLSource = new OnlineCRLSource();
            commonCertificateVerifier.setCrlSource(onlineCRLSource);

            OnlineOCSPSource onlineOCSPSource = new OnlineOCSPSource();
            commonCertificateVerifier.setOcspSource(onlineOCSPSource);
        }

        return commonCertificateVerifier;
    }

    private static boolean isTimestamped(SignatureLevel signatureLevel) {
        return signatureLevel == SignatureLevel.XAdES_BASELINE_T
                || signatureLevel == SignatureLevel.XAdES_BASELINE_LT
                || signatureLevel == SignatureLevel.XAdES_BASELINE_LTA;
    }

    private static boolean isLongTerm(SignatureLevel signatureLevel) {
        return signatureLevel == SignatureLevel.XAdES_BASELINE_LT
                || signatureLevel == SignatureLevel.XAdES_BASELINE_LTA;
    }

    private List<DSSDocument> buildDocumentList(List<AssinareDocument> docs) throws IOException {
        List<DSSDocument> documentsList = new ArrayList<>(docs.size());

        for (AssinareDocument doc : docs) {
            try (InputStream docStream = doc.openInputStream()) {
                documentsList.add(new InMemoryDocument(docStream, doc.getName()));
            }
        }

        return documentsList;
    }

}
