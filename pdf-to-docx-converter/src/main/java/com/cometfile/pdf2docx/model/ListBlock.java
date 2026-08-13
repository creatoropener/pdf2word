package com.cometfile.pdf2docx.model;

import java.util.List;

public record ListBlock(List<ListItem> items, BoundingBox boundingBox, boolean ordered) implements Block {
    public record ListItem(List<TextRun> runs, int indentLevel) {
    }
}
