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
import javafx.scene.control.TextInputDialog;
import java.util.Optional;
import javafx.scene.web.WebView;
import java.io.IOException;
import javafx.stage.Stage;

// for markdown
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class MainApp extends Application {

    private Path dirPath;
    private Path currentActiveFile;
    // default api key for gemini features
    private static final String DEFAULT_GEMINI_API_KEY = "AIzaSyCqyHBkXPXpr2rR-JAVwgncIdpKeHjg6Y8";

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
        // helper for directory related work
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
        newBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog("new file");
            dialog.setTitle("New File?");
            dialog.setHeaderText("Enter file name");
            dialog.setContentText("file name...");
            // Show the dialog and wait for the user's response
            Optional<String> result = dialog.showAndWait();
            
            // Process the result if it is present (user clicked 'OK')
            result.ifPresent(name -> {
                Path newFilePath = dirPath.resolve(name);
                try {
                Files.writeString(newFilePath, "");
                } catch (IOException ev){}
                
                currentActiveFile = newFilePath;
                input.setText("");
                loadDirectory(dirPath, sideBar);
            });
        });
        
        // save function
        Button saveBtn = new Button("Save");
        saveBtn.setOnAction(e -> {
            if(currentActiveFile == null) return;
            try {
                Files.writeString(currentActiveFile, input.getText());
            } catch (IOException ex){}
        });

        // button for roadmap generator window
        Button roadmapBtn = new Button("Roadmap Generator");
        roadmapBtn.setOnAction(e -> {
            RoadmapGenerator roadmapGenerator = new RoadmapGenerator(DEFAULT_GEMINI_API_KEY);
            roadmapGenerator.showWindow(stage, dirPath);
        });

        // basic placeholder button for future nlp feature
        Button nlpBtn = new Button("NLP");
        nlpBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("NLP");
            alert.setHeaderText("NLP feature");
            alert.setContentText("This button is added. Logic can be added later.");
            alert.showAndWait();
        });

        // button for simple pomodoro window
        Button pomodoroBtn = new Button("Pomodoro");
        pomodoroBtn.setOnAction(e -> {
            Pomodoro pomodoro = new Pomodoro();
            pomodoro.showWindow(stage);
        });
        
        // for the appbar
        ToolBar appBar = new ToolBar(
            new Label("File name"),
            new Separator(),
            newBtn,
            new Separator(),
            saveBtn,
            new Separator(),
            roadmapBtn,
            new Separator(),
            nlpBtn,
            new Separator(),
            pomodoroBtn
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
                        currentActiveFile = filePath;
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
