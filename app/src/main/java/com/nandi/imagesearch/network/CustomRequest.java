package com.nandi.imagesearch.network;

import com.nandi.imagesearch.model.DataList;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import roboguice.util.temp.Ln;

/**
 * Created by nandi_000 on 08-11-2015.
 */
public class CustomRequest extends RetrofitSpiceRequest<DataList, Api> {

    private int page;
    private String query;

    public CustomRequest(int page, String query) {
        super(DataList.class, Api.class);
        this.page = page;
        this.query = query;
    }

    @Override
    public DataList loadDataFromNetwork() throws Exception {
        Ln.d("Call web service");
        return getService().contributors(page, query);
    }
}
