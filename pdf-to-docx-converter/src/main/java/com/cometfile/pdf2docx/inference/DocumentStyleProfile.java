package com.cometfile.pdf2docx.inference;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Cheap document-wide statistics used as a baseline for heuristics - mainly
 * "what font size counts as normal body text in this document", since
 * heading detection is relative to that rather than an absolute point size
 * (an 11pt-body document and a 9pt-body document should both still detect
 * their headings correctly).
 */
record DocumentStyleProfile(float bodyFontSize) {

    static DocumentStyleProfile compute(List<Line> allLines) {
        Map<Float, Long> sizeByCharCount = allLines.stream()
                .collect(Collectors.groupingBy(Line::dominantFontSize,
                        Collectors.summingLong(l -> (long) l.characters().size())));

        float body = sizeByCharCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(11f);

        return new DocumentStyleProfile(body);
    }
}
