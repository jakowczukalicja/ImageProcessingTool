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
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.IOException;
import com.project.imagetool.service.LogManager;
import com.project.imagetool.service.TempFileManager;
import com.project.imagetool.util.InputFormatters;
import com.project.imagetool.util.ValidationUtil;

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
    @FXML private Button revertImageBtn;

    @FXML private ColorPicker singleColourPicker;

    @FXML private TextField blurKernelField;
    @FXML private TextField blurSigmaField;
    @FXML
    private TextField edgeLowField;
    @FXML private TextField edgeHighField;
    @FXML private ChoiceBox<String> rainbowDirectionChoice;
    @FXML private ToggleButton logsToggleButton;
    @FXML private VBox logScrollPane;
    @FXML private TextArea logTextArea;

    private File inputImage;
    private final TempFileManager tempFileManager = new TempFileManager();

    @FXML
    public void initialize() {
        rainbowDirectionChoice.getItems().addAll("Rows", "Columns");
        rainbowDirectionChoice.setValue("Rows");
        singleColourPicker.setValue(Color.WHITE);

        if (logsToggleButton != null && logScrollPane != null && logTextArea != null) {
            logsToggleButton.setSelected(false);
            logScrollPane.setVisible(false);
            logScrollPane.setManaged(false);
            logsToggleButton.setOnAction(evt -> {
                boolean show = logsToggleButton.isSelected();
                logScrollPane.setVisible(show);
                logScrollPane.setManaged(show);
                logsToggleButton.setText(show ? "Hide Logs" : "Show Logs");
            });

            // Attach numeric formatters
            try {
                blurKernelField.setTextFormatter(InputFormatters.createIntegerFormatter());
                blurSigmaField.setTextFormatter(InputFormatters.createDecimalFormatter());
                edgeLowField.setTextFormatter(InputFormatters.createDecimalFormatter());
                edgeHighField.setTextFormatter(InputFormatters.createDecimalFormatter());
            } catch (Exception ex) {
                // ignore formatter setup failures
            }
        }

        // Ensure process button is enabled only when at least one filter is selected
        CheckBox[] filterBoxes = new CheckBox[] {
                grayCheckBox, heartCheckBox, roseCheckBox,
                blurCheckBox, edgeCheckBox, rainbowCheckBox, singleColourCheckBox
        };

        for (CheckBox cb : filterBoxes) {
            if (cb != null) {
                cb.selectedProperty().addListener((obs, oldV, newV) -> updateProcessButtonState());
            }
        }

        // Set initial state
        updateProcessButtonState();

        // Register shutdown hook to clean up temporary files when the JVM exits - destructor
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    tempFileManager.cleanupAll();
                } catch (Exception ignore) {}
            }));
        } catch (Exception ignore) {}
    }

    private void updateProcessButtonState() {
        if (processBtn == null) return;
        boolean anySelected = (grayCheckBox != null && grayCheckBox.isSelected())
                || (heartCheckBox != null && heartCheckBox.isSelected())
                || (roseCheckBox != null && roseCheckBox.isSelected())
                || (blurCheckBox != null && blurCheckBox.isSelected())
                || (edgeCheckBox != null && edgeCheckBox.isSelected())
                || (rainbowCheckBox != null && rainbowCheckBox.isSelected())
                || (singleColourCheckBox != null && singleColourCheckBox.isSelected());
        processBtn.setDisable(!anySelected);
    }

    @FXML
    private void onLoadImage() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg")
        );
        inputImage = chooser.showOpenDialog(null);

        if (inputImage != null) {
            // discard any previous temp output when loading a new image
            try { tempFileManager.deleteTempOutput(); } catch (Exception ignore) {}
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
                Path inputPath = (tempFileManager.getTempOutputImage() != null && Files.exists(tempFileManager.getTempOutputImage()))
                    ? tempFileManager.getTempOutputImage()
                    : inputImage.toPath();

            // validate parameters before creating temp files
            if (!ValidationUtil.validateParameters(
                    (blurCheckBox != null && blurCheckBox.isSelected()),
                    blurKernelField.getText(),
                    blurSigmaField.getText(),
                    (edgeCheckBox != null && edgeCheckBox.isSelected()),
                    edgeLowField.getText(),
                    edgeHighField.getText(),
                    this::showError
            )) {
                processBtn.setDisable(false);
                return;
            }

                // prepare previous output: if there's a temp output, create prev from it, otherwise from the original input
                try {
                    if (tempFileManager.getPrevOutputImage() != null && Files.exists(tempFileManager.getPrevOutputImage())) {
                        try { Files.delete(tempFileManager.getPrevOutputImage()); } catch (Exception ignore) {}
                    }

                    if (tempFileManager.getTempOutputImage() != null && Files.exists(tempFileManager.getTempOutputImage())) {
                        tempFileManager.createPrevFrom(tempFileManager.getTempOutputImage());
                        if (revertImageBtn != null) revertImageBtn.setDisable(false);
                    } else if (inputImage != null) {
                        tempFileManager.createPrevFrom(inputImage.toPath());
                        if (revertImageBtn != null) revertImageBtn.setDisable(false);
                    }
                } catch (Exception ex) {
                    System.err.println("Could not create prev temp output: " + ex.getMessage());
                }

                Path oldOutput = tempFileManager.getTempOutputImage();
                Path newTemp = tempFileManager.createTempOutput();

                ImageJob job = new ImageJob(
                    inputPath,
                    newTemp
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
                    if (oldOutput != null && Files.exists(oldOutput) && !oldOutput.equals(tempFileManager.getTempOutputImage())) {
                        try {
                            Files.delete(oldOutput);
                        } catch (Exception deleteEx) {
                            System.err.println("Could not delete old temp file: " + deleteEx.getMessage());
                        }
                    }
                    // Load and display the new processed image
                    imageView.setImage(new Image(tempFileManager.getTempOutputImage().toUri().toString()));
                    // Append any new log lines from processing_log.txt to the log view
                    LogManager.appendProcessingLog(logTextArea);
                    processBtn.setDisable(false);
                } catch (Exception ex) {
                    showError("Failed to load processed image: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });

            // Handle errors: log to UI, clean up temp files and re-enable UI
            processTask.setOnFailed(e -> {
                Throwable exception = processTask.getException();

                StringWriter sw = new StringWriter();
                exception.printStackTrace(new PrintWriter(sw));
                String trace = sw.toString();

                if (logTextArea != null) {
                    logTextArea.appendText("Processing failed: " + exception.getMessage() + System.lineSeparator());
                    LogManager.appendProcessingLog(logTextArea);
                    logTextArea.positionCaret(logTextArea.getLength());
                } else {
                    System.err.println("Processing failed: " + exception.getMessage());
                    exception.printStackTrace();
                }

                Platform.runLater(() -> showError("Processing failed:\n" + exception.getMessage()));

                // Attempt to remove the temporary output file created for this run so future runs don't reuse a corrupted file
                try { tempFileManager.deleteTempOutput(); } catch (Exception ignore) {}

                if (processBtn != null) processBtn.setDisable(false);
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
        if (tempFileManager.getTempOutputImage() == null) {
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
                Files.copy(tempFileManager.getTempOutputImage(), target.toPath(),
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
    

    @FXML
    private void onRevertImage() {
        if (tempFileManager.getPrevOutputImage() == null || !Files.exists(tempFileManager.getPrevOutputImage())) return;
        try {
            Path restored = tempFileManager.restorePrevToTemp();
            if (revertImageBtn != null) revertImageBtn.setDisable(true);

            // load into UI
            if (restored != null) imageView.setImage(new Image(restored.toUri().toString()));
        } catch (IOException ex) {
            System.err.println("Failed to revert image: " + ex.getMessage());
        }
    }

}
