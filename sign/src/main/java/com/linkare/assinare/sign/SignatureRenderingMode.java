package com.linkare.assinare.sign;

import java.util.ResourceBundle;

public enum SignatureRenderingMode {

	PRE_DEFINED_LOGO("assinare.label.signatureOptions.preDefined", true, true, false),
	LOGO_CHOOSED_BY_USER("assinare.label.signatureOptions.custom", true, true, true),
	TEXT_ONLY("assinare.label.signatureOptions.text", true, false, false),
	INVISIBLE("assinare.label.signatureOptions.invisible", false, false, false);

	private static final String RESOURCE_BUNDLE = "com/linkare/assinare/resourceBundle/Language";
	
	private final String name;
	private final boolean showSignature;
	private final boolean showLogo;
	private final boolean userLogo;

	private SignatureRenderingMode(String nameKey, boolean showSignature, boolean showLogo, boolean userLogo) {
		this.name = ResourceBundle.getBundle(RESOURCE_BUNDLE).getString(nameKey);
		this.showSignature = showSignature;
		this.showLogo = showLogo;
		this.userLogo = userLogo;
	}

	@Override
	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	public boolean isShowSignature() {
		return showSignature;
	}

	public boolean isShowLogo() {
		return showLogo;
	}

	public boolean isUserLogo() {
		return userLogo;
	}
}
