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

public class Startup {

    File selectedDirectory;

    File DirSelect(Stage stage){
        // FileHelper fileHelper = new FileHelper();

        

        // return directoryChooser.showDialog(stage);

        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL); // blocks interaction with other windows
        popup.initOwner(stage);
        popup.setTitle("Markdwn");

        Label message = new Label("Welcome! Select a folder to begin.");

        // final File selectedDirectory;
        Button startBtn = new Button("Open Folder");
        startBtn.setOnAction(e -> {
            popup.close(); // kill the popup
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Open Folder");

            // for set initial directory
            directoryChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
            );
            selectedDirectory = directoryChooser.showDialog(stage); // start your function
        });
        
        VBox layout = new VBox(20, message, startBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 40;");

        popup.setScene(new Scene(layout, 350, 200));
        popup.showAndWait(); // blocks until popup is closed

        return selectedDirectory;
    }
}
