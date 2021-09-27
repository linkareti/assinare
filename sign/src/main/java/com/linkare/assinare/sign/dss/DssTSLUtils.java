package com.linkare.assinare.sign.dss;

import static org.apache.commons.collections4.SetUtils.unmodifiableSet;

import java.util.Set;

import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.FileCacheDataLoader;
import eu.europa.esig.dss.spi.client.http.DSSFileLoader;
import eu.europa.esig.dss.spi.client.http.IgnoreDataLoader;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.tsl.function.SchemeTerritoryOtherTSLPointer;
import eu.europa.esig.dss.tsl.job.TLValidationJob;
import eu.europa.esig.dss.tsl.source.LOTLSource;

/**
 *
 * @author bnazare
 */
public final class DssTSLUtils {

    /**
     * This contains certificates for all EU. Very complete, unfortunately also
     * veeeeery slow.
     */
    private static final String EU_LOTL_URL = "https://ec.europa.eu/tools/lotl/eu-lotl.xml";

    /**
     * List of countries whose Trust Lists we want load. Limiting this
     * drastically reduces the duration of the process.
     */
    private static final Set<String> COUNTRIES_TO_LOAD = unmodifiableSet("PT", "BE");

    private static FileCacheDataLoader onlineFileLoader;
    private static FileCacheDataLoader offlineFileLoader;

    private DssTSLUtils() {
    }

    public static TrustedListsCertificateSource buildTSLCertificateSource() {
        TrustedListsCertificateSource tslCertificateSource = new TrustedListsCertificateSource();

        TLValidationJob job = new TLValidationJob();
        job.setOnlineDataLoader(getOnlineFileloader());
        job.setOfflineDataLoader(getOfflineFileloader());
        job.setTrustedListCertificateSource(tslCertificateSource);
        job.setListOfTrustedListSources(getEuropeanLOTL());

        job.onlineRefresh();

        return tslCertificateSource;
    }

    private static DSSFileLoader getOnlineFileloader() {
        if (onlineFileLoader == null) {
            onlineFileLoader = new FileCacheDataLoader(new CommonsDataLoader());
            onlineFileLoader.setCacheExpirationTime(0);
        }
        return onlineFileLoader;
    }

    private static DSSFileLoader getOfflineFileloader() {
        if (offlineFileLoader == null) {
            offlineFileLoader = new FileCacheDataLoader(new IgnoreDataLoader());
            offlineFileLoader.setCacheExpirationTime(Long.MAX_VALUE);
        }
        return offlineFileLoader;
    }

    private static LOTLSource getEuropeanLOTL() {
        LOTLSource lotlSource = new LOTLSource();
        lotlSource.setUrl(EU_LOTL_URL);

        // prepend a country-based predicate to the default TL predicate list
        lotlSource.setTlPredicate(
                new SchemeTerritoryOtherTSLPointer(COUNTRIES_TO_LOAD).and(lotlSource.getTlPredicate())
        );
        return lotlSource;
    }

}
