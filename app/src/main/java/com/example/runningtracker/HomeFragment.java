package com.example.runningtracker;

import android.Manifest;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import java.util.Calendar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
    Button runningButton;
    TextView runningDistanceTextView;
    TextView totalTimeTextView;
    TextView msgToShowModeTextView;
    boolean isRunning = false;
    boolean isServiceBounded = false;
    private long startDateAndTime;
    private Boolean isComeBackFromPendingIntent = false;
    private String exerciseMode = "Run";
    Switch walkingModeSwitchButton;

    //isrunning and isservicebounded is necessary as the service can be running while
    //being unbinded to the fragment.

    @Override
    public void onStop() {
        Log.d("mama","onStopcalled");

        super.onStop();

        if(isServiceBounded){
            timer.cancel();
            timer.purge();
            isServiceBounded = false;
            SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("return", true);
            getContext().unbindService(connection);
            editor.commit();


        }

    }

    @Override
    public void onDestroy() {
        Log.d("mama","onDestroy called");
        super.onDestroy();
            if(isRunning){
                timer.cancel();
                timer.purge();
                saveSessionAndKillService(isServiceBounded);
                SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("return", false);
                editor.commit();
            }

            getContext().unregisterReceiver(gpsReceiver);

    }



    @Override
    public void onStart() {
        super.onStart();
        checkPermision();

        SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences("MyPref", 0);
        SharedPreferences.Editor editor = pref.edit();

        isComeBackFromPendingIntent = getActivity().getIntent().getBooleanExtra("return",false);

        if(isComeBackFromPendingIntent){
            Intent intent = new Intent(getContext(),TrackerService.class);
            getContext().bindService(intent, connection, Context.BIND_AUTO_CREATE);
            isRunning = true;
            exerciseMode= getActivity().getIntent().getStringExtra("mode");
            startDateAndTime = getActivity().getIntent().getLongExtra("startDateAndTime",0);
            msgToShowModeTextView.setText(exerciseMode.toUpperCase() + " MODE");
            walkingModeSwitchButton.setChecked(exerciseMode.equals("Walk")? true:false);
            msgToShowModeTextView.setVisibility(View.VISIBLE);
            walkingModeSwitchButton.setVisibility(View.GONE);

        }else if(pref.getBoolean("return",false)){
            Intent intent = new Intent(getContext(),TrackerService.class);
            getContext().bindService(intent, connection, Context.BIND_AUTO_CREATE);
            isRunning = true;
//            exerciseMode= getActivity().getIntent().getStringExtra("mode");
//            startDateAndTime = getActivity().getIntent().getLongExtra("startDateAndTime",0);
//            msgToShowModeTextView.setText(exerciseMode.toUpperCase() + " MODE");
//            msgToShowModeTextView.setVisibility(View.VISIBLE);
//            walkingModeSwitchButton.setVisibility(View.GONE);
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
        walkingModeSwitchButton = (Switch) view.findViewById(R.id.switchButton);
        msgToShowModeTextView = (TextView) view.findViewById(R.id.msgToShowModeTextView);
        toggleRunningButton();
        setButtonClickListener();
        setUpSwitchListener();
        return view;

    }

    private void toggleRunningButton(){
        LocationManager locationManager = (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);

        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            if(isRunning){
                runningButton.setText("Stop");
                resetTimeAndDistanceTextView();
            }else{
                runningButton.setText("Start");
            }
        }else{
            runningButton.setEnabled(false);
            runningButton.setText("Can't Start - \nGPS turned OFF");

        }

    }


    public void resetTimeAndDistanceTextView(){
        totalTimeTextView.setText("0:00");
        runningDistanceTextView.setText("0.00 km");
    }

    private void setUpSwitchListener(){
        walkingModeSwitchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    exerciseMode = "Walk";
                }else{
                    exerciseMode = "Run";
                }

                Log.d("mama","mode changed: "+ exerciseMode);
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DbHelper(this.getContext(), null, null, 1);
        getContext().registerReceiver(gpsReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));

    }

    public void checkPermision(){
        if(ContextCompat.checkSelfPermission(getContext(),Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            runningButton.setEnabled(false);
            runningButton.setText("Can't Start- Permission not granted");

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);

        }else{
            toggleRunningButton();

        }
    }

    public void setButtonClickListener(){

        runningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRunning= !isRunning;
                toggleRunningButton();
                if(isRunning){
                    runningButton.setEnabled(false);
                    startDateAndTime = Calendar.getInstance().getTimeInMillis();
                    Intent intent = new Intent(getContext(),TrackerService.class);
                    intent.putExtra("mode",exerciseMode);
                    intent.putExtra("startDateAndTime",startDateAndTime);
                    getContext().startService(intent);
                    getContext().bindService(intent, connection, Context.BIND_AUTO_CREATE);
                    walkingModeSwitchButton.setVisibility(View.GONE);
                    msgToShowModeTextView.setText(exerciseMode.toUpperCase() + " MODE");
                    msgToShowModeTextView.setVisibility(View.VISIBLE);

                }else{
                    saveSessionAndKillService(isServiceBounded);
                    walkingModeSwitchButton.setEnabled(true);
                    walkingModeSwitchButton.setVisibility(View.VISIBLE);
                    msgToShowModeTextView.setVisibility(View.GONE);

                }
            }
        });


    }

    public void saveSessionAndKillService(boolean isServiceBounded){
        timer.cancel();
        timer.purge();
        if(!trackerService.tracker.getLocationStringList().equals("")){
            RunningSession session = new RunningSession(trackerService.tracker.getLocationStringList(),startDateAndTime,trackerService.tracker.getTotalRunningDistance(),trackerService.tracker.getTotalTime(),trackerService.tracker.getTotalRunningDistance() * 1000 / 60,exerciseMode);
            dbHelper.add(session);
        }else{
            new AlertDialog.Builder(getContext())
                    .setTitle("Sessions too short < 1s")
                    .setMessage("It's not saved")

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.

                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton("OK", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        Intent intent = new Intent(getContext(),TrackerService.class);

        trackerService.tracker.stopGPS();
        getContext().stopService(intent);
        if(isServiceBounded){
            getContext().unbindService(connection);
        }
        this.isServiceBounded = false;
        this.isRunning = false;
        resetTimeAndDistanceTextView();
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
                                runningDistanceTextView.setText(String.format("%.2f",trackerService.tracker.getTotalRunningDistance()) + " km");
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
                    runningButton.setText("Start");
                }else{
                    runningButton.setEnabled(false);
                    runningButton.setText("Can't start - Permission not Granted");
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
            toggleRunningButton();
            startCalculatingDistance();
            runningButton.setEnabled(true);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(ContextCompat.checkSelfPermission(getContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                if (intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                    final LocationManager manager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );

                    if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){

                        runningButton.setText("Start");
                        runningButton.setEnabled(true);
                        msgToShowModeTextView.setVisibility(View.GONE);
                        walkingModeSwitchButton.setVisibility(View.VISIBLE);
                    }else{
                        msgToShowModeTextView.setVisibility(View.GONE);
                        walkingModeSwitchButton.setVisibility(View.VISIBLE);
                        runningButton.setText("Can't Start - \nGPS turned OFF");
                        runningButton.setEnabled(false);
                        if(isRunning){

                            saveSessionAndKillService(isServiceBounded);
                            SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putBoolean("return", false);
                            editor.commit();
                            showAlert("Your Session has been saved");

                        }else{
                            showAlert("Enable it back to start tracking");
                        }


                    }

                    resetTimeAndDistanceTextView();

                }
            }else{
                runningButton.setText("Can't Start- Permission not Granted");
                runningButton.setEnabled(false);
            }

        }
    };


    public void showAlert(String msg){
        new AlertDialog.Builder(getContext())
                .setTitle("GPS turned OFF")
                .setMessage(msg)

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton("Go to location Setting", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton("OK", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}
