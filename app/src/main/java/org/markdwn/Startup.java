package org.markdwn;

import java.io.File;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.image.Image;

public class Startup {

    private File selectedDirectory;

    public File DirSelect(Stage ownerStage) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(ownerStage);
        popup.setTitle("Markdwn");

        Label message = new Label("Welcome! Select a folder to begin.");
        Button startBtn = new Button("Open Folder");

        VBox layout = new VBox(20, message, startBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 40;");

        Scene scene = new Scene(layout, 350, 200);

        try {
            String css = getClass().getResource("/style.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.err.println("Warning: style.css not found in resources folder.");
        }

        try {
            Image appIcon = new Image(getClass().getResourceAsStream("/icon.png"));
            popup.getIcons().add(appIcon);
        } catch (Exception e) {
            System.err.println("Warning: icon.png not found in resources folder.");
        }

        popup.setScene(scene);

        startBtn.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Open Folder");
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));

            selectedDirectory = directoryChooser.showDialog(ownerStage);
            popup.close();
        });

        popup.showAndWait();
        return selectedDirectory;
    }
}