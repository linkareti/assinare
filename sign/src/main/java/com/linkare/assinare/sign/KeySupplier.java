package com.linkare.assinare.sign;

import java.util.List;

/**
 *
 * @author bnazare
 */
public interface KeySupplier extends AutoCloseable {

    public List<SigningKey> getKeys();
}
