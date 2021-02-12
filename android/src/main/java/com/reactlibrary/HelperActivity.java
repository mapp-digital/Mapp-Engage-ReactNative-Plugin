package com.reactlibrary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.content.ContextCompat;


import java.util.Objects;

/**
 * Created by Aleksandar Marinkovic on 2019-05-23.
 * Copyright (c) 2019 MAPP.
 */
public class HelperActivity extends Activity {

    @NonNull
    public static final String PERMISSIONS_EXTRA = "com.mapp.PERMISSIONS_EXTRA";

    @NonNull
    public static final String RESULT_RECEIVER_EXTRA = "com.mapp.RESULT_RECEIVER_EXTRA";

    @NonNull
    public static final String RESULT_INTENT_EXTRA = "com.mapp.RESULT_INTENT_EXTRA";


    @NonNull
    public static final String START_ACTIVITY_INTENT_EXTRA = "com.mapp.START_ACTIVITY_INTENT_EXTRA";

    private ResultReceiver resultReceiver;
    private static int requestCode = 0;

    @Override
    public final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if (intent == null) {
            finish();
            return;
        }
        if (intent.getAction() != null)
            if (!intent.getAction().equals("")) {
                Intent launchIntent = getDefaultActivityIntent();
                launchIntent.putExtra("action", intent.getAction());
                launchIntent.setData(intent.getData());
                launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(launchIntent);
                EventEmitter.shared().sendEvent(new IntentNotificationEvent(Objects.requireNonNull(intent.getData()), intent.getAction()));
                finish();
            }
        if (savedInstanceState == null) {
            Intent startActivityIntent = intent.getParcelableExtra(START_ACTIVITY_INTENT_EXTRA);
            String[] permissions = intent.getStringArrayExtra(PERMISSIONS_EXTRA);

            if (startActivityIntent != null) {
                resultReceiver = intent.getParcelableExtra(RESULT_RECEIVER_EXTRA);
                startActivityForResult(startActivityIntent, ++requestCode);
            } else if (Build.VERSION.SDK_INT >= 23 && permissions != null) {
                resultReceiver = intent.getParcelableExtra(RESULT_RECEIVER_EXTRA);
                requestPermissions(permissions, ++requestCode);
            } else {
                finish();
            }
        }
    }

    private Intent getDefaultActivityIntent() {
        PackageManager packageManager = getPackageManager();
        return packageManager.getLaunchIntentForPackage(getPackageName());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent == null) {
            return;
        }
        if (intent.getAction() != null)
            if (!intent.getAction().equals("")) {
                EventEmitter.shared().sendEvent(new IntentNotificationEvent(Objects.requireNonNull(intent.getData()), intent.getAction()));
            }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultReceiver != null) {
            Bundle bundledData = new Bundle();
            bundledData.putParcelable(RESULT_INTENT_EXTRA, data);
            resultReceiver.send(resultCode, bundledData);
        }

        super.onActivityResult(requestCode, resultCode, data);
        this.finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (resultReceiver != null) {
            Bundle bundledData = new Bundle();
            bundledData.putIntArray(RESULT_INTENT_EXTRA, grantResults);
            resultReceiver.send(Activity.RESULT_OK, bundledData);
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        finish();
    }

    /**
     * Requests permissions.
     *
     * @param context     The application context.
     * @param permissions The permissions to request.
     * @return The result from requesting permissions.
     */
    @WorkerThread
    @NonNull
    public static int[] requestPermissions(@NonNull Context context, @NonNull String... permissions) {
        context = context.getApplicationContext();
        boolean permissionsDenied = false;

        final int[] result = new int[permissions.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = ContextCompat.checkSelfPermission(context, permissions[i]);
            if (result[i] == PackageManager.PERMISSION_DENIED) {
                permissionsDenied = true;
            }
        }

        if (!permissionsDenied || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return result;
        }

        ResultReceiver receiver = new ResultReceiver(new Handler(Looper.getMainLooper())) {
            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
                int[] receiverResults = resultData.getIntArray(HelperActivity.RESULT_INTENT_EXTRA);
                if (receiverResults != null && receiverResults.length == result.length) {
                    System.arraycopy(receiverResults, 0, result, 0, result.length);
                }

                synchronized (result) {
                    result.notify();
                }
            }
        };

        Intent startingIntent = new Intent(context, HelperActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setPackage(context.getPackageName())
                .putExtra(HelperActivity.PERMISSIONS_EXTRA, permissions)
                .putExtra(HelperActivity.RESULT_RECEIVER_EXTRA, receiver);

        synchronized (result) {
            context.startActivity(startingIntent);
            try {
                result.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return result;
    }

    /**
     * Starts an activity for a result.
     *
     * @param context The application context.
     * @param intent  The activity to start.
     * @return The result of the activity in a ActivityResult object.
     */
    @NonNull
    @WorkerThread
    public static ActivityResult startActivityForResult(@NonNull Context context, @NonNull Intent intent) {
        context = context.getApplicationContext();
        final ActivityResult result = new ActivityResult();

        ResultReceiver receiver = new ResultReceiver(new Handler(Looper.getMainLooper())) {
            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
                result.setResult(resultCode, (Intent) resultData.getParcelable(HelperActivity.RESULT_INTENT_EXTRA));
                synchronized (result) {
                    result.notify();
                }
            }
        };

        Intent startingIntent = new Intent(context, HelperActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setPackage(context.getPackageName())
                .putExtra(HelperActivity.START_ACTIVITY_INTENT_EXTRA, intent)
                .putExtra(HelperActivity.RESULT_RECEIVER_EXTRA, receiver);

        synchronized (result) {
            context.startActivity(startingIntent);
            try {
                result.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new ActivityResult();
            }
        }

        return result;
    }

    /**
     * Wraps the result code and data from starting an activity
     * for a result.
     */
    public static class ActivityResult {

        private int resultCode = Activity.RESULT_CANCELED;
        private Intent intent;

        /**
         * Gets the result intent.
         *
         * @return The result intent from the activity.
         */
        @Nullable
        public Intent getIntent() {
            return intent;
        }

        /**
         * Gets the result code from the activity.
         *
         * @return The result code from the activity.
         */
        public int getResultCode() {
            return resultCode;
        }

        private void setResult(int resultCode, @Nullable Intent intent) {
            this.resultCode = resultCode;
            this.intent = intent;
        }

    }

}

