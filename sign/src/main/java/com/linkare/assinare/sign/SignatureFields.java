package com.linkare.assinare.sign;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 *
 * @author bnazare
 */
public abstract class SignatureFields {

    protected static final String NO_TSA_OPTION = "none";

    private final String tsaUrl;
    private final Boolean archiving;

    public SignatureFields(String tsaUrl, Boolean doLTV) {
        this.tsaUrl = tsaUrl;
        this.archiving = doLTV;
    }

    protected String getSpecifiedTsaUrl() {
        return tsaUrl;
    }

    protected Boolean isSpecifiedArchiving() {
        return archiving;
    }

    public String getTsaUrl() {
        if (tsaUrl == null) {
            return AssinareConstants.TS_CARTAODECIDADAO_URL;
        } else if (tsaUrl.equals(NO_TSA_OPTION)) {
            return null;
        } else {
            return tsaUrl;
        }
    }

    public Boolean isUseTsa() {
        if (tsaUrl == null || !tsaUrl.equals(NO_TSA_OPTION)) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    public Boolean isArchiving() {
        return isUseTsa() && TRUE.equals(archiving);
    }

}
