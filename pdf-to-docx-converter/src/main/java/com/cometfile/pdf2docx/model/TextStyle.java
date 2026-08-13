package com.cometfile.pdf2docx.model;

/**
 * Font characteristics for a run of text. Deliberately minimal - font
 * family/size/bold/italic is enough to reproduce visual structure in the
 * DOCX; exact PDF font matching (embedded font substitution, kerning, etc.)
 * is out of scope.
 */
public record TextStyle(String fontFamily, float fontSize, boolean bold, boolean italic) {
    public static TextStyle plain(String fontFamily, float fontSize) {
        return new TextStyle(fontFamily, fontSize, false, false);
    }
}
