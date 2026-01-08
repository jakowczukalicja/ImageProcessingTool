package com.project.imagetool.filter;

import com.project.imagetool.model.FilterType;
import com.project.imagetool.model.ImageFilter;

import java.util.List;

public class BlurFilter implements ImageFilter {

    private final int kernelSize;
    private final double sigma;

    public BlurFilter(int kernelSize, double sigma) {
        this.kernelSize = kernelSize;
        this.sigma = sigma;
    }

    @Override
    public FilterType getType() {
        return FilterType.BLUR;
    }

    @Override
    public List<String> toCliArgs() {
        return List.of(
                "--blur",
                String.valueOf(kernelSize),
                String.valueOf(sigma)
        );
    }
}

