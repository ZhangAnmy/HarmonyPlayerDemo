package com.huawei.radiolinedemo.slice;

import com.huawei.radiolinedemo.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.DirectionalLayout;
import ohos.agp.components.Text;
import ohos.event.commonevent.CommonEventData;
import ohos.event.commonevent.CommonEventManager;
import ohos.event.commonevent.CommonEventSubscribeInfo;
import ohos.event.commonevent.CommonEventSubscriber;
import ohos.event.notification.NotificationRequest;
import ohos.event.notification.NotificationSlot;
import ohos.event.notification.NotificationUserInput;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;
import ohos.utils.PacMap;
import ohos.wifi.WifiDevice;

import java.net.HttpURLConnection;
import java.net.URL;

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
        initComponents();
        queryNetworkStatus();
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
                /*slice.getUITaskDispatcher().asyncDispatch(()-> {
                    Text replyText = (Text) slice.findComponentById(ResourceTable.Id_notify2_reply);
                    replyText.setText(inputText);
                    replyText.setTruncationMode(Text.TruncationMode.AUTO_SCROLLING);
                    replyText.startAutoScrolling();
                });*/
            }
        }
    }

    private void initComponents() {
        networkStatusContnt = (DirectionalLayout) findComponentById(ResourceTable.Id_network_status_content);
        Button btn_cha1 = (Button) findComponentById(ResourceTable.Id_main_channel_1);
        Button btn_cha2 = (Button) findComponentById(ResourceTable.Id_main_channel_2);

        btn_cha1.setClickedListener(listener -> present(new PlayAbilitySlice(), new Intent()));
        btn_cha2.setClickedListener(listener -> present(new IjkAudioPlayAbilitySlice(), new Intent()));
        //btn_cha3.setClickedListener(
        //        component -> publishNotification("Notification", "Notification test"));
        Text hello_test = (Text) findComponentById(ResourceTable.Id_text_hello);
        hello_test.setTruncationMode(Text.TruncationMode.AUTO_SCROLLING);
        hello_test.startAutoScrolling();
    }

    private NotificationRequest.NotificationContent createNotificationContent(String title, String text) {
        NotificationRequest.NotificationNormalContent content
                = new NotificationRequest.NotificationNormalContent().setTitle(title).setText(text);
        NotificationRequest.NotificationContent notificationContent = new NotificationRequest.NotificationContent(
                content);
        return notificationContent;
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
                        present(new NoInternetSlice(), new Intent()); //This one to present the network unavailable page
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