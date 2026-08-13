package com.cometfile.pdf2docx.model;

import java.util.List;

public record TableBlock(List<TableRow> rows, BoundingBox boundingBox) implements Block {
    public record TableRow(List<TableCell> cells) {
    }

    public record TableCell(List<TextRun> runs, int colSpan, int rowSpan) {
    }
}
