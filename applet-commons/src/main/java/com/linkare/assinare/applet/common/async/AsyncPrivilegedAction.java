package com.linkare.assinare.applet.common.async;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 *
 * @author bnazare
 * @param <T>
 */
public abstract class AsyncPrivilegedAction<T> implements Runnable {

    protected AsyncPrivilegedAction() {
    }

    @Override
    public void run() {
        AccessController.doPrivileged(
                (PrivilegedAction<T>) this::runPrivileged
        );
    }

    protected abstract T runPrivileged();

}
