package com.linkare.assinare.applet.common.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.linkare.assinare.commons.AssinareException;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;

/**
 *
 * @author bnazare
 */
public final class AppletUtils {

    private static final Logger LOG = Logger.getLogger(AppletUtils.class.getName());

    private AppletUtils() {
    }

    public static void validatePreConditions(final JSObject window, final String callbackName) throws AssinareException {
        final String callbackMissingMsg = "Instantiation callback is not a function.";
        try {
            if (window.getMember(callbackName) == null) {
                /* TODO: maybe validate that it is a function?
                 * something like: window.Function.prototype.isPrototypeOf(callbackFunction)
                 */
                LOG.severe(callbackMissingMsg);
                throw new AssinareException(callbackMissingMsg);
            }
        } catch (JSException jsex) {
            LOG.log(Level.FINE, null, jsex);
            LOG.severe(callbackMissingMsg);
            throw new AssinareException(callbackMissingMsg);
        }
    }

}
