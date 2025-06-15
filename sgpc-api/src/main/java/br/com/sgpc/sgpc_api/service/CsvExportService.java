package br.com.sgpc.sgpc_api.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class CsvExportService {

    public <T> byte[] exportToCsv(List<T> data, Class<T> clazz) {
        if (data == null || data.isEmpty()) {
            return new byte[0];
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(outputStream)) {

            // Escrever cabeçalho
            writeHeader(writer, clazz);

            // Escrever dados
            for (T item : data) {
                writeRow(writer, item);
            }

            writer.flush();
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar CSV: " + e.getMessage());
        }
    }

    private <T> void writeHeader(PrintWriter writer, Class<T> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        StringBuilder header = new StringBuilder();

        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                header.append(",");
            }
            header.append(formatFieldName(fields[i].getName()));
        }

        writer.println(header.toString());
    }

    private <T> void writeRow(PrintWriter writer, T item) {
        Field[] fields = item.getClass().getDeclaredFields();
        StringBuilder row = new StringBuilder();

        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                row.append(",");
            }

            try {
                fields[i].setAccessible(true);
                Object value = fields[i].get(item);
                row.append(formatValue(value));
            } catch (IllegalAccessException e) {
                row.append("");
            }
        }

        writer.println(row.toString());
    }

    private String formatFieldName(String fieldName) {
        // Converter camelCase para palavras separadas
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < fieldName.length(); i++) {
            char c = fieldName.charAt(i);
            if (i > 0 && Character.isUpperCase(c)) {
                formatted.append(" ");
            }
            formatted.append(Character.toUpperCase(c));
        }
        return "\"" + formatted.toString() + "\"";
    }

    private String formatValue(Object value) {
        if (value == null) {
            return "";
        }

        String stringValue;
        if (value instanceof LocalDate) {
            stringValue = value.toString();
        } else if (value instanceof LocalDateTime) {
            stringValue = value.toString();
        } else if (value instanceof BigDecimal) {
            stringValue = ((BigDecimal) value).toPlainString();
        } else {
            stringValue = value.toString();
        }

        // Escapar aspas duplas e envolver em aspas se necessário
        if (stringValue.contains(",") || stringValue.contains("\"") || stringValue.contains("\n")) {
            stringValue = stringValue.replace("\"", "\"\"");
            return "\"" + stringValue + "\"";
        }

        return stringValue;
    }

    public String getContentType() {
        return "text/csv";
    }

    public String getFileExtension() {
        return ".csv";
    }
} 