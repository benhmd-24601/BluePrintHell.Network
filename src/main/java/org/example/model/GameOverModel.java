package org.example.model;

public class GameOverModel {
    private double lossPercent;

    public GameOverModel(double lossPercent) {
        this.lossPercent = lossPercent;
    }

    public double getLossPercent() { return lossPercent; }

    public void setLossPercent(double lossPercent) {
        this.lossPercent = lossPercent;
    }
}