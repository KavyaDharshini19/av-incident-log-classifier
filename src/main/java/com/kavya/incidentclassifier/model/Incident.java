// model/Incident.java
package com.kavya.incidentclassifier.model;

import java.time.LocalDateTime;

public class Incident {
    private LocalDateTime timestamp;
    private String severity;
    private String room;
    private String rawMessage;
    private Category category;
    private String suggestedRootCause;

    // constructor, getters, setters
    public Incident(LocalDateTime timestamp, String severity, String room, String rawMessage) {
        this.timestamp = timestamp;
        this.severity = severity;
        this.room = room;
        this.rawMessage = rawMessage;
        this.category = Category.UNKNOWN;
    }

    public void setCategory(Category category) { this.category = category; }
    public Category getCategory() { return category; }
    public void setSuggestedRootCause(String cause) { this.suggestedRootCause = cause; }
    public String getSuggestedRootCause() { return suggestedRootCause; }
    public String getRawMessage() { return rawMessage; }
    public String getRoom() { return room; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getSeverity() { return severity; }
}