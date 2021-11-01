package com.reactlibrary;

import com.appoxee.Appoxee;
import com.appoxee.internal.logger.LoggerFactory;
import com.appoxee.internal.service.AppoxeeServiceAdapter;
import com.appoxee.push.fcm.MappMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.concurrent.TimeUnit;


public class MessageService extends MappMessagingService {

    @Override
    public void onCreate() {
        super.onCreate();
        Appoxee.engage(getApplication());
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        int limit = 15;
        try {
            while (limit >= 0 &&  (AppoxeeServiceAdapter.getInstance() == null || !AppoxeeServiceAdapter.getInstance().isQueryReady())) {
                TimeUnit.MILLISECONDS.sleep(300);
                limit--;
            }
            super.onMessageReceived(remoteMessage);
        } catch (Exception e) {
            LoggerFactory.getDevLogger().e(e.getMessage());
        }
    }

    @Override
    public void onNewToken(String s) {
        int limit = 15;
        try {
            while (limit >= 0 &&  (AppoxeeServiceAdapter.getInstance() == null || !AppoxeeServiceAdapter.getInstance().isQueryReady())) {
                TimeUnit.MILLISECONDS.sleep(300);
                limit--;
            }
            super.onNewToken(s);
        } catch (Exception e) {
            LoggerFactory.getDevLogger().e(e.getMessage());
        }
    }
}
