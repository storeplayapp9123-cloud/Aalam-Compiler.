package com.aalamstudio.compiler;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buildButton = findViewById(R.id.buildButton);
        Button settingsButton = findViewById(R.id.settingsButton);

        buildButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, BuildApkActivity.class));
        });

        settingsButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });
    }
}
