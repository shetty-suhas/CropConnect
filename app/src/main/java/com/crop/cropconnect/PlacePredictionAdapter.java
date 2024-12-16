package com.crop.cropconnect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlacePredictionAdapter extends RecyclerView.Adapter<PlacePredictionAdapter.ViewHolder> {
    private List<PlacePrediction> predictions;
    private OnPlaceSelectedListener listener;

    public interface OnPlaceSelectedListener {
        void onPlaceSelected(PlacePrediction place);
    }

    public PlacePredictionAdapter(List<PlacePrediction> predictions, OnPlaceSelectedListener listener) {
        this.predictions = predictions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place_prediction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlacePrediction prediction = predictions.get(position);
        holder.primaryText.setText(prediction.primaryText);
        holder.secondaryText.setText(prediction.secondaryText);
        holder.itemView.setOnClickListener(v -> listener.onPlaceSelected(prediction));
    }

    @Override
    public int getItemCount() {
        return predictions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView primaryText;
        TextView secondaryText;

        ViewHolder(View view) {
            super(view);
            primaryText = view.findViewById(R.id.primaryText);
            secondaryText = view.findViewById(R.id.secondaryText);
        }
    }
}
