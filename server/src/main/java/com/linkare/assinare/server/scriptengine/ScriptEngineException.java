package com.linkare.assinare.server.scriptengine;

import java.io.IOException;

/**
 *
 * @author bnazare
 */
public abstract class ScriptEngineException extends IOException {

    public ScriptEngineException() {
    }

    public ScriptEngineException(String message) {
        super(message);
    }

    public ScriptEngineException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScriptEngineException(Throwable cause) {
        super(cause);
    }

}
