package org.markdwn;

import javafx.application.Platform;
import javafx.scene.control.Label;


public class Pomodoro {
    public volatile boolean isRunning = false;
    
    // State persistence
    private int secondsRemaining = 25 * 60; 
    private int currentStage = 1; // 1 = Work, 2 = Short Break, 3 = Long Break
    private int currentRep = 1;   // 1 through 4

    public void runPomo(Label label) {
        isRunning = true;

        while (isRunning) {
            // Main countdown loop for the current session
            while (secondsRemaining > 0 && isRunning) {
                int mins = secondsRemaining / 60;
                int secs = secondsRemaining % 60;
                
                String stageLabel = getStageLabel();
                String timeString = String.format("%02d:%02d", mins, secs);

                Platform.runLater(() -> label.setText(stageLabel + timeString));

                try {
                    Thread.sleep(1000);
                    secondsRemaining--;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            // Only move to the next stage if the timer actually hit zero
            if (isRunning && secondsRemaining <= 0) {
                advanceStage();
            }
        }
    }

    private String getStageLabel() {
        switch (currentStage) {
            case 1: return "Work (" + currentRep + "/4): ";
            case 2: return "Short Break: ";
            case 3: return "Long Break: ";
            default: return "Work: ";
        }
    }

    private void advanceStage() {
        if (currentStage == 1) { // Finished Work
            if (currentRep < 4) {
                currentStage = 2; // Go to Short Break
                secondsRemaining = 5 * 60;
            } else {
                currentStage = 3; // Go to Long Break
                secondsRemaining = 15 * 60;
            }
        } else if (currentStage == 2) { // Finished Short Break
            currentStage = 1;
            currentRep++;
            secondsRemaining = 25 * 60;
        } else if (currentStage == 3) { // Finished Long Break
            currentStage = 1;
            currentRep = 1; // Reset full cycle
            secondsRemaining = 25 * 60;
        }
    }

    public void stop() {
        isRunning = false;
    }

    public void reset(Label label) {
        isRunning = false;
        currentStage = 1;
        currentRep = 1;
        secondsRemaining = 25 * 60;
        Platform.runLater(() -> label.setText("Ready"));
    }
}