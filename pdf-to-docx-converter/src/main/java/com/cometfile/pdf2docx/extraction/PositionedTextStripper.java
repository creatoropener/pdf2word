package com.cometfile.pdf2docx.extraction;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Extends PDFBox's PDFTextStripper to record the position, font and size of
 * every character on every page, instead of just the flattened text that
 * the default stripper produces. This positional data is what lets the
 * inference engine later reconstruct paragraphs, headings and tables -
 * without it we'd only have a text blob with no layout information.
 */
public class PositionedTextStripper extends PDFTextStripper {

    private final Map<Integer, List<CharacterInfo>> charsByPage = new LinkedHashMap<>();
    private int currentPage = 0;

    public PositionedTextStripper() throws IOException {
        super();
        // Left-to-right, top-to-bottom order per line makes the line
        // clustering step downstream much simpler.
        setSortByPosition(true);
    }

    @Override
    protected void startPage(PDPage page) throws IOException {
        currentPage++;
        charsByPage.putIfAbsent(currentPage, new ArrayList<>());
        super.startPage(page);
    }

    @Override
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
        List<CharacterInfo> pageChars = charsByPage.computeIfAbsent(currentPage, k -> new ArrayList<>());
        for (TextPosition tp : textPositions) {
            String fontName = tp.getFont() != null ? tp.getFont().getName() : "Unknown";
            String lowerName = fontName == null ? "" : fontName.toLowerCase(Locale.ROOT);
            boolean bold = lowerName.contains("bold");
            boolean italic = lowerName.contains("italic") || lowerName.contains("oblique");

            pageChars.add(new CharacterInfo(
                    tp.getUnicode(),
                    tp.getXDirAdj(),
                    tp.getYDirAdj(),
                    tp.getWidthDirAdj(),
                    tp.getHeightDir(),
                    tp.getFontSizeInPt(),
                    fontName,
                    bold,
                    italic,
                    currentPage
            ));
        }
    }

    public Map<Integer, List<CharacterInfo>> getCharsByPage() {
        return charsByPage;
    }
}
