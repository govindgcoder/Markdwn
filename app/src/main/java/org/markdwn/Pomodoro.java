package org.markdwn;

public class Pomodoro {
    int[] pomodoroGetTimeSec(int stage, int reps){
        // Timer , Stage
        int[] out = new int[2];

        switch (stage) {
            case 1:
                out[0] = 25*60; //25 mins
                out[1] = stage+1;
                break;
            case 2:
                out[0] = 5*60; //% mins break
                out[1] = stage+1;
            default:
                out[0] = 25*60; // start over
                out[1] = 1;
                break;
        }

        return out;
    }

}
