package com.qiniu.droid.rtplayer.demo.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.qiniu.droid.rtplayer.demo.BuildConfig;
import com.qiniu.droid.rtplayer.demo.R;
import com.qiniu.droid.rtplayer.demo.utils.StreamingSettings;
import com.qiniu.droid.rtplayer.demo.utils.ToastUtils;


import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.qiniu.droid.rtplayer.demo.utils.StreamingSettings.AUTO_BITRATE_ENABLED;
import static com.qiniu.droid.rtplayer.demo.utils.StreamingSettings.CODEC_SIZE_PREBUILT_ENABLE;
import static com.qiniu.droid.rtplayer.demo.utils.StreamingSettings.DEBUG_MODE_ENABLED;
import static com.qiniu.droid.rtplayer.demo.utils.StreamingSettings.DEFAULT_CACHE;
import static com.qiniu.droid.rtplayer.demo.utils.StreamingSettings.MAX_CACHE;
import static com.qiniu.droid.rtplayer.demo.utils.StreamingSettings.PREBUILT_CODEC_SIZE_POS;
import static com.qiniu.droid.rtplayer.demo.utils.StreamingSettings.PREBUILT_VIDEO_QUALITY_POS;
import static com.qiniu.droid.rtplayer.demo.utils.StreamingSettings.QUALITY_PRIORITY_ENABLE;
import static com.qiniu.droid.rtplayer.demo.utils.StreamingSettings.QUIC_ENABLE;
import static com.qiniu.droid.rtplayer.demo.utils.StreamingSettings.SW_ENABLE;
import static com.qiniu.droid.rtplayer.demo.utils.StreamingSettings.TARGET_BITRATE;
import static com.qiniu.droid.rtplayer.demo.utils.StreamingSettings.TARGET_FPS;
import static com.qiniu.droid.rtplayer.demo.utils.StreamingSettings.TARGET_GOP;
import static com.qiniu.droid.rtplayer.demo.utils.StreamingSettings.TARGET_HEIGHT;
import static com.qiniu.droid.rtplayer.demo.utils.StreamingSettings.TARGET_WIDTH;
import static com.qiniu.droid.rtplayer.demo.utils.StreamingSettings.VIDEO_QUALITY_PREBUILT_ENABLE;

public class SettingActivity extends AppCompatActivity {

    private LinearLayout mCustomVideoQualityLayout;
    private LinearLayout mCustomCodecSizeLayout;
    private Spinner mVideoQualitySpinner;
    private Spinner mCodecSizeSpinner;
    private RadioGroup mTransportProtocolRadioGroup;
    private RadioGroup mCodecModeRadioGroup;
    private RadioGroup mVideoQualityRadioGroup;
    private RadioGroup mCodecSizeRadioGroup;
    private RadioGroup mCodecControlRadioGroup;
    private RadioGroup mAutoBitrateRadioGroup;
    private RadioGroup mDebugModeRadioGroup;
    private RadioButton mQUICModeButton;
    private RadioButton mTCPModeButton;
    private RadioButton mSWModeButton;
    private RadioButton mHWModeButton;
    private RadioButton mVideoQualityPrebuiltButton;
    private RadioButton mVideoQualityCustomButton;
    private RadioButton mCodecSizePrebuiltButton;
    private RadioButton mCodecSizeCustomButton;
    private RadioButton mQualityPriorityButton;
    private RadioButton mBitratePriorityButton;
    private RadioButton mAutoBitrateEnableButton;
    private RadioButton mAutoBitrateDisableButton;
    private RadioButton mDebugModeEnableButton;
    private RadioButton mDebugModeDisableButton;
    private EditText mCustomVideoFpsEditText;
    private EditText mCustomVideoBitrateEditText;
    private EditText mCustomVideoGopEditText;
    private EditText mCustomCodecWidthEditText;
    private EditText mCustomCodecHeightEditText;

