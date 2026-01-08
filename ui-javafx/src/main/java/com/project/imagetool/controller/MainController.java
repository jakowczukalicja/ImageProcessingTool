package com.project.imagetool.controller;

import com.project.imagetool.filter.BlurFilter;
import com.project.imagetool.filter.EdgeFilter;
import com.project.imagetool.filter.GrayFilter;
import com.project.imagetool.filter.RainbowFilter;
import com.project.imagetool.model.ImageJob;
import com.project.imagetool.service.CppApplicationService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class MainController {
    @FXML private ImageView imageView;
    @FXML private Label placeholderLabel;

    @FXML private CheckBox grayCheckBox;
    @FXML private CheckBox blurCheckBox;
    @FXML private CheckBox edgeCheckBox;
    @FXML private CheckBox rainbowCheckBox;

    @FXML private TextField blurKernelField;
    @FXML private TextField blurSigmaField;
    @FXML
    private TextField edgeLowField;
    @FXML private TextField edgeHighField;
    @FXML private ChoiceBox<String> rainbowDirectionChoice;

    private File inputImage;
    private Path tempOutputImage;

    @FXML
    public void initialize() {
        rainbowDirectionChoice.getItems().addAll("Rows", "Columns");
        rainbowDirectionChoice.setValue("Rows");
    }

    @FXML
    private void onLoadImage() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg")
        );
        inputImage = chooser.showOpenDialog(null);

        if (inputImage != null) {
            imageView.setImage(new Image(inputImage.toURI().toString()));
            placeholderLabel.setVisible(false);
        }
    }

    @FXML
    private void onProcess() {
        if (inputImage == null) {
            showError("No image loaded");
            return;
        }
        try {
            tempOutputImage = Files.createTempFile("imgtool_", ".png");

            ImageJob job = new ImageJob(
                    inputImage.toPath(),
                    tempOutputImage
            );

            if (grayCheckBox.isSelected()) {
                job.addFilter(new GrayFilter());
            }

            if (blurCheckBox.isSelected()) {
                job.addFilter(new BlurFilter(
                        Integer.parseInt(blurKernelField.getText()),
                        Double.parseDouble(blurSigmaField.getText())
                ));
            }

            if (edgeCheckBox.isSelected()) {
                job.addFilter(new EdgeFilter(
                        Double.parseDouble(edgeLowField.getText()),
                        Double.parseDouble(edgeHighField.getText())
                ));
            }

            if (rainbowCheckBox.isSelected()) {
                RainbowFilter.Mode dir = rainbowDirectionChoice.getValue().equals("Rows")
                        ? RainbowFilter.Mode.ROW
                        : RainbowFilter.Mode.COLUMN;
                job.addFilter(new RainbowFilter(dir));
            }

            Path cppExe = findCppExecutable();
            CppApplicationService service = new CppApplicationService(cppExe);

            service.run(job);

            imageView.setImage(new Image(tempOutputImage.toUri().toString()));
        } catch (NumberFormatException e) {
            showError("Invalid numeric parameter");
        } catch (Exception e) {
            showError("Processing failed:\n" + e.getMessage());
            e.printStackTrace();
        }

    }

    @FXML
    private void onSave() {
        if (tempOutputImage == null) {
            showError("Nothing to save. Process image first.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Image", "*.png")
        );

        File target = chooser.showSaveDialog(null);

        if (target != null) {
            try {
                Files.copy(tempOutputImage, target.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                showError("Failed to save file");
            }
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private Path findCppExecutable() {
        Path currentDir = Path.of(System.getProperty("user.dir"));
        Path searchDir = currentDir;
        
        for (int i = 0; i < 5; i++) {
            Path cppMain = searchDir.resolve("cpp/main");
            if (Files.exists(cppMain)) {
                return cppMain.toAbsolutePath();
            }
            
            Path parent = searchDir.getParent();
            if (parent == null) {
                break;
            }
            searchDir = parent;
        }
        
        throw new RuntimeException("Could not find cpp/main executable. Searched from: " + currentDir.toAbsolutePath());
    }

}
