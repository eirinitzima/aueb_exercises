package com.example.distributedsystemsapp.frontend;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.distributedsystemsapp.R;

import android.app.AlertDialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import backend.src.Shared;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.widget.ImageButton;
import android.view.View;


public class RateStoreActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StoreAdapter adapter;
    private List<Shared.Store> storeList = new ArrayList<>();

    private Shared.SystemConfig config;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_store);
        loadConfig();
        recyclerView = findViewById(R.id.recyclerStores);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
        fetchStoresFromMaster();
    }

    private void loadConfig() {
        // loads configuration from assets
        try {
            InputStream is = getAssets().open("system_config.json");
            config = Shared.loadSystemConfigFromStream(is);
            Toast.makeText(this, "Config loaded successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to load config: " + e.getMessage(), Toast.LENGTH_LONG).show();
            config = null;
        }
    }
    private void fetchStoresFromMaster() {
        new Thread(() -> {
            try {
                Socket socket = new Socket(config.masterHost,config.masterPort);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                Shared.SearchFilters filters = new Shared.SearchFilters(
                        0.0, 0.0,
                        Arrays.asList(Shared.FoodCategory.values()),
                        1,
                        "$"
                );
                Shared.Request request = new Shared.Request(Shared.RequestType.SEARCH_STORES, filters);

                out.writeObject(request);
                out.flush();

                List<Shared.Store> stores = (List<Shared.Store>) in.readObject();
                socket.close();

                for (Shared.Store s : stores) {
                    android.util.Log.d("DEBUG_RATE", "Store: " + s.storeName +
                            " | Stars: " + s.stars +
                            " | Votes: " + s.noOfVotes);
                }


                runOnUiThread(() -> {
                    storeList = stores;

                    if (stores == null || stores.isEmpty()) {
                        Toast.makeText(this, "Received 0 stores from master", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Toast.makeText(this, "Fetched " + stores.size() + " stores", Toast.LENGTH_SHORT).show();

                    if (adapter == null) {
                        adapter = new StoreAdapter(storeList, RateStoreActivity.this, RateStoreActivity.this::showRatingDialog, 0.0, 0.0);
                        recyclerView.setAdapter(adapter);
                    } else {
                        storeList.clear();
                        storeList.addAll(stores);
                        adapter.notifyDataSetChanged();
                    }

                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Failed to fetch stores: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
                e.printStackTrace();
            }
        }).start();
    }

    private void showRatingDialog(Shared.Store store) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rate, null);
        Spinner spinnerStars = dialogView.findViewById(R.id.spinnerStarsRate);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.star_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStars.setAdapter(adapter);

        new AlertDialog.Builder(this)
                .setTitle("Rate " + store.storeName)
                .setView(dialogView)
                .setPositiveButton("Submit", (dialog, which) -> {
                    int stars = Integer.parseInt(spinnerStars.getSelectedItem().toString());
                    sendRatingToServer(store.storeName, stars);
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void sendRatingToServer(String storeName, int stars) {
        new Thread(() -> {
            try {
                Socket socket = new Socket(config.masterHost,config.masterPort);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                Shared.RatingData rating = new Shared.RatingData(storeName, stars);
                Shared.Request req = new Shared.Request(Shared.RequestType.RATE_STORE, rating);

                out.writeObject(req);
                out.flush();

                Object response = in.readObject();
                socket.close();

                runOnUiThread(() -> {
                    Toast.makeText(this, response.toString(), Toast.LENGTH_SHORT).show();
                    fetchStoresFromMaster(); // reload updated store data
                });


            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Rating failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
                e.printStackTrace();
            }
        }).start();
    }
}