    private boolean mIsQuicEnable = false;
    private boolean mIsSwCodecEnable = false;
    private boolean mIsVideoQualityPrebuiltEnable = false;
    private boolean mIsCodecSizePrebuiltEnable = false;
    private boolean mIsQualityPriorityEnable = true;
    private boolean mIsAutoBitrateEnable = true;
    private boolean mIsDebugModeEnable = false;
    private int mPrebuiltVideoQualityPos = 3;
    private int mPrebuiltCodecSizePos = 1;
    private int mTargetFps;
    private int mTargetBitrate;
    private int mTargetGop;
    private int mEncodingWidth;
    private int mEncodingHeight;
    private int mDefaultCache;
    private int mMaxCache;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_setting);

        getDefaultConfigurations();
        initView();
    }

    @Override
    public void onBackPressed() {
        saveConfigurations();
    }

    public void onClickBack(View v) {
        saveConfigurations();
    }

    private void getDefaultConfigurations() {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        mIsQuicEnable = preferences.getBoolean(QUIC_ENABLE, false);
        mIsSwCodecEnable = preferences.getBoolean(SW_ENABLE, false);
        mIsVideoQualityPrebuiltEnable = preferences.getBoolean(VIDEO_QUALITY_PREBUILT_ENABLE, true);
        mIsCodecSizePrebuiltEnable = preferences.getBoolean(CODEC_SIZE_PREBUILT_ENABLE, true);
        mIsQualityPriorityEnable = preferences.getBoolean(QUALITY_PRIORITY_ENABLE, true);
        mIsAutoBitrateEnable = preferences.getBoolean(AUTO_BITRATE_ENABLED, true);
        mIsDebugModeEnable = preferences.getBoolean(DEBUG_MODE_ENABLED, false);
        mPrebuiltVideoQualityPos  = preferences.getInt(PREBUILT_VIDEO_QUALITY_POS, 3);
        mPrebuiltCodecSizePos = preferences.getInt(PREBUILT_CODEC_SIZE_POS, 1);
        mTargetFps = preferences.getInt(TARGET_FPS, 30);
        mTargetBitrate = preferences.getInt(TARGET_BITRATE, 512);
        mTargetGop = preferences.getInt(TARGET_GOP, 60);
        mEncodingWidth = preferences.getInt(TARGET_WIDTH, 480);
        mEncodingHeight = preferences.getInt(TARGET_HEIGHT, 848);
        mDefaultCache = preferences.getInt(DEFAULT_CACHE, 100);
        mMaxCache = preferences.getInt(MAX_CACHE, 200);
    }

    private void initView() {
        mCustomVideoQualityLayout = (LinearLayout) findViewById(R.id.custom_video_quality_layout);
        mCustomCodecSizeLayout = (LinearLayout) findViewById(R.id.custom_codec_size_layout);
        mVideoQualitySpinner = (Spinner) findViewById(R.id.video_quality_spinner);
        mCodecSizeSpinner = (Spinner) findViewById(R.id.codec_size_spinner);
        mTransportProtocolRadioGroup = (RadioGroup) findViewById(R.id.transport_protocol_radio_group);
        mCodecModeRadioGroup = (RadioGroup) findViewById(R.id.codec_mode_radio_group);
        mVideoQualityRadioGroup = (RadioGroup) findViewById(R.id.video_quality_radio_group);
        mCodecSizeRadioGroup = (RadioGroup) findViewById(R.id.codec_size_radio_group);
        mCodecControlRadioGroup =(RadioGroup) findViewById(R.id.codec_control_radio_group);
        mAutoBitrateRadioGroup = (RadioGroup) findViewById(R.id.auto_bitrate_radio_group);
        mDebugModeRadioGroup = (RadioGroup) findViewById(R.id.debug_mode_radio_group);
        mQUICModeButton = (RadioButton) findViewById(R.id.QUIC_protocol);
        mTCPModeButton = (RadioButton) findViewById(R.id.TCP_protocol);
        mSWModeButton = (RadioButton) findViewById(R.id.codec_sw);
        mHWModeButton = (RadioButton) findViewById(R.id.codec_hw);
        mVideoQualityPrebuiltButton = (RadioButton) findViewById(R.id.video_quality_prebuilt);
        mVideoQualityCustomButton = (RadioButton) findViewById(R.id.video_quality_custom);
        mCodecSizePrebuiltButton = (RadioButton) findViewById(R.id.codec_size_prebuilt);
        mCodecSizeCustomButton = (RadioButton) findViewById(R.id.codec_size_custom);
        mQualityPriorityButton = (RadioButton) findViewById(R.id.quality_priority);
        mBitratePriorityButton = (RadioButton) findViewById(R.id.bitrate_priority);
        mAutoBitrateEnableButton = (RadioButton) findViewById(R.id.enable_auto_bitrate);
        mAutoBitrateDisableButton = (RadioButton) findViewById(R.id.disable_auto_bitrate);
        mDebugModeEnableButton = (RadioButton) findViewById(R.id.enable_debug_mode);
        mDebugModeDisableButton = (RadioButton) findViewById(R.id.disable_debug_mode);
        mCustomVideoFpsEditText = (EditText) findViewById(R.id.custom_fps_edit_text);
        mCustomVideoBitrateEditText = (EditText) findViewById(R.id.custom_bitrate_edit_text);
        mCustomVideoGopEditText = (EditText) findViewById(R.id.custom_gop_edit_text);
        mCustomCodecWidthEditText = (EditText) findViewById(R.id.custom_width_edit_text);
        mCustomCodecHeightEditText = (EditText) findViewById(R.id.custom_height_edit_text);

        mTransportProtocolRadioGroup.check(mIsQuicEnable ? mQUICModeButton.getId() : mTCPModeButton.getId());
        mCodecModeRadioGroup.check(mIsSwCodecEnable ? mSWModeButton.getId() : mHWModeButton.getId());
        mVideoQualityRadioGroup.check(mIsVideoQualityPrebuiltEnable ? mVideoQualityPrebuiltButton.getId() : mVideoQualityCustomButton.getId());
        mCodecSizeRadioGroup.check(mIsCodecSizePrebuiltEnable ? mCodecSizePrebuiltButton.getId() : mCodecSizeCustomButton.getId());
        mCodecControlRadioGroup.check(mIsQualityPriorityEnable ? mQualityPriorityButton.getId() : mBitratePriorityButton.getId());
        mAutoBitrateRadioGroup.check(mIsAutoBitrateEnable ? mAutoBitrateEnableButton.getId() : mAutoBitrateDisableButton.getId());
        mDebugModeRadioGroup.check(mIsDebugModeEnable ? mDebugModeEnableButton.getId() : mDebugModeDisableButton.getId());

        mTransportProtocolRadioGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mCodecModeRadioGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mCodecSizeRadioGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mVideoQualityRadioGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mCodecSizeRadioGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mCodecControlRadioGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mAutoBitrateRadioGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mDebugModeRadioGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, StreamingSettings.VIDEO_QUALITY_ARRAY);
        mVideoQualitySpinner.setAdapter(adapter);
        mVideoQualitySpinner.setSelection(mPrebuiltVideoQualityPos);
        mCustomVideoQualityLayout.setVisibility(mIsVideoQualityPrebuiltEnable ? View.GONE : View.VISIBLE);
        mVideoQualitySpinner.setVisibility(mIsVideoQualityPrebuiltEnable ? View.VISIBLE : View.GONE);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, StreamingSettings.CODEC_SIZE_ARRAY);
        mCodecSizeSpinner.setAdapter(adapter);
        mCodecSizeSpinner.setSelection(mPrebuiltCodecSizePos);
        mCustomCodecSizeLayout.setVisibility(mIsCodecSizePrebuiltEnable ? View.GONE : View.VISIBLE);
        mCodecSizeSpinner.setVisibility(mIsCodecSizePrebuiltEnable ? View.VISIBLE : View.GONE);

        if (!mIsVideoQualityPrebuiltEnable) {
            mCustomVideoFpsEditText.setText(String.valueOf(mTargetFps));
            mCustomVideoBitrateEditText.setText(String.valueOf(mTargetBitrate));
            mCustomVideoGopEditText.setText(String.valueOf(mTargetGop));
        }

        if (!mIsCodecSizePrebuiltEnable) {
            mCustomCodecWidthEditText.setText(String.valueOf(mEncodingWidth));
            mCustomCodecHeightEditText.setText(String.valueOf(mEncodingHeight));
        }

        ((TextView)findViewById(R.id.version_code)).setText(
                String.format(getString(R.string.version_code), getVersionDescription(), getBuildTimeDescription(), getSdkVersion()));
    }

    private void saveConfigurations() {
        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).edit();
        editor.putBoolean(QUIC_ENABLE, mIsQuicEnable);
        editor.putBoolean(SW_ENABLE, mIsSwCodecEnable);
        editor.putBoolean(VIDEO_QUALITY_PREBUILT_ENABLE, mIsVideoQualityPrebuiltEnable);
        if (mIsVideoQualityPrebuiltEnable) {
            mPrebuiltVideoQualityPos = mVideoQualitySpinner.getSelectedItemPosition();
            editor.putInt(PREBUILT_VIDEO_QUALITY_POS, mPrebuiltVideoQualityPos);
        } else {
            String customFps = mCustomVideoFpsEditText.getText().toString().trim();
            String customBitrate = mCustomVideoBitrateEditText.getText().toString().trim();
            String customGop = mCustomVideoGopEditText.getText().toString().trim();
            if ("".equals(customFps) || "".equals(customBitrate) || "".equals(customGop)) {
                ToastUtils.s(this, "Input custom configuration please!!!");
                return;
            }

            editor.putInt(TARGET_FPS, Integer.parseInt(customFps));
            editor.putInt(TARGET_BITRATE, Integer.parseInt(customBitrate));
            editor.putInt(TARGET_GOP, Integer.parseInt(customGop));
        }
        editor.putBoolean(CODEC_SIZE_PREBUILT_ENABLE, mIsCodecSizePrebuiltEnable);
        if (mIsCodecSizePrebuiltEnable) {
            mPrebuiltCodecSizePos = mCodecSizeSpinner.getSelectedItemPosition();
            editor.putInt(PREBUILT_CODEC_SIZE_POS, mPrebuiltCodecSizePos);
        } else {
            String customWidth = mCustomCodecWidthEditText.getText().toString().trim();
            String customHeight = mCustomCodecHeightEditText.getText().toString().trim();
            if ("".equals(customWidth) || "".equals(customHeight)) {
                ToastUtils.s(this, "Input custom configuration please!!!");
                return;
            }

            editor.putInt(TARGET_WIDTH, Integer.parseInt(customWidth));
            editor.putInt(TARGET_HEIGHT, Integer.parseInt(customHeight));
        }
        editor.putBoolean(QUALITY_PRIORITY_ENABLE, mIsQualityPriorityEnable);
        editor.putBoolean(AUTO_BITRATE_ENABLED, mIsAutoBitrateEnable);
        editor.putBoolean(DEBUG_MODE_ENABLED, mIsDebugModeEnable);
        editor.apply();
        finish();
    }

    private RadioGroup.OnCheckedChangeListener mOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (group.getCheckedRadioButtonId()) {
                case R.id.QUIC_protocol:
                    mIsQuicEnable = true;
                    break;
                case R.id.TCP_protocol:
                    mIsQuicEnable = false;
                    break;
                case R.id.codec_sw:
                    mIsSwCodecEnable = true;
                    break;
                case R.id.codec_hw:
                    mIsSwCodecEnable = false;
                    break;
                case R.id.video_quality_prebuilt:
                    mIsVideoQualityPrebuiltEnable = true;
                    mCustomVideoQualityLayout.setVisibility(View.GONE);
                    mVideoQualitySpinner.setVisibility(View.VISIBLE);
                    break;
                case R.id.video_quality_custom:
                    mIsVideoQualityPrebuiltEnable = false;
                    mCustomVideoQualityLayout.setVisibility(View.VISIBLE);
                    mVideoQualitySpinner.setVisibility(View.GONE);
                    break;
                case R.id.codec_size_prebuilt:
                    mIsCodecSizePrebuiltEnable = true;
                    mCustomCodecSizeLayout.setVisibility(View.GONE);
                    mCodecSizeSpinner.setVisibility(View.VISIBLE);
                    break;
                case R.id.codec_size_custom:
                    mIsCodecSizePrebuiltEnable = false;
                    mCustomCodecSizeLayout.setVisibility(View.VISIBLE);
                    mCodecSizeSpinner.setVisibility(View.GONE);
                    break;
                case R.id.quality_priority:
                    mIsQualityPriorityEnable = true;
                    break;
                case R.id.bitrate_priority:
                    mIsQualityPriorityEnable = false;
                    break;
                case R.id.enable_auto_bitrate:
                    mIsAutoBitrateEnable = true;
                    break;
                case R.id.disable_auto_bitrate:
                    mIsAutoBitrateEnable = false;
                    break;
                case R.id.enable_debug_mode:
                    mIsDebugModeEnable = true;
                    break;
                case R.id.disable_debug_mode:
                    mIsDebugModeEnable = false;
                    break;
            }
        }
    };

    private String getVersionDescription() {
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "unknown";
    }

    protected String getBuildTimeDescription() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(BuildConfig.BUILD_TIMESTAMP);
    }

    protected String getSdkVersion() {
        return com.qiniu.droid.rtplayer.BuildConfig.VERSION_NAME + "-" + com.qiniu.droid.rtplayer.BuildConfig.GIT_HASH;
    }

}
