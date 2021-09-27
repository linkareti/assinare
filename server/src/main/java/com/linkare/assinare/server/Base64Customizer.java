package com.linkare.assinare.server;

import javax.inject.Singleton;
import javax.json.bind.JsonbConfig;
import javax.json.bind.config.BinaryDataStrategy;

import io.quarkus.jsonb.JsonbConfigCustomizer;

/**
 *
 * @author bnazare
 */
@Singleton
public class Base64Customizer implements JsonbConfigCustomizer {

    @Override
    public void customize(JsonbConfig config) {
        config.withBinaryDataStrategy(BinaryDataStrategy.BASE_64);
    }
}
