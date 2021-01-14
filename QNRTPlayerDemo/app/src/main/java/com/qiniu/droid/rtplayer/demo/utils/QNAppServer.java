package com.qiniu.droid.rtplayer.demo.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.qiniu.droid.rtplayer.demo.model.UpdateInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class QNAppServer {
    private static final String APP_SERVER_ADDR = "https://api-demo.qnsdk.com";
    private static final String APPID = "com.qiniu.droid.niuliving";

    private static class QNAppServerHolder {
        private static final QNAppServer instance = new QNAppServer();
    }

    private QNAppServer(){}

    public static final QNAppServer getInstance() {
        return QNAppServerHolder.instance;
    }

    public String requestPublishUrl(String roomName) {
        String url = APP_SERVER_ADDR + "/v1/live/stream/" + roomName;
        return doGetRequest(url);
    }

    public String requestPlayUrl(String roomName) {
        String url = APP_SERVER_ADDR + "/v1/live/play/" + roomName + "/rtmp";
        return doGetRequest(url);
    }

    public UpdateInfo getUpdateInfo() {
        String url = APP_SERVER_ADDR + "/v1/upgrade/app?appId=" + APPID;

        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                JSONObject jsonObject = new JSONObject(response.body().string());
                UpdateInfo updateInfo = new UpdateInfo();
                updateInfo.setAppID(jsonObject.getString(Config.APP_ID));
                updateInfo.setVersion(jsonObject.getInt(Config.VERSION));
                updateInfo.setDescription(jsonObject.getString(Config.DESCRIPTION));
                updateInfo.setDownloadURL(jsonObject.getString(Config.DOWNLOAD_URL));
                updateInfo.setCreateTime(jsonObject.getString(Config.CREATE_TIME));
                return updateInfo;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String doGetRequest(String url) {
        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
