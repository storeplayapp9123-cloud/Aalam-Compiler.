package com.aalamstudio.compiler;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GitHubApiHelper {

    private static final String GITHUB_OWNER = "storeplayapp9123-cloud";
    private static final String GITHUB_REPO = "Aalam-Compiler.";
    private static final String WORKFLOW_FILE = "build.yml";

    private String token;
    private StatusCallback callback;

    public interface StatusCallback {
        void onLog(String message);
        void onProgress(int percent);
        void onSuccess(File apkFile);
        void onError(String error);
    }

    public GitHubApiHelper(String token, StatusCallback callback) {
        this.token = token;
        this.callback = callback;
    }

    public void startBuild(File outputDir) {
        new Thread(() -> {
            try {
                callback.onLog("Triggering build on GitHub...");
                callback.onProgress(10);
                triggerWorkflow();

                Thread.sleep(5000);

                callback.onLog("Waiting for GitHub to register the run...");
                callback.onProgress(20);
                long runId = getLatestRunId();

                callback.onLog("Build started (Run ID: " + runId + ")");
                callback.onProgress(30);

                String conclusion = pollUntilComplete(runId);

                if (!"success".equals(conclusion)) {
                    callback.onError("Build failed on GitHub. Check Actions tab for details.");
                    return;
                }

                callback.onLog("Build successful! Fetching APK...");
                callback.onProgress(85);

                String artifactUrl = getArtifactDownloadUrl(runId);
                if (artifactUrl == null) {
                    callback.onError("No artifact found for this build.");
                    return;
                }

                callback.onLog("Downloading APK...");
                callback.onProgress(90);
                File apk = downloadAndExtractApk(artifactUrl, outputDir);

                callback.onLog("Done!");
                callback.onProgress(100);
                callback.onSuccess(apk);

            } catch (Exception e) {
                Log.e("GitHubApiHelper", "Error", e);
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }

    private void triggerWorkflow() throws Exception {
        String urlStr = "https://api.github.com/repos/" + GITHUB_OWNER + "/" + GITHUB_REPO
                + "/actions/workflows/" + WORKFLOW_FILE + "/dispatches";
        HttpURLConnection conn = openConnection(urlStr, "POST");
        String body = "{\"ref\":\"main\"}";
        conn.getOutputStream().write(body.getBytes());
        int code = conn.getResponseCode();
        if (code != 204) {
            throw new Exception("Failed to trigger workflow (code " + code + ")");
        }
    }

    private long getLatestRunId() throws Exception {
        String urlStr = "https://api.github.com/repos/" + GITHUB_OWNER + "/" + GITHUB_REPO
                + "/actions/runs?per_page=1";
        HttpURLConnection conn = openConnection(urlStr, "GET");
        String response = readResponse(conn);
        JSONObject json = new JSONObject(response);
        JSONArray runs = json.getJSONArray("workflow_runs");
        return runs.getJSONObject(0).getLong("id");
    }

    private String pollUntilComplete(long runId) throws Exception {
        while (true) {
            String urlStr = "https://api.github.com/repos/" + GITHUB_OWNER + "/" + GITHUB_REPO
                    + "/actions/runs/" + runId;
            HttpURLConnection conn = openConnection(urlStr, "GET");
            String response = readResponse(conn);
            JSONObject json = new JSONObject(response);
            String status = json.getString("status");

            callback.onLog("Status: " + status);

            if ("completed".equals(status)) {
                return json.optString("conclusion", "failure");
            }
            Thread.sleep(6000);
        }
    }

    private String getArtifactDownloadUrl(long runId) throws Exception {
        String urlStr = "https://api.github.com/repos/" + GITHUB_OWNER + "/" + GITHUB_REPO
                + "/actions/runs/" + runId + "/artifacts";
        HttpURLConnection conn = openConnection(urlStr, "GET");
        String response = readResponse(conn);
        JSONObject json = new JSONObject(response);
        JSONArray artifacts = json.getJSONArray("artifacts");
        if (artifacts.length() == 0) return null;
        return artifacts.getJSONObject(0).getString("archive_download_url");
    }

    private File downloadAndExtractApk(String urlStr, File outputDir) throws Exception {
        HttpURLConnection conn = openConnection(urlStr, "GET");
        InputStream in = conn.getInputStream();
        ZipInputStream zipIn = new ZipInputStream(in);
        ZipEntry entry;
        File apkFile = null;

        while ((entry = zipIn.getNextEntry()) != null) {
            if (entry.getName().endsWith(".apk")) {
                apkFile = new File(outputDir, "aalam-compiler-output.apk");
                FileOutputStream fos = new FileOutputStream(apkFile);
                byte[] buffer = new byte[4096];
                int len;
                while ((len = zipIn.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                break;
            }
        }
        zipIn.close();
        return apkFile;
    }

    private HttpURLConnection openConnection(String urlStr, String method) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Authorization", "token " + token);
        conn.setRequestProperty("Accept", "application/vnd.github+json");
        if (method.equals("POST")) {
            conn.setDoOutput(true);
        }
        return conn;
    }

    private String readResponse(HttpURLConnection conn) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        return sb.toString();
    }
}
