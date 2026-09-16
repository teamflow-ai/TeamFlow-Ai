package com.teamflow.ai.common.util;

import java.util.List;

/**
 * A deliberately tiny CSV writer — RFC 4180 quoting only, no external dependency.
 * Reports are small, operator-facing exports, not a general-purpose data
 * pipeline, so pulling in a CSV library for this would be exactly the kind of
 * unnecessary framework the platform brief asks to avoid.
 */
public final class CsvWriter {

    private CsvWriter() {
    }

    /** @param headers column headers; rows must have the same length as headers, in the same order */
    public static String write(List<String> headers, List<List<Object>> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", headers.stream().map(CsvWriter::escape).toList())).append("\r\n");
        for (List<Object> row : rows) {
            sb.append(String.join(",", row.stream().map(v -> escape(v == null ? "" : String.valueOf(v))).toList()))
                    .append("\r\n");
        }
        return sb.toString();
    }

    private static String escape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
