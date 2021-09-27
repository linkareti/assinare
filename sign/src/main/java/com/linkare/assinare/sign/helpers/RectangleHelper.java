package com.linkare.assinare.sign.helpers;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * A helper class to deal with rotated PDF pages and respective boxes, in particular
 * the CropBox.
 * @author bnazare
 */
public class RectangleHelper {
    
    /**
     * Get the size of the CropBox of the given page, ajusted according to page rotation.
     * Rotation is applied in 90 degrees increments, only.
     * <br>
     * NOTE: has NOT been tested with pages with asymmetric crop margins, meaning:
     * left != right or top != bottom
     * @param page the page to extract the CropBox from
     * @return a Rectangle representing the rotated CropBox
     */
    public static PDRectangle getRotatedCropBox(final PDPage page) {
        PDRectangle cropBox = page.getCropBox();
        int rotations =  get90Rotations(page.getRotation());
        
        for (int i = 0; i < rotations; i++) {
            cropBox = rotate90(cropBox);
        }
        
        return cropBox;
    }
    
    /**
     * Calculates the number of 90 degrees rotations that fit in a larger rotation,
     * rounded down to an integer number. The value will be normalized to the interval
     * [0-3].
     * @param rotation the larger rotation angle
     * @return the number of 90 degrees rotations
     */
    public static int get90Rotations(final int rotation) {
        return (rotation / 90) % 4;
    }
    
    /**
     * Rotates a PDRectangle 90 degrees.
     * @param rect the PDRectangle to rotate
     * @return a new rotated PDRectangle
     */
    public static PDRectangle rotate90(final PDRectangle rect) {
        return new PDRectangle(rect.getLowerLeftY(), rect.getLowerLeftX(), rect.getHeight(), rect.getWidth());
    }
    
}
