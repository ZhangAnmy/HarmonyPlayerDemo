package com.huawei.radiolinedemo.slice;

import com.huawei.radiolinedemo.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;

import ohos.agp.animation.AnimatorProperty;
import ohos.agp.components.*;
import ohos.app.dispatcher.task.Revocable;
import ohos.bundle.ElementName;
import ohos.data.DatabaseHelper;
import ohos.data.preferences.Preferences;
import ohos.event.commonevent.*;
import ohos.media.audio.AudioManager;
import ohos.media.audio.AudioRemoteException;
import ohos.media.audio.AudioInterrupt;
import ohos.multimodalinput.event.MmiPoint;
import ohos.multimodalinput.event.TouchEvent;
import ohos.rpc.RemoteException;
import ohos.telephony.RadioInfoManager;
import ohos.wifi.WifiDevice;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

import java.net.HttpURLConnection;
import java.net.URL;

public class IjkAudioPlayAbilitySlice extends AbilitySlice {
    // Define log label
    private static final HiLogLabel TAG = new HiLogLabel(HiLog.LOG_APP, 0x00201, "IjkAudioPlayAbilitySlice");
    public static final String APP_PREFERENCE_NAME = "currentPlayState";

    MmiPoint downPoint;
    MmiPoint movePoint;
    private Float endPositionX ;
    private Float startPositionX;
    private Boolean moveRight = false;
    private Image imgPlayer;
    private Image imgVolumeLoud;
    private Image imgVolumeQuiet;
    private Image loadingBtn;
    private DirectionalLayout mainContent;
    private DirectionalLayout networkStatusContnt;
    private DirectionalLayout loadingImgContent;
    private ProgressBar perProgressBar;
    private Revocable revocable = null;
    private String currentStatus = null;
    private AudioManager audioManager = new AudioManager(this);
    private AudioInterrupt audioInterrupt = new AudioInterrupt();
    DatabaseHelper databaseHelper = new DatabaseHelper(this);
    RadioInfoManager radioInfoManager = RadioInfoManager.getInstance(this);
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_player);
        mainContent = (DirectionalLayout)findComponentById(ResourceTable.Id_main_content);
        imgPlayer = (Image)findComponentById(ResourceTable.Id_imgPlayer);
        imgVolumeLoud = (Image)findComponentById(ResourceTable.Id_imgVolumeLoud);
        loadingBtn = (Image)findComponentById(ResourceTable.Id_loading_img);
        imgVolumeQuiet = (Image)findComponentById(ResourceTable.Id_imgVolumeQuiet);
        loadingImgContent = (DirectionalLayout)findComponentById(ResourceTable.Id_loading_img_content);
        perProgressBar = (ProgressBar) findComponentById(ResourceTable.Id_VideoProgressBar);
        networkStatusContnt = (DirectionalLayout) findComponentById(ResourceTable.Id_network_status_content);
        mainContent.setTouchEventListener(new touchHandler());
        animatorPropertyHandle();
        queryNetworkStatus();
        initMusicVolumn();
        currentStatus = getString("currentStatus");
        HiLog.info(TAG, "currentStatus is---"+currentStatus);
        if(currentStatus != null && currentStatus.equals("pause")){
            imgPlayer.setPixelMap(ResourceTable.Media_pause);
        }
        imgPlayer.setClickedListener(listener->{
            controlMusicStatus();
        });
        imgVolumeLoud.setClickedListener(listener -> {
            setMusicVolumn(1);
        });
        imgVolumeQuiet.setClickedListener(listener->{
            setMusicVolumn(-1);
        });
    }

    @Override
    public void onActive(){
        super.onActive();
        getUITaskDispatcher().delayDispatch(() -> {
            mainContent.setVisibility(Component.VISIBLE);
        }, 1000);
    }

    private void queryNetworkStatus(){
        WifiDevice mWifiDevice = WifiDevice.getInstance(getContext());
        boolean isConnected = mWifiDevice.isConnected();
        HiLog.warn(TAG, "======="+isConnected);
        if (!isConnected) {
            checkNetworkStatusByHttpRequest();
        }
    }

    private void checkNetworkStatusByHttpRequest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://www.google.com");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(3000);
                    int code = connection.getResponseCode();
                    if (code == 200) {
                        HiLog.error(TAG, "===========connected=.");
                    } else {
                        HiLog.error(TAG, "===========disconnected=.");
                        presentForResult(new NoInternetSlice(), new Intent(),2);
                        terminate();
                    }
                } catch (Exception e) {
                    presentForResult(new NoInternetSlice(), new Intent(),2);
                    terminate();
                    e.printStackTrace();
                }
            }
        }).start();
    }

    class touchHandler implements Component.TouchEventListener{
        @Override
        public boolean onTouchEvent(Component component, TouchEvent touchEvent) {

            if(touchEvent.getAction() == TouchEvent.PRIMARY_POINT_DOWN){
                downPoint = touchEvent.getPointerScreenPosition(0);
                startPositionX = downPoint.getX();
            }
            if(touchEvent.getAction() == TouchEvent.POINT_MOVE){
                movePoint = touchEvent.getPointerScreenPosition(0);
                endPositionX = movePoint.getX();
                if(startPositionX<endPositionX){
                    moveRight = true;
                }else{
                    moveRight = false;
                }
                terminate();
            }
            return true;
        }
    }

    private void initMusicVolumn(){
        try {
            int maxVolumn = audioManager.getMaxVolume(AudioManager.AudioVolumeType.STREAM_MUSIC);
            perProgressBar.setMaxValue(maxVolumn);
            int minVolumn = audioManager.getMinVolume(AudioManager.AudioVolumeType.STREAM_MUSIC);
            perProgressBar.setMinValue(minVolumn);
            int defaultVolumn = audioManager.getVolume(AudioManager.AudioVolumeType.STREAM_MUSIC);
            perProgressBar.setProgressValue(defaultVolumn);
        } catch (AudioRemoteException e) {
            e.printStackTrace();
        }
    }
    private void setMusicVolumn(Integer index){
        perProgressBar.setVisibility(Component.VISIBLE);
        audioManager.changeVolumeBy(AudioManager.AudioVolumeType.STREAM_MUSIC,index);
        try {
            int defaultVolumn = audioManager.getVolume(AudioManager.AudioVolumeType.STREAM_MUSIC);
            perProgressBar.setProgressValue(defaultVolumn);
        } catch (AudioRemoteException e) {
            e.printStackTrace();
        }
        if(revocable != null){
            revocable.revoke();
        }
        revocable = getUITaskDispatcher().delayDispatch(() -> {
            perProgressBar.setVisibility(Component.INVISIBLE);
        }, 3000);
    }

    //Play or stop music
    private void controlMusicStatus(){
        currentStatus = getString("currentStatus");
        if(currentStatus != null && currentStatus.equals("pause")){ //Pause the radio
            putString("currentStatus","arrow");
            HiLog.info(TAG, "-----currentStatus----"+currentStatus);
            System.out.println("-----currentStatus----"+currentStatus);
            imgPlayer.setPixelMap(ResourceTable.Media_play);

            Intent intent = new Intent();
            //intent.setElement(new ElementName("","com.huawei.radiolinedemo", "com.huawei.radiolinedemo.PlayServiceAbility"));
            intent.setElement(new ElementName("","com.huawei.radiolinedemo", ".IjkAudioPlayerServiceAbility"));
            stopAbility(intent);
        } else {
            loadingImgContent.setVisibility(Component.VISIBLE);
            imgPlayer.setPixelMap(ResourceTable.Media_pause);
            System.out.println("-----currentStatus----"+currentStatus);
            putString("currentStatus","pause");
            imgPlayer.setClickable(false);
            imgVolumeLoud.setClickable(false);
            imgVolumeQuiet.setClickable(false);
            startPlayMusic();
        }
    }

    public void putString(String name, String string) {
        Preferences modifier = databaseHelper.getPreferences(APP_PREFERENCE_NAME);
        modifier.putString(name, string);
        modifier.flush();
    }
    public String getString(String name) {
        return databaseHelper.getPreferences(APP_PREFERENCE_NAME).getString(name, "");
    }
    private void startPlayMusic(){
        Intent intent = new Intent();
        //intent.setElement(new ElementName("","com.huawei.radiolinedemo", "com.huawei.radiolinedemo.PlayServiceAbility"));
        intent.setElement(new ElementName("","com.huawei.radiolinedemo", ".IjkAudioPlayerServiceAbility"));
        startAbility(intent);

//        Receive Notifications
        String event = "com.my.test";
        MatchingSkills matchingSkills = new MatchingSkills();
        matchingSkills.addEvent(event); // 自定义事件
        CommonEventSubscribeInfo subscribeInfo = new CommonEventSubscribeInfo(matchingSkills);
        MyCommonEventSubscriber subscriber = new MyCommonEventSubscriber(subscribeInfo);
        try {
            CommonEventManager.subscribeCommonEvent(subscriber);
        } catch (RemoteException e) {
            HiLog.error(TAG, "Exception occurred during subscribeCommonEvent invocation.");
        }
    }

    // animation for loading
    private void animatorPropertyHandle() {
        AnimatorProperty mAnimatorProperty= loadingBtn.createAnimatorProperty();
        mAnimatorProperty.rotate(360).setDuration(2000).setLoopedCount(1000);
        loadingBtn.setBindStateChangedListener(new Component.BindStateChangedListener() {
            @Override
            public void onComponentBoundToWindow(Component component) {
                if(mAnimatorProperty != null){
                    mAnimatorProperty.start();
                }
            }
            @Override
            public void onComponentUnboundFromWindow(Component component) {
            }
        });
    }
    class MyCommonEventSubscriber extends CommonEventSubscriber {
        MyCommonEventSubscriber(CommonEventSubscribeInfo info) {
            super(info);
        }
        @Override
        public void onReceiveEvent(CommonEventData commonEventData) {
            loadingImgContent.setVisibility(Component.INVISIBLE);
            imgPlayer.setClickable(true);
            imgVolumeLoud.setClickable(true);
            imgVolumeQuiet.setClickable(true);
        }
    }
    @Override
    public void onStop(){
        super.onStop();
    }
}
