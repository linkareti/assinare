package com.linkare.assinare.server.test;

import static java.util.Map.entry;

import java.util.List;
import java.util.Map;

import com.linkare.assinare.server.test.resources.SCMDTestResource;
import com.linkare.assinare.server.test.resources.ScriptEngineTestResource;
import com.linkare.assinare.server.test.resources.TSATestResource;

import io.quarkus.test.junit.QuarkusTestProfile;

/**
 *
 * @author bnazare
 */
public class Profiles {

    public static class Default implements QuarkusTestProfile {

        @Override
        public List<TestResourceEntry> testResources() {
            return List.of(
                    new TestResourceEntry(ScriptEngineTestResource.class),
                    new TestResourceEntry(SCMDTestResource.class),
                    new TestResourceEntry(TSATestResource.class)
            );
        }

    }

    /**
     * This class actually represents two configuration profiles but since they
     * don't overlap we keep them together to avoid an extra Quarkus restart.
     */
    public static class AltConfiguration implements QuarkusTestProfile {

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.ofEntries(
                    // script engine conf
                    entry("asn.script.executable", "non-existant-command"),
                    // pdf signatures conf
                    entry("asn.signature.pdf.contact", "test-contact"),
                    entry("asn.signature.pdf.location", "test-location"),
                    entry("asn.signature.pdf.reason", "test-reason"),
                    entry("asn.signature.pdf.percent-x", "0.1"),
                    entry("asn.signature.pdf.percent-y", "0.1"),
                    entry("asn.signature.pdf.width", "10"),
                    entry("asn.signature.pdf.height", "10"),
                    entry("asn.signature.pdf.page-number", "10"),
                    entry("asn.signature.pdf.sig-rendering-mode", "LOGO_CHOOSED_BY_USER"),
                    entry("asn.signature.pdf.logo-file-url", "http://example.org/logo.png"),
                    entry("asn.signature.pdf.field-name", "test-field"),
                    entry("asn.signature.pdf.tsa-url", "http://example.org/tsa"),
                    entry("asn.signature.pdf.do-ltv", "true")
            );
        }

    }

}
