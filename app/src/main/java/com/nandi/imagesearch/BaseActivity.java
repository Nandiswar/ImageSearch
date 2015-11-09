package com.nandi.imagesearch;

import android.support.v7.app.AppCompatActivity;

import com.nandi.imagesearch.service.CustomService;
import com.octo.android.robospice.SpiceManager;
/*
 * Initializing SpiceManager with custom SpiceService
 * starting and stopping the service in activity lifecyle methods
 */
public class BaseActivity extends AppCompatActivity {

    SpiceManager spiceManager = new SpiceManager(CustomService.class);

    @Override
    protected void onStart() {
        spiceManager.start(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        spiceManager.shouldStop();
        super.onStop();
    }

    public SpiceManager getSpiceManager() {
        return spiceManager;
    }
}
