package com.linkare.assinare.server.scriptengine;

/**
 *
 * @author bnazare
 */
public class ScriptExecutionFailureException extends ScriptEngineException {

    public ScriptExecutionFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScriptExecutionFailureException(Throwable cause) {
        super(cause);
    }

}
