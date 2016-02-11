package com.shapps.ytube;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.shapps.ytube.YouTube.ApiKey;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by shyam on 12/2/16.
 */
public class PlayerService extends Service{

    WindowManager windowManager;
    View view;
    static WebView player;
    String VID_ID = "RgKAFK5djSk";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onCreate() {

        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        LayoutInflater inflater = (LayoutInflater)this.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        view = inflater.inflate(R.layout.player_webview, null, false);

        final ImageView icon = (ImageView) view.findViewById(R.id.song_icon);
        player = (WebView) view.findViewById(R.id.playerView);
        player.getSettings().setJavaScriptEnabled(true);
        player.setWebChromeClient(new WebChromeClient());
        player.getSettings().setMediaPlaybackRequiresUserGesture(false);
        player.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 6.2; Win64; x64; rv:21.0.0) Gecko/20121011 Firefox/21.0.0");

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 0;

        windowManager.addView(view, params);

        icon.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;

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
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(view, params);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        Log.e("Service ", "Started!");

        Bundle b = intent.getExtras();

        if(b !=null) {
            VID_ID = b.getString("VID_ID");
        }
        Map hashMap = new HashMap();
        hashMap.put("Referer", "http://www.youtube.com");
        player.loadUrl("https://www.youtube.com/embed/" + VID_ID, hashMap);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Trying to Destroy", "Destroyed!");
        if (view != null) {
            player.destroy();
            player = null;
            windowManager.removeView(view);
        }
    }

    public static void startVid(String vId) {
        Map hashMap = new HashMap();
        hashMap.put("Referer", "http://www.youtube.com");
        player.loadUrl("https://www.youtube.com/embed/" + vId);
    }
}
