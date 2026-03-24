package org.markdwn;

// all imports

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;


public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        // for the appbar
        ToolBar appBar = new ToolBar(
            new Label("File name"),
            new Separator(),
            new Button("New"),
            new Separator(),
            new Button("Save")
        );

        // sideBar
        ListView<String> sideBar = new ListView<>();

        // input for File
        TextArea input = new TextArea();

        // to display formatted
        WebView output = new WebView();

        SplitPane splitPane = new SplitPane();
        splitPane
            .getItems()
            .addAll(
                sideBar, // SideBar
                input, // Column 2
                output // Column 3
            );

        splitPane.setDividerPositions(0.15, 0.7); 
        
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
