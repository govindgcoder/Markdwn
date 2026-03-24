package org.markdwn;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Modality;

public class RoadmapGenerator {

    // keep api key here so this class can talk to GeminiHelper
    private final String apiKey;

    public RoadmapGenerator(String apiKey) {
        this.apiKey = apiKey;
    }

    public void showWindow(Stage ownerStage, Path saveDirectory) {
        // create a new simple popup window
        Stage stage = new Stage();
        stage.initOwner(ownerStage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Roadmap Generator");

        // input box where user writes the learning request
        TextArea promptInput = new TextArea();
        promptInput.setPromptText("Example: I want to learn DSA using C in 30 days");
        promptInput.setWrapText(true);
        promptInput.setPrefRowCount(4);

        // result area where Gemini response will be shown
        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        resultArea.setPrefRowCount(16);

        // button to send prompt to Gemini
        Button enterBtn = new Button("Enter");

        // button to store the response as a file
        Button storeBtn = new Button("Store as a file");
        storeBtn.setDisable(true);

        // call Gemini when user clicks enter
        enterBtn.setOnAction(e -> {
            String userPrompt = promptInput.getText().trim();

            // stop empty prompt requests
            if (userPrompt.isEmpty()) {
                showMessage("Prompt missing", "Please enter a learning goal first.");
                return;
            }

            // disable buttons while request is running
            enterBtn.setDisable(true);
            storeBtn.setDisable(true);
            resultArea.setText("Generating roadmap...");

            // make the final prompt simple and clear for Gemini
            String finalPrompt = """
                    Create a simple learning roadmap for the following goal.
                    Keep it clear, practical, and easy to follow.
                    Use headings, steps, and timelines when helpful.

                    User goal:
                    """ + userPrompt;

            // use existing GeminiHelper class for api call
            GeminiHelper geminiHelper = new GeminiHelper(apiKey);
            geminiHelper.getResponse(finalPrompt)
                    .thenAccept(response -> Platform.runLater(() -> {
                        resultArea.setText(response);
                        enterBtn.setDisable(false);
                        storeBtn.setDisable(false);
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            resultArea.setText("Error while generating roadmap:\n" + ex.getMessage());
                            enterBtn.setDisable(false);
                        });
                        return null;
                    });
        });

        // save the shown roadmap into the selected folder
        storeBtn.setOnAction(e -> {
            String content = resultArea.getText().trim();

            // stop save if there is no useful content
            if (content.isEmpty() || content.equals("Generating roadmap...")) {
                showMessage("Nothing to save", "Generate a roadmap first.");
                return;
            }

            // stop save if no directory was chosen in the main app
            if (saveDirectory == null) {
                showMessage("Folder missing", "No working folder is available.");
                return;
            }

            try {
                // create a simple timestamp based text file name
                String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                Path filePath = saveDirectory.resolve("roadmap_" + time + ".txt");
                Files.writeString(filePath, content);
                showMessage("Saved", "Roadmap saved as:\n" + filePath.getFileName());
            } catch (IOException ex) {
                showMessage("Save failed", "Could not save the file.");
            }
        });

        // use a basic layout because user requested simple ui
        VBox root = new VBox(10);
        root.setPadding(new Insets(12));
        root.getChildren().addAll(
                new Label("Enter your learning prompt"),
                promptInput,
                enterBtn,
                new Label("Generated roadmap"),
                resultArea,
                storeBtn
        );

        stage.setScene(new Scene(root, 700, 500));
        stage.show();
    }

    // simple reusable alert helper
    private void showMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
