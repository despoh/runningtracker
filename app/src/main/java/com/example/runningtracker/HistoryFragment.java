package com.example.runningtracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HistoryFragment extends Fragment {

    DbHelper dbHelper;

    RecyclerViewAdapter adapter;
    RecyclerView recyclerView;
    Spinner spinner;

    TextView totalDistanceTextView;
    TextView totalTimeTextView;
    TextView avgSpeedTextView;
    ProgressBar progressBar;
    List<RunningSession> sessions = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.history_fragment,container,false);

        totalDistanceTextView = (TextView) view.findViewById(R.id.totalDistanceTextView);
        totalTimeTextView = (TextView) view.findViewById(R.id.totalTimeTextView);
        avgSpeedTextView = (TextView) view.findViewById(R.id.avgSpeedTextView);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
        String[] sessionsArray = new String[]{"Today's session","Week's sessions","One Month sessions","Three Months sessions","Six Months","One Years","All sessions"};
        spinner = (Spinner) view.findViewById(R.id.spinner);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item,sessionsArray);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                refreshView(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        sessions.addAll(dbHelper.find(getTimeInMillisFor(0)));
        recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        adapter = new RecyclerViewAdapter(getContext(), sessions);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        progressBar.setVisibility(View.GONE);
        getView().findViewById(R.id.overlay).setVisibility(View.GONE);

    }

    @Override
    public void onPause() {
        super.onPause();
        progressBar.setVisibility(View.VISIBLE);
        getView().findViewById(R.id.overlay).setVisibility(View.VISIBLE);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
           refreshView(spinner.getSelectedItemPosition());
        }
    }

    public void refreshView(int position){

        int totalTime = 0 ;
        float totalDistance = 0;
        float avgSpeed = 0;

        if(!sessions.isEmpty()){
            sessions.clear();
        }

        sessions.addAll(dbHelper.find(getTimeInMillisFor(position)));
        adapter.notifyDataSetChanged();

        for(RunningSession session : sessions){
            totalTime += session.getTotalTime();
            totalDistance += session.getTotalDistance();
        }

        avgSpeed = totalDistance*1000/(float)totalTime/60;


        totalTimeTextView.setText(minuteSecondString(totalTime*1000));
        totalDistanceTextView.setText(String.format("%.2f", totalDistance) + " km");
        avgSpeedTextView.setText(String.format("%.2f", avgSpeed) + " m/min");

    }

    public String minuteSecondString(int millsec){
        int hours = (millsec/1000)/60/60;
        int minutes = (millsec/1000) /60;
        int second = (millsec/1000) % 60;

        if(hours == 0 && minutes == 0){
            return second + " s";
        }else if(hours == 0){
            return minutes + "m " + second + " s";
        }else{
            return hours + " h " + minutes + " m " + second + " s";
        }
    }

    public long getTimeInMillisFor(int position){
        Calendar c = Calendar.getInstance();
        long currentTime = c.getTimeInMillis();

        switch (position){
            case 0: {

                c.set(Calendar.HOUR_OF_DAY,0);
                c.set(Calendar.MINUTE,0);
                c.set(Calendar.SECOND,0);
                c.set(Calendar.MILLISECOND,0);
                return currentTime - (currentTime - c.getTimeInMillis());
            }
            case 1: {
                c.add(Calendar.DAY_OF_YEAR,-7);
                return currentTime - (currentTime - c.getTimeInMillis());
            }
            case 2: {
                c.add(Calendar.MONTH,-1);
                return currentTime - (currentTime - c.getTimeInMillis());
            }
            case 3 : {
                c.add(Calendar.MONTH,-3);
                return currentTime - (currentTime - c.getTimeInMillis());
            }
            case 4 : {
                c.add(Calendar.MONTH,-6);
                return currentTime - (currentTime - c.getTimeInMillis());
            }

            case 5 : {
                c.add(Calendar.YEAR, -1);
                return currentTime - (currentTime - c.getTimeInMillis());
            }
            default: return 0;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DbHelper(this.getContext(), null, null, 1);

    }
}
