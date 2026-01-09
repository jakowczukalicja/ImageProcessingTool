package com.project.imagetool.controller;

import com.project.imagetool.filter.*;
import com.project.imagetool.model.ImageJob;
import com.project.imagetool.service.CppApplicationService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class MainController {
    @FXML private ImageView imageView;
    @FXML private Label placeholderLabel;

    @FXML private CheckBox grayCheckBox;
    @FXML private CheckBox heartCheckBox;
    @FXML private CheckBox roseCheckBox;
    @FXML private CheckBox blurCheckBox;
    @FXML private CheckBox edgeCheckBox;
    @FXML private CheckBox rainbowCheckBox;
    @FXML private CheckBox singleColourCheckBox;

    @FXML private Button processBtn;
    @FXML private ColorPicker singleColourPicker;

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
        singleColourPicker.setValue(Color.WHITE);
    }

    @FXML
    private void onLoadImage() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg")
        );
        inputImage = chooser.showOpenDialog(null);

        if (inputImage != null) {
            tempOutputImage = null;
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
        
        processBtn.setDisable(true);
        
        try {
            Path inputPath = (tempOutputImage != null && Files.exists(tempOutputImage))
                    ? tempOutputImage 
                    : inputImage.toPath();
            
            Path oldOutput = tempOutputImage;
            tempOutputImage = Files.createTempFile("imgtool_", ".png");

            ImageJob job = new ImageJob(
                    inputPath,
                    tempOutputImage
            );

            if (grayCheckBox.isSelected()) {
                job.addFilter(new GrayFilter());
            }

            if (heartCheckBox.isSelected()) {
                job.addFilter(new HeartFilter());
            }

            if (roseCheckBox.isSelected()) {
                job.addFilter(new RoseFilter());
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

            if (singleColourCheckBox.isSelected()) {
                Color color = singleColourPicker.getValue();
                int red = (int) (color.getRed() * 255);
                int green = (int) (color.getGreen() * 255);
                int blue = (int) (color.getBlue() * 255);
                job.addFilter(new SingleColourFilter(red, green, blue));
            }

            Path cppExe = findCppExecutable();
            CppApplicationService service = new CppApplicationService(cppExe);

            Task<Void> processTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    service.run(job);
                    return null;
                }
            };

            // Update UI on success
            processTask.setOnSucceeded(e -> {
                try {
                    // Delete old temp file if it exists (from previous processing)
                    if (oldOutput != null && Files.exists(oldOutput) && !oldOutput.equals(tempOutputImage)) {
                        try {
                            Files.delete(oldOutput);
                        } catch (Exception deleteEx) {
                            System.err.println("Could not delete old temp file: " + deleteEx.getMessage());
                        }
                    }

                    // Load and display the new processed image
                    imageView.setImage(new Image(tempOutputImage.toUri().toString()));
                    processBtn.setDisable(false);
                } catch (Exception ex) {
                    showError("Failed to load processed image: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });

            // Handle errors
            processTask.setOnFailed(e -> {
                Throwable exception = processTask.getException();
                showError("Processing failed:\n" + exception.getMessage());
                exception.printStackTrace();
            });

            // Start the background task
            new Thread(processTask).start();
            
        } catch (NumberFormatException e) {
            showError("Invalid numeric parameter");
        } catch (Exception e) {
            showError("Failed to start processing:\n" + e.getMessage());
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
