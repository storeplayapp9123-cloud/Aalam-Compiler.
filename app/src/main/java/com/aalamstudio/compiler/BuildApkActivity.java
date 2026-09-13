package com.aalamstudio.compiler;

import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ProgressBar;
import android.widget.TextView;

public class BuildApkActivity extends AppCompatActivity {

    private CircularProgressView circularProgress;
    private ProgressBar linearProgress;
    private TextView percentText, buildLogText, timeRemainingText;
    private int progress = 0;
    private StringBuilder log = new StringBuilder();
    private Handler handler = new Handler();

    private String[] steps = {
        "Project loaded successfully",
        "Checking dependencies",
        "Cleaning old builds",
        "Preparing resources",
        "Compiling code",
        "Merging resources",
        "Processing assets",
        "Optimizing images",
        "Generating APK package",
        "Signing APK",
        "Finalizing build"
    };
    private int stepIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_apk);

        circularProgress = findViewById(R.id.circularProgress);
        linearProgress = findViewById(R.id.linearProgress);
        percentText = findViewById(R.id.percentText);
        buildLogText = findViewById(R.id.buildLogText);
        timeRemainingText = findViewById(R.id.timeRemainingText);

        runBuildSteps();
    }

    private void runBuildSteps() {
        if (stepIndex < steps.length) {
            log.append("✓ ").append(steps[stepIndex]).append("\n");
            buildLogText.setText(log.toString());
            stepIndex++;

            progress = (int) (((float) stepIndex / steps.length) * 100);
            circularProgress.setProgress(progress);
            linearProgress.setProgress(progress);
            percentText.setText(progress + "%");
            timeRemainingText.setText((steps.length - stepIndex) + " steps remaining");

            handler.postDelayed(this::runBuildSteps, 800);
        } else {
            timeRemainingText.setText("Build Complete!");
        }
    }
}
