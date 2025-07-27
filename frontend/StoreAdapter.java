package com.example.distributedsystemsapp.frontend;

import com.example.distributedsystemsapp.R;
import backend.src.Shared;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

// Adapter for showing stores in a RecyclerView
public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {

    private final List<Shared.Store> storeList;
    private final Context context;
    private final OnStoreClickListener listener;

    private final double userLat;
    private final double userLon;

    public StoreAdapter(List<Shared.Store> stores, Context context, OnStoreClickListener listener,
                        double userLat, double userLon) {
        this.storeList = stores;
        this.context = context;
        this.listener = listener;
        this.userLat = userLat;
        this.userLon = userLon;
    }

    // Creates each list item (card view)
    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.store_item, parent, false);
        return new StoreViewHolder(view);
    }

    // Binds data from each store to the views inside the list item
    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        Shared.Store store = storeList.get(position);

        // Set the store data
        holder.storeName.setText(store.storeName);
        holder.foodCategory.setText(store.foodCategory.name());
        holder.storeStars.setText("★ " + String.format("%.1f", store.stars));
        holder.storePrice.setText(store.priceCategory.toString());

        // Calculate distance from user
        double dist = Shared.distance(userLat, userLon, store.latitude, store.longitude);
        String distanceStr = String.format("📍 %.2f km away", dist);
        holder.storeDistance.setText(distanceStr);

        // Load image based on filename
        String imageName = store.storeLogoPath.replace(".png", "").toLowerCase().trim();
        int imageResId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
        holder.storeImage.setImageResource(imageResId == 0 ? R.drawable.store_placeholder : imageResId);

        // When user taps the item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) { // for the rate thing
                listener.onStoreClick(store);
            } else {
                // By default → open the StoreDetailsActivity
                Intent intent = new Intent(context, StoreDetailsActivity.class);
                intent.putExtra("store", store);
                context.startActivity(intent);
            }
        });
    }

    // Tells the RecyclerView how many stores to show
    @Override
    public int getItemCount() {
        return storeList.size();
    }

    // Holds references to all the views inside each list item (store card)
    public static class StoreViewHolder extends RecyclerView.ViewHolder {
        ImageView storeImage;
        TextView storeName, foodCategory, storeStars, storePrice, storeDistance;

        public StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            storeImage = itemView.findViewById(R.id.storeImage);
            storeName = itemView.findViewById(R.id.storeName);
            foodCategory = itemView.findViewById(R.id.foodCategory);
            storeStars = itemView.findViewById(R.id.storeStars);
            storePrice = itemView.findViewById(R.id.storePrice);
            storeDistance = itemView.findViewById(R.id.storeDistance);
        }
    }

    // Interface for click events
    public interface OnStoreClickListener {
        void onStoreClick(Shared.Store store);
    }
}
