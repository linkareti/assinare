package otherconf.com.linkare.assinare.server.pojo;

import static com.linkare.assinare.sign.SignatureRenderingMode.LOGO_CHOOSED_BY_USER;
import static org.junit.jupiter.api.Assertions.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.linkare.assinare.server.pojo.PDFSignatureConfiguration;
import com.linkare.assinare.server.test.Profiles;
import com.linkare.assinare.sign.pdf.PDFSignatureFields;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

/**
 *
 * @author bnazare
 */
@QuarkusTest
@TestProfile(Profiles.AltConfiguration.class)
public class PDFSignatureConfigurationTest {

    @Inject
    PDFSignatureConfiguration conf;

    @Test
    public void testGetters() throws MalformedURLException {
        assertEquals(Optional.of("test-contact"), conf.contact());
        assertEquals(Optional.of("test-location"), conf.location());
        assertEquals("test-reason", conf.reason());
        assertEquals(0.1, conf.percentX());
        assertEquals(0.1, conf.percentY());
        assertEquals(10, conf.width());
        assertEquals(10, conf.height());
        assertEquals(10, conf.pageNumber());
        assertEquals(LOGO_CHOOSED_BY_USER, conf.sigRenderingMode());
        assertEquals(Optional.of(new URL("http://example.org/logo.png")), conf.logoFileUrl());
        assertEquals(Optional.of("test-field"), conf.fieldName());
        assertEquals(Optional.of("http://example.org/tsa"), conf.tsaUrl());
        assertEquals(true, conf.doLtv());
    }

    @Test
    public void testToPDFSignatureFields() throws MalformedURLException {
        PDFSignatureFields sigFields = conf.toPDFSignatureFields();

        assertEquals("test-contact", sigFields.getContact());
        assertEquals("test-location", sigFields.getLocation());
        assertEquals("test-reason", sigFields.getReason());
        assertEquals(0.1, sigFields.getPercentX());
        assertEquals(0.1, sigFields.getPercentY());
        assertEquals(10, sigFields.getWidth());
        assertEquals(10, sigFields.getHeight());
        assertEquals(10, sigFields.getPageNumber());
        assertEquals(LOGO_CHOOSED_BY_USER, sigFields.getSigRenderingMode());
        assertEquals(new URL("http://example.org/logo.png"), sigFields.getLogoFileURL());
        assertEquals("test-field", sigFields.getFieldName());
        assertEquals("http://example.org/tsa", sigFields.getTsaUrl());
        assertEquals(true, sigFields.isUseTsa());
        assertEquals(true, sigFields.isArchiving());
        assertEquals(null, sigFields.getUiMode());
    }

}
