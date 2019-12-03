package com.example.runningtracker;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {


    private LayoutInflater inflater;
    private List<RunningSession> sessions;



    RecyclerViewAdapter(Context context, List<RunningSession>sessions){
        this.inflater = LayoutInflater.from(context);
        this.sessions = sessions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, final int position) {
        final RunningSession session = sessions.get(position);

        SimpleDateFormat formatter = new SimpleDateFormat("d MMM YYYY\nh:mm a");

        holder.totalDistanceTextView.setText(String.format("%.2f", session.getTotalDistance()) + "Km");
        holder.dateTextView.setText(formatter.format(new Date(session.getDate())) + "");
        holder.totalTimeTextView.setText(minuteSecondString((int) session.getTotalTime()*1000) + "");


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return sessions.size();
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


    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView dateTextView ;
        TextView totalDistanceTextView;
        TextView totalTimeTextView;

        ViewHolder(View view) {

            super(view);

            dateTextView = (TextView) view.findViewById(R.id.row_dateTextView);
            totalDistanceTextView = (TextView) view.findViewById(R.id.row_distanceTextView);
            totalTimeTextView = (TextView) view.findViewById(R.id.row_TimeTextView);
        }


    }
}
