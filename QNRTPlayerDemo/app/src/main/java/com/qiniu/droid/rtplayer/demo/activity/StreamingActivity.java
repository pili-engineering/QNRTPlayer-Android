package com.qiniu.droid.rtplayer.demo.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.qiniu.droid.rtplayer.demo.R;
import com.qiniu.droid.rtplayer.demo.ui.CameraPreviewFrameView;
import com.qiniu.droid.rtplayer.demo.ui.RotateLayout;
import com.qiniu.droid.rtplayer.demo.utils.Config;
import com.qiniu.droid.rtplayer.demo.utils.QNAppServer;
import com.qiniu.droid.rtplayer.demo.utils.StreamingSettings;
import com.qiniu.droid.rtplayer.demo.utils.ToastUtils;
import com.qiniu.pili.droid.streaming.AVCodecType;
import com.qiniu.pili.droid.streaming.CameraStreamingSetting;
import com.qiniu.pili.droid.streaming.MediaStreamingManager;
import com.qiniu.pili.droid.streaming.StreamStatusCallback;
import com.qiniu.pili.droid.streaming.StreamingProfile;
import com.qiniu.pili.droid.streaming.StreamingSessionListener;
import com.qiniu.pili.droid.streaming.StreamingState;
import com.qiniu.pili.droid.streaming.StreamingStateChangedListener;

import java.net.URISyntaxException;
import java.util.List;

public class StreamingActivity extends AppCompatActivity {
    public static final String TAG = "StreamingActivity";

    private static final int MESSAGE_ID_RECONNECTING = 0x01;
    private TextView mLogText;
    private ImageButton mToggleLightButton;

    private MediaStreamingManager mMediaStreamingManager;
    private StreamingProfile mStreamingProfile;
    private RotateLayout mRotateLayout;

