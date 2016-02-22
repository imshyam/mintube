package com.shapps.ytube;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.RelativeLayout;
import android.widget.RemoteViews;

import com.shapps.ytube.AsyncTask.ImageLoadTask;
import com.shapps.ytube.AsyncTask.LoadDetailsTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
    static boolean notInitialized = true;
    boolean visible = true;
    static RemoteViews viewBig;
    static RemoteViews viewSmall;

    //if play initializeWith = 1
    //if pause initializeWith = 2
    //if loadVideo initializeWith = 3

    public static void setPlayingStatus(int playingStatus) {
        if(playingStatus == 1){
            isVideoPlaying = true;
        }
        else if(playingStatus == 2) {
            isVideoPlaying = false;
        }
    }


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

            doThis(intent);

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
                if(notInitialized) {
                    initializePlayer(2, null);
                }
                else {
                    player.loadUrl("javascript:" + JavaScript.pauseVideoScript());
                }
            }
            else{
                Log.i("Trying To Play Video ", "...");
                if(notInitialized) {
                    initializePlayer(1, null);
                }
                else {
                    player.loadUrl("javascript:" + JavaScript.playVideoScript());
                }
            }
        }

        return START_NOT_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        foundPlayerId = false;
        notInitialized = true;
        Session.finishWeb();
        Log.i("Status", "Destroyed!");
        if (view != null) {
            player.destroy();
            player = null;
            windowManager.removeView(view);
        }
    }

    public static void startVid(String vId) {
        if(notInitialized) {
            initializePlayer(3, vId);
            setImageTitleAuthor(vId);
        }
        else {
            player.loadUrl("javascript:" + JavaScript.loadPlayerScript(vId));
            setImageTitleAuthor(vId);
        }
    }

    private static void initializePlayer(int type, String vId) {
        if(type == 1) {
            if (foundPlayerId) {
                Log.e("Player ", "Initialized");
                player.loadUrl("javascript:" + JavaScript.initializePlayerScript(PlayerId) +
                        JavaScript.playVideoScript());
                notInitialized = false;
            }
        }
        if(type == 2) {
            if (foundPlayerId) {
                Log.e("Player ", "Initialized");
                player.loadUrl("javascript:" + JavaScript.initializePlayerScript(PlayerId) +
                        JavaScript.pauseVideoScript());
                notInitialized = false;
            }
        }
        if(type == 3) {
            if (foundPlayerId) {
                Log.e("Player ", "Initialized");
                player.loadUrl("javascript:" + JavaScript.initializePlayerScript(PlayerId) +
                        JavaScript.loadPlayerScript(vId));
                notInitialized = false;
            }
        }
    }

    /////-----------------*****************----------------onStartCommand---------------*****************-----------
    private void doThis(Intent intent) {
        Bundle b = intent.getExtras();

        if (b != null) {
            VID_ID = b.getString("VID_ID");
        }

        //Notification
        viewBig = new RemoteViews(
                this.getPackageName(),
                R.layout.notification_large
        );

        viewSmall = new RemoteViews(
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

        //Set Image and Headings
        setImageTitleAuthor(VID_ID);

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

//         For debugging using chrome on PC
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
                                        player.loadUrl("javascript:" + JavaScript.getHtmlScript());
                                    }
                                }
        );

        foundPlayerId = Session.foundPlayerId();
        if(foundPlayerId == true) {
            PlayerId = Session.getPlayerId();
            Log.i("Yaiks!!!" , "Found Player Id.");
        }
        else{
            Log.i("Oops!!", "trying Again");
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.e("5 sec later : ", "HTML is ...");

                    player.loadUrl("javascript:"+JavaScript.getHtmlScript());

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

        final RelativeLayout rl1 = (RelativeLayout) view.findViewById(R.id.view_to_hide);
        final RelativeLayout rl2 = (RelativeLayout) view.findViewById(R.id.null_to_hide);

        icon.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Log.e("Clicked", "Click!");
                 if(visible) {
                     rl1.setVisibility(View.GONE);
                     rl2.setVisibility(View.GONE);
                     visible = false;
                 }
                 else {
                     rl1.setVisibility(View.VISIBLE);
                     rl2.setVisibility(View.VISIBLE);
                     visible = true;
                 }
             }
         });

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
                        finalTouchX = event.getRawX();
                        finalTouchY = event.getRawY();
                        if(isClicked(initialTouchX, finalTouchX, initialTouchY, finalTouchY)){
                            icon.performClick();
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(view, params);
                        return true;
                }
                return false;
            }

            private boolean isClicked(float startX, float endX, float startY, float endY) {
                float differenceX = Math.abs(startX - endX);
                float differenceY = Math.abs(startY - endY);
                if (differenceX >= 5 || differenceY >= 5) {
                    return false;
                }
                return true;
            }
        });
    }

    public static void foundPlayerId() {
        foundPlayerId = true;
        PlayerId = Session.getPlayerId();
        Log.e("Player Id ", "Found!!!" + PlayerId);
    }

    //Set Image and Headings
    public static void setImageTitleAuthor(String imageTitleAuthor) {

        try {
            Bitmap bitmap = new ImageLoadTask("https://i.ytimg.com/vi/" + imageTitleAuthor + "/mqdefault.jpg").execute().get();
            String details = new LoadDetailsTask(
                    "https://www.youtube.com/oembed?url=http://www.youtu.be/watch?v=" + imageTitleAuthor + "&format=json")
                    .execute().get();
            JSONObject detailsJson = new JSONObject(details);
            String title = detailsJson.getString("title");
            String author = detailsJson.getString("author_name");

            viewBig.setImageViewBitmap(R.id.thumbnail, bitmap);
            viewSmall.setImageViewBitmap(R.id.thumbnail, bitmap);

            viewBig.setTextViewText(R.id.title, title);

            viewBig.setTextViewText(R.id.author, author);
            viewSmall.setTextViewText(R.id.author, author);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
