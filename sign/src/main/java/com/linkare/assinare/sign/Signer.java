package com.linkare.assinare.sign;

import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.commons.ui.AssinarePINGUI;
import com.linkare.assinare.commons.ui.SignatureStageListener;
import com.linkare.assinare.sign.fileprovider.FileAccessException;
import com.linkare.assinare.sign.model.AssinareDocument;

/**
 *
 * @author bnazare
 * @param <D> the type of the data to sign
 * @param <K> the type of the signature fields
 */
public interface Signer<D, K extends SignatureFields> {

    AssinareDocument sign(SigningKey key, D srcData, AssinarePINGUI pinCallback, K sigFields, SignatureStageListener listener) throws AssinareError, AssinareException, FileAccessException;

}
