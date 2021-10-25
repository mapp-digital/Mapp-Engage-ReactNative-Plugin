package com.reactlibrary;

import com.appoxee.push.PushData;
import com.appoxee.push.fcm.MappMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public  class MessageService extends MappMessagingService {

    @Override
    public void onCreate() {
        super.onCreate();
        Appoxee.engage(getApplication());
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
    }
}
