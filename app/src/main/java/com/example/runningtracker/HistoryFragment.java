package com.example.runningtracker;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

    List<RunningSession> sessions = new ArrayList<>();



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.history_fragment,container,false);

        totalDistanceTextView = (TextView) view.findViewById(R.id.totalDistanceTextView);
        totalTimeTextView = (TextView) view.findViewById(R.id.totalTimeTextView);
        avgSpeedTextView = (TextView) view.findViewById(R.id.avgSpeedTextView);

        String[] sessionsArray = new String[]{"Today's session","Week's sessions","One Month sessions","Three Months sessions","Six Months","One Years","All sessions"};
        spinner = (Spinner) view.findViewById(R.id.spinner);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item,sessionsArray);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch(i){
                    case 0 : refreshView("one_day"); break;
                    case 1 : refreshView("one_week");break;
                    case 2: refreshView("one_month");break;
                    case 3: refreshView("three_month");break;
                    case 4 : refreshView("six_month");break;
                    case 5: refreshView("one_year");break;
                    case 6: refreshView("all_sessions");break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        sessions.addAll(dbHelper.find(getTimeInMillisFor("one_day")));
        recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        adapter = new RecyclerViewAdapter(getContext(), sessions);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            refreshView("one_day");
        }
    }

    public void refreshView(String period){

        int totalTime = 0 ;
        float totalDistance = 0;
        float avgSpeed = 0;

        if(!sessions.isEmpty()){
            sessions.clear();
        }

        sessions.addAll(dbHelper.find(getTimeInMillisFor(period)));
        adapter.notifyDataSetChanged();

        for(RunningSession session : sessions){
            totalTime += session.getTotalTime();
            totalDistance += session.getTotalDistance();
        }

        avgSpeed = totalDistance/(float)totalTime/60;


        totalTimeTextView.setText(minuteSecondString(totalTime*1000));
        totalDistanceTextView.setText(String.format("%.2f", totalDistance) + "KM");
        avgSpeedTextView.setText(String.format("%.2f", avgSpeed) + "Km/Min");

    }

    public String minuteSecondString(int millsec){
        int minutes = (millsec/1000) /60;
        int second = (millsec/1000) % 60;

        if(second<10){
            return minutes + " : 0" + second;
        }else{
            return minutes + " : " + second;

        }
    }

    public long getTimeInMillisFor(String period){
        Calendar c = Calendar.getInstance();
        long currentTime = c.getTimeInMillis();

        switch (period){
            case "one_day": {

                c.set(Calendar.HOUR_OF_DAY,0);
                c.set(Calendar.MINUTE,0);
                c.set(Calendar.SECOND,0);
                c.set(Calendar.MILLISECOND,0);
                return currentTime - (currentTime - c.getTimeInMillis());
            }
            case "one_week": {
                c.add(Calendar.DAY_OF_YEAR,-7);
                return currentTime - (currentTime - c.getTimeInMillis());
            }
            case "one_month": {
                c.add(Calendar.MONTH,-1);
                return currentTime - (currentTime - c.getTimeInMillis());
            }
            case "three_month" : {
                c.add(Calendar.MONTH,-3);
                return currentTime - (currentTime - c.getTimeInMillis());
            }
            case "six_month" : {
                c.add(Calendar.MONTH,-6);
                return currentTime - (currentTime - c.getTimeInMillis());
            }

            case "one_year" : {
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
