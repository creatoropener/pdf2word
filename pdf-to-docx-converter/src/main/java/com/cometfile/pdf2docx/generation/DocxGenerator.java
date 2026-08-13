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
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

public class DocxGenerator {

    public byte[] generate(DocumentModel document) throws IOException {
        try (XWPFDocument docx = new XWPFDocument()) {
            int pageIndex = 0;
            for (PageModel page : document.pages()) {
                pageIndex++;
                
                CTSectPr sectPr = docx.getDocument().getBody().addNewSectPr();
                CTPageSz pageSize = sectPr.addNewPgSz();
                
                float pdfWidth = 612.0f; 
                float pdfHeight = 792.0f;
                
                pageSize.setW(BigInteger.valueOf(Math.round(pdfWidth * 20.0f)));
                pageSize.setH(BigInteger.valueOf(Math.round(pdfHeight * 20.0f)));

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
                if (r == 0) {
                    paragraph.getRuns().forEach(x -> x.setBold(true));
                }
            }
        }
    }

    private void writeImage(XWPFDocument docx, ImageBlock block) throws IOException {
        XWPFParagraph paragraph = docx.createParagraph();
        XWPFRun run = paragraph.createRun();
        
        double widthPoints = block.boundingBox().width();
        double heightPoints = block.boundingBox().height();
        
        if (Double.isNaN(widthPoints) || widthPoints <= 0) widthPoints = 100.0;
        if (Double.isNaN(heightPoints) || heightPoints <= 0) heightPoints = 100.0;
        
        int widthEmu = Units.toEMU(widthPoints);
        int heightEmu = Units.toEMU(heightPoints);
        
        try (ByteArrayInputStream bais = new ByteArrayInputStream(block.imageBytes())) {
            int pictureType = switch (block.format().toLowerCase()) {
                case "jpg", "jpeg" -> XWPFDocument.PICTURE_TYPE_JPEG;
                default -> XWPFDocument.PICTURE_TYPE_PNG;
            };
            run.addPicture(bais, pictureType, "image." + block.format(), widthEmu, heightEmu);
        } catch (Exception e) {
            System.err.println("Universal Fix Warning: Skipped rendering a corrupted graphic element.");
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
