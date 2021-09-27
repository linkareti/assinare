package com.linkare.assinare.applet.common.utils;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;

/**
 *
 * @author bnazare
 */
public final class JSObjectUtils {

    private static final Logger LOG = Logger.getLogger(JSObjectUtils.class.getName());

    public static JSObject createJSObject(final JSObject jsWindow, final Map<String, ?> map) {
        JSObject newObj = JSObjectUtils.createJSObject(jsWindow);
        for (Map.Entry<String, ?> entrySet : map.entrySet()) {
            final String key = entrySet.getKey();
            final Object value = entrySet.getValue();
            if (value instanceof Map) {
                // FIXME: this cast is a bit dangerous, but this method is used only internally
                newObj.setMember(key, createJSObject(jsWindow, (Map<String, ?>) value));
            } else {
                // IE (at least 9) doesn't like to set members as null, go figure ....
                newObj.setMember(key, value != null ? value : "");
            }
        }
        return newObj;
    }

    public static JSObject createJSObject(final JSObject jsWindow) throws JSException {
        return (JSObject) jsWindow.call("Object");
    }

    public static JSObject createJSArray(final JSObject jsWindow) throws JSException {
        return (JSObject) jsWindow.call("Array");
    }

    public static JSObject createJSArray(final JSObject jsWindow, final Object[] data) throws JSException {
        JSObject jsArray = createJSArray(jsWindow);
        jsArray.call("push", data);
        return jsArray;
    }

    public static Object safeGetMember(JSObject jsObject, String name) {
        try {
            return jsObject.getMember(name);
        } catch (JSException jsex) {
            LOG.log(Level.FINEST, null, jsex);
            return null;
        }
    }

    public static <T> T[] getJSArray(JSObject jsObject, Class<T> clazz) throws JSException {
        Object length = JSObjectUtils.safeGetMember(jsObject, "length");

        if (length instanceof Number) {
            int len = ((Number) length).intValue();
            if (len >= 0) {
                T[] retArr = (T[]) Array.newInstance(clazz, len);
                for (int i = 0; i < len; i++) {
                    retArr[i] = (T) jsObject.getSlot(i);
                }

                return retArr;
            }
        }

        return null;
    }

    private JSObjectUtils() {
    }
}
