package com.qiniu.droid.rtplayer.demo.utils;

import com.qiniu.pili.droid.streaming.StreamingProfile;

public class StreamingSettings {

    public static final String QUIC_ENABLE = "QuicEnable";
    public static final String SW_ENABLE = "SwEnable";
    public static final String VIDEO_QUALITY_PREBUILT_ENABLE = "VideoQualityPrebuiltEnable";
    public static final String CODEC_SIZE_PREBUILT_ENABLE = "CodecSizePrebuiltEnable";
    public static final String QUALITY_PRIORITY_ENABLE = "QualityPriorityEnable";
    public static final String AUTO_BITRATE_ENABLED = "autoBitrateEnabled";
    public static final String DEBUG_MODE_ENABLED = "debugModeEnabled";
    public static final String PREBUILT_VIDEO_QUALITY_POS = "prebuiltVideoQualityPos";
    public static final String PREBUILT_CODEC_SIZE_POS = "prebuiltCodecSizePos";
    public static final String TARGET_FPS = "targetFps";
    public static final String TARGET_BITRATE = "targetBitrate";
    public static final String TARGET_GOP = "targetGop";
    public static final String TARGET_WIDTH = "targetWidth";
    public static final String TARGET_HEIGHT = "targetHeight";
    public static final String STREAMING_ROOMNAME = "streamingName";
    public static final String PLAYING_ROOMNAME = "playingName";
    public static final String DEFAULT_CACHE = "defaultCache";
    public static final String MAX_CACHE = "maxCache";

    public static final String[] VIDEO_QUALITY_ARRAY = {
            "LOW1(FPS:12,Bitrate:150kps)",
            "LOW2(FPS:15,Bitrate:264kps)",
            "LOW3(FPS:15,Bitrate:350kps)",
            "MEDIUM1(FPS:30,Bitrate:512kps)",
            "MEDIUM2(FPS:30,Bitrate:800kps)",
            "MEDIUM3(FPS:30,Bitrate:1000kps)",
            "HIGH1(FPS:30,Bitrate:1200kps)",
            "HIGH(FPS:30,Bitrate:2000kps))"
    };

    public static final String[] CODEC_SIZE_ARRAY = {
            "240p【424x240(16:9)】",
            "480p【848x480(16:9)】",
            "544p【960x544(16:9)】",
            "720p【1280x720(16:9)】",
            "1088p【1920x1088(16:9)】",
    };

    public static final int[][] CODEC_SIZE = {
            {240, 424},
            {480, 848},
            {544, 960},
            {720, 1280},
            {1088, 1920}
    };

    public static final int[] PREBUILT_VIDEO_QUALITY = {
        StreamingProfile.VIDEO_QUALITY_LOW1,
        StreamingProfile.VIDEO_QUALITY_LOW2,
        StreamingProfile.VIDEO_QUALITY_LOW3,
        StreamingProfile.VIDEO_QUALITY_MEDIUM1,
        StreamingProfile.VIDEO_QUALITY_MEDIUM2,
        StreamingProfile.VIDEO_QUALITY_MEDIUM3,
        StreamingProfile.VIDEO_QUALITY_HIGH1,
        StreamingProfile.VIDEO_QUALITY_HIGH2,
        StreamingProfile.VIDEO_QUALITY_HIGH3
    };

    public static final int[] PREBUILT_CODEC_SIZE = {
            StreamingProfile.VIDEO_ENCODING_HEIGHT_240,
            StreamingProfile.VIDEO_ENCODING_HEIGHT_480,
            StreamingProfile.VIDEO_ENCODING_HEIGHT_544,
            StreamingProfile.VIDEO_ENCODING_HEIGHT_720,
            StreamingProfile.VIDEO_ENCODING_HEIGHT_1088,
    };

    public static final StreamingProfile.YuvFilterMode[] YUV_FILTER_MODE_MAPPING = {
            StreamingProfile.YuvFilterMode.None,
            StreamingProfile.YuvFilterMode.Linear,
            StreamingProfile.YuvFilterMode.Bilinear,
            StreamingProfile.YuvFilterMode.Box
    };
}
