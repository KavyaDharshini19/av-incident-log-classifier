# AV/Network Incident Log Classifier

A Java tool that parses raw AV/network incident logs, automatically categorizes each incident, and suggests a likely root cause using a rule-based classification engine — including dedicated detection for certificate expiry and renewal, a category prone to causing full outages if missed.

## Problem Statement

Triaging AV/network incident logs manually is slow and inconsistent — the same underlying issue (e.g. a certificate expiring) can show up in slightly different wording across systems, and it's easy for time-sensitive issues to get buried among routine noise. This tool automates that first triage step: it parses structured log lines, classifies each into a category, and flags certificate-renewal issues as a priority so they don't get missed.

This was built as a personal project to bridge hands-on AV/infrastructure support experience with software engineering — the categories and rules reflect real incident patterns from that domain.

## Sample Input
2026-06-01 09:12:03 [ERROR] Room-204: No audio output detected on MTR endpoint. Device: MTRABCDXYZPQR 

2026-06-01 09:45:11 [ERROR] Room-118: Zoom connection timeout after 30s. Network unreachable.

2026-06-01 10:03:44 [WARN]  Room-305: TLS handshake failed. Certificate for tms.internal.corp expired 2026-05-28.

2026-06-01 11:20:09 [ERROR] Room-204: Video freeze detected. Mersive Solstice stream dropped.

2026-06-01 12:01:55 [ERROR] Room-118: NUC device unresponsive. Ping timeout.

2026-06-01 13:14:30 [WARN]  Room-410: SSL certificate for rms.internal.corp expiring in 3 days.

2026-06-01 14:02:17 [ERROR] Room-305: Microphone not detected. Audio input device missing.

2026-06-01 15:33:02 [ERROR] Room-118: Teams Room account authentication failed. Token expired.

## Sample Output

=== Incident Summary (8 total) ===

VIDEO: 1

AUDIO: 2

CERTIFICATE_RENEWAL: 2

AUTHENTICATION: 1 

CONNECTIVITY: 2

=== Certificate Renewal Alerts (priority) ===

[2026-06-01T10:03:44] Room-305: Certificate has already expired — service is down. Renew immediately and redeploy.

[2026-06-01T13:14:30] Room-410: Certificate expiring in 3 day(s) — within the 7-day warning window. Schedule renewal before failure occurs.

Note: A machine-readable JSON version of this report is also generated at `output/report.json` for downstream tooling/dashboards.

## How Classification Works

The classifier uses an ordered set of keyword/regex rules, checked from most-specific to most-generic, rather than a fixed if/else on first keyword match. This ordering matters: a line like *"TLS handshake failed. Certificate ... expired"* also contains the word "failed," which could be misread as a generic connectivity issue if certificate checks weren't prioritized first.

**Certificate renewal detection was treated as a first-class, priority requirement** — not an afterthought — because in practice, missed certificate expirations cause full service outages, not just degraded performance. The classifier explicitly distinguishes:
- **Already expired** → urgent, service is currently down
- **Expiring soon** (within a configurable warning window) → scheduled renewal needed before failure

This distinction is tested explicitly (see `RuleBasedClassifierTest`) to make sure certificate-related failures are never misclassified as ordinary connectivity issues.

The rule engine is intentionally extensible: adding a new category means adding one new rule block, not restructuring the parser or report logic.

## Tech Stack

- Java 17
- Maven
- JUnit 5 (unit tests)
- Gson (JSON report output)

## Project Structure
## How to Run

```bash
mvn compile
mvn exec:java -Dexec.mainClass="com.kavya.incidentclassifier.Main"
```

Optional: point it at your own log file, and set the certificate "expiring soon" warning window:
```bash
mvn exec:java -Dexec.mainClass="com.kavya.incidentclassifier.Main" -Dexec.args="path/to/your.log --days-window 7"
```

## Running Tests

```bash
mvn test
```

## What I'd Improve Next

- Externalize classification rules into a config file (JSON/YAML) instead of hardcoded Java, so rules can change without recompiling.
- Add a small ML-based classifier (TF-IDF + Naive Bayes) as a v2 and compare accuracy against the rule-based approach.
- Add a lightweight web dashboard on top of the JSON output.

## Author

Kavya Dharshini M — built to bridge hands-on AV/network infrastructure experience with software engineering practice.