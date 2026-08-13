package com.cometfile.pdf2docx.inference;

import com.cometfile.pdf2docx.extraction.CharacterInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Step 1 of structure inference: clusters raw characters (already sorted
 * left-to-right, top-to-bottom by PDFBox, since extraction uses
 * setSortByPosition(true)) into lines based on baseline Y proximity.
 */
class LineBuilder {

    /**
     * Characters whose baselines fall within this many points of each other
     * are treated as being on the same line. Tuned for typical body text;
     * documents with unusual line spacing may need this exposed as config
     * rather than a hard-coded constant.
     */
    private static final float Y_TOLERANCE = 2.0f;

    List<Line> buildLines(List<CharacterInfo> characters) {
        if (characters.isEmpty()) {
            return List.of();
        }

        List<List<CharacterInfo>> groups = new ArrayList<>();
        List<CharacterInfo> current = new ArrayList<>();
        float currentY = characters.get(0).y();

        for (CharacterInfo c : characters) {
            if (!current.isEmpty() && Math.abs(c.y() - currentY) > Y_TOLERANCE) {
                groups.add(current);
                current = new ArrayList<>();
            }
            current.add(c);
            currentY = c.y();
        }
        if (!current.isEmpty()) {
            groups.add(current);
        }

        List<Line> lines = new ArrayList<>();
        for (List<CharacterInfo> group : groups) {
            // Re-sort left-to-right in case minor Y jitter grouped
            // characters together slightly out of X order.
            List<CharacterInfo> sorted = group.stream()
                    .sorted(Comparator.comparingDouble(CharacterInfo::x))
                    .collect(Collectors.toList());
            lines.add(toLine(sorted));
        }
        return lines;
    }

    private Line toLine(List<CharacterInfo> chars) {
        float top = (float) chars.stream().mapToDouble(CharacterInfo::y).min().orElse(0);
        float bottom = (float) chars.stream().mapToDouble(c -> c.y() + c.height()).max().orElse(0);
        float left = (float) chars.stream().mapToDouble(CharacterInfo::x).min().orElse(0);
        float right = (float) chars.stream().mapToDouble(c -> c.x() + c.width()).max().orElse(0);

        Map<Float, Long> sizeFreq = chars.stream()
                .collect(Collectors.groupingBy(CharacterInfo::fontSize, Collectors.counting()));
        float dominantSize = sizeFreq.entrySet().stream()
                .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(0f);

        Map<String, Long> fontFreq = chars.stream()
                .collect(Collectors.groupingBy(CharacterInfo::fontName, Collectors.counting()));
        String dominantFont = fontFreq.entrySet().stream()
                .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("Unknown");

        boolean bold = chars.stream().filter(CharacterInfo::bold).count() > chars.size() / 2.0;
        boolean italic = chars.stream().filter(CharacterInfo::italic).count() > chars.size() / 2.0;

        return new Line(chars, top, bottom, left, right, dominantSize, dominantFont, bold, italic);
    }
}
