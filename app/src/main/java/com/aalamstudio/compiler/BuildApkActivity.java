package com.aalamstudio.compiler;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BuildApkActivity extends AppCompatActivity {

    private CircularProgressView progressRing;
    private TextView tvPercent;
    private RecyclerView rvBuildLog;
    private BuildLogAdapter logAdapter;
    private List<BuildLogAdapter.LogEntry> logEntries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_apk);

        progressRing = findViewById(R.id.progressRing);
        tvPercent = findViewById(R.id.tvPercent);
        rvBuildLog = findViewById(R.id.rvBuildLog);

        rvBuildLog.setLayoutManager(new LinearLayoutManager(this));
        logAdapter = new BuildLogAdapter(logEntries);
        rvBuildLog.setAdapter(logAdapter);

        seedSampleLog();
        updateBuildProgress(78);
    }

    /** Matches the exact log shown in the mockup screenshot */
    private void seedSampleLog() {
        addLog("10:24:10", "Project loaded successfully", BuildLogAdapter.TYPE_DONE);
        addLog("10:24:11", "Checking dependencies", BuildLogAdapter.TYPE_DONE);
        addLog("10:24:12", "Cleaning old builds", BuildLogAdapter.TYPE_DONE);
        addLog("10:24:13", "Preparing resources", BuildLogAdapter.TYPE_DONE);
        addLog("10:24:18", "Compiling code", BuildLogAdapter.TYPE_DONE);
        addLog("10:25:45", "Merging resources", BuildLogAdapter.TYPE_DONE);
        addLog("10:26:22", "Processing assets...   (78%)", BuildLogAdapter.TYPE_IN_PROGRESS);
        addSubStep("Optimizing images...");
        addSubStep("Minifying code...");
        addSubStep("Generating APK package...");
        addSubStep("Signing APK...");
        addSubStep("Finalizing build...");
        logAdapter.notifyDataSetChanged();
        rvBuildLog.scrollToPosition(logEntries.size() - 1);
    }

    private void addLog(String time, String text, int type) {
        logEntries.add(new BuildLogAdapter.LogEntry(time, text, type));
    }

    private void addSubStep(String text) {
        logEntries.add(new BuildLogAdapter.LogEntry(null, text, BuildLogAdapter.TYPE_SUB_STEP));
    }

    /** Call this as real GitHub Actions build progress comes in (0-100) */
    public void updateBuildProgress(int percent) {
        progressRing.setProgress(percent);
        tvPercent.setText(percent + "%");
    }

    /** Call when a new real log line arrives from the GitHub Actions poll */
    public void appendLogLine(String time, String text, int type) {
        addLog(time, text, type);
        logAdapter.notifyItemInserted(logEntries.size() - 1);
        rvBuildLog.scrollToPosition(logEntries.size() - 1);
    }
}
