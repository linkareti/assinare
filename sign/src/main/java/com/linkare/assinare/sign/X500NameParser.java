package com.linkare.assinare.sign;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;

/**
 *
 * @author bnazare
 */
public class X500NameParser {

    private final X500Name x500Name;

    public X500NameParser(X500Principal x500Principal) {
        this(X500Name.getInstance(x500Principal.getEncoded()));
    }

    public X500NameParser(X500Name x500Name) {
        this.x500Name = x500Name;
    }

    public String getCommonName() {
        return extractValue(BCStyle.CN);
    }

    public String getCountry() {
        return extractValue(BCStyle.C);
    }

    public String getOrganization() {
        return extractValue(BCStyle.O);
    }

    private String extractValue(ASN1ObjectIdentifier attributeType) {
        RDN[] rdns = x500Name.getRDNs(attributeType);
        if (rdns.length >= 1) {
            return rdnToString(rdns[0]);
        } else {
            return null;
        }
    }

    private String rdnToString(RDN rdn) {
        AttributeTypeAndValue first = rdn.getFirst();
        if (first != null) {
            return IETFUtils.valueToString(first.getValue());
        } else {
            return null;
        }
    }
}
