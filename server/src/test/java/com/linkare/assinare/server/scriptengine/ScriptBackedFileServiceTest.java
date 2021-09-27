package com.linkare.assinare.server.scriptengine;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CircularInputStream;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import com.linkare.assinare.server.test.Profiles;
import com.linkare.assinare.server.test.utils.ScriptEngineTestHelper;
import com.linkare.assinare.sign.fileprovider.FileAccessException;
import com.linkare.assinare.sign.model.AssinareDocument;
import com.linkare.assinare.sign.model.InMemoryDocument;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

/**
 *
 * @author bnazare
 */
@QuarkusTest
@TestProfile(Profiles.Default.class)
@EnabledIf(value = "python3Available",
        disabledReason = "Requires 'python3' available in the path")
public class ScriptBackedFileServiceTest {

    public static final byte[] BAD_PDF_DATA = new byte[]{1, 2, 3, 4};

    @Inject
    ScriptEngineTestHelper helper;

    @Inject
    ScriptBackedFileService fileService;

    static boolean python3Available() {
        try {
            new ProcessBuilder("python3", "--version").start();
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    @AfterEach
    public void tearDown() {
        helper.deleteAll();
    }

    @Test
    @SuppressWarnings("ThrowableResultIgnored")
    public void testGetFile() throws Exception {
        try (InputStream docData = getClass().getClassLoader().getResourceAsStream("docs/testdoc.pdf")) {
            helper.putOriginalFile("testdoc.pdf", docData);
            docData.reset();

            AssinareDocument doc = fileService.getFile("testdoc.pdf");
            assertEquals("testdoc.pdf", doc.getName());

            // for coverage
            assertThrows(UnsupportedOperationException.class,
                    () -> doc.getContentType()
            );

            // for coverage
            assertThrows(UnsupportedOperationException.class,
                    () -> doc.openOutputStream()
            );

            try (InputStream docStream = doc.openInputStream()) {
                assertNotNull(docStream);
                assertTrue(IOUtils.contentEquals(docData, docStream));
            }
        }
    }

    @Test
    public void testGetFile_Params() throws Exception {
        try (InputStream docData = getClass().getClassLoader().getResourceAsStream("docs/testdoc.pdf")) {
            helper.putOriginalFile("testdoc.pdf", docData);
            docData.reset();

            AssinareDocument doc = fileService.getFile("testdoc.pdf", Map.of("optiona", "valuea", "optionb", "valueb"));
            assertEquals("testdoc.pdf", doc.getName());

            try (InputStream docStream = doc.openInputStream()) {
                assertNotNull(docStream);
                assertTrue(IOUtils.contentEquals(docData, docStream));
            }

            // TODO: test that parameters have expected behaviour
        }
    }

    @Test
    @SuppressWarnings("ThrowableResultIgnored")
    public void testGetFile_MissingFile() throws Exception {
        AssinareDocument doc = fileService.getFile("missing-doc.pdf");

        try (InputStream docStream = doc.openInputStream()) {
            assertThrows(ScriptFileNotFoundException.class,
                    () -> IOUtils.consume(docStream)
            );
        }
    }

    @Test
    @SuppressWarnings("ThrowableResultIgnored")
    public void testGetFile_Overrun() throws Exception {
        // create a dummy stream with 1.5MB of zeros, and write it to a file
        // it is assumed that the script engine limit during tests is 1MB
        try (InputStream docData = new CircularInputStream(new byte[1], (long) (1.5 * 1024 * 1024))) {
            helper.putOriginalFile("testdoc_pdfa.pdf", docData);

            AssinareDocument doc = fileService.getFile("testdoc_pdfa.pdf");

            try (InputStream docStream = doc.openInputStream()) {
                assertThrows(ScriptStreamOverrunException.class,
                        () -> IOUtils.consume(docStream)
                );
            }
        }
    }

    @Test
    @SuppressWarnings("ThrowableResultIgnored")
    public void testGetFile_Timeout() throws Exception {
        AssinareDocument doc = fileService.getFile("testdoc.pdf", Map.of("timeout", "2"));

        try (InputStream docStream = doc.openInputStream()) {
            assertThrows(ScriptTimeoutException.class,
                    () -> IOUtils.consume(docStream)
            );
        }
    }

    @Test
    public void testPutFile() throws Exception {
        InMemoryDocument doc = new InMemoryDocument("create-doc.pdf", BAD_PDF_DATA);
        fileService.putFile("create-doc.pdf", doc);

        File signedFile = helper.getSignedFile("create-doc.pdf");
        assertNotNull(signedFile);

        byte[] signedFileData = FileUtils.readFileToByteArray(signedFile);
        assertArrayEquals(BAD_PDF_DATA, signedFileData);
    }

    @Test
    @SuppressWarnings("ThrowableResultIgnored")
    public void testGetFile_ScriptError() throws Exception {
        AssinareDocument doc = fileService.getFile("testdoc.pdf", Map.of("no-such-option", "dummy"));

        try (InputStream docStream = doc.openInputStream()) {
            assertThrows(ScriptExecutionErrorException.class,
                    () -> IOUtils.consume(docStream)
            );
        }
    }

    @Test
    public void testPutFile_ScriptError() throws Exception {
        InMemoryDocument doc = new InMemoryDocument("create-doc.pdf", BAD_PDF_DATA);

        FileAccessException ex = assertThrows(FileAccessException.class,
                () -> fileService.putFile("create-doc.pdf", doc, Map.of("no-such-option", "dummy"))
        );

        assertNotNull(ex.getCause());
        assertSame(ScriptExecutionErrorException.class, ex.getCause().getClass());
    }

}
