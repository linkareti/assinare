package com.linkare.assinare.applet.id;

import static com.linkare.assinare.applet.common.utils.AppletUtils.validatePreConditions;

import java.applet.Applet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.linkare.assinare.applet.common.async.AsyncPrivilegedAction;
import com.linkare.assinare.applet.common.utils.JSObjectUtils;
import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.id.AssinareId;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;

/**
 *
 * @author Ricardo Vaz - Linkare TI
 */
public class AssinareIdApplet extends Applet {

    private static final long serialVersionUID = -330495618764534652L;
    private static final Logger LOG = Logger.getLogger(AssinareIdApplet.class.getName());

    private AssinareId main;
    private ExecutorService executor;

    @Override
    public void init() {
        main = new AssinareId();
        executor = Executors.newSingleThreadExecutor();

        LOG.info("Applet AssinareId initialized.");
    }

    /**
     * Method called from Javascript used to retrieve the id data contained in
     * the smart card e.g.: name, id number, etc.
     * <p>
     * This method is supposed to be called from Javascript in the host web page
     * where the AssinareId Applet is initiated. It sends back the data as a
     * parameter to the Javascript function named callbackName that is in
     * Assinare host web page. The data sent as a parameter is in fact an Object
     * composed by key/value pairs. The callbackName function will be
     * responsible to present the key/value pairs data as it desires.
     *
     * @param callbackName String used to access the caller and send back the
     * data
     * @throws AssinareException
     * @see assinareId.js
     */
    public void getCitizenData(final String callbackName) throws AssinareException {
        final JSObject window = JSObject.getWindow(this);
        validatePreConditions(window, callbackName);

        AsyncPrivilegedAction<JSObject> asyncAction = new DataResponseAction(window, callbackName) {
            @Override
            protected Map<String, ?> getData() throws AssinareException, AssinareError {
                return main.getCitizenData();
            }
        };

        executor.submit(asyncAction);
    }

    /**
     * Method called from Javascript used to retrieve the address contained in
     * the smart card. This method is called from the web page that hosts the
     * AssinareId Applet. It returns the data as an Object composed by key/value
     * pairs which is sent back as a parameter to the Javascript function named
     * callbackName that is in the host Webpage. The callbackName function will
     * be responsible to present the key/value pairs data as desires.
     *
     * @param callbackName String used to access the caller and send back the
     * data
     * @throws AssinareException
     * @see assinareId.js
     */
    public void getCitizenAddress(final String callbackName) throws AssinareException {
        final JSObject window = JSObject.getWindow(this);
        validatePreConditions(window, callbackName);

        AsyncPrivilegedAction<JSObject> asyncAction = new DataResponseAction(window, callbackName) {
            @Override
            protected Map<String, ?> getData() throws AssinareException, AssinareError {
                return main.getCitizenAddress();
            }
        };

        executor.submit(asyncAction);
    }

    /**
     * Method called from Javascript used to retrieve the picture contained in
     * the smart card. This method is called from the web page that hosts the
     * AssinareId Applet. It returns the data as an Object composed by key/value
     * pairs which is sent back as a parameter to the Javascript function named
     * callbackName that is in the host Webpage. The callbackName function will
     * be responsible to present the key/value pairs data as desires.
     *
     * @param callbackName String used to access the caller and send back the
     * data
     * @throws AssinareException
     * @see assinareId.js
     * @since 1.6.0-SNAPSHOT
     */
    public void getCitizenPicture(final String callbackName) throws AssinareException {
        final JSObject window = JSObject.getWindow(this);
        validatePreConditions(window, callbackName);

        AsyncPrivilegedAction<JSObject> asyncAction = new DataResponseAction(window, callbackName) {
            @Override
            protected Map<String, ?> getData() throws AssinareException, AssinareError {
                return main.getCitizenPicture();
            }
        };

        executor.submit(asyncAction);
    }

    private abstract static class DataResponseAction extends AsyncPrivilegedAction<JSObject> {

        private final JSObject window;
        private final String callbackName;

        public DataResponseAction(JSObject window, String callbackName) {
            this.window = window;
            this.callbackName = callbackName;
        }

        @Override
        protected JSObject runPrivileged() {
            try {
                Map<String, ?> data = getData();

                JSObject jsObject = JSObjectUtils.createJSObject(window, data);
                window.call(callbackName, new Object[]{null, jsObject});
                return jsObject;
            } catch (RuntimeException | AssinareException | AssinareError rex) {
                LOG.log(Level.SEVERE, null, rex);
                doCallbackWithError(window, callbackName, rex);
                return null;
            }
        }

        private void doCallbackWithError(JSObject window, String callbackName, Exception ex) throws JSException {
            JSObject errObj = JSObjectUtils.createJSObject(window);
            errObj.setMember("message", ex.getMessage());

            window.call(callbackName, new Object[]{errObj, null});
        }

        protected abstract Map<String, ?> getData() throws AssinareException, AssinareError;
    }

}
