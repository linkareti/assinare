package com.linkare.assinare.server.test.resources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.linkare.assinare.server.test.resources.WithTestScriptEngine.Mode;

import io.quarkus.test.common.QuarkusTestResourceConfigurableLifecycleManager;

/**
 *
 * @author bnazare
 */
public class ScriptEngineTestResource implements QuarkusTestResourceConfigurableLifecycleManager<WithTestScriptEngine> {

    private boolean badCommand;
    private File rootTmpDir;

    @Override
    public void init(WithTestScriptEngine annotation) {
        badCommand = annotation.mode() == Mode.BAD_COMMAND;
    }

    @Override
    public void init(Map<String, String> initArgs) {
        badCommand = StringUtils.isNotBlank(initArgs.get("badCommand"));
    }

    @Override
    public Map<String, String> start() {
        try {
            if (badCommand) {
                return Map.of("asn.script.executable", "non-existant-command");
            } else {
                initRootTempDir();

                File loadScript = copyFile("read.py");
                File storeScript = copyFile("write.py");

                return Map.of("asn.script.executable", "python3",
                        "asn.script.load-file", loadScript.getAbsolutePath(),
                        "asn.script.store-file", storeScript.getAbsolutePath(),
                        "asn.script.timeout", String.valueOf(Duration.ofSeconds(1).toMillis()),
                        "asn.script.in-threshold", String.valueOf(1024 * 1024) // 1 MB
                );
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void initRootTempDir() throws IOException {
        rootTmpDir = File.createTempFile("asn-tests-", "");
        rootTmpDir.delete();
        rootTmpDir.mkdir();
        rootTmpDir.deleteOnExit();
    }

    private File copyFile(final String fileName) throws IOException {
        File destFile = new File(rootTmpDir, FilenameUtils.getName(fileName));
        destFile.deleteOnExit();

        InputStream srcData = getClass().getResourceAsStream(fileName);
        FileUtils.copyInputStreamToFile(srcData, destFile); // method closes the stream

        return destFile;
    }

    @Override
    public void stop() {
        FileUtils.deleteQuietly(rootTmpDir); // method handles nulls
    }

}
