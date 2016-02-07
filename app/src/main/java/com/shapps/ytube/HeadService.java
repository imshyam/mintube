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
    static YouTubePlayerView playerView;
    View view;
    RelativeLayout rl, rl1;
    boolean visible = true;
    String VID_ID = "RgKAFK5djSk";

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

//        icon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.e("Clicked", "Click!");
//                if(visible) {
//                    rl1.setVisibility(View.GONE);
//                }
//                else {
//                    rl1.setVisibility(View.VISIBLE);
//                }
//            }
//        });
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
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Trying to Destroy", "Destroyed!");
        if (playerView != null) {
            windowManager.removeView(view);
            rl.removeView(playerView);
        }
    }


    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean wasRestored) {
        if (!wasRestored) {
            player.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL);
            player.cueVideo(VID_ID);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }
}
