package com.cometfile.pdf2docx.inference;

import java.util.regex.Pattern;

/**
 * Decides what kind of line each Line most likely is, based on relative
 * font size (for headings) and leading-character patterns (for list
 * items). This runs before any grouping - grouping decides how consecutive
 * lines of the same kind get merged into a single Block.
 */
class LineClassifier {

    private static final Pattern BULLET_PATTERN = Pattern.compile("^[•◦▪‣·o\\-*]\\s+.*");
    private static final Pattern NUMBERED_PATTERN = Pattern.compile("^(\\d+[.)]|[a-zA-Z][.)])\\s+.*");

    enum Kind {HEADING, LIST_ITEM, BODY}

    record Classification(Kind kind, Integer headingLevel, boolean ordered) {
    }

    Classification classify(Line line, DocumentStyleProfile profile) {
        String text = line.text().trim();
        if (text.isEmpty()) {
            return new Classification(Kind.BODY, null, false);
        }

        float ratio = line.dominantFontSize() / profile.bodyFontSize();

        // Heading: meaningfully larger than body text, or bold and at least
        // somewhat larger. Capped at a length so long bold body sentences
        // don't get misread as headings.
        if ((ratio >= 1.15f || (line.bold() && ratio >= 1.05f)) && text.length() < 120) {
            int level = ratio >= 1.8f ? 1 : ratio >= 1.5f ? 2 : ratio >= 1.3f ? 3 : 4;
            return new Classification(Kind.HEADING, level, false);
        }

        if (BULLET_PATTERN.matcher(text).matches()) {
            return new Classification(Kind.LIST_ITEM, null, false);
        }
        if (NUMBERED_PATTERN.matcher(text).matches()) {
            return new Classification(Kind.LIST_ITEM, null, true);
        }

        return new Classification(Kind.BODY, null, false);
    }
}
