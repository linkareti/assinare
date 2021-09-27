package com.linkare.assinare.server.scriptengine;

import io.smallrye.config.ConfigMapping;

/**
 *
 * @author bnazare
 */
@ConfigMapping(prefix = "asn.script")
public interface ScriptEngineConfiguration {

    String executable();

    String loadFile();

    String storeFile();

    int timeout();

    int inThreshold();

    int errThreshold();

}
