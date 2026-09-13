package com.aalamstudio.compiler;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.io.File;

public class BuildApkActivity extends AppCompatActivity {

    private CircularProgressView circularProgress;
    private ProgressBar linearProgress;
    private TextView percentText, buildLogText, timeRemainingText;
    private StringBuilder log = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_apk);

        circularProgress = findViewById(R.id.circularProgress);
        linearProgress = findViewById(R.id.linearProgress);
        percentText = findViewById(R.id.percentText);
        buildLogText = findViewById(R.id.buildLogText);
        timeRemainingText = findViewById(R.id.timeRemainingText);

        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        String token = prefs.getString(SettingsActivity.TOKEN_KEY, "");

        if (token.isEmpty()) {
            appendLog("No GitHub token found. Please set it in Settings first.");
            timeRemainingText.setText("Error: Token missing");
            return;
        }

        GitHubApiHelper api = new GitHubApiHelper(token, new GitHubApiHelper.StatusCallback() {
            @Override
            public void onLog(String message) {
                runOnUiThread(() -> appendLog(message));
            }

            @Override
            public void onProgress(int percent) {
                runOnUiThread(() -> {
                    circularProgress.setProgress(percent);
                    linearProgress.setProgress(percent);
                    percentText.setText(percent + "%");
                });
            }

            @Override
            public void onSuccess(File apkFile) {
                runOnUiThread(() -> {
                    timeRemainingText.setText("Build Complete!");
                    appendLog("APK saved at: " + apkFile.getAbsolutePath());
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    timeRemainingText.setText("Build Failed");
                    appendLog("ERROR: " + error);
                });
            }
        });

        api.startBuild(getExternalFilesDir(null));
    }

    private void appendLog(String message) {
        log.append(message).append("\n");
        buildLogText.setText(log.toString());
    }
}
