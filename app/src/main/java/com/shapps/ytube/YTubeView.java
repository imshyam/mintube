package com.shapps.ytube;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.shapps.ytube.YouTube.ApiKey;
import com.shapps.ytube.YouTube.YouTubeFailureRecoveryActivity;

public class YTubeView extends YouTubeFailureRecoveryActivity {

    YouTubePlayerView youTubeView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        youTubeView = new YouTubePlayerView(this);

        Session.setYouTubePlayerView(youTubeView);

        startActivity(new Intent(this, MainActivity.class));

        finish();

    }



    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean wasRestored) {

    }


    @Override
    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return youTubeView;
    }
}
