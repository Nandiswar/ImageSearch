package com.nandi.imagesearch.service;

import com.octo.android.robospice.retrofit.RetrofitGsonSpiceService;

/**
 * Created by nandi_000 on 08-11-2015.
 */
public class CustomService extends RetrofitGsonSpiceService {

    private final static String BASE_URL = "https://api.imgur.com/3";

    @Override
    protected String getServerUrl() {
        return BASE_URL;
    }
}
