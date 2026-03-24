package org.markdwn;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Pomodoro {

    // default times in seconds
    private static final int STUDY_TIME = 25 * 60;
    private static final int SHORT_BREAK_TIME = 5 * 60;
    private static final int LONG_BREAK_TIME = 15 * 60;

    // keep track of the selected mode and current remaining time
    private int selectedTime = STUDY_TIME;
    private int remainingSeconds = STUDY_TIME;

    // timeline is used for the countdown
    private Timeline timeline;

    public void showWindow(Stage ownerStage) {
        // create simple popup window
        Stage stage = new Stage();
        stage.initOwner(ownerStage);
        stage.initModality(Modality.NONE);
        stage.setTitle("Pomodoro");

        // heading to show current mode
        Label modeLabel = new Label("Study Time");

        // timer text shown to the user
        Label timeLabel = new Label(formatTime(remainingSeconds));

        // button for study mode
        Button studyBtn = new Button("Study 25:00");
        studyBtn.setOnAction(e -> {
            // stop old timer and switch to study mode
            stopTimer();
            selectedTime = STUDY_TIME;
            remainingSeconds = STUDY_TIME;
            modeLabel.setText("Study Time");
            timeLabel.setText(formatTime(remainingSeconds));
        });

        // button for short break mode
        Button shortBreakBtn = new Button("Short Break 5:00");
        shortBreakBtn.setOnAction(e -> {
            // stop old timer and switch to short break
            stopTimer();
            selectedTime = SHORT_BREAK_TIME;
            remainingSeconds = SHORT_BREAK_TIME;
            modeLabel.setText("Short Break");
            timeLabel.setText(formatTime(remainingSeconds));
        });

        // button for long break mode
        Button longBreakBtn = new Button("Long Break 15:00");
        longBreakBtn.setOnAction(e -> {
            // stop old timer and switch to long break
            stopTimer();
            selectedTime = LONG_BREAK_TIME;
            remainingSeconds = LONG_BREAK_TIME;
            modeLabel.setText("Long Break");
            timeLabel.setText(formatTime(remainingSeconds));
        });

        // start button starts the timer and minimizes the popup
        Button startBtn = new Button("Start");
        startBtn.setOnAction(e -> {
            // do not create multiple timelines
            if (timeline != null) {
                timeline.stop();
            }

            // countdown each second
            timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
                if (remainingSeconds > 0) {
                    remainingSeconds--;
                    timeLabel.setText(formatTime(remainingSeconds));
                } else {
                    stopTimer();
                }
            }));

            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();

            // minimize the timer window after starting
            stage.setIconified(true);
        });

        // reset button resets current mode time
        Button resetBtn = new Button("Reset");
        resetBtn.setOnAction(e -> {
            stopTimer();
            remainingSeconds = selectedTime;
            timeLabel.setText(formatTime(remainingSeconds));
        });

        // stop timer when popup is closed
        stage.setOnCloseRequest(e -> stopTimer());

        // layout 
        HBox modeButtons = new HBox(10, studyBtn, shortBreakBtn, longBreakBtn);
        HBox actionButtons = new HBox(10, startBtn, resetBtn);
        VBox root = new VBox(12, modeLabel, timeLabel, modeButtons, actionButtons);
        root.setPadding(new Insets(12));

        stage.setScene(new Scene(root, 420, 160));
        stage.show();
    }

    // convert seconds to mm:ss format
    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // small helper to stop the running timer
    private void stopTimer() {
        if (timeline != null) {
            timeline.stop();
        }
    }
}
