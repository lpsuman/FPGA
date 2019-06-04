package hr.fer.zemris.dipl.lukasuman.fpga.util;

public class Timer {

    private long startTime;
    private long lastLapTime;
    private int lastLapDuration;
    private long stopTime;

    public Timer() {
        start();
    }

    public Timer(long stopTime) {
        this();
        setStopTime(stopTime);
    }

    public Timer(int stopTime) {
        this();
        setStopTime(stopTime);
    }

    public void start() {
        startTime = System.currentTimeMillis();
    }

    public int lap() {
        long temp = lastLapTime;
        lastLapTime = System.currentTimeMillis();
        lastLapDuration = (int)(lastLapTime - temp);
        return lastLapDuration;
    }

    public int getElapsedTime() {
        return (int)(System.currentTimeMillis() - startTime);
    }

    public int getLastLapDuration() {
        return lastLapDuration;
    }

    public void setStopTime(long deltaTime) {
        this.stopTime = System.currentTimeMillis() + deltaTime;
    }

    public void setStopTime(int deltaTime) {
        this.stopTime = System.currentTimeMillis() + deltaTime;
    }

    public boolean isTimeLimitReached() {
        return getRemainingTime() < 0;
    }

    public long getRemainingTime() {
        long currentTime = System.currentTimeMillis();
        return stopTime - currentTime;
    }
}
