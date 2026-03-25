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

import java.util.List;
import java.util.Map;

import javafx.scene.image.Image;

public class MainApp extends Application {

    public volatile boolean pomoRunning = false;
    
    private String apiKey = null;
    private Pomodoro pomo = new Pomodoro();

    private MenuItem pomoStart = new MenuItem("Start");
    private MenuItem pomoStop = new MenuItem("Stop");
    private MenuItem pomoReset = new MenuItem("Reset");

    private Label pomoLabel = new Label("");

    private Path dirPath;
    private Path currentActiveFile;
    
    private Stage createLoadingDialog(Stage owner, String message) {
        Stage loadingStage = new Stage();
        loadingStage.initOwner(owner);
        loadingStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        loadingStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
    
        ProgressIndicator spinner = new ProgressIndicator();
        
        Label msgLabel = new Label(message);
        
        javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(10, spinner, msgLabel);
        vbox.setStyle("-fx-padding: 20; -fx-background-color: white; -fx-border-color: gray; -fx-border-width: 2;");
        vbox.setAlignment(javafx.geometry.Pos.CENTER);
    
        Scene scene = new Scene(vbox);
        loadingStage.setScene(scene);
        
        return loadingStage;
    }

    private void loadDirectory(Path dirPath, TreeView<String> sideBar) {
        sideBar.getRoot().getChildren().clear();

        File folder = dirPath.toFile();
        File[] files = folder.listFiles();

        if (files == null) return;

        for (File file : files) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(".md")) {
                    sideBar.getRoot().getChildren().add(new TreeItem<>(file.getName()));
            }
        }
    }
    
    public String convertToHtml(String markdownText){
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdownText);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String html = renderer.render(document);
        
        String style = "<style>body { font-family: 'Segoe UI', sans-serif; padding: 20px; color: #333; background-color: #fff; border-radius: 12px; }</style>";
        
        String finalHtml = "<html><head>" + style + "</head><body>" + html + "</body></html>";
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
        TreeView<String> sideBar = new TreeView<>();
        sideBar.setShowRoot(false);
        TreeItem<String> rootItem = new TreeItem<>("Root");
        sideBar.setRoot(rootItem);

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
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save file: " + ex.getMessage());
                alert.showAndWait();
            }
        });
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        //for ai functionalitie
        Button setAPIKeyBtn = new Button("Set Gemini API Key");
        setAPIKeyBtn.setOnAction(e->{
            TextInputDialog dialog = new TextInputDialog("Set Gemini API Key");
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
        
        nlpBtn.setOnAction(e -> {
            if (apiKey == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please set API Key first");
                alert.showAndWait();
                return;
            }
            
            sideBar.setDisable(true);
            appBar.setDisable(true);
        
            NlpClassifierService service = new NlpClassifierService(apiKey, dirPath);
            Stage loadingDialog = createLoadingDialog(stage, "AI is processing...");
            loadingDialog.show();
            service.categorizeFiles()
                .thenAccept(categoryMap -> {
                    Platform.runLater(() -> {
                        loadingDialog.close();
                        rootItem.getChildren().clear();
                        if (categoryMap == null) return;
                        for (Map.Entry<String, List<String>> entry : categoryMap.entrySet()) {
                            String categoryName = entry.getKey();
                            List<String> files = entry.getValue();
                            TreeItem<String> categoryItem = new TreeItem<>(categoryName);
                            if (files != null) {
                                for (String file : files) {
                                    TreeItem<String> fileItem = new TreeItem<>(file);
                                    categoryItem.getChildren().add(fileItem);
                                }
                            }
                            rootItem.getChildren().add(categoryItem);
                        }
                    });
                    sideBar.setDisable(false);
                    appBar.setDisable(false);
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        loadingDialog.close();
                        Alert alert = new Alert(Alert.AlertType.ERROR, "NLP classification failed: " + ex.getMessage());
                        alert.showAndWait();
                    });
                    sideBar.setDisable(false);
                    appBar.setDisable(false);
                    return null;
                });
        });

        
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
                Stage loadingDialog = createLoadingDialog(stage, "AI is processing...");
                loadingDialog.show();
                helper.getResponse(prompt)
                      .thenAccept(resultVal -> {
                          // This block automatically runs when the network call finishes
                          Platform.runLater(() -> input.setText(resultVal));
                          loadingDialog.close();
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
                              loadingDialog.close();
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
            if (!pomo.isRunning) { // Prevent multiple threads from starting
                pomoStop.setDisable(false);
                Thread pstart = new Thread(() -> pomo.runPomo(pomoLabel));
                pstart.setDaemon(true);
                pstart.start();
            }
        });

        pomoStop.setOnAction(event -> {
            pomo.stop();
            pomoReset.setDisable(false);
        });

        pomoReset.setOnAction(e -> {
            pomo.reset(pomoLabel);
            pomoReset.setDisable(true);
            pomoStop.setDisable(true);
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
                if (newVal != null && newVal.isLeaf()) {
                    try {
                        // select the file
                        currentFileLabel.setText(newVal.getValue());
                        Path filePath = dirPath.resolve(newVal.getValue());
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
                                
                try {
                    String css = getClass().getResource("/style.css").toExternalForm();
                    scene.getStylesheets().add(css);
                } catch (NullPointerException e) {
                    System.err.println("Warning: style.css not found in resources folder.");
                }
        
                try {
                    javafx.scene.image.Image appIcon = new javafx.scene.image.Image(getClass().getResourceAsStream("/icon.png"));
                    stage.getIcons().add(appIcon);
                } catch (NullPointerException e) {
                    System.err.println("Warning: icon.png not found in resources folder.");
                }
                        
                stage.setTitle("Markdwn");
                stage.setScene(scene);
                stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
