package com.reactlibrary;

import android.app.Application;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.appoxee.Appoxee;
import com.google.firebase.messaging.RemoteMessage;

import java.util.concurrent.TimeUnit;

/**
 * Native entry points for applications that own their FirebaseMessagingService.
 * This API does not require a running React Native JavaScript runtime.
 */
public final class MappPushHelper {
    private static final String TAG = "MappPushHelper";
    private static final int READY_ATTEMPTS = 15;
    private static final long READY_INTERVAL_MILLIS = 300;

    private MappPushHelper() {}

    /** Engage Mapp using AppoxeeConfig/native defaults if it has not been engaged yet. */
    public static boolean initialize(@NonNull Application application) {
        return MappEngagementDispatcher.engageBlocking(application, null);
    }

    static boolean initialize(@NonNull Application application, @NonNull Handler handler, long timeoutMillis) {
        return MappEngagementDispatcher.engageBlocking(application, null, handler, timeoutMillis);
    }

    /** Return true only when the native Mapp SDK identifies this payload as its own. */
    public static boolean isMappMessage(@NonNull RemoteMessage remoteMessage) {
        try {
            return Appoxee.instance().isPushMessageFromMapp(remoteMessage);
        } catch (RuntimeException error) {
            Log.e(TAG, "Unable to inspect remote message", error);
            return false;
        }
    }

    /** Forward a Mapp message. Returns false for non-Mapp payloads or SDK failures. */
    public static boolean handleMessage(@NonNull Application application, @NonNull RemoteMessage remoteMessage) {
        return handleMessage(application, remoteMessage, null, MappEngagementDispatcher.ENGAGE_TIMEOUT_MILLIS);
    }

    static boolean handleMessage(
            @NonNull Application application,
            @NonNull RemoteMessage remoteMessage,
            Handler handler,
            long timeoutMillis
    ) {
        boolean initialized = handler == null
                ? initialize(application)
                : initialize(application, handler, timeoutMillis);
        if (!initialized) {
            return false;
        }
        if (!isMappMessage(remoteMessage) || !waitUntilReady()) {
            return false;
        }
        try {
            Appoxee.instance().handlePushMessage(remoteMessage);
            return true;
        } catch (RuntimeException error) {
            Log.e(TAG, "Unable to forward remote message", error);
            return false;
        }
    }

    /** Forward a refreshed native FCM token without requiring JavaScript. */
    public static boolean handleNewToken(@NonNull Application application, @NonNull String token) {
        return handleNewToken(application, token, null, MappEngagementDispatcher.ENGAGE_TIMEOUT_MILLIS);
    }

    static boolean handleNewToken(
            @NonNull Application application,
            @NonNull String token,
            Handler handler,
            long timeoutMillis
    ) {
        if (token.trim().isEmpty()) {
            return false;
        }
        boolean initialized = handler == null
                ? initialize(application)
                : initialize(application, handler, timeoutMillis);
        if (!initialized) {
            return false;
        }
        if (!waitUntilReady()) {
            return false;
        }
        try {
            Appoxee.instance().updateFirebaseToken(token).enqueue(result -> {});
            return true;
        } catch (RuntimeException error) {
            Log.e(TAG, "Unable to forward Firebase token", error);
            return false;
        }
    }

    public static boolean waitUntilReady() {
        for (int attempt = 0; attempt <= READY_ATTEMPTS; attempt++) {
            try {
                if (Appoxee.instance().isReady()) {
                    return true;
                }
                if (attempt < READY_ATTEMPTS) {
                    TimeUnit.MILLISECONDS.sleep(READY_INTERVAL_MILLIS);
                }
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                Log.w(TAG, "Interrupted while waiting for Mapp initialization", interrupted);
                return false;
            } catch (RuntimeException error) {
                Log.e(TAG, "Mapp initialization failed", error);
                return false;
            }
        }
        Log.w(TAG, "Mapp was not ready before the bounded wait expired");
        return false;
    }
}
