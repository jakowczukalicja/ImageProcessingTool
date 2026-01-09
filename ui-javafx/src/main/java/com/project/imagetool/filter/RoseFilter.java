package com.project.imagetool.filter;

import com.project.imagetool.model.FilterType;
import com.project.imagetool.model.ImageFilter;

import java.util.List;

public class RoseFilter implements ImageFilter {

    @Override
    public FilterType getType() {
        return FilterType.ROSE;
    }

    @Override
    public List<String> toCliArgs() {
        return List.of("--rose");
    }
}
