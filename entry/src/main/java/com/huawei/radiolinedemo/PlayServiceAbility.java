package com.huawei.radiolinedemo;

import com.huawei.radiolinedemo.utils.ImageTools;
import com.huawei.radiolinedemo.utils.LogUtils;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.aafwk.content.Operation;
import ohos.agp.components.ComponentProvider;
import ohos.event.commonevent.CommonEventData;
import ohos.event.commonevent.CommonEventManager;
import ohos.event.intentagent.IntentAgent;
import ohos.event.intentagent.IntentAgentConstant;
import ohos.event.intentagent.IntentAgentHelper;
import ohos.event.intentagent.IntentAgentInfo;
import ohos.event.notification.NotificationHelper;
import ohos.event.notification.NotificationRequest;
import ohos.event.notification.NotificationSlot;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.audio.AudioInterrupt;
import ohos.media.audio.AudioManager;
import ohos.media.common.Source;
import ohos.media.player.Player;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

import java.util.ArrayList;
import java.util.List;

public class PlayServiceAbility extends Ability {
    private static final HiLogLabel TAG = new HiLogLabel(HiLog.LOG_APP, 0x00201, "PlayService");
    private Player mPlayer;
    //String uri = "https://16533.mc.tritondigital.com/OMNY_CONANOBRIENNEEDSAFRIEND_PODCAST_P/media-session/bbb66f89-e01e-4fd2-bbc4-a20e7f9fec84/d/clips/aaea4e69-af51-495e-afc9-a9760146922b/0a686f81-0eeb-455b-98be-ab0d00055d5e/83272972-190e-45dd-9e82-ad42015e06aa/audio/direct/t1623297669/Energeeza.mp3?t=1623297669&in_playlist=1fab2b0b-a7f0-4d71-bf6d-ab0d00055d6c&utm_source=Podcast&_=1449481293";
    String uri = "https://stream.europe1.fr/europe1.mp3?aw_0_1st.playerid=lgrdrnwsRadioline&token=3f811fbf6aa6e073e195bbd15d5ca08a%2Fc2673535";
    Source source = new Source(uri);

    AudioInterrupt audioInterrupt = new AudioInterrupt();

    private NotificationSlot slot;
    private ComponentProvider componentProvider;
    private NotificationRequest notificationRequest;

    public void onStart(Intent intent) {
        super.onStart(intent);
        defineNotificationSlot();
        //publishNotification();
        publishNotification2();

        audioInterrupt.setInterruptListener(new AudioInterrupt.InterruptListener() {
            @Override
            public void onInterrupt(int i, int i1) {
                if (i == AudioInterrupt.INTERRUPT_TYPE_BEGIN) {
                    mPlayer.pause();
                } else if (i == AudioInterrupt.INTERRUPT_TYPE_END) {
                    mPlayer.play();
                } else {
                    LogUtils.warn(TAG,"interrupt error");
                }
            }
        });
    }

    private void defineNotificationSlot() {
        slot = new NotificationSlot("Ongoing_Overview", "Music_default", NotificationSlot.LEVEL_DEFAULT);
        slot.setDescription("NotificationSlotDescription");
        try {
            NotificationHelper.addNotificationSlot(slot);
        } catch (RemoteException ex) {
            System.out.println( "Add ongoing card slot exception");
        }
    }

