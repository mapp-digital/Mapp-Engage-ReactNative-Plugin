package com.reactlibrary;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


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
        MappPushHelper.initialize(getApplication());
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d("onMessageReceived", remoteMessage.toString());
        if (!MappPushHelper.handleMessage(getApplication(), remoteMessage)) {
            super.onMessageReceived(remoteMessage);
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        MappPushHelper.handleNewToken(getApplication(), token);
        super.onNewToken(token);
    }
}
