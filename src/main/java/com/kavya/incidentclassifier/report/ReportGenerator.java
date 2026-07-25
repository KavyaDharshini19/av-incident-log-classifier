// report/ReportGenerator.java
package com.kavya.incidentclassifier.report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kavya.incidentclassifier.model.Category;
import com.kavya.incidentclassifier.model.Incident;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.*;

public class ReportGenerator {

    public void printSummary(List<Incident> incidents) {
        Map<Category, Long> counts = incidents.stream()
                .collect(Collectors.groupingBy(Incident::getCategory, Collectors.counting()));

        System.out.println("=== Incident Summary (" + incidents.size() + " total) ===");
        counts.forEach((cat, count) -> System.out.println(cat + ": " + count));

        System.out.println("\n=== Certificate Renewal Alerts (priority) ===");
        incidents.stream()
                .filter(i -> i.getCategory() == Category.CERTIFICATE_RENEWAL)
                .forEach(i -> System.out.println(
                        "[" + i.getTimestamp() + "] Room-" + i.getRoom() + ": " + i.getSuggestedRootCause()));
    }

    public void writeJsonReport(List<Incident> incidents, String outputPath) {
        Map<Category, Long> counts = incidents.stream()
                .collect(Collectors.groupingBy(Incident::getCategory, Collectors.counting()));

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("totalIncidents", incidents.size());
        report.put("countsByCategory", counts);
        report.put("incidents", incidents.stream().map(i -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("timestamp", i.getTimestamp().toString());
            m.put("severity", i.getSeverity());
            m.put("room", i.getRoom());
            m.put("category", i.getCategory().toString());
            m.put("rawMessage", i.getRawMessage());
            m.put("suggestedRootCause", i.getSuggestedRootCause());
            return m;
        }).collect(Collectors.toList()));

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            Files.createDirectories(Paths.get(outputPath).getParent());
            try (FileWriter writer = new FileWriter(outputPath)) {
                gson.toJson(report, writer);
            }
            System.out.println("\nJSON report written to: " + outputPath);
        } catch (IOException e) {
            System.err.println("Failed to write JSON report: " + e.getMessage());
        }
    }
}