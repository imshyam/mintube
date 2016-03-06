package com.shapps.ytube;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.shapps.ytube.YouTube.YouTubeFailureRecoveryActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YTubeView extends Activity{//extends YouTubeFailureRecoveryActivity {

//    YouTubePlayerView youTubeView;
    public static int OVERLAY_PERMISSION_REQ = 1234;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//
//        youTubeView = new YouTubePlayerView(this);
//
//        Session.setYouTubePlayerView(youTubeView);


        final Intent intent = getIntent();
//        if(intent.getData() != null || intent.getStringExtra("android.intent.extra.TEXT") != null) {
//            String link;
//            if (intent.getData() != null) {
//                link = intent.getData().toString();
//            } else {
//                link = intent.getStringExtra("android.intent.extra.TEXT");
//            }
//            Log.e("Link : ", link.toString());
//            String vId = "";
//            Pattern pattern = Pattern.compile(
//                    "^https?://.*(?:youtu.be/|v/|u/\\\\w/|embed/|watch[?]v=)([^#&?]*).*$",
//                    Pattern.CASE_INSENSITIVE);
//            Matcher matcher = pattern.matcher(link.toString());
//            if (matcher.matches()) {
//                vId = matcher.group(1);
//            }
//            Log.e("Video Id : ", vId);
//            //Getting Playlist id
//            final String listID = link.substring(link.indexOf("http") + 4, link.length());
//            Log.e("List ID Is : ", listID);
//            String pId = null;
//            String regex = ".*list=([A-Za-z0-9_-]+).*?";
//            pattern = Pattern.compile(regex,
//                    Pattern.CASE_INSENSITIVE);
//            matcher = pattern.matcher(link.toString());
//            if (matcher.matches()) {
//                pId = matcher.group(1);
//                Log.e("PID Is : ", pId);
//                Constants.setItsAPlaylist();
//            }
//
//            if (isServiceRunning(PlayerService.class)) {
//                Log.e("Service : ", "Already Running!");
//                PlayerService.startVid(vId, pId);
//                finish();
//            } else {
//                Intent i = new Intent(this, PlayerService.class);
//                i.putExtra("VID_ID", vId);
//                i.putExtra("PLAYLIST_ID", pId);
//                i.setAction(Constants.ACTION.STARTFOREGROUND_WEB_ACTION);
//                startService(i);
//                finish();
//            }
//        }
//        else {
//            startActivity(new Intent(this, MainActivity.class));
//            finish();
//        }

        //Remove this
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent i = new Intent(this,
                    GetPermission.class);
            startActivityForResult(i, OVERLAY_PERMISSION_REQ);
            finish();
        }
        else {
            Intent i = new Intent(this, PlayerService.class);
            i.putExtra("VID_ID", "nIkFW78x6UA");
            i.putExtra("PLAYLIST_ID", (String[]) null);
            i.setAction(Constants.ACTION.STARTFOREGROUND_WEB_ACTION);
            startService(i);
            finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    finish();
                }else{
                    Intent i = new Intent(this, PlayerService.class);
                    i.putExtra("VID_ID", "nIkFW78x6UA");
                    i.putExtra("PLAYLIST_ID", (String[]) null);
                    i.setAction(Constants.ACTION.STARTFOREGROUND_WEB_ACTION);
                    startService(i);
                    finish();
                }
            }

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

//
//    @Override
//    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
//                                        boolean wasRestored) {
//
//    }
//
//
//    @Override
//    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
//        return youTubeView;
//    }
}
