package com.reactlibrary;//package com.appoxee.testapp.fcm;



import com.appoxee.push.PushData;
import com.appoxee.push.fcm.MappMessagingService;
import com.google.firebase.messaging.RemoteMessage;
public  class MessageService extends MappMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        //TODO handle push data if you went
        PushData pushData = getData(remoteMessage);
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
    }
}
