package com.linkare.assinare.sign.pdf;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.sign.SignatureFields;
import com.linkare.assinare.sign.SignatureRenderingMode;

/**
 *
 * @author rvaz
 */
public class PDFSignatureFields extends SignatureFields {

    private static final Logger LOG = Logger.getLogger(PDFSignatureFields.class.getName());

    private static final String DEFAULT_LOGO_FILENAME = "assinareIcon.png";

    private final String contact;
    private final String location;
    private final String reason;
    private final Double percentX;
    private final Double percentY;
    private final Integer width;
    private final Integer height;
    private final Integer pageNumber;
    private final SignatureRenderingMode sigRenderingMode;
    private final URL logoFileURL;
    private final String fieldName;
    private final UiMode uiMode;

    public PDFSignatureFields() {
        this(null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public PDFSignatureFields(Double percentX, Double percentY, Integer pageNumber, Integer width, Integer height) {
        this(null, null, null, percentX, percentY, pageNumber, width, height, null, null, null, null, null, null);
    }

    public PDFSignatureFields(String contact, String location, String reason, Double percentX, Double percentY, Integer pageNumber, Integer width, Integer height, SignatureRenderingMode sigRenderingMode, String fieldName, String tsaUrl, Boolean doLTV) {
        this(contact, location, reason, percentX, percentY, pageNumber, width, height, sigRenderingMode, fieldName, tsaUrl, doLTV, null, null);
    }

    public PDFSignatureFields(String contact, String location, String reason, Double percentX, Double percentY, Integer pageNumber, Integer width, Integer height, SignatureRenderingMode sigRenderingMode, String fieldName, String tsaUrl, Boolean doLTV, URL logoFileURL, UiMode uiMode) {
        super(tsaUrl, doLTV);
        this.contact = contact;
        this.location = location;
        this.reason = reason;
        this.percentX = percentX;
        this.percentY = percentY;
        this.logoFileURL = logoFileURL;
        this.pageNumber = pageNumber;
        this.width = width;
        this.height = height;
        this.sigRenderingMode = sigRenderingMode;
        this.fieldName = fieldName;
        this.uiMode = uiMode;
    }

    public String getContact() {
        return contact;
    }

    public String getLocation() {
        return location;
    }

    public String getReason() {
        return reason;
    }

    private BufferedImage loadDefaultLogo() throws AssinareException {
        try {
            return ImageIO.read(PDFSignatureFields.class.getClassLoader().getResource(DEFAULT_LOGO_FILENAME));
        } catch (IOException ioex) {
            throw new AssinareException("Não foi possível abrir o ficheiro.", ioex);
        }
    }

    private BufferedImage loadLogo(URL logoFileURL) throws AssinareException {
        try {
            if (logoFileURL != null) {
                return ImageIO.read(logoFileURL);
            } else {
                return null;
            }
        } catch (IOException ioex) {
            throw new AssinareException("Não foi possível abrir o ficheiro. Usado o logotipo de assinatura pré-definido.", ioex);
        }

    }

    public URL getLogoFileURL() {
        switch (sigRenderingMode) {
            case LOGO_CHOOSED_BY_USER:
                return logoFileURL;
            case PRE_DEFINED_LOGO:
                return PDFSignatureFields.class.getClassLoader().getResource(DEFAULT_LOGO_FILENAME);
            default:
                return null;
        }
    }

    public Double getPercentX() {
        return percentX;
    }

    public Double getPercentY() {
        return percentY;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public SignatureRenderingMode getSigRenderingMode() {
        return sigRenderingMode;
    }

    public boolean isShowSignature() {
        return sigRenderingMode.isShowSignature();
    }

    public boolean isShowLogo() {
        return sigRenderingMode.isShowLogo();
    }

    public boolean isUserLogo() {
        return sigRenderingMode.isUserLogo();
    }

    public String getFieldName() {
        return fieldName;
    }

    public UiMode getUiMode() {
        return uiMode;
    }

    public PDFSignatureFields merge(PDFSignatureFields otherSigFields) {
        String mContact, mLocation, mReason, mFieldName, mTsaUrl;
        Double mPercentX, mPercentY;
        Integer mPageNumber, mWidth, mHeight;
        Boolean mDoLTV;
        SignatureRenderingMode mSigRenderingMode;
        URL mLogoFileURL;
        UiMode mUiMode;

        if (otherSigFields.getContact() != null) {
            mContact = otherSigFields.getContact();
        } else {
            mContact = this.getContact();
        }

        if (otherSigFields.getLocation() != null) {
            mLocation = otherSigFields.getLocation();
        } else {
            mLocation = this.getLocation();
        }

        if (otherSigFields.getReason() != null) {
            mReason = otherSigFields.getReason();
        } else {
            mReason = this.getReason();
        }

        if (otherSigFields.getPercentX() != null) {
            mPercentX = otherSigFields.getPercentX();
        } else {
            mPercentX = this.getPercentX();
        }

        if (otherSigFields.getPercentY() != null) {
            mPercentY = otherSigFields.getPercentY();
        } else {
            mPercentY = this.getPercentY();
        }

        if (otherSigFields.getPageNumber() != null) {
            mPageNumber = otherSigFields.getPageNumber();
        } else {
            mPageNumber = this.getPageNumber();
        }

        if (otherSigFields.getWidth() != null) {
            mWidth = otherSigFields.getWidth();
        } else {
            mWidth = this.getWidth();
        }

        if (otherSigFields.getHeight() != null) {
            mHeight = otherSigFields.getHeight();
        } else {
            mHeight = this.getHeight();
        }

        if (otherSigFields.getSigRenderingMode() != null) {
            mSigRenderingMode = otherSigFields.getSigRenderingMode();
        } else {
            mSigRenderingMode = this.getSigRenderingMode();
        }

        if (otherSigFields.getFieldName() != null) {
            mFieldName = otherSigFields.getFieldName();
        } else {
            mFieldName = this.getFieldName();
        }

        if (otherSigFields.logoFileURL != null) {
            mLogoFileURL = otherSigFields.logoFileURL;
        } else {
            mLogoFileURL = this.logoFileURL;
        }

        if (otherSigFields.getSpecifiedTsaUrl() != null) {
            mTsaUrl = otherSigFields.getSpecifiedTsaUrl();
        } else {
            mTsaUrl = this.getSpecifiedTsaUrl();
        }

        if (otherSigFields.isSpecifiedArchiving() != null) {
            mDoLTV = otherSigFields.isSpecifiedArchiving();
        } else {
            mDoLTV = this.isSpecifiedArchiving();
        }

        if (otherSigFields.getUiMode() != null) {
            mUiMode = otherSigFields.getUiMode();
        } else {
            mUiMode = this.getUiMode();
        }

        return new PDFSignatureFields(mContact, mLocation, mReason,
                mPercentX, mPercentY, mPageNumber, mWidth, mHeight,
                mSigRenderingMode, mFieldName, mTsaUrl, mDoLTV, mLogoFileURL,
                mUiMode);
    }

    public PDFSignatureFields sanitize() {
        SignatureRenderingMode saneSigRenderingMode = this.sigRenderingMode;
        URL saneLogoFileURL = null;

        if (saneSigRenderingMode == SignatureRenderingMode.LOGO_CHOOSED_BY_USER) {
            if (this.logoFileURL != null) {
                try {
                    loadLogo(this.logoFileURL);
                    saneLogoFileURL = this.logoFileURL;
                } catch (AssinareException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                    saneSigRenderingMode = SignatureRenderingMode.PRE_DEFINED_LOGO;
                }
            } else {
                saneSigRenderingMode = SignatureRenderingMode.PRE_DEFINED_LOGO;
            }
        }

        if (saneSigRenderingMode == SignatureRenderingMode.PRE_DEFINED_LOGO) {
            saneLogoFileURL = null;
            try {
                loadDefaultLogo();
            } catch (AssinareException ex) {
                LOG.log(Level.SEVERE, null, ex);
                saneSigRenderingMode = SignatureRenderingMode.TEXT_ONLY;
            }
        }

        return new PDFSignatureFields(contact, location, reason, percentX, percentY,
                pageNumber, width, height, saneSigRenderingMode, fieldName, getSpecifiedTsaUrl(), isSpecifiedArchiving(), saneLogoFileURL, uiMode);
    }

}
