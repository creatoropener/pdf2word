package com.cometfile.pdf2docx.inference;

import com.cometfile.pdf2docx.extraction.CharacterInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LineBuilderTest {

    private CharacterInfo charAt(String ch, float x, float y) {
        return new CharacterInfo(ch, x, y, 6f, 12f, 11f, "Helvetica", false, false, 1);
    }

    @Test
    void groupsCharactersOnSameBaselineIntoOneLine() {
        List<CharacterInfo> chars = List.of(
                charAt("H", 72, 700),
                charAt("i", 78, 700),
                charAt("B", 72, 650),
                charAt("y", 78, 650)
        );

        List<Line> lines = new LineBuilder().buildLines(chars);

        assertEquals(2, lines.size());
        assertEquals("Hi", lines.get(0).text());
        assertEquals("By", lines.get(1).text());
    }

    @Test
    void toleratesSmallBaselineJitterWithinOneLine() {
        List<CharacterInfo> chars = List.of(
                charAt("A", 72, 700.0f),
                charAt("B", 78, 700.4f),
                charAt("C", 84, 699.7f)
        );

        List<Line> lines = new LineBuilder().buildLines(chars);

        assertEquals(1, lines.size());
        assertEquals("ABC", lines.get(0).text());
    }

    @Test
    void emptyInputProducesNoLines() {
        assertEquals(0, new LineBuilder().buildLines(List.of()).size());
    }
}
