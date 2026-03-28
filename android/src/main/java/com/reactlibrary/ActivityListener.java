package com.reactlibrary;

import android.app.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;



/**
 * Created by Aleksandar Marinkovic on 1/30/19.
 * Copyright (c) 2019 MAPP.
 */
public class ActivityListener extends Activity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Intent launchIntent = getDefaultActivityIntent();
        launchIntent.putExtra("action", intent.getAction());
        launchIntent.setData(intent.getData());
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        launchIntent.setPackage(this.getPackageName());
        startActivity(launchIntent);
        // handleRichPush() removed in SDK v7
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // handleRichPush() removed in SDK v7
    }

    private Intent getDefaultActivityIntent() {
        PackageManager packageManager = getPackageManager();
        return packageManager.getLaunchIntentForPackage(getPackageName());
    }
}
