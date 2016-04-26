package com.shapps.mintubeapp;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;

import com.shapps.mintubeapp.AsyncTask.ImageLoadTask;
import com.shapps.mintubeapp.AsyncTask.LoadDetailsTask;
import com.shapps.mintubeapp.CustomViews.CircularImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by shyam on 12/2/16.
 */
public class PlayerService extends Service implements View.OnClickListener{

    static Context mContext;
    static Bitmap bitmap;
    static String title, author;
    static PlayerService playerService;
    static WindowManager windowManager;
    static LinearLayout serviceHead, serviceClose, serviceCloseBackground, playerView, webPlayerLL;
    FrameLayout webPlayerFrame;
    static  WindowManager.LayoutParams servHeadParams, servCloseParams, servCloseBackParams, playerViewParams;
    WindowManager.LayoutParams param_player, params, param_close, param_close_back, parWebView;
    RelativeLayout viewToHide, closeImageLayout;
    static WebPlayer webPlayer;
    static String VID_ID = "";
    static String PLIST_ID = "";
    static boolean isVideoPlaying = true;
    boolean visible = true;
    static RemoteViews viewBig;
    static RemoteViews viewSmall;
    static NotificationManager notificationManager;
    static Notification notification;
    static ImageView playerHeadImage;
    int playerHeadCenterX, playerHeadCenterY, closeMinX, closeMinY, closeMaxX, closeImgSize;
    int scrnWidth, scrnHeight, defaultPlayerWidth,playerWidth, playerHeight, playerHeadSize, closeImageLayoutSize, xAtHiding, yAtHiding, xOnAppear, yOnAppear = 0;

    static Intent fullScreenIntent;

    //is inside the close button so to stop video
    boolean isInsideClose = false;
    //is width entire to show video properly
    boolean isEntireWidth = false;
    //Next Video to check whether next video is played or not
    static boolean nextVid = false;
    //Replay Video if it's ended
    static boolean replayVid = false;
    static boolean replayPlaylist = false;

    ImageView repeatTypeImg, entireWidthImg, fullScreenImg;
    SharedPreferences sharedPref;
    private static int noItemsInPlaylist, currVideoIndex;

    //Loop for Playlist
    static boolean isLoopSetPlayList = false;
    //Don't Update head's Position and Hide icons and dropDown Image
    boolean updateHead = true;

    public static void setPlayingStatus(int playingStatus) {
        if(playingStatus == -1){
            nextVid = true;
        }
        if(playingStatus == 3){
            Log.d("Status", "Buffering");
            String quality = Constants.getPlaybackQuality();
            Log.d("Quality", quality);
            webPlayer.loadScript(JavaScript.resetPlaybackQuality(quality));
        }
        if(playingStatus == 1){
            isVideoPlaying = true;
            viewBig.setImageViewResource(R.id.pause_play_video, R.drawable.ic_pause);
            viewSmall.setImageViewResource(R.id.pause_play_video, R.drawable.ic_pause);
            notificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
            if(nextVid){
                nextVid = false;
                webPlayer.loadScript(JavaScript.getVidUpdateNotiContent());
            }
            if(VID_ID.length() < 1){
                Log.d("If lenght", "Less that 1");
                webPlayer.loadScript(JavaScript.getVidUpdateNotiContent());
            }

            //Also Update if playlist is set for loop
            if(Constants.linkType == 1){
                if(Constants.repeatType == 1){
                    if(!isLoopSetPlayList) {
                        Log.d("Setting ", "Playlist on Loop");
                        webPlayer.loadScript(JavaScript.setLoopPlaylist());
                        isLoopSetPlayList = true;
                    }
                }
            }
        }
        else if(playingStatus == 2) {
            isVideoPlaying = false;
            viewBig.setImageViewResource(R.id.pause_play_video, R.drawable.ic_play);
            viewSmall.setImageViewResource(R.id.pause_play_video, R.drawable.ic_play);
            notificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
        }
        else if(playingStatus == 0) {
            if(Constants.linkType == 1) {
                Log.d("Repeat Type ", Constants.repeatType + "");
                if(Constants.repeatType == 2){
                    webPlayer.loadScript(JavaScript.prevVideo());
                }
                //If not repeating then set notification icon to repeat when playlist ends
                if(Constants.repeatType == 0){
                    isPlaylistEnded();
                }
            }
            else {
                if(Constants.repeatType > 0){
                    webPlayer.loadScript(JavaScript.playVideoScript());
                }
                else {
                    if(Constants.finishOnEnd == true){
                        playerService.destroyServiceOnFinish();
                    }
                    else {
                        replayVid = true;
                        viewBig.setImageViewResource(R.id.pause_play_video, R.drawable.ic_replay);
                        viewSmall.setImageViewResource(R.id.pause_play_video, R.drawable.ic_replay);
                        notificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
                    }
                }
            }
        }
    }

