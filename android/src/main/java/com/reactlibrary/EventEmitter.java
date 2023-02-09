/* Copyright Urban Airship and Contributors */

package com.reactlibrary;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.MainThread;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.core.RCTNativeAppEventEmitter;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * Created by Aleksandar Marinkovic on 2019-05-15.
 * Copyright (c) 2019 MAPP.
 */
class EventEmitter {

    private static final EventEmitter sharedInstance = new EventEmitter();

    private final Queue<Event> pendingEvents = new ConcurrentLinkedQueue<>();
    private final Queue<String> knownListeners = new ConcurrentLinkedQueue<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private ReactContext reactContext;

    /**
     * Returns the shared {@link EventEmitter} instance.
     *
     * @return The shared {@link EventEmitter} instance.
     */
    static EventEmitter shared() {
        return sharedInstance;
    }

    /**
     * Attaches the react context.
     *
     * @param reactContext The react context.
     */
    void attachReactContext(final ReactContext reactContext) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                EventEmitter.this.reactContext = reactContext;
                sendPendingEvents();
            }
        });
    }

    /**
     * Sends an event to the JS layer.
     *
     * @param event The event.
     */
    void sendEvent(final Event event) {
        synchronized (knownListeners) {
            if (!knownListeners.contains(event.getName()) || !emit(event)) {
                pendingEvents.add(event);
            }
        }
    }

    /**
     * Called when a new listener is added for a specified event name.
     *
     * @param eventName The event name.
     */
    void addAndroidListener(String eventName) {
        synchronized (knownListeners) {
            knownListeners.add(eventName);
        }

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                sendPendingEvents();
            }
        });
    }

    /**
     * Called when listeners are removed.
     *
     * @param count The count of listeners.
     */
    void removeAndroidListeners(int count) {
        synchronized (knownListeners) {
            for (int i = 0; i < count; i++) {
                if (knownListeners.size() > 0)
                    knownListeners.remove();
                else
                    break;
            }
        }
    }

    /**
     * Attempts to send pending events.
     */
    @MainThread
    private void sendPendingEvents() {
        synchronized (knownListeners) {
            for (Event event : new ArrayList<>(pendingEvents)) {
                if (knownListeners.contains(event.getName())) {
                    // Remove the event first before attempting to send. If it fails to
                    // send it will get added back to pendingEvents.
                    pendingEvents.remove(event);
                    sendEvent(event);
                }
            }
        }
    }

    /**
     * Helper method to emit data.
     *
     * @param event The event.
     * @return {@code true} if the event was emitted, otherwise {@code false}.
     */
    @MainThread
    private boolean emit(final Event event) {
        ReactContext reactContext = this.reactContext;
        if (reactContext == null || !reactContext.hasActiveCatalystInstance()) {
            return false;
        }

        try {
            reactContext.getJSModule(RCTNativeAppEventEmitter.class).emit(event.getName(), event.getBody());
        } catch (Exception e) {
            return false;
        }

        return true;
    }

}
