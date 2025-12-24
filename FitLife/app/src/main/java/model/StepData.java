package model;

public class StepData {
    private String day;
    private int stepCount;

    public StepData(String day, int stepCount) {
        this.day = day;
        this.stepCount = stepCount;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }
}