    private void destroyServiceOnFinish() {
        Log.i("Trying To Destroy ", "...");
        stopForeground(true);
        stopSelf();
        stopService(new Intent(mContext, PlayerService.class));
    }

    public static void isPlaylistEnded() {
        webPlayer.loadScript(JavaScript.isPlaylistEnded());
    }

    public static void setNoItemsInPlaylist(int noItemsInPlaylist) {
        PlayerService.noItemsInPlaylist = noItemsInPlaylist;
    }

    public static void setCurrVideoIndex(int currVideoIndex) {
        PlayerService.currVideoIndex = currVideoIndex;
    }

    public static Context getAppContext(){
        return mContext;
    }

    public static void compare() {
        Log.d("Compairing", PlayerService.currVideoIndex + " " + PlayerService.noItemsInPlaylist);
        if(PlayerService.currVideoIndex == PlayerService.noItemsInPlaylist -1){
            Log.d("Playlist ", "Ended");
            replayPlaylist = true;
            viewBig.setImageViewResource(R.id.pause_play_video, R.drawable.ic_replay);
            viewSmall.setImageViewResource(R.id.pause_play_video, R.drawable.ic_replay);
            notificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
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

        mContext = this.getApplicationContext();
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        this.playerService = this;
        if(intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_WEB_ACTION)) {
            Log.d("Service ", "Started!");
            sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Constants.repeatType = sharedPref.getInt(getString(R.string.repeat_type), 0);
            doThis(intent);

        }
        else if(intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_WEB_ACTION)){
            Log.i("Trying To Destroy ", "...");
            stopForeground(true);
            stopSelf();
            stopService(new Intent(this, PlayerService.class));
        } else if(intent.getAction().equals(Constants.ACTION.PAUSE_PLAY_ACTION)){
            if (isVideoPlaying) {
                if (replayVid || replayPlaylist) {
                    if (Constants.linkType == 1) {
                        Log.i("Trying to ", "Replay Playlist");
                        webPlayer.loadScript(JavaScript.replayPlaylistScript());
                        replayPlaylist = false;
                    } else {
                        Log.i("Trying to ", "Replay Video");
                        webPlayer.loadScript(JavaScript.playVideoScript());
                        replayVid = false;
                    }
                } else {
                    Log.i("Trying to ", "Pause Video");
                    webPlayer.loadScript(JavaScript.pauseVideoScript());
                }
            } else {
                Log.i("Trying to ", "Play Video");
                webPlayer.loadScript(JavaScript.playVideoScript());
            }
        }
        else if(intent.getAction().equals(Constants.ACTION.NEXT_ACTION)){
            Log.d("Trying to ", "Play Next");
            if(Constants.linkType == 0){
                webPlayer.loadScript(JavaScript.seekToZero());
            }
            else {
                webPlayer.loadScript(JavaScript.nextVideo());
                nextVid = true;
            }
        }
        else if(intent.getAction().equals(Constants.ACTION.PREV_ACTION)){
            Log.d("Trying to ", "Play Previous");
            if(Constants.linkType == 0){
                webPlayer.loadScript(JavaScript.seekToZero());
            }
            else {
                webPlayer.loadScript(JavaScript.prevVideo());
                nextVid = true;
            }
        }

