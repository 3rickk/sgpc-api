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

/**
 * Service responsável pela exportação de dados para formato CSV.
 * 
 * Este service oferece funcionalidades genéricas para:
 * - Exportação de qualquer lista de objetos para CSV
 * - Formatação automática de cabeçalhos a partir dos nomes dos campos
 * - Tratamento de tipos especiais (datas, números decimais)
 * - Escape adequado de caracteres especiais
 * - Geração de arquivos CSV compatíveis com Excel/LibreOffice
 * 
 * Características técnicas:
 * - Uso de reflexão para processar campos automaticamente
 * - Formatação de nomes de campos de camelCase para título
 * - Suporte a tipos: String, LocalDate, LocalDateTime, BigDecimal
 * - Tratamento de valores nulos
 * - Escape de aspas duplas conforme padrão RFC 4180
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Service
public class CsvExportService {

    /**
     * Exporta uma lista de objetos para formato CSV.
     * 
     * Processo de exportação:
     * 1. Gera cabeçalho baseado nos nomes dos campos da classe
     * 2. Itera sobre cada objeto da lista
     * 3. Extrai valores usando reflexão
     * 4. Formata valores conforme o tipo
     * 5. Aplica escape de caracteres especiais
     * 6. Retorna array de bytes do CSV gerado
     * 
     * @param <T> tipo dos objetos a serem exportados
     * @param data lista de objetos para exportar
     * @param clazz classe dos objetos (para reflexão)
     * @return array de bytes contendo o CSV gerado
     * @throws RuntimeException se ocorrer erro na geração
     */
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

    /**
     * Escreve o cabeçalho do CSV baseado nos campos da classe.
     * 
     * Converte nomes de campos de camelCase para títulos formatados.
     * Exemplo: "projectName" se torna "PROJECT NAME"
     * 
     * @param <T> tipo da classe
     * @param writer writer para escrever no CSV
     * @param clazz classe para extrair campos
     */
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

    /**
     * Escreve uma linha de dados no CSV.
     * 
     * Para cada campo do objeto:
     * 1. Torna o campo acessível via reflexão
     * 2. Obtém o valor do campo
     * 3. Formata o valor conforme o tipo
     * 4. Adiciona à linha CSV
     * 
     * @param <T> tipo do objeto
     * @param writer writer para escrever no CSV
     * @param item objeto cujos dados serão escritos
     */
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

    /**
     * Formata nome de campo de camelCase para título.
     * 
     * Transformações aplicadas:
     * - Adiciona espaço antes de maiúsculas
     * - Converte tudo para maiúsculas
     * - Envolve em aspas duplas
     * 
     * Exemplo: "totalBudget" → "TOTAL BUDGET"
     * 
     * @param fieldName nome do campo em camelCase
     * @return nome formatado como título CSV
     */
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

    /**
     * Formata valor conforme o tipo para CSV.
     * 
     * Regras de formatação:
     * - null: string vazia
     * - LocalDate/LocalDateTime: toString()
     * - BigDecimal: toPlainString() (sem notação científica)
     * - Outros: toString()
     * 
     * Aplica escape se necessário:
     * - Envolve em aspas se contém vírgula, aspas ou quebra de linha
     * - Duplica aspas internas ("" para representar ")
     * 
     * @param value valor a ser formatado
     * @return string formatada para CSV
     */
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

    /**
     * Retorna o Content-Type para arquivos CSV.
     * 
     * @return "text/csv"
     */
    public String getContentType() {
        return "text/csv";
    }

    /**
     * Retorna a extensão padrão para arquivos CSV.
     * 
     * @return ".csv"
     */
    public String getFileExtension() {
        return ".csv";
    }
} 