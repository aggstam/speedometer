// -------------------------------------------------------------
//
// This is the Violation Adapter used by the application, to
// populate the corresponding Recycler View in UserViolationsList Activity.
//
// Author: Aggelos Stamatiou, July 2020
//
// --------------------------------------------------------------

package com.stamatiou.violation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.stamatiou.speedometer.R;

import java.text.SimpleDateFormat;
import java.util.List;

public class ViolationAdapter extends RecyclerView.Adapter<ViolationAdapter.ViolationViewHolder> {

    private List<Violation> violations;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public static class ViolationViewHolder extends RecyclerView.ViewHolder {

        TextView latitudeView, longitudeView, speedView, timestampView;

        public ViolationViewHolder(View itemView) {
            super(itemView);
            this.latitudeView = itemView.findViewById(R.id.latitudeView);
            this.longitudeView = itemView.findViewById(R.id.longitudeView);
            this.speedView = itemView.findViewById(R.id.speedView);
            this.timestampView = itemView.findViewById(R.id.timestampView);
        }
    }

    public ViolationAdapter(List<Violation> violations) {
        this.violations = violations;
    }

    @Override
    public ViolationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.violation_card_layout, parent, false);
        ViolationViewHolder violationViewHolder = new ViolationViewHolder(view);
        return violationViewHolder;
    }

    @Override
    public void onBindViewHolder(ViolationViewHolder holder, int position) {

        TextView latitudeView = holder.latitudeView;
        TextView longitudeView = holder.longitudeView;
        TextView speedView = holder.speedView;
        TextView timestampView = holder.timestampView;

        latitudeView.setText("Latitude: " + String.format("%.6f", violations.get(position).getLatitude()));
        longitudeView.setText("Longitude: " + String.format("%.6f", violations.get(position).getLongitude()));
        speedView.setText("Speed: " + String.format("%.2f", violations.get(position).getSpeed()) + " km/h");
        timestampView.setText("Timestamp: " + dateFormatter.format(violations.get(position).getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return violations.size();
    }
}