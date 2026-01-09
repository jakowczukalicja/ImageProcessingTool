package com.project.imagetool.util;

import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

public final class InputFormatters {
    private InputFormatters() {}

    public static TextFormatter<String> createIntegerFormatter() {
        StringConverter<String> noop = new StringConverter<>() {
            @Override public String toString(String s) { return s; }
            @Override public String fromString(String s) { return s; }
        };

        return new TextFormatter<>(noop, "", change -> {
            String newText = change.getControlNewText();
            return newText.matches("\\d*") ? change : null;
        });
    }

    public static TextFormatter<String> createDecimalFormatter() {
        StringConverter<String> noop = new StringConverter<>() {
            @Override public String toString(String s) { return s; }
            @Override public String fromString(String s) { return s; }
        };

        return new TextFormatter<>(noop, "", change -> {
            String newText = change.getControlNewText();
            return newText.matches("\\d*(\\.\\d*)?") ? change : null;
        });
    }
}
