package com.aalamstudio.compiler;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BuildApkActivity extends AppCompatActivity {

    private CircularProgressView progressRing;
    private ProgressBar progressLinear;
    private TextView tvPercent, tvStatus, tvSubStatus, tvTimeRemaining;
    private RecyclerView rvBuildLog;
    private BuildLogAdapter logAdapter;
    private List<BuildLogAdapter.LogEntry> logEntries = new ArrayList<>();

    private LinearLayout[] stepViews = new LinearLayout[7];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_apk);

        progressRing = findViewById(R.id.progressRing);
        progressLinear = findViewById(R.id.progressLinear);
        tvPercent = findViewById(R.id.tvPercent);
        tvStatus = findViewById(R.id.tvStatus);
        tvSubStatus = findViewById(R.id.tvSubStatus);
        tvTimeRemaining = findViewById(R.id.tvTimeRemaining);
        rvBuildLog = findViewById(R.id.rvBuildLog);

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

        findViewById(R.id.settingsNavItem).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        Button btnClearLog = findViewById(R.id.btnClearLog);
        btnClearLog.setOnClickListener(v -> {
            logEntries.clear();
            logAdapter.notifyDataSetChanged();
        });

        findViewById(R.id.btnCancelBuild).setOnClickListener(v -> finish());

        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        String token = prefs.getString(SettingsActivity.TOKEN_KEY, "");

        if (token.isEmpty()) {
            addLog(null, "No GitHub token found. Please set it in Settings first.", BuildLogAdapter.TYPE_DONE);
            tvStatus.setText("Error");
            tvTimeRemaining.setText("Token missing");
            return;
        }

        GitHubApiHelper api = new GitHubApiHelper(token, new GitHubApiHelper.StatusCallback() {
            @Override
            public void onLog(String message) {
                runOnUiThread(() -> addLog(null, message, BuildLogAdapter.TYPE_IN_PROGRESS));
            }

            @Override
            public void onProgress(int percent) {
                runOnUiThread(() -> updateBuildProgress(percent));
            }

            @Override
            public void onSuccess(File apkFile) {
                runOnUiThread(() -> {
                    tvStatus.setText("Build Complete!");
                    tvSubStatus.setText("Your APK is ready");
                    tvTimeRemaining.setText("Done");
                    addLog(null, "APK saved at: " + apkFile.getAbsolutePath(), BuildLogAdapter.TYPE_DONE);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    tvStatus.setText("Build Failed");
                    tvTimeRemaining.setText("Error");
                    addLog(null, "ERROR: " + error, BuildLogAdapter.TYPE_DONE);
                });
            }
        });

        api.startBuild(getExternalFilesDir(null));
    }

    private void updateBuildProgress(int percent) {
        progressRing.setProgress(percent);
        progressLinear.setProgress(percent);
        tvPercent.setText(percent + "%");
        tvTimeRemaining.setText((100 - percent) + "% remaining");
        updateStepIndicator(percent);
    }

    private void updateStepIndicator(int percent) {
        int activeStep = (int) Math.ceil((percent / 100.0) * 7);
        for (int i = 0; i < 7; i++) {
            TextView circle = (TextView) stepViews[i].getChildAt(0);
            if (i < activeStep - 1) {
                circle.setBackgroundResource(R.drawable.step_circle_done);
            } else if (i == activeStep - 1) {
                circle.setBackgroundResource(R.drawable.step_circle_active);
            } else {
                circle.setBackgroundResource(R.drawable.step_circle_pending);
            }
        }
    }

    private void addLog(String time, String text, int type) {
        logEntries.add(new BuildLogAdapter.LogEntry(time, text, type));
        logAdapter.notifyItemInserted(logEntries.size() - 1);
        rvBuildLog.scrollToPosition(logEntries.size() - 1);
    }
}
