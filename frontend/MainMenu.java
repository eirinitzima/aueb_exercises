package com.example.distributedsystemsapp.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.distributedsystemsapp.R;

public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FrameLayout btnFindStores = findViewById(R.id.btnFindStores);
        btnFindStores.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenu.this, Search.class);
            startActivity(intent);
        });

        Button btnRateStore = findViewById(R.id.btnRateStore);
        btnRateStore.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenu.this, RateStoreActivity.class);
            startActivity(intent);
        });
    }
}