package com.example.distributedsystemsapp.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.distributedsystemsapp.R;
import backend.src.Shared;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;

public class Search extends AppCompatActivity {

    private Shared.SystemConfig config;
    private EditText inputLat, inputLon;
    private Spinner spinnerStars, spinnerPrice;
    private Button btnSearch;
    private CheckBox cbPizzeria, cbBurger, cbSushi, cbSalad, cbMexican, cbOther;
    // debug mode enables and pins cordinates
    private final boolean DEBUG_MODE = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);
        Toast.makeText(this, "Search screen loaded", Toast.LENGTH_SHORT).show();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // sets up ui, spinners and loads the ips and ports file
        initUI();
        setupSpinners();
        loadConfig();

        btnSearch.setOnClickListener(v -> {
            if (config == null) {
                Toast.makeText(this, "Config is null!", Toast.LENGTH_LONG).show();
                return;
            }

            Shared.SearchFilters filters;

            try {
                filters = DEBUG_MODE ? getDebugFilters() : collectFiltersFromUI();
            } catch (Exception e) {
                Toast.makeText(this, "Input error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            // calls the search method
            performSearch(filters);
        });

        ImageButton backButton = findViewById(R.id.backButtonSearch);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(Search.this, MainMenu.class);
            startActivity(intent);
        });
    }

    private void initUI() {
        //sets up ui
        inputLat = findViewById(R.id.inputLatitude);
        inputLon = findViewById(R.id.inputLongitude);
        spinnerStars = findViewById(R.id.spinnerStars);
        spinnerPrice = findViewById(R.id.spinnerPrice);
        btnSearch = findViewById(R.id.btnSearchStores);

        cbPizzeria = findViewById(R.id.categoryPizzeria);
        cbBurger = findViewById(R.id.categoryBurger);
        cbSushi = findViewById(R.id.categorySushi);
        cbSalad = findViewById(R.id.categorySalad);
        cbMexican = findViewById(R.id.categoryMexican);
        cbOther = findViewById(R.id.categoryOther);
    }

    private void setupSpinners() {
        // sets up spinners
        spinnerStars.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList(1, 2, 3, 4, 5)));
        spinnerPrice.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList("$", "$$", "$$$")));
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

    private Shared.SearchFilters collectFiltersFromUI() {
        // collects the filters from the screen and makes sure they are all correct filled
        String latStr = inputLat.getText().toString().trim();
        String lonStr = inputLon.getText().toString().trim();

        if (latStr.isEmpty() || lonStr.isEmpty()) {
            throw new IllegalArgumentException("Latitude and Longitude must not be empty.");
        }

        double lat, lon;
        try {
            lat = Double.parseDouble(latStr);
            lon = Double.parseDouble(lonStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Latitude and Longitude must be numbers.");
        }

        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            throw new IllegalArgumentException("Coordinates out of bounds.");
        }

        Object starsObj = spinnerStars.getSelectedItem();
        Object priceObj = spinnerPrice.getSelectedItem();

        if (starsObj == null || priceObj == null) {
            throw new IllegalArgumentException("Stars and price must be selected.");
        }

        int stars = (int) starsObj;
        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("Stars must be between 1 and 5.");
        }

        String price = priceObj.toString();
        List<String> allowedPrices = Arrays.asList("$", "$$", "$$$");
        if (!allowedPrices.contains(price)) {
            throw new IllegalArgumentException("Invalid price selection.");
        }

        // adds the selected categories to a list
        List<Shared.FoodCategory> selected = new ArrayList<>();
        if (cbPizzeria.isChecked()) selected.add(Shared.FoodCategory.PIZZERIA);
        if (cbBurger.isChecked()) selected.add(Shared.FoodCategory.BURGER);
        if (cbSushi.isChecked()) selected.add(Shared.FoodCategory.SUSHI);
        if (cbSalad.isChecked()) selected.add(Shared.FoodCategory.SALAD);
        if (cbMexican.isChecked()) selected.add(Shared.FoodCategory.MEXICAN);
        if (cbOther.isChecked()) selected.add(Shared.FoodCategory.OTHER);

        if (selected.isEmpty()) {
            throw new IllegalArgumentException("Select at least one food category.");
        }

        // returns an object
        return new Shared.SearchFilters(lat, lon, selected, stars, price);
    }


    private Shared.SearchFilters getDebugFilters() {
        // the debugs filters
        return new Shared.SearchFilters(
                37.9838, 23.7275,
                Arrays.asList(Shared.FoodCategory.PIZZERIA, Shared.FoodCategory.SUSHI),
                4, "$$"
        );
    }

    private void performSearch(Shared.SearchFilters filters) {
        // handles the search request to a thread
        new Thread(() -> {
            // opens the socket connection
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(config.masterHost, config.masterPort), 3000);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                // makes the request
                Shared.Request request = new Shared.Request(Shared.RequestType.SEARCH_STORES, filters);
                Log.d("DEBUG", "Sending request: " + request);
                // sends the request
                synchronized (out) {
                    out.writeObject(request);
                    out.flush();
                }

                Object response = in.readObject();

                // waits for the response
                runOnUiThread(() -> {
                    if (response instanceof List<?> list && !list.isEmpty()) {
                        Toast.makeText(this, "Stores received: " + list.size(), Toast.LENGTH_SHORT).show();

                        // takes the lat and lon from the filters
                        double userLat = filters.clientLat;
                        double userLon = filters.clientLon;

                        // starts the store results activity and passes the list of the stores, the lat and the lon
                        Intent intent = new Intent(Search.this, StoreResults.class);
                        intent.putExtra("storeList", (Serializable) list);
                        intent.putExtra("userLat", userLat);
                        intent.putExtra("userLon", userLon);
                        startActivity(intent);

                    } else {
                        Toast.makeText(this, "No stores found", Toast.LENGTH_SHORT).show();
                    }
                });


            } catch (Exception e) {
                Log.e("DEBUG", "Socket error", e);
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
