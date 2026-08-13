package com.cometfile.pdf2docx.api;

import java.io.InputStream;

/**
 * Public entry point for the module. This is the only interface cometfile
 * (or any other caller) needs to depend on - everything under
 * extraction/, inference/ and generation/ is an implementation detail and
 * can change freely as the heuristics get tuned.
 */
public interface PdfToDocxConverter {

    /**
     * Converts a text-based PDF to a .docx file.
     *
     * @param pdfInputStream the source PDF; not closed by this method, caller owns it
     * @return the generated .docx file as bytes
     * @throws ConversionException if the PDF cannot be read or converted.
     *         Scanned/image-only PDFs are explicitly out of scope for this
     *         module - they will typically produce a mostly-empty document
     *         rather than an error, so callers should route those to a
     *         separate OCR pipeline before calling this.
     */
    byte[] convert(InputStream pdfInputStream) throws ConversionException;
}
