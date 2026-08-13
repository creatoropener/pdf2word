package com.cometfile.pdf2docx.model;

import java.util.List;

/** @param level 1-6, mapped directly onto Word's built-in Heading1..Heading6 paragraph styles */
public record HeadingBlock(List<TextRun> runs, BoundingBox boundingBox, int level) implements Block {
}
