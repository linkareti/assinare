package com.linkare.assinare.server.scriptengine;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.EOF;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author bnazare
 */
@Dependent
public class ScriptEngine {

    private static final Logger LOG = Logger.getLogger(ScriptEngine.class.getName());

    @Inject
    ScriptEngineConfiguration configuration;

    private File loadScriptDir;
    private File storeScriptDir;

    @PostConstruct
    void init() {
        this.loadScriptDir = new File(configuration.loadFile()).getParentFile();
        this.storeScriptDir = new File(configuration.storeFile()).getParentFile();
    }

    public void storeDocument(String docName, InputStream docStream, Map<String, String> docParams) throws ScriptEngineException {
        List<String> command = new ArrayList<>();
        command.add(configuration.executable());
        command.add(configuration.storeFile());
        command.add(docName);
        addDocParams(command, docParams);

        Process proc;
        try {
            proc = new ProcessBuilder(command)
                    .directory(storeScriptDir)
                    .start();
        } catch (IOException ex) {
            throw new ScriptExecutionFailureException("Error starting script", ex);
        }

        final ExecuteWatchdog executeWatchdog = new ExecuteWatchdog(configuration.timeout());
        executeWatchdog.start(proc);

        try (OutputStream procInput = proc.getOutputStream()) {
            IOUtils.copy(docStream, procInput);
        } catch (IOException ex) {
            if (proc.isAlive()) {
                // something happened with the docStream, maybe?
                throw new ScriptExecutionFailureException(ex);
            } else {
                handleScriptFail(proc, executeWatchdog);
            }
        }

        try {
            if (proc.waitFor() != 0) { // No need to put a timeout here as the watchdog is already running.
                handleScriptFail(proc, executeWatchdog);
            }
        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public InputStream loadDocument(String docName, Map<String, String> docParams) throws ScriptEngineException {
        List<String> command = new ArrayList<>();
        command.add(configuration.executable());
        command.add(configuration.loadFile());
        command.add(docName);
        addDocParams(command, docParams);

        Process proc;
        try {
            proc = new ProcessBuilder(command)
                    .directory(loadScriptDir)
                    .start();
        } catch (IOException ex) {
            throw new ScriptExecutionFailureException("Error starting script", ex);
        }

        final ExecuteWatchdog executeWatchdog = new ExecuteWatchdog(configuration.timeout());
        executeWatchdog.start(proc);

        return new ScriptInputStream(proc, executeWatchdog, configuration);
    }

    private void addDocParams(List<String> command, Map<String, String> docParams) {
        if (docParams != null) {
            docParams.forEach((option, value) -> {
                command.add("--" + option);
                if (StringUtils.isNotBlank(value)) {
                    command.add(value);
                }
            });
        }
    }

    private void handleScriptFail(Process proc, final ExecuteWatchdog executeWatchdog) throws ScriptEngineException {
        ScriptEngine.handleScriptFail(proc, executeWatchdog, configuration);
    }

    private static void handleScriptFail(Process proc, final ExecuteWatchdog executeWatchdog, ScriptEngineConfiguration configuration) throws ScriptEngineException {
        if (executeWatchdog.killedProcess()) {
            throw new ScriptTimeoutException();
        } else {
            String errMsg;
            // no need to keep separate stream references as close() is propagated
            try (InputStream errorStream = new BoundedInputStream(proc.getErrorStream(), configuration.errThreshold())) {
                errMsg = IOUtils.toString(errorStream, UTF_8);
                errMsg = StringUtils.trim(errMsg);
            } catch (IOException ex) {
                errMsg = "<unparsable>";
            }

            if (proc.exitValue() == 101) {
                throw new ScriptFileNotFoundException(errMsg);
            } else {
                throw new ScriptExecutionErrorException(errMsg);
            }
        }
    }

    private static class ScriptInputStream extends ProxyInputStream {

        private final Process proc;
        private final ExecuteWatchdog executeWatchdog;
        private final ScriptEngineConfiguration configuration;
        private int byteCount = 0;

        public ScriptInputStream(Process proc, ExecuteWatchdog executeWatchdog, ScriptEngineConfiguration configuration) {
            super(proc.getInputStream());
            this.proc = proc;
            this.executeWatchdog = executeWatchdog;
            this.configuration = configuration;
        }

        @Override
        protected void beforeRead(int n) throws ScriptEngineException {
            /*
            When the process completes fully (successfully or with error), the STDIN stream
            does not get forcibly closed. Only when a timeout occurs do we need to check
            this. And even then, it just serves to give a more descriptive error message.
             */
            if (executeWatchdog.killedProcess()) {
                throw new ScriptTimeoutException();
            }
        }

        @Override
        protected void afterRead(int n) throws ScriptEngineException {
            if (n == EOF) {
                try {
                    if (proc.waitFor() != 0) { // No need to put a timeout here as the watchdog is already running.
                        ScriptEngine.handleScriptFail(proc, executeWatchdog, configuration);
                    }
                } catch (InterruptedException ex) {
                    throw new ScriptExecutionFailureException(ex);
                }
            } else {
                byteCount += n;
                if (byteCount > configuration.inThreshold()) {
                    executeWatchdog.destroyProcess();
                    throw new ScriptStreamOverrunException();
                }
            }
        }
    }

}
