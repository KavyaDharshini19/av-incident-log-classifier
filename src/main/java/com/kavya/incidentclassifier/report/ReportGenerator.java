// report/ReportGenerator.java
package com.kavya.incidentclassifier.report;

import com.kavya.incidentclassifier.model.Category;
import com.kavya.incidentclassifier.model.Incident;
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
}