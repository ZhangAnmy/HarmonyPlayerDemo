package com.huawei.radiolinedemo.slice;

import com.huawei.radiolinedemo.MainAbility;
import com.huawei.radiolinedemo.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.aafwk.content.Operation;
import ohos.agp.components.*;
import ohos.bundle.ElementName;
import ohos.event.commonevent.*;
import ohos.event.intentagent.IntentAgent;
import ohos.event.intentagent.IntentAgentConstant;
import ohos.event.intentagent.IntentAgentHelper;
import ohos.event.intentagent.IntentAgentInfo;
import ohos.event.notification.NotificationHelper;
import ohos.event.notification.NotificationRequest;
import ohos.event.notification.NotificationSlot;
import ohos.event.notification.NotificationUserInput;
import ohos.rpc.RemoteException;
import ohos.media.image.PixelMap;
import ohos.utils.PacMap;
import ohos.wifi.WifiDevice;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainAbilitySlice extends AbilitySlice{
    // Define log label
    private static final HiLogLabel TAG = new HiLogLabel(HiLog.LOG_APP, 0x00201, "MainAbilitySlice");

    private DirectionalLayout networkStatusContnt;
    private NotificationRequest notificationRequest;
    private MyCommonEventSubscriber myEventSubscriber;
    private NotificationSlot slot;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);
        //subscribeCommonEvent();
        //defineNotificationSlot("Ongoing_Overview","slot_anmyPlayer",NotificationSlot.LEVEL_DEFAULT);
        initComponents();
        queryNetworkStatus();
    }

    private void subscribeCommonEvent() {
        MatchingSkills matchingSkills = new MatchingSkills();
        matchingSkills.addEvent("REPLY_ACTION");
        CommonEventSubscribeInfo subscribeInfo = new CommonEventSubscribeInfo(matchingSkills);
        subscribeInfo.setThreadMode(CommonEventSubscribeInfo.ThreadMode.HANDLER);
        myEventSubscriber =new MyCommonEventSubscriber(subscribeInfo,this);
        try {
            CommonEventManager.subscribeCommonEvent(myEventSubscriber);
        } catch (RemoteException e) {
            HiLog.error(TAG, "%{public}s", "subscribeCommonEvent remoteException.");
        }
    }

    private void unSubscribeCommonEvent() {
        try {
            CommonEventManager.unsubscribeCommonEvent(myEventSubscriber);
        } catch (RemoteException e) {
            HiLog.error(TAG, "%{public}s", "unSubscribeCommonEvent remoteException.");
        }
    }

    class MyCommonEventSubscriber extends CommonEventSubscriber {
        private AbilitySlice slice;
        MyCommonEventSubscriber(CommonEventSubscribeInfo info, AbilitySlice slice) {
            super(info);
            this.slice = slice;
        }
        @Override
        public void onReceiveEvent(CommonEventData commonEventData) {
            Intent intent = commonEventData.getIntent();
            if (intent == null) {
                return;
            }
            if ("REPLY_ACTION".equals(intent.getAction())) {
                PacMap pacMap = NotificationUserInput.getInputsFromIntent(intent);
                if (pacMap == null) {
                    return;
                }
                String inputText =pacMap.getString("REPLY_KEY");
                slice.getUITaskDispatcher().asyncDispatch(()-> {
                    Text replyText = (Text) slice.findComponentById(ResourceTable.Id_notify2_reply);
                    replyText.setText(inputText);
                });
            }
        }
    }

    private void initComponents() {
        networkStatusContnt = (DirectionalLayout) findComponentById(ResourceTable.Id_network_status_content);
        Button btn_cha1 = (Button) findComponentById(ResourceTable.Id_main_channel_1);
        Button btn_cha2 = (Button) findComponentById(ResourceTable.Id_main_channel_2);
        Button btn_cha3 = (Button) findComponentById(ResourceTable.Id_publish_button);

        btn_cha1.setClickedListener(listener -> present(new PlayAbilitySlice(), new Intent()));
        btn_cha2.setClickedListener(listener -> present(new IjkAudioPlayAbilitySlice(), new Intent()));
        //btn_cha3.setClickedListener(
        //        component -> publishNotification("Notification", "Notification test"));
    }

    private void defineNotificationSlot(String id, String name, int importance) {
        slot = new NotificationSlot(id, name, importance);
        //slot.setEnableVibration(true);
        //slot.setLockscreenVisibleness(NotificationRequest.VISIBLENESS_TYPE_PUBLIC);
        slot.setDescription("NotificationSlotDescription");
        try {
            NotificationHelper.addNotificationSlot(slot);
        } catch (RemoteException ex) {
            HiLog.error(TAG, "Add ongoing card slot exception");
        }
    }

    private void publishNotification(String title, String text) {
        int notificationId = 10001;
        notificationRequest = new NotificationRequest(this, notificationId);
        notificationRequest.setSlotId(slot.getId());

        //notificationRequest.setLittleIcon(ResourceTable.Media_pause);
        String textContent = "Ongoing Test";
        NotificationRequest.NotificationNormalContent content = new NotificationRequest.NotificationNormalContent();
        content.setText(textContent); // 设置卡片内容
        NotificationRequest.NotificationContent notificationContent = new NotificationRequest.NotificationContent(content);
        notificationRequest.setContent(notificationContent);
        //notificationRequest.setContent(createNotificationContent(title, text));

        IntentParams params = new IntentParams();
        params.setParam("MinusOneExtraTag","Ongoing_Overview"); // MinusOneExtraTag必须准确无误
        notificationRequest.setAdditionalData(params);

        ComponentProvider componentProvider = new ComponentProvider(ResourceTable.Layout_test, this); // 创建ComponentProvider对象
        componentProvider.setText(ResourceTable.Id_ongoing_test,"Anmy music");
        //componentProvider.setImagePixelMap(ResourceTable.Id_imgPause,ImageTools.getPixelMap(this, ResourceTable.Media_pause));
        //componentProvider.setString(ResourceTable.Id_ongoing_test, "setText", "TextContent"); // 设置布局中的文本内容
        notificationRequest.setCustomView(componentProvider);

        // 获取IntentAgent实例
        IntentAgent intentAgent = createIntentAgent(MainAbility.class.getName(),
                IntentAgentConstant.OperationType.START_ABILITY);
        componentProvider.setIntentAgent(ResourceTable.Id_test,intentAgent);  // 设置跳转事件信息
        //设置通知可以触发的事件
        //notificationRequest.setIntentAgent(intentAgent);// 设置通知可以触发的事件

        try {
            //NotificationHelper.publishNotification("Ongoing_Overview",notificationRequest);
            NotificationHelper.publishNotification(notificationRequest);
            System.out.println("---Ongoing_Overview publish notification---");
        } catch (RemoteException ex) {
            HiLog.error(TAG, "%{public}s", "publishNotification remoteException.");
        }
    }

    private NotificationRequest.NotificationContent createNotificationContent(String title, String text) {
        NotificationRequest.NotificationNormalContent content
                = new NotificationRequest.NotificationNormalContent().setTitle(title).setText(text);
        NotificationRequest.NotificationContent notificationContent = new NotificationRequest.NotificationContent(
                content);
        return notificationContent;
    }

    private IntentAgent createIntentAgent(String ability, IntentAgentConstant.OperationType operationType) {
        // 指定要启动的ability属性
        Intent intent = new Intent();
        Operation operation = new Intent.OperationBuilder()
                .withDeviceId("")
                .withBundleName("com.huawei.radiolinedemo")
                .withAbilityName("com.huawei.radiolinedemo.PlayServiceAbility")
                .build();
        intent.setOperation(operation); // 创建跳转的目的ability
        List<Intent> intentList = new ArrayList<>();
        intentList.add(intent);
        // 指定启动一个有页面的ability, 0x1000001 is notification ID
        IntentAgentInfo agentInfo = new IntentAgentInfo(notificationRequest.getNotificationId(), operationType,
                IntentAgentConstant.Flags.UPDATE_PRESENT_FLAG, intentList, new IntentParams());
        return IntentAgentHelper.getIntentAgent(getContext(), agentInfo);
    }

    private void queryNetworkStatus(){
        WifiDevice mWifiDevice = WifiDevice.getInstance(getContext());
        boolean isConnected = mWifiDevice.isConnected();
        HiLog.warn(TAG, "======="+isConnected);
        if (isConnected) {
            networkStatusContnt.setVisibility(Component.INVISIBLE);
        }else{
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
                    connection.setConnectTimeout(1000);
                    int code = connection.getResponseCode();
                    if (code == 200) {
                        HiLog.error(TAG, "===========connected.");
                    }else {
                        HiLog.error(TAG, "===========disconnected.");
                        present(new NoInternetSlice(), new Intent());
                        terminate();
                    }
                } catch (Exception e) {
                    present(new NoInternetSlice(), new Intent());
                    e.printStackTrace();
                    terminate();
                }
            }
        }).start();
    }

    public void onActive() {
        super.onActive();
    }

    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unSubscribeCommonEvent();
    }
}