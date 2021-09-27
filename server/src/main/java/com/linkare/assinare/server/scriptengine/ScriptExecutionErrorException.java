package com.linkare.assinare.server.scriptengine;

/**
 *
 * @author bnazare
 */
public class ScriptExecutionErrorException extends ScriptEngineException {

    public ScriptExecutionErrorException(String scriptErrorMsg) {
        super("Script execution error:\n[" + scriptErrorMsg + "]");
    }

}
