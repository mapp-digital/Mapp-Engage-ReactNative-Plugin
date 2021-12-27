package com.reactlibrary;

import androidx.annotation.NonNull;

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
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        waitInitialization();
        super.onMessageReceived(remoteMessage);
    }

    @Override
    public void onNewToken(String s) {
        waitInitialization();
        super.onNewToken(s);
    }

    private void waitInitialization(){
        int limit=15;
        try{
            while (limit >= 0 && !Appoxee.instance().isReady()) {
                TimeUnit.MILLISECONDS.sleep(300);
                limit--;
            }
        }catch (Exception e){
            LoggerFactory.getDevLogger().e(e.getMessage());
        }
    }
}
