package com.cometfile.pdf2docx.extraction;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Entry point for the extraction phase: turns a PDDocument into a per-page
 * list of positioned characters plus page geometry. This is the only class
 * that drives PDFBox's low-level text-position APIs directly - everything
 * downstream works off ExtractedPage / CharacterInfo.
 */
public class PdfExtractor {

    public record ExtractedPage(int pageNumber, float width, float height, List<CharacterInfo> characters) {
    }

    public List<ExtractedPage> extract(PDDocument document) throws IOException {
        PositionedTextStripper stripper = new PositionedTextStripper();
        stripper.setStartPage(1);
        stripper.setEndPage(document.getNumberOfPages());
        // Triggers the page-by-page walk; the returned flattened string is
        // discarded - we only care about the per-character side effects
        // captured by writeString() above.
        stripper.getText(document);

        Map<Integer, List<CharacterInfo>> byPage = stripper.getCharsByPage();
        List<ExtractedPage> pages = new ArrayList<>();
        for (int i = 1; i <= document.getNumberOfPages(); i++) {
            PDPage page = document.getPage(i - 1);
            float width = page.getMediaBox().getWidth();
            float height = page.getMediaBox().getHeight();
            pages.add(new ExtractedPage(i, width, height, byPage.getOrDefault(i, List.of())));
        }
        return pages;
    }
}
