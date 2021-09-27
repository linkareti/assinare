package com.linkare.assinare.server;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheKey;
import io.quarkus.cache.CacheResult;

/**
 * Implements a {@link SigningContext} cache via Quarkus' cache extension. Uses
 * Caffeine underneath it all.
 *
 * @author bnazare
 */
@ApplicationScoped
public class SigningContextCache {

    private static final String CACHE_NAME = "sig-ctx";

    /**
     * Puts a value in the cache. In fact, this method cheats a little on the
     * Quarkus' cache mechanics. The Quarkus' caches don't have any way of
     * adding values procedimentally, instead they store the result of any given
     * method. That's why, to perform a "put", this method needs to return the
     * exact same value it receives as a parameter.
     *
     * @param key the value's key
     * @param value the value
     * @return the {@code  value} received
     */
    @CacheInvalidate(cacheName = CACHE_NAME)
    @CacheResult(cacheName = CACHE_NAME)
    public SigningContext put(@CacheKey String key, SigningContext value) {
        return value;
    }

    /**
     * Gets a value from the cache, if present. Otherwise, returns {@code null}.
     *
     * @param key the value's key
     * @return the cached value for the provided key, or {@code null}
     */
    @CacheResult(cacheName = CACHE_NAME)
    public SigningContext getIfPresent(@CacheKey String key) {
        return null;
    }

}
