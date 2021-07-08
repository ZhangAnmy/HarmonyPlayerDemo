package com.huawei.radiolinedemo;

import com.huawei.radiolinedemo.slice.NoInternetSlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.rpc.IRemoteObject;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

import ohos.event.commonevent.CommonEventData;
import ohos.event.commonevent.CommonEventManager;
import ohos.event.notification.NotificationRequest;
import ohos.powermanager.PowerManager;

import ohos.media.audio.AudioInterrupt;
import ohos.media.audio.AudioManager;
import ohos.media.audio.AudioRemoteException;
import ohos.rpc.RemoteException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import java.io.IOException;

public class IjkAudioPlayerServiceAbility extends Ability {
    private static final HiLogLabel TAG = new HiLogLabel(3, 0xD001100, "IjkAudioPlayerServiceAbility");

    //private Uri mUri = "https://16533.mc.tritondigital.com/OMNY_CONANOBRIENNEEDSAFRIEND_PODCAST_P/media-session/bbb66f89-e01e-4fd2-bbc4-a20e7f9fec84/d/clips/aaea4e69-af51-495e-afc9-a9760146922b/0a686f81-0eeb-455b-98be-ab0d00055d5e/83272972-190e-45dd-9e82-ad42015e06aa/audio/direct/t1623297669/Energeeza.mp3?t=1623297669&in_playlist=1fab2b0b-a7f0-4d71-bf6d-ab0d00055d6c&utm_source=Podcast&_=1449481293";
    //private Uri mUri = "https://stream.europe1.fr/europe1.mp3?aw_0_1st.playerid=lgrdrnwsRadioline&token=3f811fbf6aa6e073e195bbd15d5ca08a%2Fc2673535";
    private String mUri = "http://icecast.skyrock.net/s/natio_mp3_128k?tvr_name=radioline2018&tvr_section1=128mp3";
    private IMediaPlayer mMediaPlayer = null;

    private int mCurrentBufferPercentage;
    private IMediaPlayer.OnCompletionListener mOnCompletionListener;
    private IMediaPlayer.OnPreparedListener mOnPreparedListener;
    private IMediaPlayer.OnErrorListener mOnErrorListener;
    private IMediaPlayer.OnInfoListener mOnInfoListener;

    // all possible internal states
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;

    // mCurrentState is a VideoView object's current state.
    // mTargetState is the state that a method caller intends to reach.
    // For instance, regardless the VideoView object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private int mCurrentState = STATE_IDLE;
    private int mTargetState = STATE_IDLE;

    private long mPrepareStartTime = 0;

    private AudioManager audioManager;
    private AudioInterrupt audioInterrupt;

    @Override
    public void onStart(Intent intent) {
        HiLog.info (TAG, "IjkAudioPlayerServiceAbility::onStart");
        super.onStart(intent);
        NotificationRequest request = new NotificationRequest(1005); //notificationId 1005
        NotificationRequest.NotificationNormalContent content = new NotificationRequest.NotificationNormalContent();
        NotificationRequest.NotificationContent notificationContent = new NotificationRequest.NotificationContent(content);
        request.setContent(notificationContent);
        keepBackgroundRunning(1006,request);
        PowerManager powerManager = new PowerManager();
        PowerManager.RunningLock runningLock = powerManager.createRunningLock("test",PowerManager.RunningLockType.BACKGROUND);
        runningLock.lock(5000000);
    }

    @Override
    public void onBackground() {
        super.onBackground();
        HiLog.info(TAG, "IjkAudioPlayerServiceAbility::onBackground");
    }

    @Override
    public void onStop() {
        super.onStop();
        HiLog.info(TAG, "IjkAudioPlayerServiceAbility::onStop");
        stopPlayback();
    }

    @Override
    public void onCommand(Intent intent, boolean restart, int startId) {
        openAudio();
    }

    @Override
    public IRemoteObject onConnect(Intent intent) {
        return null;
    }

    @Override
    public void onDisconnect(Intent intent) {
    }

/*    public IMediaPlayer getMediaPlayer() {
        if (mMediaPlayer != null) {
            return mMediaPlayer;
        }
        return null;
    }*/

