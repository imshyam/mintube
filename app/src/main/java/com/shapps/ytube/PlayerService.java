package com.shapps.ytube;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    static Bitmap bitmap;
    static PlayerService playerService;
    WindowManager windowManager;
    LinearLayout player_view, serviceHead, serviceClose, serviceCloseBackground;
    RelativeLayout viewToHide;
    static WebView player;
    static String VID_ID = "";
    String PLIST_ID = "";
    static boolean isVideoPlaying = true;
    boolean visible = true;
    static RemoteViews viewBig;
    static RemoteViews viewSmall;
    static NotificationManager notificationManager;
    static Notification notification;
    static ImageView playerHeadImage;
    int scrnWidth, scrnHeight, playerWidth, playerHeight, playerHeadSize, xAtHiding, yAtHiding;
    int playerHeadCenterX, playerHeadCenterY, closeMinX, closeMinY, closeMaxX, closeMaxY;

    //Next Video to check whether next video is played or not
    static boolean nextVid = false;
    //Replay Video if it's ended
    static boolean replayVid = false;

    public static void setPlayingStatus(int playingStatus) {
        if(playingStatus == -1){
            nextVid = true;
        }
        if(playingStatus == 1){
            isVideoPlaying = true;
            viewBig.setImageViewResource(R.id.pause_play_video, R.drawable.ic_pause);
            viewSmall.setImageViewResource(R.id.pause_play_video, R.drawable.ic_pause);
            if(nextVid){
                nextVid = false;
                player.loadUrl(JavaScript.getVidUpdateNotiContent());
            }
            if(VID_ID.length() < 1){
                player.loadUrl(JavaScript.getVidUpdateNotiContent());
            }
        }
        else if(playingStatus == 2) {
            isVideoPlaying = false;
            viewBig.setImageViewResource(R.id.pause_play_video, R.drawable.ic_play);
            viewSmall.setImageViewResource(R.id.pause_play_video, R.drawable.ic_play);
        }
        else if(playingStatus == 0) {
            if(Constants.linkType == 1) {
                player.loadUrl(JavaScript.nextVideo());
                nextVid = true;
            }
            else {
                replayVid = true;
                viewBig.setImageViewResource(R.id.pause_play_video, R.drawable.ic_replay);
                viewSmall.setImageViewResource(R.id.pause_play_video, R.drawable.ic_replay);
            }
        }
        notificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
//        if (drawable0 instanceof Animatable) {
//            ((Animatable) drawable0).start();
//        }
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
        } else if(intent.getAction().equals(Constants.ACTION.PAUSE_PLAY_ACTION)){
            if(isVideoPlaying) {
                if(replayVid){
                    Log.i("Trying to ", "Replay Video");
                    player.loadUrl(JavaScript.playVideoScript());
                    replayVid = false;
                }
                else {
                    Log.i("Trying to ", "Pause Video");
                    player.loadUrl(JavaScript.pauseVideoScript());
                }
            }
            else{
                Log.i("Trying to ", "Play Video");
                player.loadUrl(JavaScript.playVideoScript());
            }
        }
        else if(intent.getAction().equals(Constants.ACTION.NEXT_ACTION)){
            Log.e("Trying to ", "Play Next");
            player.loadUrl(JavaScript.nextVideo());
            nextVid = true;
        }
        else if(intent.getAction().equals(Constants.ACTION.PREV_ACTION)){
            Log.e("Trying to ", "Play Previous");
            player.loadUrl(JavaScript.prevVideo());
            nextVid = true;
        }

        return START_NOT_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        isVideoPlaying = true;
        Session.finishWeb();
        Log.i("Status", "Destroyed!");
        if (player_view != null) {
            player.destroy();
            player = null;
            windowManager.removeView(player_view);
            windowManager.removeView(serviceHead);
            windowManager.removeView(serviceClose);
        }
    }

    public static void startVid(String vId, String pId) {
        if(pId == null) {
            setImageTitleAuthor(vId);
            player.loadUrl(JavaScript.loadVideoScript(vId));
        }
        else{
            player.loadUrl(JavaScript.loadPlaylistScript(pId));
            Log.e("Starting ", "Playlist.");
            setImageTitleAuthor(vId);
        }
    }

    /////-----------------*****************----------------onStartCommand---------------*****************-----------
    private void doThis(Intent intent) {
        Bundle b = intent.getExtras();

        if (b != null) {
            VID_ID = b.getString("VID_ID");
            PLIST_ID = b.getString("PLAYLIST_ID");
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
        notificationManager = (NotificationManager) getSystemService(this.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)

                .setSmallIcon(R.drawable.thumbnail)

                .setVisibility(Notification.VISIBILITY_PUBLIC)

                .setContent(viewSmall)


                        // Automatically dismiss the notification when it is touched.
                .setAutoCancel(false);

        notification = builder.build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification.bigContentView = viewBig;
        }

        //Set Image and Headings
        setImageTitleAuthor(VID_ID);

        //stop Service using doThings Intent
        viewSmall.setOnClickPendingIntent(R.id.stop_service,
                PendingIntent.getService(getApplicationContext(), 0,
                        doThings.setAction(Constants.ACTION.STOPFOREGROUND_WEB_ACTION), 0));

        //Pause, Play Video using doThings Intent
        viewSmall.setOnClickPendingIntent(R.id.pause_play_video,
                PendingIntent.getService(getApplicationContext(), 0,
                        doThings.setAction(Constants.ACTION.PAUSE_PLAY_ACTION) , 0));

        viewBig.setOnClickPendingIntent(R.id.pause_play_video,
                PendingIntent.getService(getApplicationContext(), 0,
                        doThings.setAction(Constants.ACTION.PAUSE_PLAY_ACTION), 0));

        //Next Video using doThings Intent
        viewSmall.setOnClickPendingIntent(R.id.next_video,
                PendingIntent.getService(getApplicationContext(), 0,
                        doThings.setAction(Constants.ACTION.NEXT_ACTION) , 0));

        viewBig.setOnClickPendingIntent(R.id.next_video,
                PendingIntent.getService(getApplicationContext(), 0,
                        doThings.setAction(Constants.ACTION.NEXT_ACTION), 0));

        //Previous Video using doThings Intent
        viewBig.setOnClickPendingIntent(R.id.previous_video,
                PendingIntent.getService(getApplicationContext(), 0,
                        doThings.setAction(Constants.ACTION.PREV_ACTION) , 0));

        //Start Foreground Service
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);

        //View
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        //Service Head
        serviceHead = (LinearLayout) inflater.inflate(R.layout.service_head, null, false);
        playerHeadImage = (ImageView) serviceHead.findViewById(R.id.song_icon);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 0;
        windowManager.addView(serviceHead, params);

        //Player View
        player_view = (LinearLayout) inflater.inflate(R.layout.player_webview, null, false);
        viewToHide= (RelativeLayout) player_view.findViewById(R.id.view_to_hide);

        player = (WebView) player_view.findViewById(R.id.playerView);
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
                                        player.loadUrl(JavaScript.getHtmlScript());
                                    }
                                }
        );
        //------------------------------Got Player Id--------------------------------------------------------
        Map hashMap = new HashMap();
        hashMap.put("Referer", "http://www.youtube.com");
        if(Constants.linkType == 1) {
            Log.e("Starting ", "Playlist!!!");
            player.loadUrl("https://www.youtube.com/embed/"
                    + "?iv_load_policy=3&rel=0&modestbranding=1&fs=0&autoplay=1&list=" + PLIST_ID
                    , hashMap);
        }
        else {
            Log.e("Starting ", "Single Video!!!");
            player.loadUrl("https://www.youtube.com/embed/" + VID_ID
                    + "?iv_load_policy=3&rel=0&modestbranding=1&fs=0&autoplay=1"
                    , hashMap);
        }

        final WindowManager.LayoutParams param_player = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);

        param_player.gravity = Gravity.TOP | Gravity.LEFT;
        param_player.x = 0;
        param_player.y = playerHeadSize;
        windowManager.addView(player_view, param_player);

        //ChatHead Size
        ViewTreeObserver vto = serviceHead.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                serviceHead.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                playerHeadSize = serviceHead.getMeasuredHeight();
                Log.e("ChatHead Size", String.valueOf(playerHeadSize));
                param_player.y = playerHeadSize;
                windowManager.updateViewLayout(player_view, param_player);
            }
        });

        //Player Width and Height
        vto = player_view.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                player_view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                playerWidth = player_view.getMeasuredWidth();
                playerHeight = player_view.getMeasuredHeight();
                Log.e("Player W and H ", playerWidth + " " + playerHeight);
            }
        });

        //Chat Head Close
        serviceCloseBackground = (LinearLayout) inflater.inflate(R.layout.service_close_background, null, false);
        final WindowManager.LayoutParams param_close_back = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        param_close_back.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        serviceCloseBackground.setVisibility(View.GONE);
        windowManager.addView(serviceCloseBackground, param_close_back);

        serviceClose = (LinearLayout) inflater.inflate(R.layout.service_close, null, false);
        final WindowManager.LayoutParams param_close = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        param_close.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        serviceClose.setVisibility(View.GONE);
        windowManager.addView(serviceClose, param_close);

        //-----------------Handle Click-----------------------------
        playerHeadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Clicked", "Click!");
                if (visible) {
                    Log.e("Head x , y ", params.x + " " + params.y);
                    Log.e("Player x , y ", param_player.x + " " + param_player.y);
                    Log.e("Head Size", String.valueOf(playerHeadImage.getHeight()));
                    xAtHiding = params.x;
                    yAtHiding = params.y;

                    if (params.x > scrnWidth / 2) {
                        params.x = scrnWidth - playerHeadSize + playerHeadSize / 4;
                    } else {
                        params.x = -playerHeadSize / 4;
                    }
                    viewToHide.setVisibility(View.GONE);
                    windowManager.updateViewLayout(serviceHead, params);
                    visible = false;
                } else {
                    params.x = xAtHiding;
                    params.y = yAtHiding;
                    param_player.x = xAtHiding;
                    param_player.y = yAtHiding + playerHeadSize;
                    viewToHide.setVisibility(View.VISIBLE);
                    windowManager.updateViewLayout(player_view, param_player);
                    windowManager.updateViewLayout(serviceHead, params);
                    visible = true;
                }
            }
        });

        //getting Screen Width and Height
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        scrnWidth = size.x;
        scrnHeight = size.y;

        //-----------------Handle Touch-----------------------------
        playerHeadImage.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY, finalTouchX, finalTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                WindowManager.LayoutParams params = (WindowManager.LayoutParams) serviceHead.getLayoutParams();
                WindowManager.LayoutParams param_player = (WindowManager.LayoutParams) player_view.getLayoutParams();
                serviceCloseBackground.setVisibility(View.VISIBLE);
                Handler handleLongTouch = new Handler();
                Runnable setVisible = new Runnable() {
                    @Override
                    public void run() {
                        serviceClose.setVisibility(View.VISIBLE);
                    }
                };
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        handleLongTouch.postDelayed(setVisible, 500);
                        return true;
                    case MotionEvent.ACTION_UP:
                        finalTouchX = event.getRawX();
                        finalTouchY = event.getRawY();
                        handleLongTouch.removeCallbacksAndMessages(null);
                        serviceCloseBackground.setVisibility(View.GONE);
                        serviceClose.setVisibility(View.GONE);
                        if (isClicked(initialTouchX, finalTouchX, initialTouchY, finalTouchY)) {
                            playerHeadImage.performClick();
                        }
                        else {
                            playerHeadCenterX = params.x + playerHeadSize / 2;
                            playerHeadCenterY = params.y + playerHeadSize / 2;
                            //Player Width and Height
                            final RelativeLayout closeImage = (RelativeLayout) serviceClose.findViewById(R.id.close_image);
                            ViewTreeObserver vto = closeImage.getViewTreeObserver();
                            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                @Override
                                public void onGlobalLayout() {
                                    closeImage.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                                    int closeImageSize = closeImage.getMeasuredHeight();
                                    int [] t = new int[2];
                                    closeImage.getLocationOnScreen(t);
                                    closeMinX = t[0];
                                    closeMinY = t[1];
                                    closeMaxX = closeMinX + closeImageSize;
                                    closeMaxY = closeMinY + closeImageSize;
                                    Log.e("Close Image Size ", String.valueOf(closeImageSize));
                                    Log.e("X ", closeMinX + " " + playerHeadCenterX + " " + closeMaxX);
                                    Log.e("Y ", closeMinY + " " + playerHeadCenterY + " " + closeMaxY);
                                }
                            });
                            if(isInsideClose()){
                                Log.i("Inside Close ", "...");
                                stopForeground(true);
                                stopSelf();
                                stopService(new Intent(PlayerService.this, PlayerService.class));
                            }
                            else if (!visible) {
                                if (params.x > scrnWidth / 2) {
                                    params.x = scrnWidth - playerHeadSize + playerHeadSize / 4;
                                } else {
                                    params.x = -playerHeadSize / 4;
                                }
                                windowManager.updateViewLayout(serviceHead, params);
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        if (visible) {
                            if (params.x < 0) {
                                param_player.x = 0;
                                params.x = 0;
                            } else if (playerWidth + params.x > scrnWidth) {
                                param_player.x = scrnWidth - playerWidth;
                                params.x = scrnWidth - playerWidth;
                            } else {
                                param_player.x = params.x;
                            }
                            if (params.y < 0) {
                                param_player.y = playerHeadSize;
                                params.y = 0;
                            } else if (playerHeight + params.y + playerHeadSize > scrnHeight) {
                                param_player.y = scrnHeight - playerHeight;
                                params.y = scrnHeight - playerHeight - playerHeadSize;
                            } else {
                                param_player.y = params.y + playerHeadSize;
                            }
                            windowManager.updateViewLayout(serviceHead, params);
                            windowManager.updateViewLayout(player_view, param_player);
                        }
                        else {
                            windowManager.updateViewLayout(serviceHead, params);
                        }
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

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    //Set Image and Headings
    public static void setImageTitleAuthor(String videoId) {

        Log.e("Setting ", "Image, Title, Author");

        try {
            bitmap = new ImageLoadTask("https://i.ytimg.com/vi/" + videoId + "/mqdefault.jpg").execute().get();
            String details = new LoadDetailsTask(
                    "https://www.youtube.com/oembed?url=http://www.youtu.be/watch?v=" + videoId + "&format=json")
                    .execute().get();
            JSONObject detailsJson = new JSONObject(details);
            String title = detailsJson.getString("title");
            String author = detailsJson.getString("author_name");

            viewBig.setImageViewBitmap(R.id.thumbnail, bitmap);
            viewSmall.setImageViewBitmap(R.id.thumbnail, bitmap);
//            playerHeadImage.setImageBitmap(bitmap);

            viewBig.setTextViewText(R.id.title, title);

            viewBig.setTextViewText(R.id.author, author);
            viewSmall.setTextViewText(R.id.author, author);

            notificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);

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

    public static void tryAgainForPlayerID() {
        Log.e("Trying Again : ", ":(");
        player.loadUrl(JavaScript.getHtmlScript());
    }

    public static void InitializePlayer() {
        Log.e("Initializing ", Session.getPlayerId());
        player.loadUrl(JavaScript.initializePlayerScript(Session.getPlayerId()));
    }

    public boolean isInsideClose() {
        if(playerHeadCenterX >= closeMinX && playerHeadCenterX <= closeMaxX){
            if(playerHeadCenterY >= closeMinY && playerHeadCenterY <= closeMaxY){
                return true;
            }
        }
        return false;
    }
}
