package com.project.imagetool.service;

import com.project.imagetool.model.ImageFilter;
import com.project.imagetool.model.ImageJob;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CppApplicationService {
    private final Path executablePath;

    public CppApplicationService(Path executablePath) {
        this.executablePath = executablePath;
    }

    public void run(ImageJob job)
            throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(executablePath.toString());
        command.add(job.getInputPath().toString());
        command.add(job.getOutputPath().toString());

        // Polymorphism
        for(ImageFilter filter : job.getFilters()) {
            command.addAll(filter.toCliArgs());
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // Close the process stdin immediately since we don't write to it
        try {
            process.getOutputStream().close();
        } catch (IOException ignore) {}

        Thread outputReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                System.err.println("Error reading process output: " + e.getMessage());
            }
        });
        outputReader.setDaemon(true);
        outputReader.start();

        int exitCode = process.waitFor();
        
        try {
            outputReader.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (exitCode != 0) {
            throw new RuntimeException("C++ app failed with code " + exitCode);
        }
    }
}
