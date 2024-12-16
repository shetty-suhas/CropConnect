package com.crop.cropconnect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MarketsAdapter extends RecyclerView.Adapter<MarketsAdapter.ViewHolder> {
    private List<Market> markets;

    public MarketsAdapter(List<Market> markets) {
        this.markets = markets;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_market, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Market market = markets.get(position);
        holder.nameText.setText(market.name);
        holder.distanceText.setText(market.distance);
        holder.statusText.setText(market.status);
        holder.ratingText.setText(market.rating);
    }

    @Override
    public int getItemCount() {
        return markets.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, distanceText, statusText, ratingText;

        ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.marketName);
            distanceText = view.findViewById(R.id.marketDistance);
            statusText = view.findViewById(R.id.marketStatus);
            ratingText = view.findViewById(R.id.marketRating);
        }
    }
}
