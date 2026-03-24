package org.markdwn;

// all imports

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import java.io.File;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;



public class MainApp extends Application {
    
    private Path dirPath;
    
    private void loadDirectory(Path dirPath, ListView<String> sideBar){
        sideBar.getItems().clear();
        
        File folder = dirPath.toFile();
        File[] files = folder.listFiles();
        
        if (files == null) return;
        
        for (File file : files) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(".md")) {
                sideBar.getItems().add(file.getName());
            }
        }
    }

    @Override
    public void start(Stage stage) {
        // sideBar
        ListView<String> sideBar = new ListView<>();

        // input for File
        TextArea input = new TextArea();

        // to display formatted
        WebView output = new WebView();
        
        // for selecting folder we use DirectoryChooser
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a Directory");
    
        // for set initial directory
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
    
        File selectedDirectory = directoryChooser.showDialog(stage);
    
        if (selectedDirectory != null) {
            dirPath = Paths.get(selectedDirectory.getAbsolutePath());
            loadDirectory(dirPath, sideBar);
        } else {
            // System.out.println("No directory selected");
            Platform.exit();
        }
        
        // for the appbar
        ToolBar appBar = new ToolBar(
            new Label("File name"),
            new Separator(),
            new Button("New"),
            new Separator(),
            new Button("Save")
        );

        SplitPane splitPane = new SplitPane();
        splitPane
            .getItems()
            .addAll(
                sideBar, // SideBar
                input, // Column 2
                output // Column 3
            );

        splitPane.setDividerPositions(0.15, 0.7); 
        
        // for reading selected file
        sideBar.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                try {
                    // select the file
                    Path filePath = dirPath.resolve(newVal);
                    // get content
                    String content = Files.readString(filePath);
                    input.setText(content);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        // for main layout:
        BorderPane root = new BorderPane();
        root.setTop(appBar);
        root.setCenter(splitPane);

        // Scene is container for all elements
        Scene scene = new Scene(root, 1200, 600); 
        stage.setTitle("Markdwn");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
