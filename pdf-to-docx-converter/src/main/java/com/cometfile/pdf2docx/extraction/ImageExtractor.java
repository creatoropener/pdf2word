package com.cometfile.pdf2docx.extraction;

import com.cometfile.pdf2docx.model.BoundingBox;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Walks a page's content stream to find drawn images (the "Do" operator)
 * and records both the raw image bytes and the bounding box it was drawn
 * into, derived from the current transformation matrix (CTM) at draw time.
 *
 * PDPage.getResources() alone only tells you which image XObjects *exist*
 * on a page, not where they were placed - position only shows up by
 * tracking the CTM as the content stream is interpreted, which is why this
 * extends PDFStreamEngine rather than just iterating resources directly.
 */
public class ImageExtractor extends PDFStreamEngine {

    public record ExtractedImage(byte[] bytes, String format, BoundingBox boundingBox) {
    }

    private final List<ExtractedImage> images = new ArrayList<>();

    public List<ExtractedImage> extractImages(PDPage page) throws IOException {
        images.clear();
        processPage(page);
        return List.copyOf(images);
    }

    @Override
    protected void processOperator(Operator operator, List<COSBase> operands) throws IOException {
        if ("Do".equals(operator.getName()) && !operands.isEmpty() && operands.get(0) instanceof COSName name) {
            PDXObject xobject = getResources().getXObject(name);

            if (xobject instanceof PDImageXObject imageXObject) {
                captureImage(imageXObject);
            } else if (xobject instanceof PDFormXObject formXObject) {
                // Some PDFs wrap images inside form XObjects - recurse so
                // those aren't silently dropped.
                showForm(formXObject);
            }
        } else {
            super.processOperator(operator, operands);
        }
    }

    private void captureImage(PDImageXObject imageXObject) throws IOException {
        Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
        // The unit square [0,1]x[0,1] mapped through the CTM gives the
        // image's placed position and size in page (user) space.
        float x = ctm.getTranslateX();
        float y = ctm.getTranslateY();
        float width = (float) Math.hypot(ctm.getScaleX(), ctm.getShearY());
        float height = (float) Math.hypot(ctm.getShearX(), ctm.getScaleY());

        BufferedImage bufferedImage = imageXObject.getImage();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String format = "png";
        ImageIO.write(bufferedImage, format, baos);

        images.add(new ExtractedImage(baos.toByteArray(), format, new BoundingBox(x, y, width, height)));
    }
}
