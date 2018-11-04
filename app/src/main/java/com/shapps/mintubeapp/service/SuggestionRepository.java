package com.shapps.mintubeapp.service;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import com.shapps.mintubeapp.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class SuggestionRepository {

    private String TAG = SuggestionRepository.class.getName();
    private SuggestionService suggestionService;
    private static SuggestionRepository suggestionRepository;

    SuggestionRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.SUGGESTIONS_BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        suggestionService = retrofit.create(SuggestionService.class);
    }

    public static SuggestionRepository getInstance() {
        if (suggestionRepository == null) {
            suggestionRepository = new SuggestionRepository();
        }
        return suggestionRepository;
    }

    public LiveData<String> getSuggestions(String query) {
        final MutableLiveData<String> stringMutableLiveData = new MutableLiveData<>();

        suggestionService.getSuggestions(Constants.SUGGESTIONS_CLIENT, Constants.SUGGESTIONS_CLIENT_RETURN_TYPE,
                Constants.SUGGESTIONS_DS, query).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.body() != null) {
                    stringMutableLiveData.setValue(response.body());
                } else {
                    Log.d(TAG, "Empty Response");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d(TAG, "Suggestions response failure.");
            }
        });
        return stringMutableLiveData;
    }
}
