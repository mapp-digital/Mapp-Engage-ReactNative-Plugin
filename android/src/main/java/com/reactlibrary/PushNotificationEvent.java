package com.reactlibrary;

import androidx.annotation.NonNull;

import com.appoxee.push.PushData;
import com.facebook.react.bridge.WritableMap;

/**
 * Created by Aleksandar Marinkovic on 2019-05-16.
 * Copyright (c) 2019 MAPP.
 */
public class PushNotificationEvent implements Event {

    private static final String PUSH_RECEIVED_EVENT = "con.mapp.rich_message_received";

    @NonNull
    @Override
    public String getName() {
        return PUSH_RECEIVED_EVENT;
    }

    @NonNull
    @Override
    public WritableMap getBody() {
        return RNUtils.getPushMessageToJSon(message, type);
    }

    private final PushData message;
    private final String type;

    public PushNotificationEvent(@NonNull PushData message, String type) {
        this.message = message;
        this.type = type;
    }


}
