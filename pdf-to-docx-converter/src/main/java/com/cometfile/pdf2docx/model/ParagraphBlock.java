package com.cometfile.pdf2docx.model;

import java.util.List;

public record ParagraphBlock(List<TextRun> runs, BoundingBox boundingBox, Alignment alignment) implements Block {
    public enum Alignment {LEFT, CENTER, RIGHT, JUSTIFY}
}
