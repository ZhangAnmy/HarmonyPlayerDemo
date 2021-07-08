package com.huawei.radiolinedemo.slice;

import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.app.Context;
import ohos.global.resource.BaseFileDescriptor;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.audio.AudioRenderer;
import ohos.media.audio.AudioRendererInfo;
import ohos.media.audio.AudioStreamInfo;
import ohos.media.common.Source;
import ohos.media.player.Player;
import ohos.utils.net.Uri;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;

import static ohos.media.audio.AudioRenderer.PlayMode;

public class PlayUtil {
    private static final HiLogLabel TAG = new HiLogLabel(HiLog.LOG_APP, 0x00201, "PlayUtil");

    private static Player mPlayer;
    private static Context context;
    private boolean isInitialized = false;

    private static final int ONE_SECONDS_MS = 1000;
    private static final int ONE_MINS_MINUTES = 60;
    private static final String TIME_FORMAT = "%02d";

    private PlayUtil() {
    }

    private static PlayUtil instance;

    public synchronized static PlayUtil getInstance() {
        if (instance == null) {
            instance = new PlayUtil();
        }
        return instance;
    }

    public void setContext(Context mcontext) {
        if (instance == null) {
            instance = new PlayUtil();
        }
        this.context = mcontext;
    }

    /*public void startService(){
        Intent intent = new Intent();
        Operation operation = new Intent.OperationBuilder()
                .withDeviceId("")
                .withBundleName("com.huawei.radiolinedemo")
                .withAbilityName("com.huawei.radiolinedemo.PlayServiceAbility")
                .build();
        intent.setOperation(operation);
        intent.setParam("filePath","/sdcard/haps/");
        context.startAbility(intent,1);
    }*/

    public static Player getPlayer(){
        return mPlayer;
    }

    public String getPath(){
        HiLog.info(TAG, "path: " + context.getDataDir().getPath());

        HiLog.info(TAG, "path: " + context.getFilesDir().getPath());
        HiLog.info(TAG, "path: " + context.getCacheDir().getPath());

        HiLog.info(TAG, "path: " + context.getCodeCacheDir().getPath());
        HiLog.info(TAG, "path: " + context.getExternalCacheDir().getPath());
        return  context.getFilesDir().getPath();
    }

    public void play(String uri, int a) {
        HiLog.info(TAG, "Play uri");
        if (mPlayer == null) {
            mPlayer = new Player(context);
            //mPlayer.setPlayerCallback(iPlayerCallback);
            Source source = new Source(uri);
            mPlayer.setSource(source);
            mPlayer.prepare();
            mPlayer.play();
            HiLog.info(TAG, "Play success mPlayer == null");
        } else {
            if(mPlayer.isNowPlaying()) {
                mPlayer.pause();
            } else {
                mPlayer.play();
                HiLog.info(TAG, "Play success play");
            }
            HiLog.info(TAG, "Play success else");
        }
        HiLog.info(TAG, "Play success end");
    }

    public void playraw(String filePath) {
        HiLog.info(TAG, "play");
        if (mPlayer != null) {
            HiLog.info(TAG, "Stop and release old player");
            if (isPlaying()) {
                mPlayer.stop();
            }
            mPlayer.release();
        }
        mPlayer = new Player(context);

        BaseFileDescriptor assetfd = null;
        try {
            assetfd = this.context.getResourceManager().getRawFileEntry(filePath).openRawFileDescriptor();
        } catch (IOException e) {
            HiLog.info(TAG, "Audio resource is unavailable: " + e.toString());
            return;
        }
        if (!mPlayer.setSource(assetfd)) {
            HiLog.info(TAG, "Set audio source failed");
            return;
        }

        if (!mPlayer.prepare()) {
            HiLog.info(TAG, "Prepare audio file failed");
            return;
        }
        if (mPlayer.play()) {
            HiLog.info(TAG, "Play success");
        } else {
            HiLog.info(TAG, "Play failed");
        }
    }

    public void play(String filePath) {
        HiLog.info(TAG, "play");
        if (mPlayer != null) {
            HiLog.info(TAG, "Stop and release old player");
            if (isPlaying()) {
                mPlayer.stop();
            }
            mPlayer.release();
        }
        mPlayer = new Player(context);

        FileDescriptor assetfd = null;
        try {
            File f = new File(filePath);
            FileInputStream soundInputStream = new FileInputStream(f);
            HiLog.info(TAG, "Audio resource is unavailable FileInputStream");
            assetfd = soundInputStream.getFD();
        } catch (IOException e) {
            HiLog.info(TAG, "Audio resource is unavailable: " + e.toString());
            e.printStackTrace();
            return;
        }
        if (!mPlayer.setSource(new Source(assetfd))) {
            HiLog.info(TAG, "Set audio source failed");
            return;
        }

        if (!mPlayer.prepare()) {
            HiLog.info(TAG, "Prepare audio file failed");
            return;
        }
        if (mPlayer.play()) {
            HiLog.info(TAG, "Play success");
        } else {
            HiLog.info(TAG, "Play failed");
        }
    }

    public void pause() {
        HiLog.info(TAG, "pause");
        if (mPlayer != null) {
            if (isPlaying()) {
                mPlayer.pause();
            }
        }
    }

    public void rePlay() {
        if (mPlayer != null) {
            if (!isPlaying()) {
                mPlayer.play();
            }
        }
        HiLog.info(TAG, "rePlay");
    }

    public void stop() {
        if(mPlayer != null) {
            if (isPlaying()) {
                mPlayer.stop();
            }
            mPlayer.release();
            mPlayer = null;
        }
        HiLog.info(TAG, "stop");
    }

    public void setSource(String uri) {
        mPlayer.setSource(new Source(uri));
    }

    public boolean isPlaying() {
        if (mPlayer == null) {
            return false;
        }
        return mPlayer.isNowPlaying();
    }

    public int getDuration() {
        if (mPlayer == null) {
            return 0;
        }
        return mPlayer.getDuration();
    }

    public int getCurrentPosition() {
        if (mPlayer == null) {
            return 0;
        }
        return mPlayer.getCurrentTime();
    }
    public float getSpeed() {
        if (mPlayer == null) {
            return 0;
        }
        return mPlayer.getPlaybackSpeed();
    }

    public void release() {
        if (isPlaying()) {
            mPlayer.stop();
        }
        mPlayer.release();
        mPlayer = null;
    }

    public String getDurationText() {
        return msToString(getDuration());
    }

    public String getCurrentText() {
        return msToString(getCurrentPosition());
    }

    private String msToString (int ms) {
        StringBuilder sb = new StringBuilder(16);
        int seconds = ms / ONE_SECONDS_MS;
        int minutes = seconds / ONE_MINS_MINUTES;
        if (minutes > ONE_MINS_MINUTES) {
            sb.append(String.format(Locale.ENGLISH, TIME_FORMAT, minutes / ONE_MINS_MINUTES));
            sb.append(":");
            sb.append(String.format(Locale.ENGLISH, TIME_FORMAT, minutes % ONE_MINS_MINUTES));
            sb.append(":");
        } else {
            sb.append("00:");
            sb.append(String.format(Locale.ENGLISH, TIME_FORMAT, minutes));
            sb.append(":");
        }

        if (seconds > minutes * ONE_MINS_MINUTES) {
            sb.append(String.format(Locale.ENGLISH, TIME_FORMAT, seconds - minutes * ONE_MINS_MINUTES));
        } else {
            sb.append("00");
        }
        return sb.toString();
    }
}
