package com.teamflow.ai.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CsvWriter")
class CsvWriterTest {

    @Test
    @DisplayName("writes a simple header and row with CRLF line endings")
    void writesSimpleRow() {
        String csv = CsvWriter.write(List.of("Name", "Score"), List.of(List.of("Alex", 42)));
        assertEquals("Name,Score\r\nAlex,42\r\n", csv);
    }

    @Test
    @DisplayName("quotes a value containing a comma")
    void quotesValueWithComma() {
        String csv = CsvWriter.write(List.of("Notes"), List.of(List.of("Reviewed, approved")));
        assertTrue(csv.contains("\"Reviewed, approved\""));
    }

    @Test
    @DisplayName("escapes an embedded quote by doubling it")
    void escapesEmbeddedQuote() {
        String csv = CsvWriter.write(List.of("Notes"), List.of(List.of("She said \"go\"")));
        assertTrue(csv.contains("\"She said \"\"go\"\"\""));
    }

    @Test
    @DisplayName("a plain value with no special characters is never quoted")
    void plainValueIsNotQuoted() {
        String csv = CsvWriter.write(List.of("Status"), List.of(List.of("APPROVED")));
        assertEquals("Status\r\nAPPROVED\r\n", csv);
    }

    @Test
    @DisplayName("a null value renders as an empty field")
    void nullValueRendersEmpty() {
        java.util.List<Object> row = new java.util.ArrayList<>();
        row.add(null);
        String csv = CsvWriter.write(List.of("Value"), List.of(row));
        assertEquals("Value\r\n\r\n", csv);
    }
}
