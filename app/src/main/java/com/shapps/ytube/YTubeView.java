package com.shapps.ytube;

import android.app.ActivityManager;
import android.app.Notification;
import android.content.Context;
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
import com.shapps.ytube.YouTube.PlayingNotification;
import com.shapps.ytube.YouTube.YouTubeFailureRecoveryActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YTubeView extends YouTubeFailureRecoveryActivity {

    YouTubePlayerView youTubeView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        youTubeView = new YouTubePlayerView(this);

        Session.setYouTubePlayerView(youTubeView);


        final Intent intent = getIntent();
        if(intent.getData() != null || intent.getStringExtra("android.intent.extra.TEXT") != null) {
            String link;
            if (intent.getData() != null) {
                link = intent.getData().toString();
            } else {
                link = intent.getStringExtra("android.intent.extra.TEXT");
            }
            Log.e("Link : ", link);
            String vId = "RgKAFK5djSk";
            Pattern pattern = Pattern.compile(
                    "^https?://.*(?:youtu.be/|v/|u/\\\\w/|embed/|watch[?]v=)([^#&?]*).*$",
                    Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(link.toString());
            if (matcher.matches()) {
                vId = matcher.group(1);
            }
            Log.e("Video Id : ", vId);
            if (isServiceRunning(PlayerService.class)) {
                Log.e("Service : ", "Already Running!");
                PlayerService.startVid(vId);
                finish();
            } else {
                Intent i = new Intent(this, PlayerService.class);
                i.putExtra("VID_ID", vId);
                startService(i);
                PlayingNotification.notify(this, "string", 2);
                finish();
            }
        }
        else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

    }

    private boolean isServiceRunning(Class<PlayerService> playerServiceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (playerServiceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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
