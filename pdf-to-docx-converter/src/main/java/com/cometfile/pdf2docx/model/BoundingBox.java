package com.cometfile.pdf2docx.model;

/**
 * Position and size of a block on its source PDF page, in PDF points
 * (72 points per inch), top-left origin. Kept around on every block mainly
 * so blocks can be sorted into reading order and so images can be sized
 * proportionally when placed into the DOCX.
 */
public record BoundingBox(float x, float y, float width, float height) {
    public float right() {
        return x + width;
    }

    public float bottom() {
        return y + height;
    }
}