    private void publishNotification() {
        int notificationId = 1005;
        notificationRequest = new NotificationRequest(this, notificationId);
        notificationRequest.setSlotId(slot.getId());

        String textContent = "AnmyPlayer";
        NotificationRequest.NotificationNormalContent content = new NotificationRequest.NotificationNormalContent();
        content.setText(textContent);
        NotificationRequest.NotificationContent notificationContent = new NotificationRequest.NotificationContent(content);
        notificationRequest.setContent(notificationContent);
        keepBackgroundRunning(notificationId, notificationRequest);

        IntentParams params = new IntentParams();
        params.setParam("MinusOneExtraTag","Ongoing_Overview");
        notificationRequest.setAdditionalData(params);

        componentProvider = new ComponentProvider(ResourceTable.Layout_test, this);
        componentProvider.setText(ResourceTable.Id_ongoing_test,"Anmy music");
        //componentProvider.setImagePixelMap(ResourceTable.Id_imgPause,ImageTools.getPixelMap(this, ResourceTable.Media_pause));
        //componentProvider.setString(ResourceTable.Id_ongoing_test, "Ongoing setText", "Ongoing TextContent"); // 设置布局中的文本内容
        notificationRequest.setCustomView(componentProvider);

        // 获取IntentAgent实例
        IntentAgent intentAgent = createIntentAgent(MainAbility.class.getName(),
                IntentAgentConstant.OperationType.START_ABILITY);
        componentProvider.setIntentAgent(ResourceTable.Id_ongoing_title,intentAgent);  // 设置跳转事件信息
        //设置通知可以触发的事件
        notificationRequest.setIntentAgent(intentAgent);// 设置通知可以触发的事件

        try {
            //NotificationHelper.publishNotification("Ongoing_Overview",notificationRequest);
            NotificationHelper.publishNotification(notificationRequest);
            System.out.println("---Ongoing_Overview publish notification---");
        } catch (RemoteException ex) {
            HiLog.error(TAG, "%{public}s", "publishNotification remoteException.");
        }
        keepBackgroundRunning(notificationId, notificationRequest);
    }

    private void publishNotification2() {
        int notificationId = 1005;
        NotificationRequest request = new NotificationRequest(this, notificationId);
        request.setSlotId(slot.getId());
        NotificationRequest.NotificationNormalContent content = new NotificationRequest.NotificationNormalContent();
        NotificationRequest.NotificationContent notificationContent = new NotificationRequest.NotificationContent(content);
        request.setContent(notificationContent);
        keepBackgroundRunning(notificationId, request);

        IntentParams params = new IntentParams();
        params.setParam("MinusOneExtraTag","Ongoing_Overview"); // MinusOneExtraTag必须准确无误
        request.setAdditionalData(params);

        componentProvider = new ComponentProvider(ResourceTable.Layout_test, this);
        componentProvider.setText(ResourceTable.Id_ongoing_title, "Local Music");
        componentProvider.setImagePixelMap(ResourceTable.Id_ongoing_play, ImageTools.getPixelMap(this, ResourceTable.Media_pause));
        request.setCustomView(componentProvider);

        Intent intent = new Intent();
        Operation operation = new Intent.OperationBuilder()
                .withDeviceId("")
                .withBundleName("com.huawei.radiolinedemo")
                .withAbilityName("com.huawei.radiolinedemo.MainAbility")
                .build();
        intent.setOperation(operation);
        List<Intent> intentList = new ArrayList<>();
        intentList.add(intent);
        IntentAgentInfo paramsInfo = new IntentAgentInfo(request.getNotificationId(),
                IntentAgentConstant.OperationType.START_ABILITY, IntentAgentConstant.Flags.UPDATE_PRESENT_FLAG, intentList, null);
        IntentAgent intentAgent = IntentAgentHelper.getIntentAgent(getContext(), paramsInfo);
        componentProvider.setIntentAgent(ResourceTable.Id_ongoing_title, intentAgent);
        try {
            NotificationHelper.publishNotification(request);
        } catch (RemoteException exception) {
            System.out.println("A remote exception occurred when publish ongoing card notification.");
        }
        keepBackgroundRunning(notificationId, request);
    }

