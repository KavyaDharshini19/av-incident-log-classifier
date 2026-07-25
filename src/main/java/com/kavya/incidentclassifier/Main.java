// Main.java
package com.kavya.incidentclassifier;

import com.kavya.incidentclassifier.classifier.RuleBasedClassifier;
import com.kavya.incidentclassifier.model.Incident;
import com.kavya.incidentclassifier.parser.LogParser;
import com.kavya.incidentclassifier.report.ReportGenerator;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        String logFile = args.length > 0 ? args[0] : "sample-logs/sample_incidents.log";

        LogParser parser = new LogParser();
        List<Incident> incidents = parser.parseFile(logFile);

        RuleBasedClassifier classifier = new RuleBasedClassifier();
        incidents.forEach(classifier::classify);

        new ReportGenerator().printSummary(incidents);
    }
}