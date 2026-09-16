package com.teamflow.ai.project.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.List;

/**
 * A deliberately minimal XLSX writer: one sheet, a header row, plain values.
 * Sticks to the most basic, long-stable part of the POI API (no styling, no
 * formulas) since a report export needs to be correct and readable, not
 * decorative — matching the platform brief's "avoid unnecessary complexity."
 */
public final class ExcelWriter {

    private ExcelWriter() {
    }

    public static byte[] write(String sheetName, List<String> headers, List<List<Object>> rows) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet(sheetName);

            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < headers.size(); col++) {
                headerRow.createCell(col).setCellValue(headers.get(col));
            }

            for (int r = 0; r < rows.size(); r++) {
                Row row = sheet.createRow(r + 1);
                List<Object> values = rows.get(r);
                for (int col = 0; col < values.size(); col++) {
                    setCellValue(row.createCell(col), values.get(col));
                }
            }

            for (int col = 0; col < headers.size(); col++) {
                sheet.autoSizeColumn(col);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to write Excel report", ex);
        }
    }

    private static void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number number) {
            // Covers BigDecimal too (BigDecimal extends Number) — no separate branch needed.
            cell.setCellValue(number.doubleValue());
        } else if (value instanceof LocalDate date) {
            cell.setCellValue(date.toString());
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }
}
