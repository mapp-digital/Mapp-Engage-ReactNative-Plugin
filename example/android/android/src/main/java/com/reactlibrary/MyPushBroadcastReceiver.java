package com.reactlibrary;

import android.util.Log;

import com.appoxee.push.PushData;
import com.appoxee.push.PushDataReceiver;
/**
 * Created by Aleksandar Marinkovic on 2019-05-15.
 * Copyright (c) 2019 MAPP.
 */
public  class MyPushBroadcastReceiver extends PushDataReceiver {
    @Override
    public void onPushReceived(PushData pushData) {
        Log.d("APX", "Push received " + pushData);
        EventEmitter.shared().sendEvent(new PushNotificationEvent(pushData,"onPushReceived"));
        super.onPushReceived(pushData);
    }

    @Override
    public void onPushOpened(PushData pushData) {
        Log.d("APX", "Push opened " + pushData);
        EventEmitter.shared().sendEvent(new PushNotificationEvent(pushData,"onPushOpened"));
       super.onPushOpened(pushData);
    }

    @Override
    public void onPushDismissed(PushData pushData) {
        Log.d("APX", "Push dismissed " + pushData);
        EventEmitter.shared().sendEvent(new PushNotificationEvent(pushData,"onPushDismissed"));
        super.onPushDismissed(pushData);
    }


    @Override
    public void onSilentPush(PushData pushData) {
        Log.d("APX", "Push Silent " + pushData);
        EventEmitter.shared().sendEvent(new PushNotificationEvent(pushData,"onSilentPush"));
        super.onSilentPush(pushData);

    }


}
