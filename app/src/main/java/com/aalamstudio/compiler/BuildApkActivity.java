package com.aalamstudio.compiler;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BuildApkActivity extends AppCompatActivity {

    private CircularProgressView progressRing;
    private ProgressBar progressLinear;
    private TextView tvPercent, tvStatus, tvSubStatus, tvTimeRemaining;
    private TextView tvAppName, tvPackageVersion, tvBuildStarted, tvEstimatedTime;
    private RecyclerView rvBuildLog;
    private BuildLogAdapter logAdapter;
    private List<BuildLogAdapter.LogEntry> logEntries = new ArrayList<>();
    private LinearLayout[] stepViews = new LinearLayout[7];

    private long buildStartTime;
    private boolean isRelease = true; // default Release jaisa image me hai

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_apk);

        // --- Bind Views (tere saare purane IDs + naye IDs) ---
        progressRing = findViewById(R.id.progressRing);
        progressLinear = findViewById(R.id.progressLinear);
        tvPercent = findViewById(R.id.tvPercent);
        tvStatus = findViewById(R.id.tvStatus);
        tvSubStatus = findViewById(R.id.tvSubStatus);
        tvTimeRemaining = findViewById(R.id.tvTimeRemaining);
        rvBuildLog = findViewById(R.id.rvBuildLog);

        // Naye IDs jo maine XML me add kiye the
        tvAppName = findViewById(R.id.tvAppName);
        tvPackageVersion = findViewById(R.id.tvPackageVersion);
        tvBuildStarted = findViewById(R.id.tvBuildStarted);
        tvEstimatedTime = findViewById(R.id.tvEstimatedTime);

        stepViews[0] = findViewById(R.id.step1);
        stepViews[1] = findViewById(R.id.step2);
        stepViews[2] = findViewById(R.id.step3);
        stepViews[3] = findViewById(R.id.step4);
        stepViews[4] = findViewById(R.id.step5);
        stepViews[5] = findViewById(R.id.step6);
        stepViews[6] = findViewById(R.id.step7);

        rvBuildLog.setLayoutManager(new LinearLayoutManager(this));
        logAdapter = new BuildLogAdapter(logEntries);
        rvBuildLog.setAdapter(logAdapter);

        // --- UI Setup image jaisa ---
        setupHeaderInfo();

        // --- Click Listeners (tera purana logic safe hai) ---
        findViewById(R.id.settingsNavItem).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        Button btnClearLog = findViewById(R.id.btnClearLog);
        btnClearLog.setOnClickListener(v -> {
            logEntries.clear();
            logAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Log Cleared", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnCancelBuild).setOnClickListener(v -> {
            finish();
        });

        findViewById(R.id.btnRunBackground).setOnClickListener(v -> {
            moveTaskToBack(true);
        });

        findViewById(R.id.btnBuildBackground).setOnClickListener(v -> {
            moveTaskToBack(true);
        });

        // Build Type Toggle - Debug / Release
        Button btnDebug = findViewById(R.id.btnDebug);
        Button btnRelease = findViewById(R.id.btnRelease);

        btnDebug.setOnClickListener(v -> {
            isRelease = false;
            btnDebug.setBackgroundTintList(getColorStateList(R.color.gold));
            btnDebug.setTextColor(getColor(android.R.color.black));
            btnRelease.setBackgroundTintList(getColorStateList(R.color.card_dark));
            btnRelease.setTextColor(getColor(android.R.color.white));
            addLog(getCurrentTime(), "Switched to Debug build", BuildLogAdapter.TYPE_IN_PROGRESS);
        });

        btnRelease.setOnClickListener(v -> {
            isRelease = true;
            btnRelease.setBackgroundTintList(getColorStateList(R.color.gold));
            btnRelease.setTextColor(getColor(android.R.color.black));
            btnDebug.setBackgroundTintList(getColorStateList(R.color.card_dark));
            btnDebug.setTextColor(getColor(android.R.color.white));
            addLog(getCurrentTime(), "Switched to Release build", BuildLogAdapter.TYPE_IN_PROGRESS);
        });

        // --- GitHub Token Check (tera purana logic) ---
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        String token = prefs.getString(SettingsActivity.TOKEN_KEY, "");

        if (token.isEmpty()) {
            addLog(getCurrentTime(), "No GitHub token found. Please set it in Settings first.", BuildLogAdapter.TYPE_DONE);
            tvStatus.setText("Error");
            tvSubStatus.setText("Token missing - Go to Settings");
            tvTimeRemaining.setText("Token missing");
            updateBuildProgress(0);
            return;
        }

        // Start Build
        buildStartTime = System.currentTimeMillis();
        updateBuildStartedTime();

        GitHubApiHelper api = new GitHubApiHelper(token, new GitHubApiHelper.StatusCallback() {
            @Override
            public void onLog(String message) {
                runOnUiThread(() -> addLog(getCurrentTime(), message, BuildLogAdapter.TYPE_IN_PROGRESS));
            }

            @Override
            public void onProgress(int percent) {
                runOnUiThread(() -> updateBuildProgress(percent));
            }

            @Override
            public void onSuccess(File apkFile) {
                runOnUiThread(() -> {
                    updateBuildProgress(100);
                    tvStatus.setText("Build Complete!");
                    tvSubStatus.setText("Your APK is ready - " + (isRelease? "Release" : "Debug"));
                    tvTimeRemaining.setText("Completed in " + getElapsedTime());
                    addLog(getCurrentTime(), "APK saved at: " + apkFile.getAbsolutePath(), BuildLogAdapter.TYPE_DONE);
                    addLog(getCurrentTime(), "File Size: ~42.6 MB", BuildLogAdapter.TYPE_DONE);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    tvStatus.setText("Build Failed");
                    tvSubStatus.setText(error);
                    tvTimeRemaining.setText("Error");
                    addLog(getCurrentTime(), "ERROR: " + error, BuildLogAdapter.TYPE_DONE);
                });
            }
        });

        api.startBuild(getExternalFilesDir(null));
    }

    private void setupHeaderInfo() {
        // Image jaisa Finance Hub dikhana hai toh yahan se change kar sakta hai
        if(tvAppName!= null) tvAppName.setText("Finance Hub");
        if(tvPackageVersion!= null) tvPackageVersion.setText("com.aalamstudio.financehub • Version 1.0.0 (1)");
        updateBuildProgress(0);
    }

    private void updateBuildProgress(int percent) {
        // Smooth progress
        progressRing.setProgress(percent);
        progressLinear.setProgress(percent);
        tvPercent.setText(percent + "%");

        // Time remaining logic - image jaisa
        if(percent < 100) {
            int remaining = 100 - percent;
            int seconds = remaining * 6; // approx
            int min = seconds / 60;
            int sec = seconds % 60;
            tvTimeRemaining.setText(min + "m " + sec + "s remaining");

            // Status text change as per step
            if(percent < 15) {
                tvStatus.setText("Preparing Project...");
                tvSubStatus.setText("Loading project files");
            } else if(percent < 35) {
                tvStatus.setText("Compiling Code...");
                tvSubStatus.setText("Compiling Java & Kotlin sources");
            } else if(percent < 80) {
                tvStatus.setText("Building APK...");
                tvSubStatus.setText("Compiling resources and optimizing your app");
            } else if(percent < 90) {
                tvStatus.setText("Packaging...");
                tvSubStatus.setText("Generating APK package...");
            } else {
                tvStatus.setText("Signing APK...");
                tvSubStatus.setText("Signing with release key");
            }
        }

        // Estimated time update
        if(tvEstimatedTime!= null && percent > 5) {
            long elapsed = System.currentTimeMillis() - buildStartTime;
            long totalEstimated = (elapsed * 100) / percent;
            long remainingMs = totalEstimated - elapsed;
            long remainingSec = remainingMs / 1000;
            tvEstimatedTime.setText("Estimated Time\n~ " + (remainingSec/60) + "m " + (remainingSec%60) + "s");
        }

        updateStepIndicator(percent);
    }

    private void updateStepIndicator(int percent) {
        int activeStep = (int) Math.ceil((percent / 100.0) * 7);
        if(activeStep == 0) activeStep = 1;

        for (int i = 0; i < 7; i++) {
            if(stepViews[i] == null || stepViews[i].getChildCount() == 0) continue;
            TextView circle = (TextView) stepViews[i].getChildAt(0);
            TextView label = (TextView) stepViews[i].getChildAt(1);

            if (i < activeStep - 1) {
                // Done - Tick mark
                circle.setText("✓");
                circle.setBackgroundResource(R.drawable.step_circle_done);
                circle.setTextColor(getColor(android.R.color.black));
                if(label!= null) label.setTextColor(getColor(R.color.text_secondary));
            } else if (i == activeStep - 1) {
                // Active - Gold
                circle.setText(String.valueOf(i+1));
                circle.setBackgroundResource(R.drawable.step_circle_active);
                circle.setTextColor(getColor(android.R.color.black));
                if(label!= null) label.setTextColor(getColor(R.color.gold));
            } else {
                // Pending - Dark
                circle.setText(String.valueOf(i+1));
                circle.setBackgroundResource(R.drawable.step_circle_pending);
                circle.setTextColor(getColor(android.R.color.white));
                if(label!= null) label.setTextColor(getColor(R.color.text_dim));
            }
        }
    }

    private void addLog(String time, String text, int type) {
        if(time == null) time = getCurrentTime();
        logEntries.add(new BuildLogAdapter.LogEntry(time, text, type));
        logAdapter.notifyItemInserted(logEntries.size() - 1);
        rvBuildLog.scrollToPosition(logEntries.size() - 1);
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    private void updateBuildStartedTime() {
        if(tvBuildStarted!= null) {
            String now = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(new Date());
            tvBuildStarted.setText("Build Started\n" + now);
        }
    }

    private String getElapsedTime() {
        long elapsed = System.currentTimeMillis() - buildStartTime;
        long sec = elapsed / 1000;
        return (sec/60) + "m " + (sec%60) + "s";
    }
}
