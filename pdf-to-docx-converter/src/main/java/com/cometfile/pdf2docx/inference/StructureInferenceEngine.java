package com.cometfile.pdf2docx.inference;

import com.cometfile.pdf2docx.extraction.ImageExtractor;
import com.cometfile.pdf2docx.extraction.PdfExtractor;
import com.cometfile.pdf2docx.model.BoundingBox;
import com.cometfile.pdf2docx.model.Block;
import com.cometfile.pdf2docx.model.HeadingBlock;
import com.cometfile.pdf2docx.model.ImageBlock;
import com.cometfile.pdf2docx.model.ListBlock;
import com.cometfile.pdf2docx.model.PageModel;
import com.cometfile.pdf2docx.model.ParagraphBlock;
import com.cometfile.pdf2docx.model.TextRun;
import com.cometfile.pdf2docx.model.TextStyle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Orchestrates the full per-page inference pipeline:
 * <pre>
 *   characters -&gt; lines -&gt; (table detection) -&gt; (paragraph/heading/list grouping) -&gt; Block list
 * </pre>
 * then merges in images at their approximate vertical position.
 *
 * This is the class most likely to need tuning as you run it against real
 * documents. The sub-steps (LineBuilder, LineClassifier, TableInference)
 * are kept separate and independently unit-testable specifically so you
 * can iterate on one without destabilizing the others.
 */
public class StructureInferenceEngine {

    private final LineBuilder lineBuilder = new LineBuilder();
    private final LineClassifier lineClassifier = new LineClassifier();
    private final TableInference tableInference = new TableInference();

    /**
     * Consecutive lines whose vertical gap exceeds the previous line's font
     * size times (this factor - 1) start a new paragraph rather than
     * continuing the current one.
     */
    private static final float PARAGRAPH_GAP_FACTOR = 1.6f;

    public PageModel buildPage(PdfExtractor.ExtractedPage extractedPage,
                                List<ImageExtractor.ExtractedImage> images) {

        List<Line> lines = lineBuilder.buildLines(extractedPage.characters());
        DocumentStyleProfile profile = DocumentStyleProfile.compute(lines);

        List<LineClassifier.Classification> classifications = new ArrayList<>();
        for (Line line : lines) {
            classifications.add(lineClassifier.classify(line, profile));
        }

        List<Block> blocks = new ArrayList<>();
        int i = 0;
        while (i < lines.size()) {
            LineClassifier.Kind kind = classifications.get(i).kind();

            if (kind == LineClassifier.Kind.BODY) {
                int bodyRunEnd = i;
                while (bodyRunEnd < lines.size() && classifications.get(bodyRunEnd).kind() == LineClassifier.Kind.BODY) {
                    bodyRunEnd++;
                }
                blocks.addAll(processBodyRun(lines.subList(i, bodyRunEnd)));
                i = bodyRunEnd;

            } else if (kind == LineClassifier.Kind.HEADING) {
                int headingRunEnd = i;
                while (headingRunEnd < lines.size() && classifications.get(headingRunEnd).kind() == LineClassifier.Kind.HEADING) {
                    headingRunEnd++;
                }
                blocks.add(groupHeading(lines.subList(i, headingRunEnd), classifications.get(i).headingLevel()));
                i = headingRunEnd;

            } else {
                int listRunEnd = i;
                while (listRunEnd < lines.size() && classifications.get(listRunEnd).kind() == LineClassifier.Kind.LIST_ITEM) {
                    listRunEnd++;
                }
                blocks.add(groupList(lines.subList(i, listRunEnd), classifications.subList(i, listRunEnd)));
                i = listRunEnd;
            }
        }

        blocks.addAll(toImageBlocks(images));
        blocks.sort(Comparator.comparingDouble(b -> b.boundingBox().y()));

        return new PageModel(extractedPage.pageNumber(), extractedPage.width(), extractedPage.height(), blocks);
    }

