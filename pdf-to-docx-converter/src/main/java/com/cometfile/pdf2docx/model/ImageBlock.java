package com.cometfile.pdf2docx.model;

/** @param format lowercase image format, e.g. "png" or "jpeg", used to pick the right POI picture-type constant */
public record ImageBlock(byte[] imageBytes, String format, BoundingBox boundingBox) implements Block {
}
