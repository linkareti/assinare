package com.linkare.assinare.web;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Bruno Nazaré - Linkare TI
 * @author Paulo Zenida - Linkare TI
 *
 */
public final class Utils {

    private Utils() {
    }

    /**
     * Works around a servlet spec issue where query parameters are always
     * parsed as ISO-8859-1.
     *
     * @param request
     * @param paramName
     * @return
     */
    static String getUTF8Parameter(HttpServletRequest request, String paramName) {

        try {
            final String queryString = URLDecoder.decode(request.getQueryString(), "UTF-8");
            final String[] queryParams = queryString.split("&");

            return Stream.of(queryParams)
                    .map(queryParam -> queryParam.split("="))
                    .filter(paramElements -> paramElements[0].equals(paramName))
                    .findAny()
                    .map(paramElements -> paramElements[1])
                    .orElse(null);
        } catch (UnsupportedEncodingException e) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
    }
}
