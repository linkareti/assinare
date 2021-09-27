package com.linkare.assinare.sign;

import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;

/**
 *
 * @author bnazare
 * @param <D> the type of the data to sign
 * @param <K> the type of the signature fields
 */
public interface SignatureProcess<D, K extends SignatureFields> {

    String doSignature(String[] docList, K sigFields) throws AssinareError, AssinareException;

}
