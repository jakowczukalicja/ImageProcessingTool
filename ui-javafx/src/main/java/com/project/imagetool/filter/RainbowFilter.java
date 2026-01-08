package com.project.imagetool.filter;
import com.project.imagetool.model.FilterType;
import com.project.imagetool.model.ImageFilter;

import java.util.List;

public class RainbowFilter implements ImageFilter {

    public enum Mode { ROW, COLUMN }

    private final Mode mode;

    public RainbowFilter() {
        this.mode = Mode.ROW;
    }

    public RainbowFilter(Mode mode) {
        this.mode = mode;
    }

    @Override
    public FilterType getType() {
        return FilterType.RAINBOW;
    }

    @Override
    public List<String> toCliArgs() {
        return List.of("--rainbow", mode == Mode.ROW ? "r" : "c");
    }
}

