package com.example.runningtracker;

import android.location.Location;

import java.util.Date;
import java.util.List;

public class RunningSession {

    private int id;
    private String stringList;
    private long date;
    private float totalDistance;
    private int totalTime;
    private float avgSpeed;

    public RunningSession(String stringList, long date, float totalDistance, int totalTime, float avgSpeed){
        this.date = date;
        this.totalDistance = totalDistance;
        this.totalTime = totalTime;
        this.avgSpeed = avgSpeed;
        this.stringList = stringList;
    }


    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public float getAvgSpeed() {
        return avgSpeed;
    }

    public float getTotalDistance() {
        return totalDistance;
    }

    public float getTotalTime() {
        return totalTime;
    }

    public long getDate() {
        return date;
    }

    public String getStringList() {
        return stringList;
    }
}
