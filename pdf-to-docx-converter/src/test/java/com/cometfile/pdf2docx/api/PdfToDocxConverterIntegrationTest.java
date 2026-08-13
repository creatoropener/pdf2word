package com.cometfile.pdf2docx.api;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end smoke test: builds a small synthetic PDF in memory (so the
 * test has no external file dependencies), converts it, and checks the
 * resulting DOCX has the expected shape.
 *
 * This is deliberately NOT a replacement for the golden-file regression
 * corpus described in src/test/resources/golden-corpus/README.md - it just
 * proves the extraction -&gt; inference -&gt; generation pipeline is wired
 * together correctly end to end on a minimal, controlled input.
 */
class PdfToDocxConverterIntegrationTest {

    @Test
    void convertsHeadingAndParagraphToDocx() throws Exception {
        byte[] pdfBytes = buildSamplePdf();
        PdfToDocxConverter converter = new PdfToDocxConverterImpl();

        byte[] docxBytes = converter.convert(new ByteArrayInputStream(pdfBytes));

        assertNotNull(docxBytes);
        assertTrue(docxBytes.length > 0);

        try (XWPFDocument docx = new XWPFDocument(new ByteArrayInputStream(docxBytes))) {
            List<XWPFParagraph> paragraphs = docx.getParagraphs();
            assertFalse(paragraphs.isEmpty(), "Expected at least one paragraph in generated DOCX");

            boolean hasHeadingStyle = paragraphs.stream()
                    .anyMatch(p -> p.getStyle() != null && p.getStyle().startsWith("Heading"));
            assertTrue(hasHeadingStyle, "Expected the large-font line to be detected as a heading");

            String allText = String.join(" ", paragraphs.stream().map(XWPFParagraph::getText).toList());
            assertTrue(allText.contains("Quarterly Report"));
            assertTrue(allText.contains("This is a body paragraph"));
        }
    }

    private byte[] buildSamplePdf() throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font bodyFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

                stream.beginText();
                stream.setFont(boldFont, 24);
                stream.newLineAtOffset(72, 700);
                stream.showText("Quarterly Report");
                stream.endText();

                stream.beginText();
                stream.setFont(bodyFont, 11);
                stream.newLineAtOffset(72, 650);
                stream.showText("This is a body paragraph explaining the quarterly results in detail.");
                stream.endText();
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }
}
