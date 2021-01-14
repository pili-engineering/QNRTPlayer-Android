package com.qiniu.droid.rtplayer.demo.activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.qiniu.droid.rtplayer.demo.R;
import com.qiniu.droid.rtplayer.demo.utils.Config;
import com.qiniu.droid.rtplayer.demo.utils.QNAppServer;
import com.qiniu.droid.rtplayer.demo.utils.StreamingSettings;
import com.qiniu.droid.rtplayer.demo.utils.ToastUtils;

public class AddressConfigActivity extends AppCompatActivity {
    private static final String URL_REGEX = "(webrtc|rtmp|http)://[-a-zA-Z0-9._?=/%&+~:]+";
    private static final String ROOM_NAME_REGEX = "[-a-zA-Z0-9_]+";

    private EditText mAddressConfigEditText;
    private Button mStartLivingButton;
    private RadioButton mProtocolRadioButton;
    private LinearLayout mProtocolLayout;

    private int mOpenType;
    private boolean mIsProtocolAgreed;
    private String mPublishUrl;
    private String mPlayingUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_address_config);
        mAddressConfigEditText = (EditText) findViewById(R.id.address_config_edit_text);
        mStartLivingButton = (Button) findViewById(R.id.start_living_button);
        mProtocolLayout = (LinearLayout) findViewById(R.id.protocol_layout);
        mProtocolRadioButton = (RadioButton) findViewById(R.id.protocol_radio_button);
        mProtocolRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsProtocolAgreed = !mIsProtocolAgreed;
                mProtocolRadioButton.setChecked(mIsProtocolAgreed);
            }
        });

        mOpenType = getIntent().getIntExtra(Config.MODE, 0);

        if (isStreamingType()) {
            mProtocolLayout.setVisibility(View.VISIBLE);
        }

        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        String roomName = preferences.getString(isStreamingType() ?
                StreamingSettings.STREAMING_ROOMNAME : StreamingSettings.PLAYING_ROOMNAME, "http://pili-hls.qnsdk.com/sdk-live/timestamp.m3u8");
        mAddressConfigEditText.setText(roomName);

        if (isStreamingType()) {
            mAddressConfigEditText.setHint(R.string.streaming_mode_hint);
            mStartLivingButton.setText(R.string.streaming_mode_button_text);
        } else {
            mAddressConfigEditText.setHint(R.string.playing_mode_hint);
            mStartLivingButton.setText(R.string.playing_mode_button_text);
        }

        mStartLivingButton.setOnClickListener(mOnStartLivingClickListener);
    }

    public void onClickBack(View v) {
        finish();
    }

    private View.OnClickListener mOnStartLivingClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!QNAppServer.isNetworkAvailable(AddressConfigActivity.this)) {
                ToastUtils.s(AddressConfigActivity.this, getString(R.string.network_disconnected));
                return;
            }
            if (isStreamingType() && !mProtocolRadioButton.isChecked()) {
                ToastUtils.s(AddressConfigActivity.this, getString(R.string.niuliving_protocol_tips));
                return;
            }
            final String roomName = mAddressConfigEditText.getText().toString().trim();
            if ("".equals(roomName)) {
                ToastUtils.s(AddressConfigActivity.this, getString(R.string.null_room_name_toast));
                return;
            }
            SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).edit();
            if (isStreamingType()) {
                editor.putString(StreamingSettings.STREAMING_ROOMNAME, roomName);
                editor.apply();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (roomName.matches(ROOM_NAME_REGEX)) {
                            mPublishUrl = QNAppServer.getInstance().requestPublishUrl(roomName);
                        } else if (roomName.matches(URL_REGEX)) {
                            mPublishUrl = roomName;
                        } else {
                            mPublishUrl = null;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mPublishUrl == null) {
                                    ToastUtils.s(AddressConfigActivity.this,
                                            roomName.matches(ROOM_NAME_REGEX)
                                                    ? getString(R.string.get_url_failed)
                                                    : getString(R.string.illegal_publish_url));
                                    return;
                                }
                                Intent intent = new Intent(AddressConfigActivity.this, StreamingActivity.class);
                                intent.putExtra(Config.STREAMING_URL, mPublishUrl);
                                startActivity(intent);
                            }
                        });
                    }
                }).start();
            } else {
                editor.putString(StreamingSettings.PLAYING_ROOMNAME, roomName);
                editor.apply();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (roomName.matches(ROOM_NAME_REGEX)) {
                            mPlayingUrl = QNAppServer.getInstance().requestPlayUrl(roomName);
                        } else if (roomName.matches(URL_REGEX)) {
                            mPlayingUrl = roomName;
                        } else {
                            mPlayingUrl = null;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mPlayingUrl == null) {
                                    ToastUtils.s(AddressConfigActivity.this,
                                            roomName.matches(ROOM_NAME_REGEX)
                                                    ? getString(R.string.get_url_failed)
                                                    : getString(R.string.illegal_play_url));
                                    return;
                                }
                                Intent intent = new Intent(AddressConfigActivity.this, PlayingActivity.class);
                                intent.putExtra(Config.PLAYING_URL, mPlayingUrl);
                                startActivity(intent);
                            }
                        });
                    }
                }).start();
            }
        }
    };

    public void onClickProtocol(View v) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_protocol);
        WebView webView = (WebView) dialog.findViewById(R.id.protocol_web_view);
        webView.loadUrl("file:///android_asset/user_declare.html");
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        dialog.getWindow().setAttributes(params);
        dialog.show();
    }

    private boolean isStreamingType() {
        return mOpenType == Config.OPEN_TYPE_STREAMING;
    }
}
