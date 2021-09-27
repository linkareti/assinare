package com.linkare.assinare.server.pojo;

import javax.validation.constraints.NotEmpty;

/**
 *
 * @author bnazare
 */
public class GetHashReturn {

    @NotEmpty
    private final byte[] hash;

    public GetHashReturn(byte[] hash) {
        this.hash = hash;
    }

    /**
     * @return the hash
     */
    public byte[] getHash() {
        return hash;
    }

}
