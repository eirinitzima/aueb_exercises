package com.example.distributedsystemsapp.frontend;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.distributedsystemsapp.R;
import backend.src.Shared;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StoreResults extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView emptyView;
    private StoreAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_results);

        recyclerView = findViewById(R.id.recyclerStores);
        emptyView = findViewById(R.id.emptyView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get store list from Intent
        ArrayList<Shared.Store> storeList = (ArrayList<Shared.Store>) getIntent().getSerializableExtra("storeList");

        if (storeList == null || storeList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            double userLat = getIntent().getDoubleExtra("userLat", 0.0);
            double userLon = getIntent().getDoubleExtra("userLon", 0.0);

            adapter = new StoreAdapter(storeList, this, null, userLat, userLon);
            recyclerView.setAdapter(adapter);
        }

        ImageButton backButton = findViewById(R.id.backButtonStoreResults);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(StoreResults.this, Search.class);
            startActivity(intent);
        });
    }
}
