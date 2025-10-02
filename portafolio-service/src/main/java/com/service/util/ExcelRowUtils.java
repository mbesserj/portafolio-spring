package com.service.util;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ExcelRowUtils {

    /**
     * Lee el valor de una celda como un String, eliminando espacios en blanco.
     */
    public String getString(Cell cell) {
        if (cell == null) {
            return null;
        }
        // DataFormatter es la forma más segura de leer cualquier tipo de celda como String
        return new DataFormatter().formatCellValue(cell).trim();
    }

    /**
     * Lee el valor de una celda y lo convierte a BigDecimal.
     * Maneja celdas numéricas y de texto.
     */
    public BigDecimal getBigDecimal(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue());
            } else if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().replace(".", "").replace(",", ".");
                if (value.matches("[-+]?\\d*\\.?\\d+")) {
                    return new BigDecimal(value);
                }
            }
        } catch (Exception ignored) {
            // Si la conversión falla, se devuelve null
        }
        return null;
    }

    /**
     * Lee el valor de una celda y lo convierte a LocalDate.
     * Es robusto y soporta múltiples formatos de fecha como texto.
     */
    public LocalDate getLocalDate(Cell cell) {
        if (cell == null) {
            return null;
        }

        // 1. Intenta leer como fecha numérica de Excel
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
        } catch (Exception ignored) {}

        // 2. Si no, intenta leer como texto
        String dateStr = getString(cell);
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }

        // 3. Intenta varios formatos de fecha comunes
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy")
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (Exception ignored) {}
        }

        return null; // Si ningún formato funcionó
    }
    
    /**
     * Verifica si una fila debe ser ignorada (ej. está completamente vacía).
     */
    public boolean shouldSkipRow(Row row) {
        if (row == null) {
            return true;
        }
        Cell firstCell = row.getCell(0);
        return firstCell == null || firstCell.getCellType() == CellType.BLANK || getString(firstCell).isBlank();
    }
}