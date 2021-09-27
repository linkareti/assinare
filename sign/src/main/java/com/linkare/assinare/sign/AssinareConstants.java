package com.linkare.assinare.sign;

import java.awt.Color;
import java.text.DateFormat;

/**
 *
 * @author rvaz
 */
public final class AssinareConstants {

    public static final int SIGNATURE_DEFAULT_WIDTH = 149;
    public static final int SIGNATURE_DEFAULT_HEIGHT = 58;
    
    public static final double SIGNATURE_DEFAULT_X_PCT = 0.5;
    public static final double SIGNATURE_DEFAULT_Y_PCT = 0.95;

    public static final int PDF_DEFAULT_PAGE = 1;

    public static final Color VALID_EXISTING_SIGNATURES_COLOR = new Color(83, 169, 63, 220);
    public static final Color INVALID_EXISTING_SIGNATURES_COLOR = new Color(200, 0, 0, 220);
    public static final DateFormat DEFAULT_DATE_FORMATTER = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);

    public static final String SIGNATURE_NAME = "Signature-";

    public static final String SIGNATURE_LTV_NAME = "SignatureLTV-";

    public static final String NOT_AVAILABLE = "N/A";
    
    
    public static final String SUCCESS_CODE = "0";
    public static final String BATCH_END_CODE = "1";
    
    public static final String ASSINARE_INIT_ERROR_CODE = "2";
    public static final String SIGNATURE_BATCH_ERROR_CODE = "3";
    public static final String SIGNATURE_ERROR_CODE = "4";
    public static final String SIGNATURE_DOC_GET_HTTP_ERROR_CODE = "5";
    public static final String SIGNATURE_DOC_PUT_HTTP_ERROR_CODE = "6";
    public static final String SIGNATURE_DOC_TSA_ERROR_CODE = "7";
        
    public static final String REASON_PREFIX_DEMO_RELEASE_PT = "Produzido com uma versão de demonstração do Assinare - mais informação em http://www.assinare.eu - ";
    public static final String REASON_PREFIX_DEMO_RELEASE_EN = "Produced with a demo version of Assinare - more information at http://www.assinare.eu - ";
    
    public static final String TS_CARTAODECIDADAO_URL = "http://ts.cartaodecidadao.pt/tsa/server";

    private AssinareConstants() {
    }
}
