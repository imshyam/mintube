package com.shapps.ytube;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;

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

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        if(intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_WEB_ACTION)) {
            Log.e("Service ", "Started!");

            Bundle b = intent.getExtras();

            if (b != null) {
                VID_ID = b.getString("VID_ID");
            }

            //Notification
            RemoteViews viewBig = new RemoteViews(
                    this.getPackageName(),
                    R.layout.notification_large
            );

            RemoteViews viewSmall = new RemoteViews(
                    this.getPackageName(),
                    R.layout.notification_small
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)

                    .setSmallIcon(R.drawable.thumbnail)

                    .setVisibility(Notification.VISIBILITY_PUBLIC)

                    .setContent(viewSmall)


                            // Automatically dismiss the notification when it is touched.
                    .setAutoCancel(false);

            Notification notification = builder.build();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                notification.bigContentView = viewBig;
            }

            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);

            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

            LayoutInflater inflater = (LayoutInflater) this.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);

            view = inflater.inflate(R.layout.player_webview, null, false);

            final ImageView icon = (ImageView) view.findViewById(R.id.song_icon);
            //        icon.setImageResource(R.drawable.circle);
            //        int len = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
            //        icon.setLayoutParams(new ViewGroup.LayoutParams(
            //                len,len));

            player = (WebView) view.findViewById(R.id.playerView);
            player.getSettings().setJavaScriptEnabled(true);
            player.setWebChromeClient(new WebChromeClient());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                player.getSettings().setMediaPlaybackRequiresUserGesture(false);
            }
            player.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 6.2; Win64; x64; rv:21.0.0) Gecko/20121011 Firefox/21.0.0");

            Map hashMap = new HashMap();
            hashMap.put("Referer", "http://www.youtube.com");
            player.loadUrl("https://www.youtube.com/embed/" + VID_ID + "?fs=0&autoplay=1&playlist=" + VID_ID
                    , hashMap);

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
        player.loadUrl("https://www.youtube.com/embed/" + vId+ "?fs=0&autoplay=1&playlist=" + vId
                , hashMap);
    }
}
