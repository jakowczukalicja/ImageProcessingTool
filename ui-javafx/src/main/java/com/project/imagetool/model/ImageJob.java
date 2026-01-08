package com.project.imagetool.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ImageJob {
    private final Path inputPath;
    private final Path outputPath;
    private final List<ImageFilter> filters = new ArrayList<>();

    public ImageJob(Path inputPath, Path outputPath) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
    }

    public Path getInputPath() {
        return inputPath;
    }

    public Path getOutputPath() {
        return outputPath;
    }

    public List<ImageFilter> getFilters() {
        return filters;
    }

    public void addFilter(ImageFilter filter) {
        filters.add(filter);
    }
}
