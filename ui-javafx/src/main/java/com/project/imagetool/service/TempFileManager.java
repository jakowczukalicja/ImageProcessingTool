package com.project.imagetool.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class TempFileManager {
    private Path tempOutputImage;
    private Path prevOutputImage;

    public Path getTempOutputImage() {
        return tempOutputImage;
    }

    public Path getPrevOutputImage() {
        return prevOutputImage;
    }

    public Path createTempOutput() throws IOException {
        tempOutputImage = Files.createTempFile("imgtool_", ".png");
        return tempOutputImage;
    }

    public Path createPrevFrom(Path source) throws IOException {
        if (source == null) return null;
        if (prevOutputImage != null && Files.exists(prevOutputImage)) {
            try { Files.delete(prevOutputImage); } catch (Exception ignore) {}
            prevOutputImage = null;
        }
        prevOutputImage = Files.createTempFile("imgtool_prev_", ".png");
        Files.copy(source, prevOutputImage, StandardCopyOption.REPLACE_EXISTING);
        return prevOutputImage;
    }

    public void deleteTempOutput() {
        if (tempOutputImage != null && Files.exists(tempOutputImage)) {
            try { Files.delete(tempOutputImage); } catch (Exception ignore) {}
        }
        tempOutputImage = null;
    }

    public void deletePrevOutput() {
        if (prevOutputImage != null && Files.exists(prevOutputImage)) {
            try { Files.delete(prevOutputImage); } catch (Exception ignore) {}
        }
        prevOutputImage = null;
    }

    public Path restorePrevToTemp() throws IOException {
        if (prevOutputImage == null || !Files.exists(prevOutputImage)) return null;
        // delete current temp if exists
        if (tempOutputImage != null && Files.exists(tempOutputImage)) {
            try { Files.delete(tempOutputImage); } catch (Exception ignore) {}
        }
        Path restored = Files.createTempFile("imgtool_restored_", ".png");
        Files.copy(prevOutputImage, restored, StandardCopyOption.REPLACE_EXISTING);
        try { Files.delete(prevOutputImage); } catch (Exception ignore) {}
        prevOutputImage = null;
        tempOutputImage = restored;
        return tempOutputImage;
    }

    public void cleanupAll() {
        deleteTempOutput();
        deletePrevOutput();
    }
}
