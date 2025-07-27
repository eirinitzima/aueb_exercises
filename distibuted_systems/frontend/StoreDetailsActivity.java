package com.example.distributedsystemsapp.frontend;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;

import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.distributedsystemsapp.R;
import backend.src.Shared;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class StoreDetailsActivity extends AppCompatActivity {

    private ImageView storeImage;
    private TextView storeName, storeCategory, storeStars, storePrice;
    private LinearLayout productListContainer;
    private Shared.Store store;
    private Shared.SystemConfig config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_details);

        initUI();
        loadConfig();
        loadStore();

        if (store == null) {
            Toast.makeText(this, "Store data not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        displayStoreDetails();
        addProductViews();

        ImageButton backButton = findViewById(R.id.backButtonStoreDetails);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(StoreDetailsActivity.this, MainMenu.class);
            startActivity(intent);
        });
    }

    private void initUI() {
        storeImage = findViewById(R.id.storeImageDetails);
        storeName = findViewById(R.id.storeNameDetails);
        storeCategory = findViewById(R.id.storeCategoryDetails);
        storeStars = findViewById(R.id.storeStarsDetails);
        storePrice = findViewById(R.id.storePriceDetails);
        productListContainer = findViewById(R.id.productListContainer);
    }

    private void loadConfig() {
        try {
            config = Shared.loadSystemConfigFromStream(getAssets().open("system_config.json"));
        } catch (IOException e) {
            Toast.makeText(this, "Failed to load config: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadStore() {
        store = (Shared.Store) getIntent().getSerializableExtra("store");
    }

    private void displayStoreDetails() {
        storeName.setText(store.storeName);
        storeCategory.setText(store.foodCategory.name());
        storeStars.setText("★ " + String.format("%.1f", store.stars));
        storePrice.setText(store.priceCategory.toString());

        String imageName = store.storeLogoPath.replace(".png", "").toLowerCase().trim();
        int imageResId = getResources().getIdentifier(imageName, "drawable", getPackageName());
        storeImage.setImageResource(imageResId == 0 ? R.drawable.store_placeholder : imageResId);
    }

    private void addProductViews() {
        for (Shared.Product p : store.products) {
            View row = getLayoutInflater().inflate(R.layout.product_item, productListContainer, false);

            ImageView productImage = row.findViewById(R.id.productImage);
            TextView productText = row.findViewById(R.id.productText);
            Button buyButton = row.findViewById(R.id.buyButton);

            // Text
            productText.setText(p.productName + " (" + p.productType + ") - " + p.price + "€");

            // Image based on store category
            int imageResId;
            switch (store.foodCategory) {
                case PIZZERIA:
                    imageResId = R.drawable.pizza_icon;
                    break;
                case SUSHI:
                    imageResId = R.drawable.sushi_icon;
                    break;
                case BURGER:
                    imageResId = R.drawable.burger_icon;
                    break;
                case SALAD:
                    imageResId = R.drawable.salad_icon;
                    break;
                case MEXICAN:
                    imageResId = R.drawable.taco_icon;
                    break;
                case OTHER:
                default:
                    imageResId = R.drawable.food_placeholder;
                    break;
            }

            productImage.setImageResource(imageResId);

            // Buy button logic
            buyButton.setOnClickListener(v -> showBuyDialog(p));

            productListContainer.addView(row);
        }
    }


    private void showBuyDialog(Shared.Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter quantity");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("e.g. 1");
        builder.setView(input);

        builder.setPositiveButton("Buy Now", (dialog, which) -> {
            String qtyText = input.getText().toString().trim();
            int quantity;

            try {
                quantity = Integer.parseInt(qtyText);
                if (quantity <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            Shared.BuyData buyData = new Shared.BuyData(store.storeName, product.productName, quantity);
            Shared.Request request = new Shared.Request(Shared.RequestType.BUY_FROM_SEARCH, buyData);

            new Thread(() -> {
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(config.masterHost, config.masterPort), 3000);
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    out.flush();
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                    synchronized (out) {
                        out.writeObject(request);
                        out.flush();
                    }

                    Object response = in.readObject();

                    runOnUiThread(() -> {
                        Toast.makeText(StoreDetailsActivity.this, "Server says: " + response, Toast.LENGTH_LONG).show();

                        new AlertDialog.Builder(StoreDetailsActivity.this)
                                .setTitle("Order Confirmation ")
                                .setMessage(response.toString())
                                .setPositiveButton("OK", null)
                                .show();
                    });

                    socket.close();
                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(StoreDetailsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }).start();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}

