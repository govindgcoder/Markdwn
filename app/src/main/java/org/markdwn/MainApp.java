package org.markdwn;

// all imports

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import java.io.File;
import javafx.stage.Stage;

// for markdown
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class MainApp extends Application {

    private Path dirPath;

    private void loadDirectory(Path dirPath, ListView<String> sideBar) {
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
    
    public String convertToHtml(String markdownText){
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdownText);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String html = renderer.render(document);
        String finalHtml = "<html><body>" + html + "</body></html>";
        return finalHtml;
    }

    @Override
    public void start(Stage stage) {
        FileHelper fileHelper = new FileHelper();
        // sideBar
        ListView<String> sideBar = new ListView<>();

        // input for File
        TextArea input = new TextArea();

        // to display formatted
        WebView output = new WebView();

        // for set initial directory
        File selectedDirectory = fileHelper.directorySelector(stage);
    
        if (selectedDirectory != null) {
            dirPath = Paths.get(selectedDirectory.getAbsolutePath());
            loadDirectory(dirPath, sideBar);
        } else {
            // System.out.println("No directory selected");
            Platform.exit();
        }
        
        // button functionalities
        Button newBtn = new Button("New");
        Button saveBtn = new Button("Save");

        // for the appbar
        ToolBar appBar = new ToolBar(
            new Label("File name"),
            new Separator(),
            newBtn,
            new Separator(),
            saveBtn
        );

        SplitPane splitPane = new SplitPane();
        splitPane
            .getItems()
            .addAll(
                sideBar, // SideBar
                input, // Text edit
                output // Web view
            );

        splitPane.setDividerPositions(0.15, 0.6);

        // for reading selected file
        sideBar
            .getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, oldVal, newVal) -> {
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
            
        // debounced conversion from markdown
        PauseTransition pause = new PauseTransition(Duration.millis(500));    
        pause.setOnFinished(e -> {
            String rawMarkdown = input.getText();
            String htmlResult = convertToHtml(rawMarkdown);
            output.getEngine().loadContent(htmlResult, "text/html");
        });
        // listener to input
        input.textProperty().addListener((observable, oldValue, newValue) -> {
            // to restart the transition from the beginning
            pause.playFromStart();
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
