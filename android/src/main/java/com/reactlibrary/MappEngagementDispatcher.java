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

/** Keeps every Appoxee.engage call on Android's main looper. */
final class MappEngagementDispatcher {
    private static final String TAG = "MappEngage";
    static final long ENGAGE_TIMEOUT_MILLIS = 5_000;

    private MappEngagementDispatcher() {}

    static void engageAsync(
            @NonNull Application application,
            @Nullable AppoxeeOptions options,
            @Nullable Runnable afterEngage
    ) {
        Runnable operation = () -> {
            if (engageNow(application, options) && afterEngage != null) {
                afterEngage.run();
            }
        };
        if (Looper.myLooper() == Looper.getMainLooper()) {
            operation.run();
            return;
        }
        if (!new Handler(Looper.getMainLooper()).post(operation)) {
            Log.e(TAG, "Unable to post Mapp initialization to the main looper");
        }
    }

    static boolean engageBlocking(@NonNull Application application, @Nullable AppoxeeOptions options) {
        return engageBlocking(
                application,
                options,
                new Handler(Looper.getMainLooper()),
                ENGAGE_TIMEOUT_MILLIS
        );
    }

    static boolean engageBlocking(
            @NonNull Application application,
            @Nullable AppoxeeOptions options,
            @NonNull Handler mainHandler,
            long timeoutMillis
    ) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            return engageNow(application, options);
        }

        CountDownLatch completion = new CountDownLatch(1);
        AtomicBoolean engaged = new AtomicBoolean(false);
        boolean posted = mainHandler.post(() -> {
            try {
                engaged.set(engageNow(application, options));
            } finally {
                completion.countDown();
            }
        });
        if (!posted) {
            Log.e(TAG, "Unable to post Mapp initialization to the main looper");
            return false;
        }

        try {
            if (!completion.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
                Log.e(TAG, "Mapp initialization timed out on the main looper");
                return false;
            }
            return engaged.get();
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            Log.w(TAG, "Interrupted while waiting for Mapp initialization", interrupted);
            return false;
        }
    }

    private static boolean engageNow(
            @NonNull Application application,
            @Nullable AppoxeeOptions options
    ) {
        try {
            Appoxee.engage(application, options);
            return true;
        } catch (RuntimeException error) {
            Log.e(TAG, "Mapp initialization failed", error);
            return false;
        }
    }
}