        return START_NOT_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        isVideoPlaying = true;
        Constants.linkType = 0;
        Log.i("Status", "Destroyed!");
        if (playerView != null) {
            if(FullscreenWebPlayer.active){
                FullscreenWebPlayer.fullScreenAct.onBackPressed();
            }
            windowManager.removeView(playerView);
            windowManager.removeView(serviceHead);
            windowManager.removeView(serviceClose);
            webPlayer.destroy();
        }
    }

    public static void startVid(String vId, String pId) {
        PlayerService.VID_ID = vId;
        PlayerService.PLIST_ID = pId;
        if(pId == null) {
            setImageTitleAuthor(vId);
            webPlayer.loadScript(JavaScript.loadVideoScript(vId));
        }
        else{
            webPlayer.loadScript(JavaScript.loadPlaylistScript(pId));
            Log.d("Starting ", "Playlist.");
            setImageTitleAuthor(vId);
        }
    }

    /////-----------------*****************----------------onStartCommand---------------*****************-----------
    private void doThis(Intent intent) {

        Bundle b = intent.getExtras();

        if (b != null) {
            PlayerService.VID_ID = b.getString("VID_ID");
            PlayerService.PLIST_ID = b.getString("PLAYLIST_ID");
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

                .setSmallIcon(R.drawable.ic_status_bar)

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

        viewBig.setOnClickPendingIntent(R.id.stop_service,
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
                        doThings.setAction(Constants.ACTION.PREV_ACTION), 0));

        //Start Foreground Service
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);

        //View
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        //Initialize Layout Parameters For All View
        InitParams();

        LayoutInflater inflater = (LayoutInflater) this.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        //Service Head
        serviceHead = (LinearLayout) inflater.inflate(R.layout.service_head, null, false);
        playerHeadImage = (ImageView) serviceHead.findViewById(R.id.song_icon);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 0;
        windowManager.addView(serviceHead, params);

        //Player View
        playerView = (LinearLayout) inflater.inflate(R.layout.player_webview, null, false);
        viewToHide = (RelativeLayout) playerView.findViewById(R.id.view_to_hide);
        webPlayerFrame = (FrameLayout) playerView.findViewById(R.id.web_player_frame);
        webPlayerLL = (LinearLayout) playerView.findViewById(R.id.web_player_ll);

        webPlayer = new WebPlayer(this);
        webPlayer.setupPlayer();

        viewToHide.addView(webPlayer.getPlayer(), parWebView);

        //------------------------------Got Player Id--------------------------------------------------------
        Map hashMap = new HashMap();
        hashMap.put("Referer", "http://www.youtube.com");
        if(Constants.linkType == 1) {
            Log.d("Starting ", "Playlist!!!");
            ConstantStrings.setPList(PLIST_ID);
            webPlayer.loadDataWithUrl("https://www.youtube.com/player_api",ConstantStrings.getPlayListHTML(),
                    "text/html", null, null);
        }
        else {
            ConstantStrings.setVid(VID_ID);
            Log.d("Starting ", "Single Video!!!");
            webPlayer.loadDataWithUrl("https://www.youtube.com/player_api",ConstantStrings.getVideoHTML(),
                    "text/html", null, null);
        }

        param_player.gravity = Gravity.TOP | Gravity.LEFT;
        param_player.x = 0;
        param_player.y = playerHeadSize;
        windowManager.addView(playerView, param_player);

        //ChatHead Size
        ViewTreeObserver vto = serviceHead.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                serviceHead.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                playerHeadSize = serviceHead.getMeasuredHeight();

                Log.d("ChatHead Size", String.valueOf(playerHeadSize));
                param_player.y = playerHeadSize;
                xOnAppear = - playerHeadSize / 4;
                windowManager.updateViewLayout(playerView, param_player);
            }
        });

        //Player Width and Height
        vto = playerView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                playerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                playerWidth = playerView.getMeasuredWidth();
                defaultPlayerWidth = playerWidth;
                playerHeight = playerView.getMeasuredHeight();
                Log.d("Player W and H ", playerWidth + " " + playerHeight);
            }
        });


        //Player Controls
        repeatTypeImg = (ImageView) playerView.findViewById(R.id.repeat_type);
        entireWidthImg = (ImageView) playerView.findViewById(R.id.entire_width);
        fullScreenImg = (ImageView) playerView.findViewById(R.id.fullscreen);

        //update Repeat Type Onclick
        updateRepeatTypeImage();
        repeatTypeImg.setOnClickListener(this);

        //Handle Entire Width

        entireWidthImg.setOnClickListener(this);

        //Handle Full Screen
        fullScreenImg.setOnClickListener(this);

        //Chat Head Close
        serviceCloseBackground = (LinearLayout) inflater.inflate(R.layout.service_close_background, null, false);

        param_close_back.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        serviceCloseBackground.setVisibility(View.GONE);
        windowManager.addView(serviceCloseBackground, param_close_back);

        serviceClose = (LinearLayout) inflater.inflate(R.layout.service_close, null, false);

        param_close.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        serviceClose.setVisibility(View.GONE);
        windowManager.addView(serviceClose, param_close);
        closeImageLayout = (RelativeLayout) serviceClose.findViewById(R.id.close_image_layout);
        vto = closeImageLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                closeImageLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                closeImageLayoutSize = closeImageLayout.getMeasuredHeight();
                Log.d("Close Image Size ", String.valueOf(closeImageLayoutSize));
            }
        });

        final CircularImageView closeImage = (CircularImageView) serviceClose.findViewById(R.id.close_image);

        //-----------------Handle Click-----------------------------
        playerHeadImage.setOnClickListener(this);

        //getting Screen Width and Height
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        scrnWidth = size.x;
        scrnHeight = size.y;

        //-----------------Handle Touch-----------------------------

        //if just a click no need to show the close button
        final boolean[] needToShow = {true};

        playerHeadImage.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY, finalTouchX, finalTouchY;

            @Override
            public boolean onTouch(View v, final MotionEvent event) {
                if(isEntireWidth) {
                    playerWidth = scrnWidth;
                }
                else{
                    playerWidth = defaultPlayerWidth;
                }
                final WindowManager.LayoutParams params = (WindowManager.LayoutParams) serviceHead.getLayoutParams();
                WindowManager.LayoutParams param_player = (WindowManager.LayoutParams) playerView.getLayoutParams();
                serviceCloseBackground.setVisibility(View.VISIBLE);
                final Handler handleLongTouch = new Handler();
                final Runnable setVisible = new Runnable() {
                    @Override
                    public void run() {
                        if(needToShow[0]) {
                            serviceClose.setVisibility(View.VISIBLE);
                        }
                    }
                };
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        needToShow[0] = true;
                        handleLongTouch.postDelayed(setVisible, 100);
                        closeImgSize = closeImage.getLayoutParams().width;
                        return true;
                    case MotionEvent.ACTION_UP:
                        finalTouchX = event.getRawX();
                        finalTouchY = event.getRawY();
                        needToShow[0] = false;
                        handleLongTouch.removeCallbacksAndMessages(null);
                        serviceCloseBackground.setVisibility(View.GONE);
                        serviceClose.setVisibility(View.GONE);
                        if (isClicked(initialTouchX, finalTouchX, initialTouchY, finalTouchY)) {
                            playerHeadImage.performClick();
                        }
                        else {
                            //stop if inside the close Button
                            if(isInsideClose){
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
                        int newX, newY;
                        newX = initialX + (int) (event.getRawX() - initialTouchX);
                        newY = initialY + (int) (event.getRawY() - initialTouchY);
                        if (visible) {
                            if (newX < 0) {
                                param_player.x = 0;
                                params.x = 0;
                            } else if (playerWidth + newX > scrnWidth) {
                                param_player.x = scrnWidth - playerWidth;
                                params.x = scrnWidth - playerWidth;
                            }
                            else {
                                param_player.x = newX;
                                params.x = newX;
                            }
                            if (newY < 0) {
                                param_player.y = playerHeadSize;
                                params.y = 0;
                            } else if (playerHeight + newY + playerHeadSize > scrnHeight) {
                                if(visible){
                                    //Continue with the drag and don't update head params
                                    updateHead = false;
                                    hidePlayer();
                                }
                                params.y = newY;
                            } else {
                                param_player.y = newY + playerHeadSize;
                                params.y = newY;
                            }
                            windowManager.updateViewLayout(serviceHead, params);
                            //update player params if visible
                            if(visible)
                                windowManager.updateViewLayout(playerView, param_player);
                        }
                        else {
                            if(newY + playerHeadSize > scrnHeight){
                                params.y = scrnHeight - playerHeadSize;
                            }
                            else{
                                params.y = newY;
                            }
                            params.x = newX;
                            int [] t = new int[2];
                            closeImageLayout.getLocationOnScreen(t);
                            updateIsInsideClose(params.x, params.y, t);
                            if(isInsideClose){
                                params.x = t[0];
                                params.y = t[1] - getStatusBarHeight();
                                params.width = closeImageLayoutSize;
                                params.height = closeImageLayoutSize;
                                if(closeImage.getLayoutParams().width == closeImgSize){
                                    closeImage.getLayoutParams().width = closeImgSize * 2;
                                    closeImage.getLayoutParams().height = closeImgSize * 2;
                                    closeImage.requestLayout();
                                }
                            }
                            else{
                                params.width = playerHeadSize;
                                params.height = playerHeadSize;
                                if(closeImage.getLayoutParams().width > closeImgSize){
                                    closeImage.getLayoutParams().width = closeImgSize;
                                    closeImage.getLayoutParams().height = closeImgSize;
                                    closeImage.requestLayout();
                                }
                            }
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
    //Update Image of Repeat Type Button
    private void updateRepeatTypeImage() {
        if(Constants.repeatType == 0){
            repeatTypeImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat_none));
        }
        else if(Constants.repeatType == 1){
            repeatTypeImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat));
        }
        else if(Constants.repeatType == 2){
            repeatTypeImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat_one));
        }
    }

    //Set Image and Headings in Notification
    public static void setImageTitleAuthor(String videoId) {

        Log.d("Setting ", "Image, Title, Author");

        try {
            bitmap = new ImageLoadTask("https://i.ytimg.com/vi/" + videoId + "/mqdefault.jpg").execute().get();
            String details = new LoadDetailsTask(
                    "https://www.youtube.com/oembed?url=http://www.youtu.be/watch?v=" + videoId + "&format=json")
                    .execute().get();
            JSONObject detailsJson = new JSONObject(details);
            title = detailsJson.getString("title");
            author = detailsJson.getString("author_name");

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

    public static void addStateChangeListener() {
        webPlayer.loadScript(JavaScript.onPlayerStateChangeListener());
    }
    private void updateIsInsideClose(int x, int y, int[] t) {
        playerHeadCenterX = x + playerHeadSize / 2 ;
        playerHeadCenterY = y + playerHeadSize / 2;
        closeMinX = t[0] - 10;
        closeMinY = t[1] - getStatusBarHeight() - 10;
        closeMaxX = closeMinX + closeImageLayoutSize + 10;
        if(isInsideClose()){
            isInsideClose = true;
        }
        else {
            isInsideClose = false;
        }
    }
    public boolean isInsideClose() {
        if(playerHeadCenterX >= closeMinX && playerHeadCenterX <= closeMaxX){
            if(playerHeadCenterY >= closeMinY){
                return true;
            }
        }
        return false;
    }
    private int getStatusBarHeight() {
        int statusBarHeight = (int) Math.ceil(25 * getApplicationContext().getResources().getDisplayMetrics().density);
        return statusBarHeight;
    }

    //Play video again on exit full screen
    public static void startAgain() {
        windowManager.addView(serviceHead, servHeadParams);
        windowManager.addView(serviceClose, servCloseParams);
        windowManager.addView(serviceCloseBackground, servCloseBackParams);
        windowManager.addView(playerView, playerViewParams);
        webPlayer.loadScript(JavaScript.playVideoScript());
    }


    //Clicks Handled
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //Handle Hiding of player
            case R.id.song_icon:
                Log.d("Clicked", "Click!");
                if (visible) {
                    //Make head sticky with the edge so update head params
                    updateHead = true;
                    hidePlayer();
                } else {
                   showPlayer();
                }
                break;
            //Handle Full Screen
            case R.id.fullscreen:
                webPlayer.loadScript(JavaScript.pauseVideoScript());
                fullScreenIntent = new Intent(getAppContext(), FullscreenWebPlayer.class);
                fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //remove Views
                windowManager.removeView(serviceHead);
                servHeadParams = (WindowManager.LayoutParams) serviceHead.getLayoutParams();
                windowManager.removeView(serviceClose);
                servCloseParams = (WindowManager.LayoutParams) serviceClose.getLayoutParams();
                windowManager.removeView(serviceCloseBackground);
                servCloseBackParams = (WindowManager.LayoutParams) serviceCloseBackground.getLayoutParams();
                windowManager.removeView(playerView);
                playerViewParams = (WindowManager.LayoutParams) playerView.getLayoutParams();
                //start full Screen Player
                mContext.startActivity(fullScreenIntent);
                break;
            //Handle Entire Width
            case R.id.entire_width:
                //Enter Entire Width
                if(WebPlayer.getPlayer().getMeasuredWidth() != scrnWidth) {
                    param_player.width = WindowManager.LayoutParams.MATCH_PARENT;
                    windowManager.updateViewLayout(playerView, param_player);
                    ViewGroup.LayoutParams fillWidthParamLL = webPlayerLL.getLayoutParams();
                    fillWidthParamLL.width = WindowManager.LayoutParams.MATCH_PARENT;
                    webPlayerLL.setLayoutParams(fillWidthParamLL);
                    ViewGroup.LayoutParams fillWidthParamFrame = webPlayerFrame.getLayoutParams();
                    fillWidthParamFrame.width = WindowManager.LayoutParams.MATCH_PARENT;
                    webPlayerFrame.setLayoutParams(fillWidthParamFrame);
                    ViewGroup.LayoutParams fillWidthParam = viewToHide.getLayoutParams();
                    fillWidthParam.width = WindowManager.LayoutParams.MATCH_PARENT;
                    viewToHide.setLayoutParams(fillWidthParam);
                    ViewGroup.LayoutParams playerEntireWidPar = WebPlayer.getPlayer().getLayoutParams();
                    playerEntireWidPar.width = WindowManager.LayoutParams.MATCH_PARENT;
                    viewToHide.updateViewLayout(WebPlayer.getPlayer(), playerEntireWidPar);
                    entireWidthImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_entire_width_exit));
                    isEntireWidth = true;
                }
                //Exit Entire Width
                else{
                    param_player.width = defaultPlayerWidth;
                    windowManager.updateViewLayout(playerView, param_player);
                    ViewGroup.LayoutParams fillWidthParamLL = webPlayerLL.getLayoutParams();
                    fillWidthParamLL.width = defaultPlayerWidth;
                    webPlayerLL.setLayoutParams(fillWidthParamLL);
                    ViewGroup.LayoutParams fillWidthParamFrame = webPlayerFrame.getLayoutParams();
                    fillWidthParamFrame.width = defaultPlayerWidth;
                    webPlayerFrame.setLayoutParams(fillWidthParamFrame);
                    ViewGroup.LayoutParams fillWidthParam = viewToHide.getLayoutParams();
                    fillWidthParam.width = defaultPlayerWidth;
                    viewToHide.setLayoutParams(fillWidthParam);
                    ViewGroup.LayoutParams playerEntireWidPar = WebPlayer.getPlayer().getLayoutParams();
                    playerEntireWidPar.width = defaultPlayerWidth;
                    viewToHide.updateViewLayout(WebPlayer.getPlayer(), playerEntireWidPar);
                    entireWidthImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_entire_width));
                    isEntireWidth = false;
                }
                break;
            //Handle Repeat Settings
            case R.id.repeat_type:
                SharedPreferences.Editor editor = sharedPref.edit();
                if (Constants.repeatType == 0) {
                    editor.putInt(getString(R.string.repeat_type), 1);
                    editor.commit();
                    Constants.repeatType = 1;
                    if (Constants.linkType == 1) {
                        webPlayer.loadScript(JavaScript.setLoopPlaylist());
                    }
                    updateRepeatTypeImage();
                } else if (Constants.repeatType == 1) {
                    editor.putInt(getString(R.string.repeat_type), 2);
                    editor.commit();
                    Constants.repeatType = 2;
                    if (Constants.linkType == 1) {
                        webPlayer.loadScript(JavaScript.unsetLoopPlaylist());
                    }
                    updateRepeatTypeImage();
                } else if (Constants.repeatType == 2) {
                    editor.putInt(getString(R.string.repeat_type), 0);
                    editor.commit();
                    Constants.repeatType = 0;
                    if (Constants.linkType == 1) {
                        webPlayer.loadScript(JavaScript.unsetLoopPlaylist());
                    }
                    updateRepeatTypeImage();
                }
                break;
            default:
                break;
        }
    }

    private void showPlayer() {
        viewToHide.setVisibility(View.VISIBLE);
        //Store current to again hidden icon will come here
        if(params.x > 0) {
            xOnAppear = scrnWidth - playerHeadSize + playerHeadSize / 4;
        }
        else{
            xOnAppear = - playerHeadSize / 4;
        }
        yOnAppear = params.y;
        //Update the icon and player to player's hidden position
        params.x = xAtHiding;
        params.y = yAtHiding;
        param_player.x = xAtHiding;
        param_player.y = yAtHiding + playerHeadSize;
        windowManager.updateViewLayout(playerView, param_player);
        windowManager.updateViewLayout(serviceHead, params);
        visible = true;
    }

    private void hidePlayer() {
//        Log.d("Head x , y ", params.x + " " + params.y);
//        Log.d("Player x , y ", param_player.x + " " + param_player.y);
//        Log.d("Head Size", String.valueOf(playerHeadImage.getHeight()));
        xAtHiding = params.x;
        yAtHiding = params.y;
        //To hide the Player View
        final WindowManager.LayoutParams tmpPlayerParams = new WindowManager.LayoutParams(
                100,
                100,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        tmpPlayerParams.x = scrnWidth;
        tmpPlayerParams.y = scrnHeight;
        windowManager.updateViewLayout(playerView, tmpPlayerParams);
        viewToHide.setVisibility(View.GONE);
        if(updateHead) {
            params.x = xOnAppear;
            params.y = yOnAppear;
            windowManager.updateViewLayout(serviceHead, params);
        }
        visible = false;
    }

    //Layout Params Initialized
    private void InitParams() {
        //Service Head Params
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
        //Web Player Params
        parWebView = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
        );

        //Player View Params
        param_player = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        //Close Backgroung Params
        param_close_back = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        //Close Image Params
        param_close = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);

    }
}