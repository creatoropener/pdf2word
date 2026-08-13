package com.cometfile.pdf2docx.model;

import java.util.List;

/** @param blocks in top-to-bottom reading order for this page */
public record PageModel(int pageNumber, float pageWidth, float pageHeight, List<Block> blocks) {
}
