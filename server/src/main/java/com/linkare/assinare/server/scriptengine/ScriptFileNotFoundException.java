package com.linkare.assinare.server.scriptengine;

/**
 *
 * @author bnazare
 */
public class ScriptFileNotFoundException extends ScriptEngineException {

    public ScriptFileNotFoundException(String scriptErrorMsg) {
        super("Script failed to find a file, with error:\n[" + scriptErrorMsg + "]");
    }

}
