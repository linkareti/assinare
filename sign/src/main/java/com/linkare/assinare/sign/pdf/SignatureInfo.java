package com.linkare.assinare.sign.pdf;

import java.util.Date;
import java.util.Objects;

/**
 *
 * @author rvaz
 */
public class SignatureInfo extends SignatureFieldInfo {

    private final String signerName;
    private final Date date;
    private final String location;
    private final String reason;
    private final String contactDetails;

    private final boolean valid;

    public SignatureInfo(String fieldName, int revision, int page, float percentX, float percentY, double percentWidth, double percentHeight, String signerName, Date date, String location, String reason, String contactDetails, boolean valid) {
        super(fieldName, revision, page, percentX, percentY, percentWidth, percentHeight);
        this.signerName = signerName;
        this.date = (Date) date.clone();
        this.location = location;
        this.reason = reason;
        this.contactDetails = contactDetails;
        this.valid = valid;
    }

    public String getSignerName() {
        return signerName;
    }

    public boolean isValid() {
        return valid;
    }

    public Date getDate() {
        return (Date) date.clone();
    }

    public String getLocation() {
        return location;
    }

    public String getReason() {
        return reason;
    }

    public String getContactDetails() {
        return contactDetails;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 79 * hash + Objects.hashCode(this.signerName);
        hash = 79 * hash + Objects.hashCode(this.date);
        hash = 79 * hash + Objects.hashCode(this.location);
        hash = 79 * hash + Objects.hashCode(this.reason);
        hash = 79 * hash + Objects.hashCode(this.contactDetails);
        hash = 79 * hash + (this.valid ? 1 : 0);
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
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SignatureInfo other = (SignatureInfo) obj;
        if (this.valid != other.valid) {
            return false;
        }
        if (!Objects.equals(this.signerName, other.signerName)) {
            return false;
        }
        if (!Objects.equals(this.location, other.location)) {
            return false;
        }
        if (!Objects.equals(this.reason, other.reason)) {
            return false;
        }
        if (!Objects.equals(this.contactDetails, other.contactDetails)) {
            return false;
        }
        if (!Objects.equals(this.date, other.date)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SignatureInfo{" + "fieldName=" + getFieldName() + ", revision=" + getRevision() + ", page=" + getPage() + ", percentX=" + getPercentX() + ", percentY=" + getPercentY() + ", percentWidth=" + getPercentWidth() + ", percentHeight=" + getPercentHeight() + "signerName=" + signerName + ", date=" + date + ", location=" + location + ", reason=" + reason + ", contactDetails=" + contactDetails + ", valid=" + valid + '}';
    }

}
