package com.cometfile.pdf2docx.extraction;

/**
 * Minimal per-character snapshot captured while PDFBox walks a page's
 * content stream. This is the raw signal the inference engine works from -
 * everything from paragraph breaks to heading detection to table columns
 * gets derived from lists of these.
 */
public record CharacterInfo(
        String unicode,
        float x,
        float y,
        float width,
        float height,
        float fontSize,
        String fontName,
        boolean bold,
        boolean italic,
        int pageNumber
) {
}
