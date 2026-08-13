package com.cometfile.pdf2docx.model;

/**
 * A single structural unit on a page: a paragraph, heading, list, table or
 * image. This is the intermediate representation the whole pipeline is
 * built around - extraction/inference produce a tree of these, and
 * generation only ever reads from this interface, never from PDFBox
 * directly. Keeping this boundary firm is what lets the heuristics evolve
 * independently of the DOCX-writing code.
 */
public sealed interface Block permits ParagraphBlock, HeadingBlock, TableBlock, ImageBlock, ListBlock {
    BoundingBox boundingBox();
}
