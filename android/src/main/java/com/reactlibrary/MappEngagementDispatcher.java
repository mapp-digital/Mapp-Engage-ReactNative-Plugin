package com.reactlibrary;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appoxee.Appoxee;
import com.appoxee.shared.AppoxeeOptions;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/** Dispatches engagement and receiver setup; the native SDK owns initialization reuse. */
final class MappEngagementDispatcher {
    private static final String TAG = "MappEngage";
    static final long ENGAGE_TIMEOUT_MILLIS = 5_000;

    private MappEngagementDispatcher() {}

    interface EngagementCallback {
        void onSuccess();
        void onFailure(@NonNull Exception error);
    }

    static void engageAsync(Application application, @Nullable AppoxeeOptions options,
                            @Nullable Runnable afterEngage) {
        engageAsync(application, options, afterEngage, null);
    }

    static void engageAsync(Application application, @Nullable AppoxeeOptions options,
                            @Nullable Runnable afterEngage, @Nullable EngagementCallback callback) {
        Runnable operation = () -> {
            try {
                ensureEngaged(application, options);
                if (afterEngage != null) afterEngage.run();
            } catch (Exception error) {
                Log.e(TAG, "Mapp initialization failed", error);
                if (callback != null) callback.onFailure(error);
                return;
            }
            if (callback != null) callback.onSuccess();
        };
        if (Looper.myLooper() == Looper.getMainLooper()) {
            operation.run();
        } else if (!new Handler(Looper.getMainLooper()).post(operation)) {
            IllegalStateException error = new IllegalStateException(
                    "Unable to post Mapp initialization to the main looper");
            Log.e(TAG, error.getMessage(), error);
            if (callback != null) callback.onFailure(error);
        }
    }

    static boolean engageBlocking(Application application, @Nullable AppoxeeOptions options) {
        return engageBlocking(application, options, new Handler(Looper.getMainLooper()),
                ENGAGE_TIMEOUT_MILLIS);
    }

    static boolean engageBlocking(Application application, @Nullable AppoxeeOptions options,
                                  @NonNull Handler mainHandler, long timeoutMillis) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            return engageNow(application, options);
        }
        CountDownLatch completion = new CountDownLatch(1);
        AtomicBoolean engaged = new AtomicBoolean(false);
        // 0: queued, 1: started, 2: abandoned. A timeout cannot undo work already started.
        AtomicInteger requestState = new AtomicInteger(0);
        Runnable operation = () -> {
            try {
                if (requestState.compareAndSet(0, 1)) {
                    engaged.set(engageNow(application, options));
                }
            } finally {
                completion.countDown();
            }
        };
        if (!mainHandler.post(operation)) {
            Log.e(TAG, "Unable to post Mapp initialization to the main looper");
            return false;
        }
        try {
            if (!completion.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
                abandon(mainHandler, operation, requestState);
                Log.e(TAG, "Mapp initialization timed out on the main looper");
                return false;
            }
            return engaged.get();
        } catch (InterruptedException interrupted) {
            abandon(mainHandler, operation, requestState);
            Thread.currentThread().interrupt();
            Log.w(TAG, "Interrupted while waiting for Mapp initialization", interrupted);
            return false;
        }
    }

    private static void abandon(Handler handler, Runnable operation, AtomicInteger requestState) {
        if (requestState.compareAndSet(0, 2)) handler.removeCallbacks(operation);
    }

    private static boolean engageNow(Application application, AppoxeeOptions options) {
        try {
            ensureEngaged(application, options);
            return true;
        } catch (Exception error) {
            Log.e(TAG, "Mapp initialization failed", error);
            return false;
        }
    }

    private static void ensureEngaged(Application application, AppoxeeOptions options) {
        Appoxee.engage(application, options);
        // SDK 7.1.4 reuses valid initialization and publishes receiver registration immediately.
        Appoxee.instance().setPushBroadcast(MyPushBroadcastReceiver.class);
    }
}
