package com.project.imagetool.util;

import java.util.function.Consumer;

public final class ValidationUtil {
    private ValidationUtil() {}

    public static boolean validateParameters(
            boolean blurSelected,
            String kernelText,
            String sigmaText,
            boolean edgeSelected,
            String lowText,
            String highText,
            Consumer<String> showError
    ) {
        if (blurSelected) {
            String kText = kernelText == null ? "" : kernelText.trim();
            if (kText.isEmpty() || !kText.matches("\\d+")) {
                showError.accept("Blur kernel must be a positive odd integer");
                return false;
            }
            int k;
            try {
                k = Integer.parseInt(kText);
            } catch (NumberFormatException ex) {
                showError.accept("Blur kernel must be a positive odd integer");
                return false;
            }
            if (k <= 0 || k % 2 == 0) {
                showError.accept("Blur kernel must be a positive odd integer");
                return false;
            }

            String sText = sigmaText == null ? "" : sigmaText.trim();
            if (sText.isEmpty()) {
                showError.accept("Blur sigma must be a numeric value");
                return false;
            }
            try {
                Double.parseDouble(sText);
            } catch (Exception ex) {
                showError.accept("Blur sigma must be a numeric value");
                return false;
            }
        }

        if (edgeSelected) {
            String lText = lowText == null ? "" : lowText.trim();
            String hText = highText == null ? "" : highText.trim();
            if (lText.isEmpty() || hText.isEmpty()) {
                showError.accept("Edge thresholds must be numeric values");
                return false;
            }
            try {
                Double.parseDouble(lText);
                Double.parseDouble(hText);
            } catch (Exception ex) {
                showError.accept("Edge thresholds must be numeric values");
                return false;
            }
        }

        return true;
    }
}
