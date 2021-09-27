package com.linkare.assinare.server.pojo;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.linkare.assinare.server.ErrorCode;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 *
 * @author bnazare
 */
@RegisterForReflection
public class ProcessingError {

    private static final String BUNDLE_NAME = "bundles.Errors";
    private static final String ERROR_MSG_PREFIX = "msg.error.";

    private static final String[] LANGUAGES = {"pt", "en"};

    private static final ResourceBundle DEFAULT_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, Locale.ROOT);
    private static final Map<String, ResourceBundle> BUNDLES = new HashMap<>();

    /**
     * This is NOT the most correct way of dealing with resource bundles but the
     * native image tool has some issues with multiple locales so we need to set
     * them during static (build time) initialization. See:
     * https://github.com/oracle/graal/issues/911
     */
    static {
        for (String language : LANGUAGES) {
            ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, new Locale(language));
            BUNDLES.put(language, bundle);
        }
    }

    private final ErrorCode code;

    private final String message;

    public ProcessingError(ErrorCode code) {
        this.code = code;
        this.message = DEFAULT_BUNDLE.getString(ERROR_MSG_PREFIX + code.name());
    }

    public ProcessingError(ErrorCode code, Locale locale) {
        this.code = code;
        ResourceBundle bundle = getBundle(locale.getLanguage());
        this.message = bundle.getString(ERROR_MSG_PREFIX + code.name());
    }

    private ResourceBundle getBundle(String language) {
        return BUNDLES.getOrDefault(language, DEFAULT_BUNDLE);
    }

    /**
     * @return the code
     */
    public ErrorCode getCode() {
        return code;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

}
