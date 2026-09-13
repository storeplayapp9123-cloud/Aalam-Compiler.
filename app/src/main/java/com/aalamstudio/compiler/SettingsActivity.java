package com.aalamstudio.compiler;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "AalamCompilerPrefs";
    public static final String TOKEN_KEY = "github_token";

    private EditText tokenInput;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        tokenInput = findViewById(R.id.tokenInput);
        saveButton = findViewById(R.id.saveButton);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedToken = prefs.getString(TOKEN_KEY, "");
        if (!savedToken.isEmpty()) {
            tokenInput.setText(savedToken);
        }

        saveButton.setOnClickListener(v -> {
            String token = tokenInput.getText().toString().trim();
            if (token.isEmpty()) {
                Toast.makeText(this, "Please enter a token", Toast.LENGTH_SHORT).show();
                return;
            }
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(TOKEN_KEY, token);
            editor.apply();
            Toast.makeText(this, "Token saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
