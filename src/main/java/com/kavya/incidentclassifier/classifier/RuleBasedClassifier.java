// classifier/RuleBasedClassifier.java
package com.kavya.incidentclassifier.classifier;

import com.kavya.incidentclassifier.model.Category;
import com.kavya.incidentclassifier.model.Incident;
import java.util.regex.*;

public class RuleBasedClassifier {

    public void classify(Incident incident) {
        String msg = incident.getRawMessage().toLowerCase();

        // --- Certificate renewal (check first — often mixed with "connection failed" wording) ---
        if (msg.contains("certificate") || msg.contains("tls handshake") || msg.contains("ssl")) {
            incident.setCategory(Category.CERTIFICATE_RENEWAL);
            if (msg.contains("expired")) {
                incident.setSuggestedRootCause(
                        "Certificate has already expired — service is down. Renew immediately and redeploy.");
            } else if (containsExpiringSoon(msg)) {
                incident.setSuggestedRootCause(
                        "Certificate expiring within the warning window — schedule renewal before failure occurs.");
            } else {
                incident.setSuggestedRootCause(
                        "Certificate/TLS-related failure — verify certificate chain and expiry date.");
            }
            return;
        }

        // --- Authentication ---
        if (msg.contains("authentication failed") || msg.contains("token expired")) {
            incident.setCategory(Category.AUTHENTICATION);
            incident.setSuggestedRootCause("Credential or token expiry — check identity provider / re-auth device.");
            return;
        }

        // --- Connectivity ---
        if (msg.contains("timeout") || msg.contains("unreachable") || msg.contains("ping")) {
            incident.setCategory(Category.CONNECTIVITY);
            incident.setSuggestedRootCause("Network path issue — check switch port, VLAN, or firewall rules.");
            return;
        }

        // --- Audio ---
        if (msg.contains("audio") || msg.contains("microphone") || msg.contains("no sound")) {
            incident.setCategory(Category.AUDIO);
            incident.setSuggestedRootCause("Audio device/driver issue — check device enumeration and cabling.");
            return;
        }

        // --- Video ---
        if (msg.contains("video") || msg.contains("stream") || msg.contains("freeze")) {
            incident.setCategory(Category.VIDEO);
            incident.setSuggestedRootCause("Video pipeline issue — check bandwidth, codec, or capture device.");
            return;
        }

        // --- Hardware (catch-all for device-level issues) ---
        if (msg.contains("unresponsive") || msg.contains("device")) {
            incident.setCategory(Category.HARDWARE);
            incident.setSuggestedRootCause("Endpoint hardware fault — power-cycle device, check firmware version.");
            return;
        }

        incident.setCategory(Category.UNKNOWN);
        incident.setSuggestedRootCause("No matching rule — review manually and consider adding a new rule.");
    }

    private boolean containsExpiringSoon(String msg) {
        Pattern p = Pattern.compile("expir\\w* in \\d+ days?");
        return p.matcher(msg).find();
    }
}