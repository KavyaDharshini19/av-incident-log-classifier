// test/RuleBasedClassifierTest.java
package com.kavya.incidentclassifier.classifier;

import com.kavya.incidentclassifier.model.Category;
import com.kavya.incidentclassifier.model.Incident;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class RuleBasedClassifierTest {

    private final RuleBasedClassifier classifier = new RuleBasedClassifier();

    @Test
    void detectsExpiredCertificate() {
        Incident i = new Incident(LocalDateTime.now(), "WARN", "305",
                "TLS handshake failed. Certificate for tms.internal.corp expired 2026-05-28.");
        classifier.classify(i);
        assertEquals(Category.CERTIFICATE_RENEWAL, i.getCategory());
        assertTrue(i.getSuggestedRootCause().toLowerCase().contains("already expired"));
    }

    @Test
    void detectsExpiringSoonCertificate() {
        Incident i = new Incident(LocalDateTime.now(), "WARN", "410",
                "SSL certificate for rms.internal.corp expiring in 5 days.");
        classifier.classify(i);
        assertEquals(Category.CERTIFICATE_RENEWAL, i.getCategory());
        assertTrue(i.getSuggestedRootCause().toLowerCase().contains("schedule renewal"));
    }

    @Test
    void doesNotMisclassifyCertificateFailureAsConnectivity() {
        Incident i = new Incident(LocalDateTime.now(), "ERROR", "118",
                "Connection failed: TLS handshake failed due to expired certificate.");
        classifier.classify(i);
        assertEquals(Category.CERTIFICATE_RENEWAL, i.getCategory());
    }

    @Test
    void detectsConnectivityTimeout() {
        Incident i = new Incident(LocalDateTime.now(), "ERROR", "118",
                "Zoom connection timeout after 30s. Network unreachable.");
        classifier.classify(i);
        assertEquals(Category.CONNECTIVITY, i.getCategory());
    }

    @Test
    void respectsCustomDaysWindow() {
        RuleBasedClassifier tightWindow = new RuleBasedClassifier(3); // only flag within 3 days
        Incident i = new Incident(LocalDateTime.now(), "WARN", "410",
                "SSL certificate for rms.internal.corp expiring in 5 days.");
        tightWindow.classify(i);
        assertEquals(Category.CERTIFICATE_RENEWAL, i.getCategory());
        assertTrue(i.getSuggestedRootCause().contains("outside current warning window"));
    }
}