    private boolean mIsInReadyState;
    private boolean mIsActivityPaused = true;
    private boolean mIsQuicEnabled = false;
    private boolean mIsLightOn = false;
    private boolean mIsFaceBeautyOn = true;
    private int mCurrentZoom = 0;
    private int mMaxZoom = 0;
    private int mCurrentCamFacingIndex;
    private int mEncodingWidth;
    private int mEncodingHeight;
    private String mRoomName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_streaming);

        mLogText = (TextView) findViewById(R.id.log_text);
        mToggleLightButton = (ImageButton) findViewById(R.id.toggle_light_button);
        CameraPreviewFrameView cameraPreviewFrameView = (CameraPreviewFrameView) findViewById(R.id.camera_preview_surfaceview);

        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        mRoomName = preferences.getString(StreamingSettings.STREAMING_ROOMNAME, "");
        mIsQuicEnabled = preferences.getBoolean(StreamingSettings.QUIC_ENABLE, false);

        CameraStreamingSetting.CAMERA_FACING_ID facingId = chooseCameraFacingId();
        mCurrentCamFacingIndex = facingId.ordinal();

        /**
         * config camera & microphone settings
         */
        CameraStreamingSetting cameraStreamingSetting = new CameraStreamingSetting();
        cameraStreamingSetting.setCameraFacingId(facingId)
                .setContinuousFocusModeEnabled(true)
                .setRecordingHint(false)
                .setResetTouchFocusDelayInMs(3000)
                .setFocusMode(CameraStreamingSetting.FOCUS_MODE_CONTINUOUS_PICTURE)
                .setCameraPrvSizeLevel(CameraStreamingSetting.PREVIEW_SIZE_LEVEL.MEDIUM)
                .setCameraPrvSizeRatio(CameraStreamingSetting.PREVIEW_SIZE_RATIO.RATIO_16_9)
                .setPreviewAdaptToEncodingSize(false)
                .setBuiltInFaceBeautyEnabled(mIsFaceBeautyOn)
                .setFaceBeautySetting(new CameraStreamingSetting.FaceBeautySetting(0.8f, 0.8f, 0.6f))
                .setVideoFilter(CameraStreamingSetting.VIDEO_FILTER_TYPE.VIDEO_FILTER_BEAUTY); // set the beauty on/off

        AVCodecType codecType = preferences.getBoolean(StreamingSettings.SW_ENABLE, false)
                ? AVCodecType.SW_VIDEO_WITH_SW_AUDIO_CODEC : AVCodecType.HW_VIDEO_SURFACE_AS_INPUT_WITH_HW_AUDIO_CODEC;

        mMediaStreamingManager = new MediaStreamingManager(getApplicationContext(), cameraPreviewFrameView, codecType);
        mMediaStreamingManager.setStreamingStateListener(mStreamingStateChangedListener);
        mMediaStreamingManager.setStreamingSessionListener(mStreamingSessionListener);
        mMediaStreamingManager.setStreamStatusCallback(mStreamStatusCallback);

        mStreamingProfile = new StreamingProfile();
        try {
            mStreamingProfile.setPublishUrl(getIntent().getStringExtra(Config.STREAMING_URL));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        mStreamingProfile.setAudioQuality(StreamingProfile.AUDIO_QUALITY_MEDIUM1)
                .setEncoderRCMode(preferences.getBoolean(StreamingSettings.QUALITY_PRIORITY_ENABLE, true) ?
                        StreamingProfile.EncoderRCModes.QUALITY_PRIORITY : StreamingProfile.EncoderRCModes.BITRATE_PRIORITY)
                .setFpsControllerEnable(true)
                .setQuicEnable(mIsQuicEnabled)
                .setYuvFilterMode(StreamingSettings.YUV_FILTER_MODE_MAPPING[getIntent().getIntExtra("yuvFilterMode", 0)])
                .setSendingBufferProfile(new StreamingProfile.SendingBufferProfile(0.2f, 0.8f, 3.0f, 20 * 1000))
                .setBitrateAdjustMode(preferences.getBoolean(StreamingSettings.AUTO_BITRATE_ENABLED, true)
                        ? StreamingProfile.BitrateAdjustMode.Auto : StreamingProfile.BitrateAdjustMode.Disable);

        // set the video quality
        if (preferences.getBoolean(StreamingSettings.VIDEO_QUALITY_PREBUILT_ENABLE, true)) {
            mStreamingProfile.setVideoQuality(StreamingSettings.PREBUILT_VIDEO_QUALITY[preferences.getInt(StreamingSettings.PREBUILT_VIDEO_QUALITY_POS, 3)]);
        } else {
            StreamingProfile.AudioProfile aProfile = new StreamingProfile.AudioProfile(44100, 48 * 1024);
            StreamingProfile.VideoProfile vProfile = new StreamingProfile.VideoProfile(preferences.getInt(StreamingSettings.TARGET_FPS, 20),
                    preferences.getInt(StreamingSettings.TARGET_BITRATE, 1000) * 1024,
                    preferences.getInt(StreamingSettings.TARGET_GOP, 60),
                    StreamingProfile.H264Profile.HIGH);
            StreamingProfile.AVProfile avProfile = new StreamingProfile.AVProfile(vProfile, aProfile);
            mStreamingProfile.setAVProfile(avProfile);
        }

        // set the encoding size
        if (preferences.getBoolean(StreamingSettings.CODEC_SIZE_PREBUILT_ENABLE, true)) {
            int prebuiltCodecSizePos = preferences.getInt(StreamingSettings.PREBUILT_CODEC_SIZE_POS, 1);
            mStreamingProfile.setEncodingSizeLevel(StreamingSettings.PREBUILT_CODEC_SIZE[prebuiltCodecSizePos]);
            mEncodingWidth = StreamingSettings.CODEC_SIZE[prebuiltCodecSizePos][0];
            mEncodingHeight = StreamingSettings.CODEC_SIZE[prebuiltCodecSizePos][1];
        } else {
            mEncodingWidth = preferences.getInt(StreamingSettings.TARGET_WIDTH, 480);
            mEncodingHeight = preferences.getInt(StreamingSettings.TARGET_HEIGHT, 848);
            mStreamingProfile.setPreferredVideoEncodingSize(mEncodingWidth, mEncodingHeight);
        }

        mMediaStreamingManager.prepare(cameraStreamingSetting, null, null, mStreamingProfile);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsActivityPaused = false;
        mMediaStreamingManager.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsActivityPaused = true;
        mMediaStreamingManager.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaStreamingManager.destroy();
    }

    public void onClickLogButton(View v) {
        mLogText.setVisibility(mLogText.getVisibility() == View.INVISIBLE ? View.VISIBLE : View.INVISIBLE);
    }

    public void onClickClose(View v) {
        mMediaStreamingManager.stopStreaming();
        finish();
    }

    public void onClickSwitchCamera(View v) {
        mCurrentCamFacingIndex = (mCurrentCamFacingIndex + 1) % CameraStreamingSetting.getNumberOfCameras();
        CameraStreamingSetting.CAMERA_FACING_ID facingId;
        if (mCurrentCamFacingIndex == CameraStreamingSetting.CAMERA_FACING_ID.CAMERA_FACING_BACK.ordinal()) {
            facingId = CameraStreamingSetting.CAMERA_FACING_ID.CAMERA_FACING_BACK;
        } else if (mCurrentCamFacingIndex == CameraStreamingSetting.CAMERA_FACING_ID.CAMERA_FACING_FRONT.ordinal()) {
            facingId = CameraStreamingSetting.CAMERA_FACING_ID.CAMERA_FACING_FRONT;
        } else {
            facingId = CameraStreamingSetting.CAMERA_FACING_ID.CAMERA_FACING_3RD;
        }
        Log.i(TAG, "switchCamera:" + facingId);
        mMediaStreamingManager.switchCamera(facingId);
    }

    public void onClickCopyRoomName(View v) {

    }

    public void onClickToggleLight(View v) {
        if (mCurrentCamFacingIndex == CameraStreamingSetting.CAMERA_FACING_ID.CAMERA_FACING_FRONT.ordinal()) {
            ToastUtils.s(StreamingActivity.this, getString(R.string.cannot_toggle_light_in_front));
            return;
        }
        mIsLightOn = !mIsLightOn;
        if (mIsLightOn) {
            mMediaStreamingManager.turnLightOn();
        } else {
            mMediaStreamingManager.turnLightOff();
        }
        mToggleLightButton.setImageResource(mIsLightOn ? R.mipmap.light_off : R.mipmap.light_on);
    }

    public void onClickToggleFaceBeauty(View v) {
        mIsFaceBeautyOn = !mIsFaceBeautyOn;
        mMediaStreamingManager.setVideoFilterType(mIsFaceBeautyOn ?
                CameraStreamingSetting.VIDEO_FILTER_TYPE.VIDEO_FILTER_BEAUTY
                : CameraStreamingSetting.VIDEO_FILTER_TYPE.VIDEO_FILTER_NONE);
    }

    protected void setFocusAreaIndicator() {
        if (mRotateLayout == null) {
            mRotateLayout = (RotateLayout) findViewById(R.id.focus_indicator_rotate_layout);
            mMediaStreamingManager.setFocusAreaIndicator(mRotateLayout, mRotateLayout.findViewById(R.id.focus_indicator));
        }
    }

    private CameraStreamingSetting.CAMERA_FACING_ID chooseCameraFacingId() {
        if (CameraStreamingSetting.hasCameraFacing(CameraStreamingSetting.CAMERA_FACING_ID.CAMERA_FACING_3RD)) {
            return CameraStreamingSetting.CAMERA_FACING_ID.CAMERA_FACING_3RD;
        } else if (CameraStreamingSetting.hasCameraFacing(CameraStreamingSetting.CAMERA_FACING_ID.CAMERA_FACING_FRONT)) {
            return CameraStreamingSetting.CAMERA_FACING_ID.CAMERA_FACING_FRONT;
        } else {
            return CameraStreamingSetting.CAMERA_FACING_ID.CAMERA_FACING_BACK;
        }
    }

    private void copyToClipboard(String content) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            ClipData clipData = ClipData.newPlainText("Label", content);
            cm.setPrimaryClip(clipData);
        }
    }

    private void showToast(final String text, final int duration) {
        if (mIsActivityPaused) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (duration == Toast.LENGTH_SHORT) {
                    ToastUtils.s(StreamingActivity.this, text);
                } else {
                    ToastUtils.l(StreamingActivity.this, text);
                }
            }
        });
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what != MESSAGE_ID_RECONNECTING || mIsActivityPaused) {
                return;
            }
            if (!QNAppServer.isNetworkAvailable(StreamingActivity.this)) {
                sendReconnectMessage();
                return;
            }
            Log.d(TAG, "do reconnecting ...");
            mMediaStreamingManager.startStreaming();
        }
    };

    private void sendReconnectMessage() {
        showToast(getString(R.string.reconnecting), Toast.LENGTH_SHORT);
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_ID_RECONNECTING), 500);
    }

    private StreamingStateChangedListener mStreamingStateChangedListener = new StreamingStateChangedListener() {
        @Override
        public void onStateChanged(final StreamingState state, Object o) {
            switch (state) {
                case PREPARING:
                    Log.d(TAG, "onStateChanged state:" + "preparing");
                    break;
                case READY:
                    mIsInReadyState = true;
                    mMaxZoom = mMediaStreamingManager.getMaxZoom();
                    mMediaStreamingManager.startStreaming();
                    Log.d(TAG, "onStateChanged state:" + "ready");
                    break;
                case CONNECTING:
                    Log.d(TAG, "onStateChanged state:" + "connecting");
                    break;
                case STREAMING:
                    Log.d(TAG, "onStateChanged state:" + "streaming");
                    break;
                case SHUTDOWN:
                    mIsInReadyState = true;
                    Log.d(TAG, "onStateChanged state:" + "shutdown");
                    break;
                case UNKNOWN:
                    Log.d(TAG, "onStateChanged state:" + "unknown");
                    break;
                case SENDING_BUFFER_EMPTY:
                    Log.d(TAG, "onStateChanged state:" + "sending buffer empty");
                    break;
                case SENDING_BUFFER_FULL:
                    Log.d(TAG, "onStateChanged state:" + "sending buffer full");
                    break;
                case OPEN_CAMERA_FAIL:
                    Log.d(TAG, "onStateChanged state:" + "open camera failed");
                    showToast( getString(R.string.failed_open_camera), Toast.LENGTH_SHORT);
                    break;
                case AUDIO_RECORDING_FAIL:
                    Log.d(TAG, "onStateChanged state:" + "audio recording failed");
                    showToast( getString(R.string.failed_open_microphone), Toast.LENGTH_SHORT);
                    break;
                case IOERROR:
                    /**
                     * Network-connection is unavailable when `startStreaming`.
                     * You can do reconnecting or just finish the streaming
                     */
                    Log.d(TAG, "onStateChanged state:" + "io error");
                    showToast( getString(R.string.streaming_io_error), Toast.LENGTH_SHORT);
                    sendReconnectMessage();
                    break;
                case DISCONNECTED:
                    /**
                     * Network-connection is broken after `startStreaming`.
                     * You can do reconnecting in `onRestartStreamingHandled`
                     */
                    Log.d(TAG, "onStateChanged state:" + "disconnected");
                    showToast( getString(R.string.disconnected), Toast.LENGTH_SHORT);
                    // we will process this state in `onRestartStreamingHandled`
                    break;
            }
        }
    };

    private StreamingSessionListener mStreamingSessionListener = new StreamingSessionListener() {
        @Override
        public boolean onRecordAudioFailedHandled(int code) {
            return false;
        }

        /**
         * When the network-connection is broken, StreamingState#DISCONNECTED will notified first,
         * and then invoked this method if the environment of restart streaming is ready.
         *
         * @return true means you handled the event; otherwise, given up and then StreamingState#SHUTDOWN
         * will be notified.
         */
        @Override
        public boolean onRestartStreamingHandled(int code) {
            Log.d(TAG, "onRestartStreamingHandled, reconnect ...");
            return mMediaStreamingManager.startStreaming();
        }

        @Override
        public Camera.Size onPreviewSizeSelected(List<Camera.Size> list) {
            for (Camera.Size size : list) {
                if (size.height >= 480) {
                    return size;
                }
            }
            return null;
        }

        @Override
        public int onPreviewFpsSelected(List<int[]> list) {
            return -1;
        }
    };

    private StreamStatusCallback mStreamStatusCallback = new StreamStatusCallback() {
        @Override
        public void notifyStreamStatusChanged(final StreamingProfile.StreamStatus streamStatus) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String stat = String.format(getString(R.string.streaming_log_text),
                            mIsQuicEnabled ? getString(R.string.QUIC_protocol) : getString(R.string.TCP_protocol),
                            mEncodingWidth, mEncodingHeight, streamStatus.videoBitrate / 1024, streamStatus.audioBitrate / 1024,
                            streamStatus.videoFps, streamStatus.audioFps);
                    mLogText.setText(stat);
                }
            });
        }
    };

    private CameraPreviewFrameView.Listener mCameraPreviewListener = new CameraPreviewFrameView.Listener() {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.i(TAG, "onSingleTapUp X:" + e.getX() + ",Y:" + e.getY());
            if (mIsInReadyState) {
                setFocusAreaIndicator();
                mMediaStreamingManager.doSingleTapUp((int) e.getX(), (int) e.getY());
                return true;
            }
            return false;
        }

        @Override
        public boolean onZoomValueChanged(float factor) {
            if (mIsInReadyState && mMediaStreamingManager.isZoomSupported()) {
                mCurrentZoom = (int) (mMaxZoom * factor);
                mCurrentZoom = Math.min(mCurrentZoom, mMaxZoom);
                mCurrentZoom = Math.max(0, mCurrentZoom);
                Log.d(TAG, "zoom ongoing, scale: " + mCurrentZoom + ",factor:" + factor + ",maxZoom:" + mMaxZoom);
                mMediaStreamingManager.setZoomValue(mCurrentZoom);
            }
            return false;
        }
    };
}
