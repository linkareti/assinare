package com.linkare.assinare.server;

import java.net.URL;
import java.util.UUID;

import io.smallrye.config.ConfigMapping;

/**
 *
 * @author bnazare
 */
@ConfigMapping(prefix = "asn.cmd")
public interface CMDConfiguration {

    UUID applicationId();

    String applicationUser();

    String applicationPassword();

    URL wsdlLocation();

}
