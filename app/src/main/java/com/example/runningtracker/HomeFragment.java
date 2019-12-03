package com.example.runningtracker;

import android.Manifest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import java.util.Calendar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class HomeFragment extends Fragment {

    TrackerService trackerService;

    private DbHelper dbHelper;

    public static final int LOCATION_REQUEST_CODE = 3231;

    private Timer timer;
    private TimerTask updateDistanceTask;
    private TimerTask updateTimeTask;
    Thread updateUIThread ;
    Button runningButton;
    int totalTime = 0;
    TextView runningDistanceTextView;
    TextView totalTimeTextView;
    boolean isRunning = false;
    boolean isServiceBounded = false;
    private long startDateAndTime;
    private Boolean isComeBackFromPendingIntent = false;


    @Override
    public void onStop() {
        super.onStop();

        if(isServiceBounded){
            timer.cancel();
            timer.purge();
            SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("return", true);

            getContext().unbindService(connection);
            editor.commit();
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences("MyPref", 0);
        SharedPreferences.Editor editor = pref.edit();

        isComeBackFromPendingIntent = getActivity().getIntent().getBooleanExtra("return",false);
        if(isComeBackFromPendingIntent || pref.getBoolean("return",false)){
            Intent intent = new Intent(getContext(),TrackerService.class);
            getContext().bindService(intent, connection, Context.BIND_AUTO_CREATE);
            startCalculatingDistance();
            editor.putBoolean("return",false);
            editor.commit();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment,container,false);
        runningButton = (Button) view.findViewById(R.id.runningButton);
        runningDistanceTextView = (TextView) view.findViewById(R.id.runningDistanceTextView);
        totalTimeTextView = (TextView) view.findViewById(R.id.timeTextView);
        toggleRunningButton();
        setButtonClickListener();
        return view;

    }

    private void toggleRunningButton(){
        if(isRunning){
            runningButton.setText("Stop");
        }else{
            runningButton.setText("Start");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DbHelper(this.getContext(), null, null, 1);
        checkPermision();

    }

    public void checkPermision(){
        if(ContextCompat.checkSelfPermission(getContext(),Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            runningButton.setEnabled(false);
            runningButton.setText("GPS is Disabled");
            if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
            }else{
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
            }
        }
    }

    public void setButtonClickListener(){

        runningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRunning = !isRunning;
                toggleRunningButton();
                if(isRunning){
                    Intent intent = new Intent(getContext(),TrackerService.class);
                    getContext().startService(intent);
                    getContext().bindService(intent, connection, Context.BIND_AUTO_CREATE);
                    startDateAndTime = Calendar.getInstance().getTimeInMillis() - (long) 2*30*24*60*60*1000;
                }else{
                    RunningSession session = new RunningSession(trackerService.tracker.getLocationStringList(),startDateAndTime,trackerService.tracker.getTotalRunningDistance(),trackerService.tracker.getTotalTime(),trackerService.tracker.getTotalRunningDistance() * 1000 / 60);
                    dbHelper.add(session);
                    Intent intent = new Intent(getContext(),TrackerService.class);
                    getContext().stopService(intent);
                    getContext().unbindService(connection);
                    isServiceBounded = false;
                    timer.cancel();
                }
            }
        });


    }

    public void startCalculatingDistance(){
        try{
            timer = new Timer();
            updateDistanceTask = new TimerTask() {
                @Override
                public void run() {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                runningDistanceTextView.setText(String.format("%.2f",trackerService.tracker.getTotalRunningDistance()) + " Km");
                            }
                        });
                    }

                }
            };
            updateTimeTask = new TimerTask() {

                @Override
                public void run() {
                    if(getActivity()!=null){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                totalTimeTextView.setText(minuteSecondString( trackerService.tracker.getTotalTime() * 1000));
                            }
                        });
                    }

                }
            };

            timer.schedule(updateTimeTask,0,1000);
            timer.schedule(updateDistanceTask,1000,10000);
        }catch (IllegalStateException e){

        }
    }

    public String minuteSecondString(int millsec){
        int hours = (millsec/1000)/60/60;
        int minutes = (millsec/1000) /60;
        int second = (millsec/1000) % 60;

        if(second<10){
            return minutes + ":0" + second;
        }else if(hours<1){
            return minutes + ":" + second;
        }else{
            return hours + ":" + minutes + ":" +second ;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case LOCATION_REQUEST_CODE:
            {
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    runningButton.setEnabled(true);
                }else{
                    runningButton.setEnabled(false);
                }
            }
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            TrackerService.MyBinder binder = (TrackerService.MyBinder) iBinder;
            trackerService = binder.getService();
            isServiceBounded = true;
            startCalculatingDistance();
            Log.d("mama","connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

}
