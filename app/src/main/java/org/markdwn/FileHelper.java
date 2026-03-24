package org.markdwn;

import java.io.File;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class FileHelper {

    public File directorySelector(Stage stage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a Directory");

        // for set initial directory
        directoryChooser.setInitialDirectory(
            new File(System.getProperty("user.home"))
        );

        return directoryChooser.showDialog(stage);
    }
}
