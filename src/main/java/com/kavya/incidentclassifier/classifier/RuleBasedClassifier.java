// classifier/RuleBasedClassifier.java
package com.kavya.incidentclassifier.classifier;

import com.kavya.incidentclassifier.model.Category;
import com.kavya.incidentclassifier.model.Incident;
import java.util.regex.*;

public class RuleBasedClassifier {

    private final int daysWindow;

    // Default constructor — 7-day warning window if nothing specified
    public RuleBasedClassifier() {
        this(7);
    }

    public RuleBasedClassifier(int daysWindow) {
        this.daysWindow = daysWindow;
    }

    public void classify(Incident incident) {
        String msg = incident.getRawMessage().toLowerCase();

        if (msg.contains("certificate") || msg.contains("tls handshake") || msg.contains("ssl")) {
            incident.setCategory(Category.CERTIFICATE_RENEWAL);
            if (msg.contains("expired")) {
                incident.setSuggestedRootCause(
                        "Certificate has already expired — service is down. Renew immediately and redeploy.");
            } else {
                Integer daysUntilExpiry = extractDaysUntilExpiry(msg);
                if (daysUntilExpiry != null && daysUntilExpiry <= daysWindow) {
                    incident.setSuggestedRootCause(
                            "Certificate expiring in " + daysUntilExpiry + " day(s) — within the "
                                    + daysWindow + "-day warning window. Schedule renewal before failure occurs.");
                } else if (daysUntilExpiry != null) {
                    incident.setSuggestedRootCause(
                            "Certificate expiring in " + daysUntilExpiry
                                    + " day(s) — outside current warning window (" + daysWindow + " days). Monitor.");
                } else {
                    incident.setSuggestedRootCause(
                            "Certificate/TLS-related failure — verify certificate chain and expiry date.");
                }
            }
            return;
        }

        if (msg.contains("authentication failed") || msg.contains("token expired")) {
            incident.setCategory(Category.AUTHENTICATION);
            incident.setSuggestedRootCause("Credential or token expiry — check identity provider / re-auth device.");
            return;
        }

        if (msg.contains("timeout") || msg.contains("unreachable") || msg.contains("ping")) {
            incident.setCategory(Category.CONNECTIVITY);
            incident.setSuggestedRootCause("Network path issue — check switch port, VLAN, or firewall rules.");
            return;
        }

        if (msg.contains("audio") || msg.contains("microphone") || msg.contains("no sound")) {
            incident.setCategory(Category.AUDIO);
            incident.setSuggestedRootCause("Audio device/driver issue — check device enumeration and cabling.");
            return;
        }

        if (msg.contains("video") || msg.contains("stream") || msg.contains("freeze")) {
            incident.setCategory(Category.VIDEO);
            incident.setSuggestedRootCause("Video pipeline issue — check bandwidth, codec, or capture device.");
            return;
        }

        if (msg.contains("unresponsive") || msg.contains("device")) {
            incident.setCategory(Category.HARDWARE);
            incident.setSuggestedRootCause("Endpoint hardware fault — power-cycle device, check firmware version.");
            return;
        }

        incident.setCategory(Category.UNKNOWN);
        incident.setSuggestedRootCause("No matching rule — review manually and consider adding a new rule.");
    }

    private Integer extractDaysUntilExpiry(String msg) {
        Pattern p = Pattern.compile("expir\\w* in (\\d+) days?");
        Matcher m = p.matcher(msg);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return null;
    }
}