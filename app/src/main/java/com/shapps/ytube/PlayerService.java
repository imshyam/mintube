package com.shapps.ytube;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RemoteViews;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by shyam on 12/2/16.
 */
public class PlayerService extends Service{

    static PlayerService playerService;
    WindowManager windowManager;
    View view;
    static WebView player;
    String VID_ID = "RgKAFK5djSk";
    static String PlayerId = "not yet";
    static boolean foundPlayerId = false;
    static boolean isVideoPlaying = true;


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

        this.playerService = this;
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

            //Intent to do things
            Intent doThings = new Intent(this, PlayerService.class);

            //Notification
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

            //stop Service using doThings Intent
            viewSmall.setOnClickPendingIntent(R.id.stop_service,
                    PendingIntent.getService(getApplicationContext(), 0,
                    doThings.setAction(Constants.ACTION.STOPFOREGROUND_WEB_ACTION) , 0));

            //Pause Video using doThings Intent
            viewSmall.setOnClickPendingIntent(R.id.pause_play_video,
                    PendingIntent.getService(getApplicationContext(), 0,
                            doThings.setAction(Constants.ACTION.PAUSE_PLAY_ACTION) , 0));

            viewBig.setOnClickPendingIntent(R.id.pause_play_video,
                    PendingIntent.getService(getApplicationContext(), 0,
                            doThings.setAction(Constants.ACTION.PAUSE_PLAY_ACTION) , 0));

            //Start Foreground Service
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);

            //View
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

            LayoutInflater inflater = (LayoutInflater) this.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);

            view = inflater.inflate(R.layout.player_webview, null, false);

            final ImageView icon = (ImageView) view.findViewById(R.id.song_icon);

            player = (WebView) view.findViewById(R.id.playerView);
            player.getSettings().setJavaScriptEnabled(true);

            // For debugging using chrome on PC
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                player.setWebContentsDebuggingEnabled(true);
//            }
            player.setWebChromeClient(new WebChromeClient());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                player.getSettings().setMediaPlaybackRequiresUserGesture(false);
            }
            player.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 6.2; Win64; x64; rv:21.0.0) Gecko/20121011 Firefox/21.0.0");

            //----------------------------To get Player Id-------------------------------------------

            player.addJavascriptInterface(new GetHtmlInterface(this), "HtmlViewer");
            player.setWebViewClient(new WebViewClient() {
                                        @Override
                                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                            return true;
                                        }
                                        @Override
                                        public void onPageFinished(WebView view, String url) {
                                            player.loadUrl("javascript:window.HtmlViewer.showHTML" +
                                                    "('&lt;body&gt;'+document.getElementsByTagName('body')[0].innerHTML+'&lt;/body&gt;');");
                                        }
                                    }
            );

            foundPlayerId = GetHtmlInterface.foundPlayerId();
            if(foundPlayerId == true) {
                PlayerId = GetHtmlInterface.getPlayerId();
                Log.i("Yaiks!!!" , "Found Player Id.");
            }
            else{
                Log.i("Oops!!", "trying Again");
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("5 sec later : ", "HTML is ...");

                        player.loadUrl("javascript:window.HtmlViewer.showHTML" +
                                "('&lt;body&gt;'+document.getElementsByTagName('body')[0].innerHTML+'&lt;/body&gt;');");

                    }
                }, 5000);
            }
            //------------------------------Got Player Id--------------------------------------------------------
            Map hashMap = new HashMap();
            hashMap.put("Referer", "http://www.youtube.com");
            player.loadUrl("https://www.youtube.com/embed/" + VID_ID
                    + "?iv_load_policy=3&rel=0&modestbranding=1&fs=0&autoplay=1&playlist=" + VID_ID
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
        else if(intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_WEB_ACTION)){
            Log.i("Trying To Destroy ", "...");
            stopForeground(true);
            stopSelf();
            stopService(new Intent(this, PlayerService.class));
        }

        else if(intent.getAction().equals(Constants.ACTION.PAUSE_PLAY_ACTION)){
            if(isVideoPlaying) {
                Log.i("Trying To Pause Video ", "...");
                String jsPause = "var player = document.getElementById(\"" + GetHtmlInterface.getPlayerId() + "\");\n" +
                        "player.pauseVideo();";
                Log.i("JS ", jsPause);
                player.loadUrl("javascript:" + jsPause);
                isVideoPlaying = false;
            }
            else{
                Log.i("Trying To Play Video ", "...");
                String jsPause = "var player = document.getElementById(\"" + GetHtmlInterface.getPlayerId() + "\");\n" +
                        "player.playVideo();";
                Log.i("JS ", jsPause);
                player.loadUrl("javascript:" + jsPause);
                isVideoPlaying = true;
            }
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Status", "Destroyed!");
        if (view != null) {
            player.destroy();
            player = null;
            windowManager.removeView(view);
        }
    }

    public static void startVid(String vId) {

        isVideoPlaying = true;
        //----------------------------To get Player Id-------------------------------------------
        player.addJavascriptInterface(new GetHtmlInterface(playerService), "HtmlViewer");
        player.setWebViewClient(new WebViewClient() {
                                    @Override
                                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                        return true;
                                    }
                                    @Override
                                    public void onPageFinished(WebView view, String url) {
                                        player.loadUrl("javascript:window.HtmlViewer.showHTML" +
                                                "('&lt;html&gt;'+document.getElementsByTagName('body')[0].innerHTML+'&lt;/html&gt;');");
                                    }
                                }
        );
        foundPlayerId = GetHtmlInterface.foundPlayerId();
        if(foundPlayerId == true) {
            PlayerId = GetHtmlInterface.getPlayerId();
            Log.i("Yaiks!!!" , "Found Player Id.");
        }
        else{
            Log.i("Oops!!", "trying Again");
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.e("5 sec later : ", "HTML is ...");

                    player.addJavascriptInterface(new GetHtmlInterface(playerService), "HtmlViewer");
                    player.loadUrl("javascript:window.HtmlViewer.showHTML" +
                            "('&lt;body&gt;'+document.getElementsByTagName('body')[0].innerHTML+'&lt;/body&gt;');");

                }
            }, 5000);
        }
        //-----------------------------------Got Player Id--------------------------------------------------------------
        Map hashMap = new HashMap();
        hashMap.put("Referer", "http://www.youtube.com");
        player.loadUrl("https://www.youtube.com/embed/" + vId
                + "?iv_load_policy=3&rel=0&modestbranding=1&fs=0&autoplay=1&playlist=" + vId
                , hashMap);
    }
}
