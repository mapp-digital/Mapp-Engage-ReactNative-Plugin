package com.reactlibrary;

import android.app.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;



import com.appoxee.Appoxee;

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
        startActivity(launchIntent);
        Appoxee.handleRichPush(Appoxee.instance().getLastActivity(),intent);
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Appoxee.handleRichPush(this, intent);
    }

    private Intent getDefaultActivityIntent() {
        PackageManager packageManager = getPackageManager();
        return packageManager.getLaunchIntentForPackage(getPackageName());
    }
}
