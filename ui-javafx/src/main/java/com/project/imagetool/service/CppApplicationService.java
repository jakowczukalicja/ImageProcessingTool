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

        for(ImageFilter filter : job.getFilters()) {
            command.addAll(filter.toCliArgs());
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
        {
            reader.lines().forEach(System.out::println);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("C++ app failed with code " + exitCode);
        }
    }
}
