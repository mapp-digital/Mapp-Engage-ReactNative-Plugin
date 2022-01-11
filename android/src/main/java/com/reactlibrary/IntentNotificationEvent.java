package com.reactlibrary;

import android.net.Uri;


import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

/**
 * Created by Aleksandar Marinkovic on 2019-05-16.
 * Copyright (c) 2019 MAPP.
 */
public class IntentNotificationEvent implements Event {

    private static final String PUSH_RECEIVED_EVENT = "com.mapp.deep_link_received";

    @Override
    public String getName() {
        return PUSH_RECEIVED_EVENT;
    }

    @Override
    public WritableMap getBody() {

        WritableMap writableMap = new WritableNativeMap();
        if (message != null) {
            try {
                String link = message.getQueryParameter("link");
                String messageId = message.getQueryParameter("message_id");
                String event_trigger = message.getQueryParameter("event_trigger");
                writableMap.putString("action", messageId);
                writableMap.putString("url", link);
                writableMap.putString("event_trigger", event_trigger);
            } catch (Exception e) {
                writableMap.putString("url", message.toString());
            }
        } else {
            writableMap.putString("url", "");
            writableMap.putString("action", type);
        }

        return writableMap;
    }

    private final Uri message;
    private final String type;

    public IntentNotificationEvent(Uri message, String type) {
        this.message = message;
        this.type = type;
    }
}
