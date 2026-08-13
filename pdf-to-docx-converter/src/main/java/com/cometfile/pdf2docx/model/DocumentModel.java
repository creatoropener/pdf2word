package com.cometfile.pdf2docx.model;

import java.util.List;

/**
 * The complete intermediate representation of a converted document.
 * Extraction + inference produce this; generation consumes it. Nothing
 * downstream of this class knows PDFBox or POI exist on the extraction
 * side - only DocxGenerator touches POI, and only for writing.
 */
public record DocumentModel(List<PageModel> pages) {
}
