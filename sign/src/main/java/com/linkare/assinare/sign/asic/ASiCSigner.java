package com.linkare.assinare.sign.asic;

import java.util.List;

import com.linkare.assinare.sign.Signer;
import com.linkare.assinare.sign.model.AssinareDocument;

/**
 *
 * @author bnazare
 */
public interface ASiCSigner extends Signer<List<AssinareDocument>, ASiCSignatureFields> {
}
