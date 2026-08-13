package com.cometfile.pdf2docx.inference;

import com.cometfile.pdf2docx.extraction.CharacterInfo;

import java.util.List;

/**
 * A horizontal run of characters inferred to sit on the same visual line,
 * sorted left-to-right. Package-private on purpose - callers outside this
 * package only ever see finished Block objects, never lines.
 */
record Line(List<CharacterInfo> characters, float top, float bottom, float left, float right,
            float dominantFontSize, String dominantFontName, boolean bold, boolean italic) {

    String text() {
        StringBuilder sb = new StringBuilder();
        for (CharacterInfo c : characters) {
            sb.append(c.unicode());
        }
        return sb.toString();
    }
}