    private List<Block> processBodyRun(List<Line> bodyRun) {
        List<Block> blocks = new ArrayList<>();
        List<TableInference.TableRegion> tables = tableInference.findTables(bodyRun);

        if (tables.isEmpty()) {
            blocks.addAll(groupParagraphs(bodyRun));
            return blocks;
        }

        int cursor = 0;
        for (TableInference.TableRegion region : tables) {
            if (region.startLine() > cursor) {
                blocks.addAll(groupParagraphs(bodyRun.subList(cursor, region.startLine())));
            }
            blocks.add(region.table());
            cursor = region.endLineExclusive();
        }
        if (cursor < bodyRun.size()) {
            blocks.addAll(groupParagraphs(bodyRun.subList(cursor, bodyRun.size())));
        }
        return blocks;
    }

    private List<Block> groupParagraphs(List<Line> bodyLines) {
        List<Block> paragraphs = new ArrayList<>();
        List<Line> current = new ArrayList<>();

        for (Line line : bodyLines) {
            if (!current.isEmpty()) {
                Line prev = current.get(current.size() - 1);
                float gap = line.top() - prev.bottom();
                float threshold = prev.dominantFontSize() * (PARAGRAPH_GAP_FACTOR - 1f);
                if (gap > threshold) {
                    paragraphs.add(toParagraphBlock(current));
                    current = new ArrayList<>();
                }
            }
            current.add(line);
        }
        if (!current.isEmpty()) {
            paragraphs.add(toParagraphBlock(current));
        }
        return paragraphs;
    }

    private ParagraphBlock toParagraphBlock(List<Line> lines) {
        List<TextRun> runs = new ArrayList<>();
        for (int idx = 0; idx < lines.size(); idx++) {
            Line line = lines.get(idx);
            TextStyle style = new TextStyle(line.dominantFontName(), line.dominantFontSize(), line.bold(), line.italic());
            // Join wrapped lines back into flowing sentences with a space,
            // rather than hard line breaks, to match how Word paragraphs
            // reflow.
            String text = idx < lines.size() - 1 ? line.text() + " " : line.text();
            runs.add(new TextRun(text, style));
        }
        return new ParagraphBlock(runs, boundingBoxOf(lines), ParagraphBlock.Alignment.LEFT);
    }

    private HeadingBlock groupHeading(List<Line> lines, int level) {
        List<TextRun> runs = new ArrayList<>();
        for (Line line : lines) {
            TextStyle style = new TextStyle(line.dominantFontName(), line.dominantFontSize(), true, line.italic());
            runs.add(new TextRun(line.text(), style));
        }
        return new HeadingBlock(runs, boundingBoxOf(lines), level);
    }

    private ListBlock groupList(List<Line> lines, List<LineClassifier.Classification> classifications) {
        List<ListBlock.ListItem> items = new ArrayList<>();
        boolean ordered = classifications.get(0).ordered();
        for (Line line : lines) {
            TextStyle style = new TextStyle(line.dominantFontName(), line.dominantFontSize(), line.bold(), line.italic());
            String text = line.text()
                    .replaceFirst("^[•◦▪‣·o\\-*]\\s+", "")
                    .replaceFirst("^(\\d+[.)]|[a-zA-Z][.)])\\s+", "");
            items.add(new ListBlock.ListItem(List.of(new TextRun(text, style)), 0));
        }
        return new ListBlock(items, boundingBoxOf(lines), ordered);
    }

    private BoundingBox boundingBoxOf(List<Line> lines) {
        float top = lines.get(0).top();
        float bottom = lines.get(lines.size() - 1).bottom();
        float left = (float) lines.stream().mapToDouble(Line::left).min().orElse(0);
        float right = (float) lines.stream().mapToDouble(Line::right).max().orElse(0);
        return new BoundingBox(left, top, right - left, bottom - top);
    }

    private List<ImageBlock> toImageBlocks(List<ImageExtractor.ExtractedImage> images) {
        List<ImageBlock> result = new ArrayList<>();
        for (ImageExtractor.ExtractedImage img : images) {
            result.add(new ImageBlock(img.bytes(), img.format(), img.boundingBox()));
        }
        return result;
    }
}
