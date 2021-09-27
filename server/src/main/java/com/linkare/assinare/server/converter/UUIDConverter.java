package com.linkare.assinare.server.converter;

import java.util.UUID;

import org.eclipse.microprofile.config.spi.Converter;

public class UUIDConverter implements Converter<UUID> {

    @Override
    public UUID convert(String value) {
        return UUID.fromString(value);
    }

}
