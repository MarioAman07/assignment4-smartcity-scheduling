package com.example.assignment4.metrics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CsvExporter {

    private static final String RESULTS_DIR = "data" + File.separator + "output";
    private static final String FILE_NAME = "performance_metrics.csv";
    private static final String CSV_SEPARATOR = ",";

    public static void writeResults(List<Map<String, Object>> allResults, String[] headerOrder) {
        if (allResults == null || allResults.isEmpty()) {
            System.out.println("No results to export.");
            return;
        }

        new File(RESULTS_DIR).mkdirs();
        String filePath = RESULTS_DIR + File.separator + FILE_NAME;

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {

            writer.println(String.join(CSV_SEPARATOR, headerOrder));

            for (Map<String, Object> result : allResults) {
                String row = createCsvRow(result, headerOrder);
                writer.println(row);
            }

            System.out.println("\n Successfully exported performance metrics to " + filePath);

        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + e.getMessage());
        }
    }

    private static String createCsvRow(Map<String, Object> result, String[] headerOrder) {
        StringBuilder sb = new StringBuilder();

        for (String header : headerOrder) {
            Object value = result.getOrDefault(header, "");

            if (header.contains("TIME_MS") && value instanceof Double) {
                sb.append(String.format(Locale.US, "%.3f", value));
            } else {
                sb.append(value);
            }
            sb.append(CSV_SEPARATOR);
        }

        return sb.substring(0, sb.length() - 1);
    }
}