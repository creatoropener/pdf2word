package com.cometfile.pdf2docx.model;

/** A contiguous span of text sharing one style, roughly POI's XWPFRun concept. */
public record TextRun(String text, TextStyle style) {
}
