package com.project.imagetool.filter;

import com.project.imagetool.model.FilterType;
import com.project.imagetool.model.ImageFilter;

import java.util.List;

public class EdgeFilter implements ImageFilter {

    private final double low;
    private final double high;

    public EdgeFilter(double low, double high) {
        this.low = low;
        this.high = high;
    }

    @Override
    public FilterType getType() {
        return FilterType.EDGE;
    }

    @Override
    public List<String> toCliArgs() {
        return List.of(
                "--edge",
                String.valueOf(low),
                String.valueOf(high)
        );
    }
}

