package com.project.imagetool.service;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LogManager {

    public static Path findProcessingLogFile() {
        Path currentDir = Path.of(System.getProperty("user.dir"));
        Path searchDir = currentDir;

        for (int i = 0; i < 6; i++) {
            Path p = searchDir.resolve("processing_log.txt");
            if (Files.exists(p)) {
                return p.toAbsolutePath();
            }
            Path parent = searchDir.getParent();
            if (parent == null) break;
            searchDir = parent;
        }
        return null;
    }

    public static void appendProcessingLog(TextArea logTextArea) {
        if (logTextArea == null) return;

        Thread reader = new Thread(() -> {
            try {
                Path p = findProcessingLogFile();
                if (p == null || !Files.exists(p)) return;

                List<String> lines = Files.readAllLines(p);
                if (!lines.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (String l : lines) {
                        String t = l == null ? "" : l.trim();
                        if ("=== Logger started ===".equals(t) || "=== Logger ended ===".equals(t)) continue;
                        sb.append(l).append(System.lineSeparator());
                    }
                    final String content = sb.toString();
                    Platform.runLater(() -> {
                        logTextArea.appendText(content);
                        logTextArea.positionCaret(logTextArea.getLength());
                    });
                }
            } catch (IOException ex) {
                String msg = "Failed to append processing log lines: " + ex.getMessage();
                System.err.println(msg);
                Platform.runLater(() -> logTextArea.appendText(msg + System.lineSeparator()));
            }
        }, "LogReader");
        reader.setDaemon(true);
        reader.start();
    }

}
