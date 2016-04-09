package com.shapps.mintubeapp;

import android.util.Log;

/**
 * Created by shyam on 2/2/16.
 */
public class Session{

//    private static YouTubePlayerView youTubePlayerView;
    private static String playerId;
    private static boolean foundPlayerId = false;

//
//    public Session() {
//    }
//
//    public Session(YouTubePlayerView youTubePlayerView) {
//
//        this.youTubePlayerView = youTubePlayerView;
//
//    }
//
//    public static YouTubePlayerView getYouTubePlayerView() {
//        return youTubePlayerView;
//    }
//
//    public static void setYouTubePlayerView(YouTubePlayerView View) {
//        youTubePlayerView = View;
//    }

    public static void setPlayerId(String playerId) {
        Session.playerId = playerId;
        Session.foundPlayerId = true;
        Log.e("Setting ", " Player Id!!");
    }

    public static String getPlayerId() {
        return playerId;
    }

    public static boolean foundPlayerId() {
        return foundPlayerId;
    }

    public static void finishWeb() {
        Session.foundPlayerId = false;
        Session.playerId = null;
    }
}
