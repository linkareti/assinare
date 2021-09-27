package com.linkare.assinare.sign.asic.dss;

import java.io.IOException;
import java.io.InputStream;

import com.linkare.assinare.sign.asic.ASiCParser;
import com.linkare.assinare.sign.model.AssinareDocument;

import eu.europa.esig.dss.asic.common.ASiCExtractResult;
import eu.europa.esig.dss.asic.common.ASiCUtils;
import eu.europa.esig.dss.asic.xades.ASiCWithXAdESContainerExtractor;
import eu.europa.esig.dss.asic.xades.validation.ASiCContainerWithXAdESValidator;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.validation.AdvancedSignature;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;

/**
 *
 * @author bnazare
 */
public class DssASiCParser implements ASiCParser {

    @Override
    public boolean isReSignable(AssinareDocument doc) {
        try (final InputStream docStream = doc.openInputStream()) {
            InMemoryDocument dssDoc = new InMemoryDocument(docStream);
            if (!ASiCUtils.isZip(dssDoc)) {
                return false;
            } else {
                ASiCExtractResult extract = new ASiCWithXAdESContainerExtractor(dssDoc).extract();
                ASiCContainerType containerType = ASiCUtils.getContainerType(dssDoc, extract.getMimeTypeDocument(), extract.getZipComment(), extract.getSignedDocuments());
                if (containerType != ASiCContainerType.ASiC_S) {
                    return false;
                }
            }
            SignedDocumentValidator validator = new ASiCContainerWithXAdESValidator(dssDoc);
            validator.setCertificateVerifier(new CommonCertificateVerifier());
            if (validator.getSignatures().isEmpty()) {
                return false;
            } else {
                for (AdvancedSignature signature : validator.getSignatures()) {
                    if (signature.getSignatureForm() != SignatureForm.XAdES) {
                        return false;
                    }
                }
            }
            return true;
        } catch (DSSException | IOException ex) {
            return false;
        }
    }

}
