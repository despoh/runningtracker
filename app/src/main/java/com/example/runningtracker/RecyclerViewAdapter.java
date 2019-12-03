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

        holder.totalDistanceTextView.setText(session.getTotalDistance()+ "");
        holder.dateTextView.setText(formatter.format(new Date(session.getDate())) + "");
        holder.totalTimeTextView.setText(session.getTotalTime() + "");


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
