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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

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
    
    private String apiKey = null;
    private Pomodoro pomo = new Pomodoro();

    private MenuItem pomoStart = new MenuItem("Start");
    private MenuItem pomoStop = new MenuItem("Stop");
    private MenuItem pomoReset = new MenuItem("Reset");

    private Label pomoLabel = new Label("");
    private int pomoSleep = 0;

    private int[] pomodoroState = {1, 1}; // sleep time, stage

    private Path dirPath;
    private Path currentActiveFile;

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
        pomoStop.setDisable(true);
        pomoReset.setDisable(true);
        // FileHelper fileHelper = new FileHelper();
        Startup startup = new Startup();
        // for filename on appbar
        Label currentFileLabel = new Label("No File Selected");
        

        // sideBar
        ListView<String> sideBar = new ListView<>();

        // input for File
        TextArea input = new TextArea();

        // to display formatted
        WebView output = new WebView();

        // for set initial directory
        // File selectedDirectory = fileHelper.directorySelector(stage);
        File selectedDirectory = startup.DirSelect(stage);
        
        if (selectedDirectory != null) {
            dirPath = Paths.get(selectedDirectory.getAbsolutePath());
            loadDirectory(dirPath, sideBar);
        } else {
            // System.out.println("No directory selected");
            Platform.exit();
        }
        
        // button functionalities
        ToolBar appBar = new ToolBar(); 
        
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
                if (!name.endsWith(".md")) { name += ".md"; }
                currentFileLabel.setText(name);
                Path newFilePath = dirPath.resolve(name);
                try {
                Files.writeString(newFilePath, "");
                } catch (IOException ex){
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save file: " + ex.getMessage());
                    alert.showAndWait();
                }
                
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
            } catch (IOException ex){
                // Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save file: " + ex.getMessage());
                // alert.showAndWait();
            }
        });
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        //for ai functionalitie
        Button setAPIKeyBtn = new Button("Set API Key");
        setAPIKeyBtn.setOnAction(e->{
            TextInputDialog dialog = new TextInputDialog("Set API Key");
            dialog.setTitle("Set API Key");
            dialog.setHeaderText("enter API Key:");
            dialog.setContentText("");
            // Show the dialog and wait for the user's response
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(key -> {
                this.apiKey = key;
            });
            
        });
        
        Button nlpBtn = new Button("NLP classification");
        
        Button roadmapBtn = new Button("Roadmap Generator");
        roadmapBtn.setOnAction(e -> {
            if (apiKey == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please set API Key first");
                alert.showAndWait();
                return;
            }

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Roadmap Generator");
            dialog.setHeaderText("Enter Topic for Roadmap");
            dialog.setContentText("Topic for Roadmap:");
            
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(topic -> {
                GeminiHelper helper = new GeminiHelper(apiKey);
                String prompt = "Create a detailed roadmap in markdown format (no emojies, pure markdown format) for learning: " + topic;
                // asynchronous helper call uses Java's built-in ForkJoin pool.
                sideBar.setDisable(true);
                input.setText("Loading... Please Wait!");
                appBar.setDisable(true);
                helper.getResponse(prompt)
                      .thenAccept(resultVal -> {
                          // This block automatically runs when the network call finishes
                          Platform.runLater(() -> input.setText(resultVal));
                          // create new file and paste result
                          sideBar.setDisable(false);
                          appBar.setDisable(false);
                          String name = topic+".md";
                          currentFileLabel.setText(name);
                          Path newFilePath = dirPath.resolve(name);
                          try {
                          Files.writeString(newFilePath, input.getText());
                          } catch (IOException ex){
                              Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save file: " + ex.getMessage());
                              alert.showAndWait();
                          }
                          
                          currentActiveFile = newFilePath;
                          loadDirectory(dirPath, sideBar);
                      })
                      .exceptionally(ex -> {
                          // This catches any network/JSON errors seamlessly
                          Platform.runLater(() -> {
                              Alert alert = new Alert(Alert.AlertType.ERROR, "API Error: " + ex.getMessage());
                              alert.showAndWait();
                              sideBar.setDisable(false);
                              appBar.setDisable(false);
                          });
                          return null;
                    });
                
            });
        });

        // Pomodoro
        MenuButton menuButton = new MenuButton("Pomodoro Timer", null, pomoStart, pomoStop, pomoReset);
        pomoStart.setOnAction(e -> {
            pomoStop.setDisable(false);
            pomodoroState = pomo.pomodoroGetTimeSec(pomodoroState[0], pomodoroState[1]);
            Thread pstart = new Thread(() -> {
                int a = 0;
            });
        });
        // for the appbar
        appBar.getItems().addAll(
            currentFileLabel,
            new Separator(),
            newBtn,
            new Separator(),
            saveBtn,
            new Separator(),
            setAPIKeyBtn,
            nlpBtn,
            roadmapBtn,
            spacer,
            menuButton,
            pomoLabel
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
                        currentFileLabel.setText(newVal);
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
