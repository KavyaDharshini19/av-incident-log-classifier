// parser/LogParser.java
package com.kavya.incidentclassifier.parser;

import com.kavya.incidentclassifier.model.Incident;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.*;

public class LogParser {

    // Matches: 2026-06-01 09:12:03 [ERROR] Room-204: message text
    private static final Pattern LOG_PATTERN = Pattern.compile(
            "^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})\\s+\\[(\\w+)\\]\\s+Room-(\\S+):\\s+(.*)$"
    );
    private static final DateTimeFormatter TS_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<Incident> parseFile(String filePath) throws IOException {
        List<Incident> incidents = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher m = LOG_PATTERN.matcher(line.trim());
                if (m.matches()) {
                    LocalDateTime ts = LocalDateTime.parse(m.group(1), TS_FORMAT);
                    String severity = m.group(2);
                    String room = m.group(3);
                    String message = m.group(4);
                    incidents.add(new Incident(ts, severity, room, message));
                }
                // silently skip lines that don't match — log a warning in a real system
            }
        }
        return incidents;
    }
}
