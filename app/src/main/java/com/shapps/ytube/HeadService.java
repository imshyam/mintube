package com.shapps.ytube;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubePlayerView;
import com.shapps.ytube.YouTube.ApiKey;
import com.shapps.ytube.YouTube.YouTubeFailureRecoveryActivity;
import com.shapps.ytube.YouTube.YoutubePlayer;


/**
 * Created by shyam on 7/1/16.
 */
public class HeadService extends Service implements YouTubePlayer.OnInitializedListener{

    WindowManager windowManager;
    private static YouTubePlayer player;
    static YouTubePlayerView playerView;
    View view;
    RelativeLayout rl, rl1;
    boolean initialized = false;
    String VID_ID = "RgKAFK5djSk";
    private MyPlayerStateChangeListener playerStateChangeListener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {

        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        LayoutInflater inflater = (LayoutInflater)this.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        view = inflater.inflate(R.layout.player_view, null, false);

        rl = (RelativeLayout) view.findViewById(R.id.playerView);
        final ImageView icon = (ImageView) view.findViewById(R.id.song_icon);
        rl1 = (RelativeLayout) view.findViewById(R.id.view_to_hide);


        playerView = Session.getYouTubePlayerView();
        playerStateChangeListener = new MyPlayerStateChangeListener();
        playerView.initialize(ApiKey.API_KEY, this);
        rl.addView(playerView);


        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 500;

        windowManager.addView(view, params);

//         icon.setOnClickListener(new View.OnClickListener() {
//             @Override
//             public void onClick(View v) {
//                 Log.e("Clicked", "Click!");
//                 if(visible) {
//                     rl1.setVisibility(View.GONE);
//                 }
//                 else {
//                     rl1.setVisibility(View.VISIBLE);
//                 }
//             }
//         });
        icon.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY, finalTouchX, finalTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
//                        finalTouchX = event.getRawX();
//                        finalTouchY = event.getRawY();
//                        if(isClicked(initialTouchX, finalTouchX, initialTouchY, finalTouchY)){
//                            icon.performClick();
//                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(view, params);
                        return true;
                }
                return false;
            }
//            private boolean isClicked(float startX, float endX, float startY, float endY) {
//                float differenceX = Math.abs(startX - endX);
//                float differenceY = Math.abs(startY - endY);
//                if (differenceX >= 5 || differenceY >= 5) {
//                    return false;
//                }
//                return true;
//            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        Log.e("Service ", "Started!");

        Bundle b = intent.getExtras();

        if(b !=null) {
            VID_ID = b.getString("VID_ID");
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Trying to Destroy", "Destroyed!");
        if (playerView != null) {
            windowManager.removeView(view);
            rl.removeView(playerView);
            player.release();
        }
    }


    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean wasRestored) {
        if (!wasRestored) {
            initialized = true;
            log("Player Initialized.");
            this.player = player;
            player.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL);
            player.setPlayerStateChangeListener(playerStateChangeListener);
            player.loadVideo(VID_ID);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }


    public static void startVid(String vId) {
        if (player != null) {
            try {
                player.loadVideo(vId);
            } catch (IllegalStateException e) {
                Log.e("exception is : ", e.toString());
            }
        }
        else
            Log.e("exception is : ", "Null player");
    }

    private final class MyPlayerStateChangeListener implements YouTubePlayer.PlayerStateChangeListener {
        String playerState = "UNINITIALIZED";

        @Override
        public void onLoading() {
            playerState = "LOADING";
            log(playerState);
        }

        @Override
        public void onLoaded(String videoId) {
            playerState = String.format("LOADED %s", videoId);
            log(playerState);
        }

        @Override
        public void onAdStarted() {
            playerState = "AD_STARTED";
            log(playerState);
        }

        @Override
        public void onVideoStarted() {
            playerState = "VIDEO_STARTED";
            log(playerState);
        }

        @Override
        public void onVideoEnded() {
            playerState = "VIDEO_ENDED";
            log(playerState);
        }

        @Override
        public void onError(YouTubePlayer.ErrorReason reason) {
            playerState = "ERROR (" + reason + ")";
            if (reason == YouTubePlayer.ErrorReason.UNEXPECTED_SERVICE_DISCONNECTION) {
                // When this error occurs the player is released and can no longer be used.
                player = null;
            }
            log(playerState);
        }

    }

    private void log(String playerState) {
        Log.e("Player State : ", playerState);
    }
}
