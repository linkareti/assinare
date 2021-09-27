package com.linkare.assinare.server.pojo;

import static com.linkare.assinare.sign.AssinareConstants.PDF_DEFAULT_PAGE;
import static com.linkare.assinare.sign.AssinareConstants.SIGNATURE_DEFAULT_HEIGHT;
import static com.linkare.assinare.sign.AssinareConstants.SIGNATURE_DEFAULT_WIDTH;
import static com.linkare.assinare.sign.AssinareConstants.SIGNATURE_DEFAULT_X_PCT;
import static com.linkare.assinare.sign.AssinareConstants.SIGNATURE_DEFAULT_Y_PCT;

import java.net.URL;
import java.util.Optional;

import com.linkare.assinare.sign.SignatureRenderingMode;
import com.linkare.assinare.sign.pdf.PDFSignatureFields;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 *
 * @author bnazare
 */
@ConfigMapping(prefix = "asn.signature.pdf")
public interface PDFSignatureConfiguration {

    Optional<String> contact();

    Optional<String> location();

    @WithDefault("my-reason")
    String reason();

    @WithDefault("" + SIGNATURE_DEFAULT_X_PCT)
    Double percentX();

    @WithDefault("" + SIGNATURE_DEFAULT_Y_PCT)
    Double percentY();

    @WithDefault("" + SIGNATURE_DEFAULT_WIDTH)
    Integer width();

    @WithDefault("" + SIGNATURE_DEFAULT_HEIGHT)
    Integer height();

    @WithDefault("" + PDF_DEFAULT_PAGE)
    Integer pageNumber();

    @WithDefault("TEXT_ONLY")
    SignatureRenderingMode sigRenderingMode();

    Optional<URL> logoFileUrl();

    Optional<String> fieldName();

    Optional<String> tsaUrl();

    @WithDefault("false")
    Boolean doLtv();

    default PDFSignatureFields toPDFSignatureFields() {
        return new PDFSignatureFields(contact().orElse(null), location().orElse(null), reason(),
                percentX(), percentY(), pageNumber(),
                width(), height(), sigRenderingMode(),
                fieldName().orElse(null), tsaUrl().orElse(null),
                doLtv(), logoFileUrl().orElse(null), null);
    }

}
