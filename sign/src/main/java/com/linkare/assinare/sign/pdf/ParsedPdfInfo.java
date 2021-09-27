package com.linkare.assinare.sign.pdf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author bnazare
 */
public class ParsedPdfInfo {

    private final List<SignatureInfo> existingSignatures;
    private final List<SignatureFieldInfo> signatureFields;

    public ParsedPdfInfo(List<SignatureInfo> signatures, List<SignatureFieldInfo> signatureFields) {
        this.existingSignatures = Collections.unmodifiableList(new ArrayList(signatures));
        this.signatureFields = Collections.unmodifiableList(new ArrayList(signatureFields));
    }

    public List<SignatureInfo> getExistingSignatures() {
        return existingSignatures;
    }

    public List<SignatureFieldInfo> getSignatureFields() {
        return signatureFields;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(this.existingSignatures);
        hash = 23 * hash + Objects.hashCode(this.signatureFields);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ParsedPdfInfo other = (ParsedPdfInfo) obj;
        if (!Objects.equals(this.existingSignatures, other.existingSignatures)) {
            return false;
        }
        if (!Objects.equals(this.signatureFields, other.signatureFields)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ParsedPdfInfo{" + "existingSignatures=" + existingSignatures + ", signatureFields=" + signatureFields + '}';
    }

}
