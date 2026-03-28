package com.reactlibrary;

import android.util.Log;

import androidx.annotation.NonNull;

import com.appoxee.Appoxee;
import com.appoxee.shared.AppoxeeOptions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.concurrent.TimeUnit;


/**
 * Updated for Engage SDK v7:
 * - isPushMessageFromMapp() replaces data.containsKey("p") check
 * - handlePushMessage() replaces setRemoteMessage()
 * - updateFirebaseToken() replaces setToken()
 */
public class MessageService extends FirebaseMessagingService {

    @Override
    public void onCreate() {
        super.onCreate();
        Appoxee.engage(getApplication(), null);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d("onMessageReceived", remoteMessage.toString());
        if (Appoxee.instance().isPushMessageFromMapp(remoteMessage)) {
            waitInitialization();
            Appoxee.instance().handlePushMessage(remoteMessage);
        } else {
            super.onMessageReceived(remoteMessage);
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        waitInitialization();
        Appoxee.instance().updateFirebaseToken(token).enqueue(result -> {});
        super.onNewToken(token);
    }

    private void waitInitialization() {
        int limit = 15;
        try {
            while (limit >= 0 && !Appoxee.instance().isReady()) {
                TimeUnit.MILLISECONDS.sleep(300);
                limit--;
            }
        } catch (Exception e) {
            Log.e("MessageService", "waitInitialization interrupted", e);
        }
    }
}
