# CLAUDE.md

This file provides development context for AI coding tools working in this repository.

## Project Overview

UniReq is a Java 17 Burp Suite extension that deduplicates HTTP requests observed by Burp Proxy. It uses the Montoya API 2026.4 and supports Burp Suite Professional and Community Edition 2026.4 or later.

## Build and Test

```bash
mvn clean verify
# or:
./build.sh
build.bat
```

Output: `target/unireq-deduplicator-1.0.2.jar`

Prerequisites: Java 17+ and Maven 3.6+. The Montoya API is `provided` and is not bundled in the release JAR. JUnit and Mockito are test-only dependencies.

## Architecture

### Entry Point

- `extension/UniReqExtension.java` initializes the extension, registers the suite tab, native settings panel, proxy handlers, and unload handler.
- `extension/RequestFingerprintListener.java` observes Proxy requests and responses without modifying or blocking them.

### Core Logic

- `core/FingerprintGenerator.java` produces fingerprints in the form `METHOD | HOST | NORMALIZED_PATH | HASH(CONTENT)`. It uses a thread-local SHA-256 digest and skips large or recognized binary bodies.
- `core/RequestDeduplicator.java` uses concurrent maps and a FIFO queue for request/response association, deduplication, and atomic statistics. Stored entries are capped at 1000.
- `core/FilterEngine.java` applies method, status, MIME type, host/path, extension, response-presence, and Burp-scope filters.

### UI

- `ui/UniReqGui.java` coordinates the main tab, exports, context actions, and debounced table refreshes.
- `ui/components/AdvancedFilterSettingsPanel.java` implements Montoya's native `SettingsPanel`.
- `ui/components/PatternFilterPanel.java` provides the shared host/path fields and explicit regex, case-sensitive, and inverted-host options.
- Other components provide controls, statistics, filtering, the request table, request/response viewers, exports, and the modal compatibility fallback.

### Export

- `export/ExportManager.java` coordinates JSON, CSV, and Markdown exports.
- File writes run in `SwingWorker` background tasks so the Swing event thread remains responsive.
- Exporters escape untrusted HTTP-derived content for their respective formats.

### Models

- `model/RequestResponseEntry.java` represents a captured request, optional response, fingerprint, timestamp, and arrival sequence.
- `model/FilterCriteria.java` contains basic and advanced filter state and compiled regex patterns.
- `model/ExportConfiguration.java` describes an export job.

## Design Constraints

- All proxy traffic continues unchanged; deduplication only affects UniReq's displayed entries.
- Shared deduplication state must remain thread-safe.
- Swing components must only be mutated on the event-dispatch thread.
- Potentially slow file writes must remain off the event-dispatch thread.
- All dialogs and file choosers must have a Burp-owned parent component.
- UI colors and components must remain compatible with Burp light and dark themes.
- The extension must unload cleanly by stopping timers and clearing retained state.
