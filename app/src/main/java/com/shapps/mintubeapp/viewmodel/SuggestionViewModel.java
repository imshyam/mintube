package com.shapps.mintubeapp.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.shapps.mintubeapp.service.SuggestionRepository;

public class SuggestionViewModel extends ViewModel {

    private String query;

    public LiveData<String> getLiveData() {
        return SuggestionRepository.getInstance().getSuggestions(query);
    }

    public void setQuery(String query) {
        this.query = query;
        getLiveData();
    }
}
