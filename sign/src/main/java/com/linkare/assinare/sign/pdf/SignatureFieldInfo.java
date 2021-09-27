package com.linkare.assinare.sign.pdf;

import java.util.Objects;

/**
 *
 * @author bnazare
 */
public class SignatureFieldInfo {

    private final String fieldName;
    private final int revision;
    private final int page;
    private final float percentX;
    private final float percentY;
    private final double percentWidth;
    private final double percentHeight;

    public SignatureFieldInfo(String fieldName, int revision, int page, float percentX, float percentY, double percentWidth, double percentHeight) {
        this.fieldName = fieldName;
        this.revision = revision;
        this.page = page;
        this.percentX = percentX;
        this.percentY = percentY;
        this.percentWidth = percentWidth;
        this.percentHeight = percentHeight;
    }

    public double getPercentWidth() {
        return percentWidth;
    }

    public double getPercentHeight() {
        return percentHeight;
    }

    public float getPercentX() {
        return percentX;
    }

    public float getPercentY() {
        return percentY;
    }

    public int getRevision() {
        return revision;
    }

    public int getPage() {
        return page;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.fieldName);
        hash = 29 * hash + this.revision;
        hash = 29 * hash + this.page;
        hash = 29 * hash + Float.floatToIntBits(this.percentX);
        hash = 29 * hash + Float.floatToIntBits(this.percentY);
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.percentWidth) ^ (Double.doubleToLongBits(this.percentWidth) >>> 32));
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.percentHeight) ^ (Double.doubleToLongBits(this.percentHeight) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SignatureFieldInfo other = (SignatureFieldInfo) obj;
        if (this.revision != other.revision) {
            return false;
        }
        if (this.page != other.page) {
            return false;
        }
        if (Float.floatToIntBits(this.percentX) != Float.floatToIntBits(other.percentX)) {
            return false;
        }
        if (Float.floatToIntBits(this.percentY) != Float.floatToIntBits(other.percentY)) {
            return false;
        }
        if (Double.doubleToLongBits(this.percentWidth) != Double.doubleToLongBits(other.percentWidth)) {
            return false;
        }
        if (Double.doubleToLongBits(this.percentHeight) != Double.doubleToLongBits(other.percentHeight)) {
            return false;
        }
        if (!Objects.equals(this.fieldName, other.fieldName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SignatureFieldInfo{" + "fieldName=" + fieldName + ", revision=" + revision + ", page=" + page + ", percentX=" + percentX + ", percentY=" + percentY + ", percentWidth=" + percentWidth + ", percentHeight=" + percentHeight + '}';
    }

}
