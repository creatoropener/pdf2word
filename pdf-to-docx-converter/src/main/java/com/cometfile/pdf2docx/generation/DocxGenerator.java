package com.cometfile.pdf2docx.generation;

import com.cometfile.pdf2docx.model.Block;
import com.cometfile.pdf2docx.model.DocumentModel;
import com.cometfile.pdf2docx.model.HeadingBlock;
import com.cometfile.pdf2docx.model.ImageBlock;
import com.cometfile.pdf2docx.model.ListBlock;
import com.cometfile.pdf2docx.model.PageModel;
import com.cometfile.pdf2docx.model.ParagraphBlock;
import com.cometfile.pdf2docx.model.TableBlock;
import com.cometfile.pdf2docx.model.TextRun;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.TableRowAlign;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Walks a DocumentModel and produces a .docx byte array via Apache POI.
 * This class only knows about the intermediate model - it has no idea
 * PDFBox or any particular inference heuristic exists, which is what keeps
 * it swappable and testable independently of the extraction and inference
 * layers.
 */
public class DocxGenerator {

    public byte[] generate(DocumentModel document) throws IOException {
        try (XWPFDocument docx = new XWPFDocument()) {
            int pageIndex = 0;
            for (PageModel page : document.pages()) {
                pageIndex++;
                for (Block block : page.blocks()) {
                    writeBlock(docx, block);
                }
                if (pageIndex < document.pages().size()) {
                    XWPFParagraph pageBreak = docx.createParagraph();
                    pageBreak.setPageBreak(true);
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            docx.write(out);
            return out.toByteArray();
        }
    }

    private void writeBlock(XWPFDocument docx, Block block) throws IOException {
        switch (block) {
            case HeadingBlock h -> writeHeading(docx, h);
            case ParagraphBlock p -> writeParagraph(docx, p);
            case ListBlock l -> writeList(docx, l);
            case TableBlock t -> writeTable(docx, t);
            case ImageBlock img -> writeImage(docx, img);
        }
    }

    private void writeHeading(XWPFDocument docx, HeadingBlock block) {
        XWPFParagraph paragraph = docx.createParagraph();
        paragraph.setStyle("Heading" + Math.min(Math.max(block.level(), 1), 6));
        for (TextRun run : block.runs()) {
            applyRun(paragraph.createRun(), run);
        }
    }

    private void writeParagraph(XWPFDocument docx, ParagraphBlock block) {
        XWPFParagraph paragraph = docx.createParagraph();
        paragraph.setAlignment(switch (block.alignment()) {
            case LEFT -> ParagraphAlignment.LEFT;
            case CENTER -> ParagraphAlignment.CENTER;
            case RIGHT -> ParagraphAlignment.RIGHT;
            case JUSTIFY -> ParagraphAlignment.BOTH;
        });
        for (TextRun run : block.runs()) {
            applyRun(paragraph.createRun(), run);
        }
    }

    private void writeList(XWPFDocument docx, ListBlock block) {
        // Uses simple bullet/number text prefixes rather than a full
        // numbering.xml definition - keeps generation dependency-free and
        // visually equivalent. Swap for docx.createNumbering() list styles
        // later if native Word list semantics (auto-renumbering, etc.) turn
        // out to matter for cometfile's use case.
        int index = 1;
        for (ListBlock.ListItem item : block.items()) {
            XWPFParagraph paragraph = docx.createParagraph();
            paragraph.setIndentationLeft(360 * (item.indentLevel() + 1));
            XWPFRun prefixRun = paragraph.createRun();
            prefixRun.setText(block.ordered() ? (index++ + ". ") : "\u2022 ");
            for (TextRun run : item.runs()) {
                applyRun(paragraph.createRun(), run);
            }
        }
    }

    private void writeTable(XWPFDocument docx, TableBlock block) {
        if (block.rows().isEmpty()) {
            return;
        }
        int colCount = block.rows().get(0).cells().size();
        if (colCount == 0) {
            return;
        }
        XWPFTable table = docx.createTable(block.rows().size(), colCount);
        table.setTableAlignment(TableRowAlign.LEFT);

        for (int r = 0; r < block.rows().size(); r++) {
            TableBlock.TableRow row = block.rows().get(r);
            XWPFTableRow xrow = table.getRow(r);
            for (int c = 0; c < row.cells().size() && c < colCount; c++) {
                TableBlock.TableCell cell = row.cells().get(c);
                XWPFTableCell xcell = xrow.getCell(c);
                xcell.removeParagraph(0);
                XWPFParagraph paragraph = xcell.addParagraph();
                for (TextRun run : cell.runs()) {
                    applyRun(paragraph.createRun(), run);
                }
                // First row rendered bold as a header - a reasonable default
                // since most detected tables lead with a header row. Revisit
                // if TableInference starts distinguishing header rows
                // explicitly rather than assuming row 0.
                if (r == 0) {
                    paragraph.getRuns().forEach(x -> x.setBold(true));
                }
            }
        }
    }

    private void writeImage(XWPFDocument docx, ImageBlock block) throws IOException {
        XWPFParagraph paragraph = docx.createParagraph();
        XWPFRun run = paragraph.createRun();
        int widthEmu = Units.toEMU(block.boundingBox().width());
        int heightEmu = Units.toEMU(block.boundingBox().height());
        try (ByteArrayInputStream bais = new ByteArrayInputStream(block.imageBytes())) {
            int pictureType = switch (block.format().toLowerCase()) {
                case "jpg", "jpeg" -> XWPFDocument.PICTURE_TYPE_JPEG;
                default -> XWPFDocument.PICTURE_TYPE_PNG;
            };
            run.addPicture(bais, pictureType, "image." + block.format(), widthEmu, heightEmu);
        } catch (InvalidFormatException e) {
            throw new IOException("Failed to embed image in DOCX", e);
        }
    }

    private void applyRun(XWPFRun xrun, TextRun run) {
        xrun.setText(run.text());
        xrun.setFontFamily(run.style().fontFamily());
        xrun.setFontSize((int) run.style().fontSize());
        xrun.setBold(run.style().bold());
        xrun.setItalic(run.style().italic());
    }
}
