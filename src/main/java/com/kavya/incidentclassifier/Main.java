// Main.java
package com.kavya.incidentclassifier;

import com.kavya.incidentclassifier.classifier.RuleBasedClassifier;
import com.kavya.incidentclassifier.model.Incident;
import com.kavya.incidentclassifier.parser.LogParser;
import com.kavya.incidentclassifier.report.ReportGenerator;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        String logFile = "sample-logs/sample_incidents.log";
        int daysWindow = 7; // default

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--days-window") && i + 1 < args.length) {
                daysWindow = Integer.parseInt(args[i + 1]);
                i++;
            } else if (!args[i].startsWith("--")) {
                logFile = args[i];
            }
        }

        LogParser parser = new LogParser();
        List<Incident> incidents = parser.parseFile(logFile);

        RuleBasedClassifier classifier = new RuleBasedClassifier(daysWindow);
        incidents.forEach(classifier::classify);

        ReportGenerator reportGenerator = new ReportGenerator();
        reportGenerator.printSummary(incidents);
        reportGenerator.writeJsonReport(incidents, "output/report.json");   // ← ADD THIS LINE
    }
}