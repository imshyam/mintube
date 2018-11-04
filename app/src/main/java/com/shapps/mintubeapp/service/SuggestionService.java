package com.shapps.mintubeapp.service;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SuggestionService {

    @GET("search")
    Call<String> getSuggestions(@Query("client") String client,
                                @Query("client") String clientType,
                                @Query("ds") String ds,
                                @Query("q") String query
    );
}