    private void openAudio() {
        if (mUri == null) {
            return;
        }
        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        release(false);

        audioManager = new AudioManager(getContext());
        try {
            audioManager.setVolume(
                    AudioManager.AudioVolumeType.STREAM_MUSIC,
                    audioManager.getVolume(AudioManager.AudioVolumeType.STREAM_MUSIC));
        } catch (AudioRemoteException e) {
            HiLog.error(TAG, e.getMessage());
        }
        audioInterrupt = new AudioInterrupt();
        audioManager.activateAudioInterrupt(audioInterrupt);

        try {
            mMediaPlayer = createPlayer(0);
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mCurrentBufferPercentage = 0;
            mMediaPlayer.setDataSource(mUri);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mPrepareStartTime = System.currentTimeMillis();
            mMediaPlayer.prepareAsync();
            mCurrentState = STATE_PREPARING;
            HiLog.error(TAG, "---openAudio CurrentState" + mCurrentState);
        } catch (IOException | IllegalArgumentException ex) {
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
        }
    }

    public void pause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mCurrentState = STATE_PAUSED;
        }
        mTargetState = STATE_PAUSED;
    }

    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
            // 释放音频焦点
            if (audioManager != null && audioInterrupt != null) {
                audioManager.deactivateAudioInterrupt(audioInterrupt);
            }
        }
    }

    private IMediaPlayer.OnPreparedListener mPreparedListener =
            new IMediaPlayer.OnPreparedListener() {
                public void onPrepared(IMediaPlayer mp) {
                    long mPrepareEndTime = System.currentTimeMillis();
                    mCurrentState = STATE_PREPARED;
                    // Get the capabilities of the player for this stream
                    // REMOVED: Metadata
                    if (mOnPreparedListener != null) {
                        mOnPreparedListener.onPrepared(mMediaPlayer);
                    }

                    try {
                        Intent intent = new Intent();
                        Operation operation = new Intent.OperationBuilder().withAction("com.my.test").build();
                        intent.setOperation(operation);
                        CommonEventData eventData = new CommonEventData(intent);
                        CommonEventManager.publishCommonEvent(eventData);
                    } catch (RemoteException e) {
                        HiLog.error(TAG,"RomoteException: "+e);
                    }

                }
            };

    private IMediaPlayer.OnCompletionListener mCompletionListener =
            new IMediaPlayer.OnCompletionListener() {
                public void onCompletion(IMediaPlayer mp) {
                    mCurrentState = STATE_PLAYBACK_COMPLETED;
                    mTargetState = STATE_PLAYBACK_COMPLETED;
                    if (mOnCompletionListener != null) {
                        mOnCompletionListener.onCompletion(mMediaPlayer);
                    }
                }
            };

    private IMediaPlayer.OnInfoListener mInfoListener =
            new IMediaPlayer.OnInfoListener() {
                public boolean onInfo(IMediaPlayer mp, int arg1, int arg2) {
                    if (mOnInfoListener != null) {
                        mOnInfoListener.onInfo(mp, arg1, arg2);
                    }
                    switch (arg1) {
                        case IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                            HiLog.debug(TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                            HiLog.debug(TAG, "MEDIA_INFO_VIDEO_RENDERING_START:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                            HiLog.debug(TAG, "MEDIA_INFO_BUFFERING_START:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                            HiLog.debug(TAG, "MEDIA_INFO_BUFFERING_END:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:
                            HiLog.debug(TAG, "MEDIA_INFO_NETWORK_BANDWIDTH: " + arg2);
                            break;
                        case IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                            HiLog.debug(TAG, "MEDIA_INFO_BAD_INTERLEAVING:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                            HiLog.debug(TAG, "MEDIA_INFO_NOT_SEEKABLE:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                            HiLog.debug(TAG, "MEDIA_INFO_METADATA_UPDATE:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                            HiLog.debug(TAG, "MEDIA_INFO_UNSUPPORTED_SUBTITLE:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                            HiLog.debug(TAG, "MEDIA_INFO_SUBTITLE_TIMED_OUT:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                            //mVideoRotationDegree = arg2;
                            HiLog.debug(TAG, "MEDIA_INFO_VIDEO_ROTATION_CHANGED: " + arg2);
                            break;
                        case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                            HiLog.debug(TAG, "MEDIA_INFO_AUDIO_RENDERING_START:");
                            break;
                    }
                    return true;
                }
            };

    private IMediaPlayer.OnErrorListener mErrorListener =
            new IMediaPlayer.OnErrorListener() {
                public boolean onError(IMediaPlayer mp, int framework_err, int impl_err) {
                    HiLog.error(TAG, " OnErrorListener Error: " + framework_err + "," + impl_err);
                    mCurrentState = STATE_ERROR;
                    mTargetState = STATE_ERROR;

                    /* If an error handler has been supplied, use it and finish. */
                    if (mOnErrorListener != null) {
                        if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                            return true;
                        }
                    }
                    return true;
                }
            };

    private IMediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
            new IMediaPlayer.OnBufferingUpdateListener() {
                public void onBufferingUpdate(IMediaPlayer mp, int percent) {
                    HiLog.info(TAG, " onBufferingUpdate " + percent);
                    mCurrentBufferPercentage = percent;
                }
            };

    /**
     * Register a callback to be invoked when the media file
     * is loaded and ready to go.
     *
     * @param l The callback that will be run
     */
    public void setOnPreparedListener(IMediaPlayer.OnPreparedListener l) {
        mOnPreparedListener = l;
    }

    /**
     * Register a callback to be invoked when the end of a media file
     * has been reached during playback.
     *
     * @param l The callback that will be run
     */
    public void setOnCompletionListener(IMediaPlayer.OnCompletionListener l) {
        mOnCompletionListener = l;
    }

    /**
     * Register a callback to be invoked when an error occurs
     * during playback or setup.  If no listener is specified,
     * or if the listener returned false, VideoView will inform
     * the user of any errors.
     *
     * @param l The callback that will be run
     */
    public void setOnErrorListener(IMediaPlayer.OnErrorListener l) {
        mOnErrorListener = l;
    }

    /**
     * Register a callback to be invoked when an informational event
     * occurs during playback or setup.
     *
     * @param l The callback that will be run
     */
    public void setOnInfoListener(IMediaPlayer.OnInfoListener l) {
        mOnInfoListener = l;
    }

    /**
     * release the media player in any state
     *
     * @param cleartargetstate true: clear state
     */
    public void release(boolean cleartargetstate) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            if (cleartargetstate) {
                mTargetState = STATE_IDLE;
            }
            // 释放音频焦点
            if (audioManager != null && audioInterrupt != null) {
                audioManager.deactivateAudioInterrupt(audioInterrupt);
            }
        }
    }

    public IMediaPlayer createPlayer(int playerType) {
        IMediaPlayer mediaPlayer = null;
        switch (playerType) {
            default: {
                IjkMediaPlayer ijkMediaPlayer = null;
                if (mUri != null) {
                    ijkMediaPlayer = new IjkMediaPlayer();
                    /**
                     * ohos中暂时没法判断是debug还是release,后续优化
                     * 请调用IjkMediaPlayer.native_setLogLevel以打印ijk相关的native日志，正式版本请关闭，默认是关闭的
                     * */
                    IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG);

                    // 是否启用硬解码，目前ohos平台的硬解码未适配，不支持硬解，默认0是软解
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);
                    // 是否启用opensles用于音频解析，默认用的ohos的AudioRenderer
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
                    // 跳帧处理,当CPU处理较慢时，进行跳帧处理，保证播放流畅，画面和声音同步
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1L);
                    // 设置是否开启环路过滤: 0开启，画面质量高，解码开销大，48关闭，画面质量差点，解码开销小
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
                    // 设置播放前的探测时间 1,达到首屏秒开效果，有一定的效果
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 1);

                    // ============================以下ffmepg相关的Option设置项请自行按需调节参数值================

                    //                    // 自定义缓冲大小,单位kb
                    //                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER,
                    // "max-buffer-size", 100 * 1024);
                    //                    // 最大fps
                    //                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames",
                    // 100);
                    //                    // 设置播放前的最大探测时间
                    //                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT,
                    // "analyzemaxduration", 100L);
                    //                    // 播放前的探测Size， 改小一点会出画面更快, 但是值过小会导致一些视频播放时有画面但没有声音,需要注意
                    //                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize",
                    // 10240 * 10);
                    //                    // 每处理一个packet之后刷新io上下文
                    //                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets",
                    // 1L);
                    //                    // 是否开启预缓冲，一般直播项目会开启，达到秒开的效果，不过会带来播放丢帧卡顿的问题
                    //                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER,
                    // "packet-buffering", 0L);
                    //
                    //                    // SeekTo设置优化:seek只支持关键帧,当压缩的视频文件关键帧比较少时，在执行SeekTo的时候，存在会跳回到拖动前的位置的问题
                    //                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER,
                    // "enable-accurate-seek", 1);
                    //
                    //                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER,
                    // "start-on-prepared", 0);
                    //                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT,
                    // "http-detect-range-support", 0);

                }
                mediaPlayer = ijkMediaPlayer;
            }
            break;
        }
        return mediaPlayer;
    }
}