package com.project.imagetool.model;

import java.util.List;

public interface ImageFilter {
    FilterType getType();
    List<String> toCliArgs();
}
