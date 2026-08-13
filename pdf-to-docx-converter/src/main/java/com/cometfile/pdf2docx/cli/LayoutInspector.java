package com.cometfile.pdf2docx.cli;

import com.cometfile.pdf2docx.model.*;
import java.io.PrintStream;

public class LayoutInspector {
    public static void inspect(DocumentModel document, PrintStream out) {
        out.println("====== CODESPACE STRUCTURE INFERENCE LOGS ======");
        int pageNum = 0;
        for (PageModel page : document.pages()) {
            pageNum++;
            out.printf("\n--- PAGE %d BREAKDOWN ---\n", pageNum);
            for (Block block : page.blocks()) {
                // Print the type of block mapped alongside its content preview
                switch (block) {
                    case HeadingBlock h -> 
                        out.printf("[HEADING L%d]: \"%s\"\n", h.level(), getBlockText(h.runs()));
                    case ParagraphBlock p -> 
                        out.printf("[PARAGRAPH] (Align: %s): \"%s\"\n", p.alignment(), getBlockText(p.runs()));
                    case ListBlock l -> 
                        out.printf("[LIST] (Ordered: %b, Items: %d)\n", l.ordered(), l.items().size());
                    case TableBlock t -> 
                        out.printf("[TABLE DETECTED]: Rows: %d, Cols: %d (ERROR FLAG: Paragraph accidentally split)\n", 
                                   t.rows().size(), t.rows().isEmpty() ? 0 : t.rows().get(0).cells().size());
                    case ImageBlock img -> 
                        out.printf("[IMAGE BLOCK]: Bounds [W: %.2f, H: %.2f]\n", img.boundingBox().width(), img.boundingBox().height());
                }
            }
        }
        out.println("=================================================");
    }

    private static String getBlockText(java.util.List<TextRun> runs) {
        StringBuilder sb = new StringBuilder();
        for (TextRun run : runs) sb.append(run.text());
        String text = sb.toString().trim();
        return text.length() > 60 ? text.substring(0, 57) + "..." : text;
    }
}
