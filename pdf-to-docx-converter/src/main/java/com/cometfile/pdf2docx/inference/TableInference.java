package com.cometfile.pdf2docx.inference;

import com.cometfile.pdf2docx.extraction.CharacterInfo;
import com.cometfile.pdf2docx.model.BoundingBox;
import com.cometfile.pdf2docx.model.TableBlock;
import com.cometfile.pdf2docx.model.TextRun;
import com.cometfile.pdf2docx.model.TextStyle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Detects table regions within a run of body lines by looking for X
 * coordinates that recur as "column starts" across several consecutive
 * lines - i.e. text lining up vertically the way it does in a grid.
 *
 * This is by far the weakest heuristic in the pipeline - it works
 * reasonably for simple, evenly-spaced, ruled or semi-ruled tables, and
 * will miss or misfire on borderless tables with irregular spacing, or
 * tables that mix single-line and wrapped multi-line cells. Treat this as
 * a first pass: expect to spend real iteration time here against a
 * golden-file corpus of real tables (see src/test/resources/golden-corpus)
 * before trusting it in production.
 */
class TableInference {

    private static final float COLUMN_X_TOLERANCE = 4.0f;
    private static final int MIN_ROWS_FOR_TABLE = 3;
    private static final int MIN_COLUMNS_FOR_TABLE = 2;

    record TableRegion(int startLine, int endLineExclusive, TableBlock table) {
    }

    /**
     * Scans a contiguous list of BODY-classified lines and returns any
     * sub-ranges that look like tables. Lines not covered by a returned
     * region should fall through to normal paragraph grouping.
     */
    List<TableRegion> findTables(List<Line> bodyLines) {
        List<TableRegion> regions = new ArrayList<>();
        int i = 0;
        while (i < bodyLines.size()) {
            int runEnd = extendTableRun(bodyLines, i);
            if (runEnd - i >= MIN_ROWS_FOR_TABLE) {
                List<Line> tableLines = bodyLines.subList(i, runEnd);
                List<Float> columnStarts = findColumnStarts(tableLines);
                if (columnStarts.size() >= MIN_COLUMNS_FOR_TABLE) {
                    TableBlock block = buildTableBlock(tableLines, columnStarts);
                    regions.add(new TableRegion(i, runEnd, block));
                    i = runEnd;
                    continue;
                }
            }
            i++;
        }
        return regions;
    }

    private int extendTableRun(List<Line> lines, int start) {
        // A candidate table run is a maximal sequence of lines that share
        // at least MIN_COLUMNS_FOR_TABLE recurring X positions with the
        // first line in the run.
        List<Float> baseline = wordStartXPositions(lines.get(start));
        int end = start + 1;
        while (end < lines.size()) {
            List<Float> next = wordStartXPositions(lines.get(end));
            if (sharedPositionCount(baseline, next) < MIN_COLUMNS_FOR_TABLE) {
                break;
            }
            end++;
        }
        return end;
    }

    private List<Float> wordStartXPositions(Line line) {
        // Approximates "column starts" as the X of the first character of
        // each whitespace-delimited word on the line.
        List<Float> positions = new ArrayList<>();
        boolean atWordStart = true;
        for (CharacterInfo c : line.characters()) {
            boolean isSpace = c.unicode() == null || c.unicode().isBlank();
            if (!isSpace && atWordStart) {
                positions.add(c.x());
                atWordStart = false;
            } else if (isSpace) {
                atWordStart = true;
            }
        }
        return positions;
    }

    private int sharedPositionCount(List<Float> a, List<Float> b) {
        int shared = 0;
        for (float pa : a) {
            for (float pb : b) {
                if (Math.abs(pa - pb) <= COLUMN_X_TOLERANCE) {
                    shared++;
                    break;
                }
            }
        }
        return shared;
    }

    private List<Float> findColumnStarts(List<Line> tableLines) {
        // Candidate column starts = every distinct word-start X observed;
        // confirmed column starts = the subset that recurs across a
        // majority of the rows in this run.
        Map<Float, Boolean> candidates = new TreeMap<>();
        for (Line line : tableLines) {
            for (float x : wordStartXPositions(line)) {
                boolean matchesExisting = candidates.keySet().stream()
                        .anyMatch(existing -> Math.abs(existing - x) <= COLUMN_X_TOLERANCE);
                if (!matchesExisting) {
                    candidates.put(x, true);
                }
            }
        }

        List<Float> confirmed = new ArrayList<>();
        for (float x : candidates.keySet()) {
            long rowsWithColumn = tableLines.stream()
                    .filter(l -> wordStartXPositions(l).stream().anyMatch(px -> Math.abs(px - x) <= COLUMN_X_TOLERANCE))
                    .count();
            if (rowsWithColumn >= Math.max(2, tableLines.size() / 2)) {
                confirmed.add(x);
            }
        }
        Collections.sort(confirmed);
        return confirmed;
    }

    private TableBlock buildTableBlock(List<Line> tableLines, List<Float> columnStarts) {
        List<TableBlock.TableRow> rows = new ArrayList<>();
        for (Line line : tableLines) {
            StringBuilder[] cellText = new StringBuilder[columnStarts.size()];
            for (int c = 0; c < cellText.length; c++) {
                cellText[c] = new StringBuilder();
            }
            for (CharacterInfo c : line.characters()) {
                int col = columnIndexFor(c.x(), columnStarts);
                cellText[col].append(c.unicode());
            }

            List<TableBlock.TableCell> cells = new ArrayList<>();
            TextStyle style = TextStyle.plain(line.dominantFontName(), line.dominantFontSize());
            for (StringBuilder text : cellText) {
                cells.add(new TableBlock.TableCell(List.of(new TextRun(text.toString().trim(), style)), 1, 1));
            }
            rows.add(new TableBlock.TableRow(cells));
        }

        float top = tableLines.get(0).top();
        float bottom = tableLines.get(tableLines.size() - 1).bottom();
        float left = (float) tableLines.stream().mapToDouble(Line::left).min().orElse(0);
        float right = (float) tableLines.stream().mapToDouble(Line::right).max().orElse(0);

        return new TableBlock(rows, new BoundingBox(left, top, right - left, bottom - top));
    }

    private int columnIndexFor(float x, List<Float> columnStarts) {
        int idx = 0;
        for (int i = 0; i < columnStarts.size(); i++) {
            if (x + 0.01f >= columnStarts.get(i)) {
                idx = i;
            }
        }
        return idx;
    }
}
