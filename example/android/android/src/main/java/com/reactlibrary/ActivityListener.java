package com.reactlibrary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.v4.content.ContextCompat;

/**
 * Created by Aleksandar Marinkovic on 1/30/19.
 * Copyright (c) 2019 MAPP.
 */
public class ActivityListener extends Activity {


    public static final String PERMISSIONS_EXTRA = "PERMISSIONS_EXTRA";

    /**
     * Intent extra holding an activity result receiver.
     */
    public static final String RESULT_RECEIVER_EXTRA = "RESULT_RECEIVER_EXTRA";

    /**
     * Intent extra holding activity result intent.
     */
    public static final String RESULT_INTENT_EXTRA = "RESULT_INTENT_EXTRA";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Intent launchIntent = getDefaultActivityIntent();
        intent.putExtra("action", intent.getAction());
        launchIntent.setData(intent.getData());
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(launchIntent);
        finish();
    }


    private Intent getDefaultActivityIntent() {
        PackageManager packageManager = getPackageManager();
        return packageManager.getLaunchIntentForPackage(getPackageName());
    }

    private ResultReceiver resultReceiver;

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


    @WorkerThread
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
                int[] receiverResults = resultData.getIntArray(ActivityListener.RESULT_INTENT_EXTRA);
                if (receiverResults != null && receiverResults.length == result.length) {
                    System.arraycopy(receiverResults, 0, result, 0, result.length);
                }

                synchronized (result) {
                    result.notify();
                }
            }
        };

        Intent startingIntent = new Intent(context, ActivityListener.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setPackage(context.getPackageName())
                .putExtra(ActivityListener.PERMISSIONS_EXTRA, permissions)
                .putExtra(ActivityListener.RESULT_RECEIVER_EXTRA, receiver);

        synchronized (result) {
            context.startActivity(startingIntent);
            try {
                result.wait();
            } catch (InterruptedException e) {

            }
        }

        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultReceiver != null) {
            Bundle bundledData = new Bundle();
            bundledData.putParcelable(RESULT_INTENT_EXTRA, data);
            resultReceiver.send(resultCode, bundledData);
        }

        super.onActivityResult(requestCode, resultCode, data);
        this.finish();
    }

    public static class ActivityResult {
        private int resultCode = Activity.RESULT_CANCELED;
        private Intent intent;

        public Intent getIntent() {
            return intent;
        }


        public int getResultCode() {
            return resultCode;
        }


        private void setResult(int resultCode, Intent intent) {
            this.resultCode = resultCode;
            this.intent = intent;
        }
    }
}
