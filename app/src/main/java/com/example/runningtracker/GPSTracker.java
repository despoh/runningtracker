package com.example.runningtracker;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationListener;

import java.security.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

public class GPSTracker extends Service implements LocationListener {

    private final Context context;
    List<Location> locationList = new ArrayList<>();
    float totalRunningDistance = 0 ;
    private int totalTime = 0;
    String locationStringList = "";

    LocationManager manager ;
    private static final long MIN_TIME = 5;
    private static final long MIN_DISTANCE = 5;

    private Timer timer;

    public GPSTracker(Context context){
        this.context = context;
        manager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

    }


    public void startGPS(){
        try{
            if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME,MIN_DISTANCE,this);
                startTimer();
            }
        }catch(SecurityException e){
            Log.d("mama","error");
        }

    }

    public void startTimer(){
        try{
             timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    totalTime += 1;
                }
            };
            timer.schedule(task,0,1000);
        }catch (IllegalStateException e){

        }

    }

    public void stopGPS(){
        timer.cancel();
        manager.removeUpdates(GPSTracker.this);
    }

    public int getTotalTime() {
        return totalTime;
    }

    public float getTotalRunningDistance(){
        return totalRunningDistance;
    }

    public String getLocationStringList(){
        return locationStringList;
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        locationList.add(location);
        if(locationList.size()>1){
            totalRunningDistance += location.distanceTo(locationList.get(locationList.size()-2))/1000;
            locationStringList += " + " + location.getLatitude() + "," + location.getLongitude();
        }else{
            locationStringList += location.getLatitude() + "," + location.getLongitude();
        }

        Log.d("mama","tracking" + location.getLongitude() + ", " + location.getLatitude() );
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
}
