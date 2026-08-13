package com.cometfile.pdf2docx.cli;

import com.cometfile.pdf2docx.api.PdfToDocxConverter;
import com.cometfile.pdf2docx.api.PdfToDocxConverterImpl;

import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Minimal manual-testing entry point: run against a PDF on disk and inspect
 * the resulting DOCX before wiring the module into cometfile at all. Built
 * via the shade plugin into a runnable fat jar (see pom.xml).
 *
 * Usage: java -jar target/pdf-to-docx-converter-0.1.0-SNAPSHOT.jar input.pdf output.docx
 */
public class ConvertCli {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: ConvertCli <input.pdf> <output.docx>");
            System.exit(1);
        }

        PdfToDocxConverter converter = new PdfToDocxConverterImpl();
        try (FileInputStream in = new FileInputStream(args[0])) {
            byte[] docx = converter.convert(in);
            try (FileOutputStream out = new FileOutputStream(args[1])) {
                out.write(docx);
            }
        }
        System.out.println("Wrote " + args[1]);
    }
}
