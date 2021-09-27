package com.linkare.assinare.sign.asic;

import com.linkare.assinare.sign.model.AssinareDocument;

/**
 *
 * @author bnazare
 */
public interface ASiCParser {

    boolean isReSignable(AssinareDocument doc);
    
}
