package com.linkare.assinare.sign.asic;

import org.apache.commons.lang3.ArrayUtils;

import com.linkare.assinare.sign.SignatureFields;

/**
 *
 * @author bnazare
 */
public class ASiCSignatureFields extends SignatureFields {

    private final String containerName;
    private final String location;
    private final String[] claimedRoles;
    private final CommitmentType[] commitmentTypes;

    public ASiCSignatureFields() {
        this(null, null, (String[]) null, null, null, null);
    }

    public ASiCSignatureFields(String containerName, String location, String claimedRole, CommitmentType commitmentType, String tsaUrl, Boolean withLTA) {
        super(tsaUrl, withLTA);
        this.containerName = containerName;
        this.location = location;
        if (claimedRole != null) {
            this.claimedRoles = new String[]{claimedRole};
        } else {
            this.claimedRoles = new String[0];
        }
        if (commitmentType != null) {
            this.commitmentTypes = new CommitmentType[]{commitmentType};
        } else {
            this.commitmentTypes = new CommitmentType[0];
        }
    }

    public ASiCSignatureFields(String containerName, String location, String[] claimedRoles, CommitmentType[] commitmentTypes, String tsaUrl, Boolean withLTA) {
        super(tsaUrl, withLTA);
        this.containerName = containerName;
        this.location = location;
        if (claimedRoles != null) {
            this.claimedRoles = claimedRoles.clone();
        } else {
            this.claimedRoles = new String[0];
        }
        if (commitmentTypes != null) {
            this.commitmentTypes = commitmentTypes.clone();
        } else {
            this.commitmentTypes = new CommitmentType[0];
        }
    }

    public String getContainerName() {
        return containerName;
    }

    public String getLocation() {
        return location;
    }

    public String[] getClaimedRoles() {
        return claimedRoles.clone();
    }

    public CommitmentType[] getCommitmentTypes() {
        return commitmentTypes.clone();
    }

    public CommitmentType getCommitmentType() {
        return ArrayUtils.isNotEmpty(commitmentTypes) ? commitmentTypes[0] : null;
    }

    public ASiCSignatureFields merge(ASiCSignatureFields otherSigFields) {
        String mContainerName;
        String mLocation;
        String[] mClaimedRoles;
        CommitmentType[] mCommitmentTypes;
        String mTsaUrl;
        Boolean mWithLTA;

        if (otherSigFields.getContainerName() != null) {
            mContainerName = otherSigFields.getContainerName();
        } else {
            mContainerName = this.getContainerName();
        }

        if (otherSigFields.getLocation() != null) {
            mLocation = otherSigFields.getLocation();
        } else {
            mLocation = this.getLocation();
        }

        if (ArrayUtils.isNotEmpty(otherSigFields.getClaimedRoles())) {
            mClaimedRoles = otherSigFields.getClaimedRoles();
        } else {
            mClaimedRoles = this.getClaimedRoles();
        }

        if (ArrayUtils.isNotEmpty(otherSigFields.getCommitmentTypes())) {
            mCommitmentTypes = otherSigFields.getCommitmentTypes();
        } else {
            mCommitmentTypes = this.getCommitmentTypes();
        }

        if (otherSigFields.getSpecifiedTsaUrl() != null) {
            mTsaUrl = otherSigFields.getSpecifiedTsaUrl();
        } else {
            mTsaUrl = this.getSpecifiedTsaUrl();
        }

        if (otherSigFields.isSpecifiedArchiving() != null) {
            mWithLTA = otherSigFields.isSpecifiedArchiving();
        } else {
            mWithLTA = this.isSpecifiedArchiving();
        }

        return new ASiCSignatureFields(mContainerName, mLocation, mClaimedRoles, mCommitmentTypes, mTsaUrl, mWithLTA);
    }

}
