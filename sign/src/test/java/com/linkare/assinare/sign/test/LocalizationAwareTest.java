package com.linkare.assinare.sign.test;

import java.util.Locale;
import java.util.TimeZone;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

/**
 *
 * @author bnazare
 */
public abstract class LocalizationAwareTest {

    private static final Locale DEFAULT_LOCALE = Locale.getDefault();
    private static final Locale TEST_LOCALE = new Locale("pt", "PT");
    private static final TimeZone TEST_TIMEZONE = TimeZone.getTimeZone("Europe/Lisbon");

    @BeforeAll
    public static void setUpClass() throws Exception {
        Locale.setDefault(TEST_LOCALE);
        TimeZone.setDefault(TEST_TIMEZONE);
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
        TimeZone.setDefault(null);
        Locale.setDefault(DEFAULT_LOCALE);
    }

}