    private IntentAgent createIntentAgent(String ability, IntentAgentConstant.OperationType operationType) {
        Intent intent = new Intent();
        Operation operation = new Intent.OperationBuilder()
                .withDeviceId("")
                .withBundleName("com.huawei.radiolinedemo")
                .withAbilityName("com.huawei.radiolinedemo.MainAbility")
                .build();
        intent.setOperation(operation);
        List<Intent> intentList = new ArrayList<>();
        intentList.add(intent);
        IntentAgentInfo agentInfo = new IntentAgentInfo(notificationRequest.getNotificationId(), operationType,
                IntentAgentConstant.Flags.UPDATE_PRESENT_FLAG, intentList, null);
        return IntentAgentHelper.getIntentAgent(getContext(), agentInfo);
    }

    @Override
    public void onBackground() {
        super.onBackground();
        HiLog.warn(TAG, "--------onBackground-----");
    }

    //Call it when play
    public void toggle() {
        if (mPlayer.isNowPlaying()) {
            //Audio out of focus
            VolumeUtils.getAudioManager().deactivateAudioInterrupt(audioInterrupt);
            mPlayer.pause();
        } else {
            //Audio gets focus
            VolumeUtils.getAudioManager().activateAudioInterrupt(audioInterrupt);
            mPlayer.play();
        }
    }

    public static class VolumeUtils {
        private static final int MAX = 5;
        private static final int MIN = 0;
        private static AudioManager mAudioManager = null;

        public static AudioManager getAudioManager() {
            if (mAudioManager == null) {
                mAudioManager = new AudioManager("com.huawei.radiolinedemo");
            }
            return mAudioManager;
        }
    }

    @Override
    protected void onCommand(Intent intent, boolean restart, int startId) {
        if (mPlayer != null) {
            HiLog.info(TAG, "---Stop and release old player");
            if (mPlayer.isNowPlaying()) {
                mPlayer.stop();
            }
            mPlayer.release();
        }

        mPlayer = new Player(this);
        mPlayer.setPlayerCallback(iPlayerCallback);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!mPlayer.setSource(source)) {
                    HiLog.info(TAG, "Set audio source failed---");
                }
                if (!mPlayer.prepare()) {
                    HiLog.info(TAG, "Prepare audio file failed---");
                }
                /*if (mPlayer.play()) {
                    HiLog.info(TAG, "Play success---");
                } else {
                    HiLog.info(TAG, "Play failed---");
                }*/
                toggle();
            }
        }).start();
    }

    @Override
    protected IRemoteObject onConnect(Intent intent) {
        HiLog.warn(TAG, "----onConnect-----");
        return super.onConnect(intent);
    }

    @Override
    protected void onDisconnect(Intent intent) {
        HiLog.warn(TAG, "----onDisconnect-----");
        if (mPlayer != null) {
            if (mPlayer.isNowPlaying()) {
                mPlayer.pause();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        HiLog.warn(TAG, "----onStop-----");
        if (mPlayer != null) {
            if (mPlayer.isNowPlaying()) {
                mPlayer.pause();
            }
            mPlayer.release();
        }
    }

    Player.IPlayerCallback iPlayerCallback = new Player.IPlayerCallback() {
        @Override
        public void onPrepared() {
            HiLog.warn(TAG, "==============Complete the preparation");
        }

        @Override
        public void onMessage(int i, int i1) {
            HiLog.warn(TAG, "==============Receive Messages");
        }

        @Override
        public void onError(int i, int i1) {
            HiLog.warn(TAG, "=======playback=======throw error" );

        }

        @Override
        public void onResolutionChanged(int i, int i1) {
            HiLog.warn(TAG, "==============onResolutionChanged" );
        }

        @Override
        public void onPlayBackComplete() {
            HiLog.warn(TAG, "==============onPlayBackComplete" );
        }

        @Override
        public void onRewindToComplete() {
            HiLog.warn(TAG, "==============onRewindToComplete" );
        }

        @Override
        public void onBufferingChange(int i) {
            HiLog.warn(TAG, "==============onBufferingChange" );
        }

        @Override
        public void onNewTimedMetaData(Player.MediaTimedMetaData mediaTimedMetaData) {

        }

        @Override
        public void onMediaTimeIncontinuity(Player.MediaTimeInfo mediaTimeInfo) {
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
}
