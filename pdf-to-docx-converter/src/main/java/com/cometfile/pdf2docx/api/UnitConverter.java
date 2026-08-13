package com.cometfile.pdf2docx.api;

public class UnitConverter {
    // 1 PDF Point = 20 DOCX Twips
    public static int pointsToTwips(float points) {
        if (Float.isNaN(points) || points <= 0) return 0;
        return Math.round(points * 20.0f);
    }

    // 1 PDF Point = 12700 EMUs (For images and shapes)
    public static long pointsToEmus(float points) {
        if (Float.isNaN(points) || points <= 0) return 0L;
        return Math.round(points * 12700.0f);
    }
}
