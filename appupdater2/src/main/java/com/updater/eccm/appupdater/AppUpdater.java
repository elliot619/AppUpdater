package com.updater.eccm.appupdater;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.os.Environment.DIRECTORY_DOWNLOADS;


public class AppUpdater {

    private Context ctx;
    private DownloadManager dm;
    private Float currentVersion;
    private String downloadedApkFileName;

    public AppUpdater(Context ctx) {
        this.ctx = ctx;
    }

    public void sendNetworkUpdateAppRequest(String updateUrl) {
        try {
            currentVersion = Float.parseFloat(this.ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName);
            new UpdateAppAsyncTask().execute(updateUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void install(String apkFileName) {
        try {
            Intent intent;
            File toInstall = new File(apkFileName);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri apkUri = FileProvider.getUriForFile(ctx, ctx.getPackageName() + ".provider", toInstall);
                intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                intent.setData(apkUri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                Uri apkUri = Uri.fromFile(toInstall);
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }

            ctx.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(ctx, ctx.getString(R.string.update_error), Toast.LENGTH_LONG).show();
        }
    }

    private static JSONObject getJson(String address) {
        JSONObject jsonObject = null;
        try {
            URL url = new URL(address);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream in = new BufferedInputStream(conn.getInputStream());

            String result = IOUtils.toString(in, "UTF-8");
            in.close();
            conn.disconnect();

            jsonObject = new JSONObject(result);

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return jsonObject;
    }

    private void UpdateApp(final String apkurl, final Double newVersion) {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {

                    Bundle extras = intent.getExtras();
                    DownloadManager.Query q = new DownloadManager.Query();
                    q.setFilterById(extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID));
                    Cursor c = dm.query(q);

                    if (c.moveToFirst()) {
                        int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            String apkLocation = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                            apkLocation = apkLocation.replace("file://", "");
                            install(apkLocation);
                        } else {
                            Toast.makeText(ctx, ctx.getString(R.string.update_error), Toast.LENGTH_LONG).show();
                        }

                        Log.i("handleData()", "Reason: " + c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON)));
                    }
                }
            }
        };

        ctx.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(ctx.getString(R.string.update_available_title));
        builder.setMessage(ctx.getString(R.string.update_available_msg))
                .setCancelable(false)
                .setPositiveButton(ctx.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        downloadedApkFileName = ctx.getApplicationInfo().loadLabel(ctx.getPackageManager()) + "_" + String.valueOf(newVersion) + ".apk";
                        downloadedApkFileName = downloadedApkFileName.replace(" ", "_");

                        dialog.cancel();
                        dm = (DownloadManager) ctx.getSystemService(ctx.DOWNLOAD_SERVICE);
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkurl));
                        request.setMimeType("application/vnd.android.package-archive");
                        request.setDescription(ctx.getString(R.string.update_provider_name));
                        request.setDestinationInExternalFilesDir(ctx, DIRECTORY_DOWNLOADS, downloadedApkFileName);
                        request.setTitle(downloadedApkFileName);
                        dm.enqueue(request);
                    }
                })
                .setNegativeButton(ctx.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private class UpdateAppAsyncTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... urls) {
            return getJson(urls[0]);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                Double newVersion = jsonObject.getDouble("currentVersion");
                if (currentVersion < newVersion) {
                    String newApkUrl = jsonObject.getString("apkUrl");
                    UpdateApp(newApkUrl, newVersion);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}