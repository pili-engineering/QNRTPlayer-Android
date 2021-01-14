package com.qiniu.droid.rtplayer.demo.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;

import com.qiniu.droid.rtplayer.demo.R;
import com.qiniu.droid.rtplayer.demo.model.ProgressEvent;
import com.qiniu.droid.rtplayer.demo.model.UpdateInfo;
import com.qiniu.droid.rtplayer.demo.service.DownloadService;
import com.qiniu.droid.rtplayer.demo.utils.Config;
import com.qiniu.droid.rtplayer.demo.utils.PermissionChecker;
import com.qiniu.droid.rtplayer.demo.utils.QNAppServer;
import com.qiniu.droid.rtplayer.demo.utils.ToastUtils;

import de.greenrobot.event.EventBus;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_main);

        EventBus.getDefault().registerSticky(this);

        checkUpdate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onClickSetting(View v) {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    public void onClickToStreaming(View v) {
        if (!isPermissionOK()) {
            return;
        }
        Intent intent = new Intent(MainActivity.this, AddressConfigActivity.class);
        intent.putExtra(Config.MODE, Config.OPEN_TYPE_STREAMING);
        startActivity(intent);
    }

    public void onClickToPlaying(View v) {
        if (!isPermissionOK()) {
            return;
        }
        Intent intent = new Intent(MainActivity.this, AddressConfigActivity.class);
        intent.putExtra(Config.MODE, Config.OPEN_TYPE_PLAYING);
        startActivity(intent);
    }

    public void onEvent(ProgressEvent progressEvent){
        mProgressDialog.setProgress(progressEvent.getProgress());
        if (progressEvent.getProgress() == 100) {
            mProgressDialog.dismiss();
        }
    }

    private boolean isPermissionOK() {
        PermissionChecker checker = new PermissionChecker(this);
        boolean isPermissionOK = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checker.checkPermission();
        if (!isPermissionOK) {
            ToastUtils.l(this, "Some permissions is not approved !!!");
        }
        return isPermissionOK;
    }

    private void checkUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final UpdateInfo updateInfo = QNAppServer.getInstance().getUpdateInfo();
                if (updateInfo != null && updateInfo.getVersion() > Config.CURRENT_VERSION_CODE) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showUpgradeDialog(updateInfo.getDescription(), updateInfo.getDownloadURL());
                        }
                    });
                }
            }
        }).start();
    }

    private void showUpgradeDialog(String content, final String downloadUrl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.android_auto_update_dialog_title);
        builder.setMessage(Html.fromHtml(content))
                .setPositiveButton(R.string.android_auto_update_dialog_btn_download, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        createProgressDialog();
                        goToDownload(downloadUrl);
                    }
                })
                .setNegativeButton(R.string.android_auto_update_dialog_btn_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {}
                });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void createProgressDialog() {
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setTitle(getString(R.string.updating));
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
    }

    private void goToDownload(String downloadUrl) {
        Intent intent = new Intent(MainActivity.this, DownloadService.class);
        intent.putExtra(Config.DOWNLOAD_URL, downloadUrl);
        startService(intent);
    }
}
