package com.reactlibrary;

import android.util.Log;

import com.appoxee.shared.LocalPushBroadcast;
import com.appoxee.shared.MappPush;

/**
 * Created by Aleksandar Marinkovic on 2019-05-15.
 * Copyright (c) 2019 MAPP.
 *
 * Updated for Engage SDK v7: extends LocalPushBroadcast instead of PushDataReceiver.
 */
public class MyPushBroadcastReceiver extends LocalPushBroadcast {

    @Override
    public void onReceived(MappPush mappPush) {
        Log.d("APX", "Push received " + mappPush);
        EventEmitter.shared().sendEvent(new PushNotificationEvent(mappPush, "onPushReceived"));
    }

    @Override
    public void onOpened(MappPush mappPush) {
        Log.d("APX", "Push opened " + mappPush);
        EventEmitter.shared().sendEvent(new PushNotificationEvent(mappPush, "onPushOpened"));
    }

    @Override
    public void onDismissed(MappPush mappPush) {
        Log.d("APX", "Push dismissed " + mappPush);
        EventEmitter.shared().sendEvent(new PushNotificationEvent(mappPush, "onPushDismissed"));
    }

    @Override
    public void onSilent(MappPush mappPush) {
        Log.d("APX", "Push silent " + mappPush);
        EventEmitter.shared().sendEvent(new PushNotificationEvent(mappPush, "onSilentPush"));
    }

    @Override
    public void onButtonClick(MappPush mappPush) {
        Log.d("APX", "Push button clicked " + mappPush);
        EventEmitter.shared().sendEvent(new PushNotificationEvent(mappPush, "onPushButtonClicked"));
    }

    @Override
    public void onRichPush(MappPush mappPush) {
        Log.d("APX", "Rich push " + mappPush);
        EventEmitter.shared().sendEvent(new PushNotificationEvent(mappPush, "onRichPush"));
    }
}
