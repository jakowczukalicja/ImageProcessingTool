package com.project.imagetool.filter;

import com.project.imagetool.model.FilterType;
import com.project.imagetool.model.ImageFilter;

import java.util.List;

public class SingleColourFilter implements ImageFilter {

    private final int red;
    private final int green;
    private final int blue;

    public SingleColourFilter(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    @Override
    public FilterType getType() {
        return FilterType.SINGLE_COLOR;
    }

    @Override
    public List<String> toCliArgs() {
        return List.of(
                "--singlecolour",
                String.valueOf(red),
                String.valueOf(green),
                String.valueOf(blue)
        );
    }
}

