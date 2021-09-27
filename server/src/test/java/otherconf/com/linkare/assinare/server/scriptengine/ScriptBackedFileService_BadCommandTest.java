package otherconf.com.linkare.assinare.server.scriptengine;

import static com.linkare.assinare.server.scriptengine.ScriptBackedFileServiceTest.BAD_PDF_DATA;
import static org.junit.jupiter.api.Assertions.*;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.linkare.assinare.server.scriptengine.ScriptBackedFileService;
import com.linkare.assinare.server.scriptengine.ScriptEngineConfiguration;
import com.linkare.assinare.server.scriptengine.ScriptExecutionFailureException;
import com.linkare.assinare.server.test.Profiles;
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
@TestProfile(Profiles.AltConfiguration.class)
public class ScriptBackedFileService_BadCommandTest {

    @Inject
    ScriptBackedFileService fileService;

    @Inject
    ScriptEngineConfiguration conf;

    @Test
    @SuppressWarnings("ThrowableResultIgnored")
    public void testGetFile() throws Exception {
        AssinareDocument doc = fileService.getFile("testdoc.pdf");

        assertThrows(ScriptExecutionFailureException.class,
                () -> doc.openInputStream()
        );
    }

    @Test
    public void testPutFile() throws Exception {
        InMemoryDocument doc = new InMemoryDocument("create-doc.pdf", BAD_PDF_DATA);

        FileAccessException ex = assertThrows(FileAccessException.class,
                () -> fileService.putFile("create-doc.pdf", doc)
        );

        assertNotNull(ex.getCause());
        assertSame(ScriptExecutionFailureException.class, ex.getCause().getClass());
    }

}
