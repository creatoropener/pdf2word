package com.cometfile.pdf2docx.api;

import com.cometfile.pdf2docx.extraction.ImageExtractor;
import com.cometfile.pdf2docx.extraction.PdfExtractor;
import com.cometfile.pdf2docx.generation.DocxGenerator;
import com.cometfile.pdf2docx.inference.StructureInferenceEngine;
import com.cometfile.pdf2docx.model.DocumentModel;
import com.cometfile.pdf2docx.model.PageModel;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Default, stateless implementation of {@link PdfToDocxConverter}. Safe to
 * hold as a singleton (e.g. a Spring {@code @Bean}) - each call to
 * {@link #convert(InputStream)} is independent and holds no mutable state
 * between invocations.
 */
public class PdfToDocxConverterImpl implements PdfToDocxConverter {

    private final PdfExtractor pdfExtractor = new PdfExtractor();
    private final ImageExtractor imageExtractor = new ImageExtractor();
    private final StructureInferenceEngine inferenceEngine = new StructureInferenceEngine();
    private final DocxGenerator docxGenerator = new DocxGenerator();

    @Override
    public byte[] convert(InputStream pdfInputStream) throws ConversionException {
        try (PDDocument document = Loader.loadPDF(pdfInputStream.readAllBytes())) {
            List<PdfExtractor.ExtractedPage> extractedPages = pdfExtractor.extract(document);

            List<PageModel> pages = new ArrayList<>();
            for (PdfExtractor.ExtractedPage extractedPage : extractedPages) {
                PDPage page = document.getPage(extractedPage.pageNumber() - 1);
                List<ImageExtractor.ExtractedImage> images = imageExtractor.extractImages(page);
                pages.add(inferenceEngine.buildPage(extractedPage, images));
            }

            // ADJUSTMENT: Assemble the document model structure
            DocumentModel documentModel = new DocumentModel(pages);

            // ADJUSTMENT: Run our custom Layout Inspector to dump the issue list to terminal
            com.cometfile.pdf2docx.cli.LayoutInspector.inspect(documentModel, System.out);

            // Continue with original generation flow
            return docxGenerator.generate(documentModel);

        } catch (IOException e) {
            throw new ConversionException("Failed to convert PDF to DOCX", e);
        }
    }
}
