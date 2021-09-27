package com.linkare.assinare.sign.asic;

/**
 * Represents one of the CommitmentTypes identified at http://uri.etsi.org/01903/v1.3.2/ts_101903v010302p.pdf, section 7.2.6.
 * @author bnazare
 */
public enum CommitmentType {

    /** Indicates that the signer recognizes to have created, approved and sent the signed data object. */
    PROOF_OF_ORIGIN("assinare.asic.label.source", "http://uri.etsi.org/01903/v1.2.2#ProofOfOrigin"),
    /** Indicates that signer recognizes to have received the content of the signed data object. */
    PROOF_OF_RECEIPT("assinare.asic.label.reception", "http://uri.etsi.org/01903/v1.2.2#ProofOfReceipt"),
    /** Indicates that the TSP providing that indication has delivered a signed data object in a local store accessible to the recipient of the signed data object. */
    PROOF_OF_DELIVERY("assinare.asic.label.delivery", "http://uri.etsi.org/01903/v1.2.2#ProofOfDelivery"),
    /** Indicates that the entity providing that indication has sent the signed data object (but not necessarily created it). */
    PROOF_OF_SENDER("assinare.asic.label.sender", "http://uri.etsi.org/01903/v1.2.2#ProofOfSender"),
    /** Indicates that the signer has approved the content of the signed data object. */
    PROOF_OF_APPROVAL("assinare.asic.label.approval", "http://uri.etsi.org/01903/v1.2.2#ProofOfApproval"),
    /** Indicates that the signer has created the signed data object (but not necessarily approved, nor sent it) */
    PROOF_OF_CREATION("assinare.asic.label.creation", "http://uri.etsi.org/01903/v1.2.2#ProofOfCreation");

    private final String name;
    private final String uriId;
    private static final String RESOURCE_BUNDLE_LOCATION = "com/linkare/assinare/resourceBundle/Language";

    private CommitmentType(String bundleKey, String uriId) {
        this.name = java.util.ResourceBundle.getBundle(RESOURCE_BUNDLE_LOCATION).getString(bundleKey);
        this.uriId = uriId;
    }

    public String getName() {
        return name;
    }

    public String getUriId() {
        return uriId;
    }

    @Override
    public String toString() {
        return getName();
    }
}
