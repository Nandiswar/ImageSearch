package com.nandi.imagesearch.network;

import com.nandi.imagesearch.model.DataList;

import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by nandi_000 on 08-11-2015.
 * api to search the gallery using @param searchText
 * pagination using {page}
 * 
 */
public interface Api {

    @Headers("Authorization: Client-ID a59d1c4a4689d02")
    @GET("/gallery/search/{page}/")
    DataList contributors(@Path("page") int page, @Query("q") String searchText);
}
