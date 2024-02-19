package com.reactlibrary;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * Created by Aleksandar Marinkovic on 2019-05-23.
 * Copyright (c) 2019 MAPP.
 */
public class HelperActivity extends Activity {

    @Override
    public final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if (intent == null) {
            finish();
            return;
        }

        if (intent.getAction() != null && !intent.getAction().equals("")) {
            Intent launchIntent = getDefaultActivityIntent();
            launchIntent.setPackage(this.getPackageName());
            launchIntent.putExtra("action", intent.getAction());
            launchIntent.setData(intent.getData());
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(launchIntent);

            EventEmitter.shared().sendEvent(new IntentNotificationEvent(Objects.requireNonNull(intent.getData()), intent.getAction()));
            finish();
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

        ComponentName name = intent.getComponent();
        String packageName = getPackageName();

        if (Objects.equals(name.getPackageName(), packageName)) {
            if (intent.getAction() != null && !intent.getAction().equals("")) {
                EventEmitter.shared().sendEvent(new IntentNotificationEvent(Objects.requireNonNull(intent.getData()), intent.getAction()));
            }
        }
    }
}