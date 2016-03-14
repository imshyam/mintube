package com.shapps.ytube;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.shapps.ytube.YouTube.ApiKey;
import com.shapps.ytube.YouTube.YouTubeFailureRecoveryActivity;

/**
 * Created by shyam on 13/3/16.
 */
public class YouTubeFullScreenActivity extends YouTubeFailureRecoveryActivity{

    static YouTubeFullScreenActivity youTubeFullScreenActivity;
    YouTubePlayerView playerView;
    private YouTubePlayer YTPlayer;
    String VIDEO_ID, PLIST_ID;
    int startAt;
    private FullScreenExitListener fullScreenExitListener;
    static boolean active = false;

    public static YouTubeFullScreenActivity getInstance() {
        return youTubeFullScreenActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        active = true;
        youTubeFullScreenActivity = this;

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_youtube_fullscreen);

        Log.e("Here We", "Are");

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if(Constants.linkType == 0) {
                VIDEO_ID = bundle.getString("VID");
                startAt = bundle.getInt("START_AT");
            }
            else{
                PLIST_ID = bundle.getString("PID");
            }
        }

        playerView = (YouTubePlayerView) findViewById(R.id.fullscreen_player);
        playerView.initialize(ApiKey.API_KEY, this);

        fullScreenExitListener = new FullScreenExitListener();

    }

    @Override
    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return null;
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
        this.YTPlayer = player;
        if (!wasRestored) {
            player.setFullscreen(true);
            player.cueVideo(VIDEO_ID);
            player.seekRelativeMillis(startAt * 1000);
            player.setOnFullscreenListener(fullScreenExitListener);
            player.play();
        }
    }
    

    private final class FullScreenExitListener implements YouTubePlayer.OnFullscreenListener {

        @Override
        public void onFullscreen(boolean b) {
            if(!b){
                onBackPressed();
            }
        }
    }
    @Override
    public void onBackPressed() {
        if(YTPlayer != null) {
            Log.e("Finishing ", "Time : " + YTPlayer.getCurrentTimeMillis());
            active = false;
            PlayerService.startAgainAt(YTPlayer.getCurrentTimeMillis());
        }
        super.onBackPressed();
    }

}
