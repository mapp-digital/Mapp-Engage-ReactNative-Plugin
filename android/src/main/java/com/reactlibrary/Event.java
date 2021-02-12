/* Copyright Urban Airship and Contributors */

package com.reactlibrary;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.WritableMap;

/**
 * Created by Aleksandar Marinkovic on 2019-05-15.
 * Copyright (c) 2019 MAPP.
 */
public interface Event {

    /**
     * The event name.
     * @return The event name.
     */
    @NonNull
    String getName();

    /**
     * The event body.
     * @return The event body.
     */
    @NonNull
    WritableMap getBody();
}
