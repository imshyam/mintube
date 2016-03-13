package com.shapps.ytube;

import android.util.Log;

/**
 * Created by shyam on 19/2/16.
 */
public class JavaScript {

    public static String loadVideoScript(String vId){;
        return "javascript:player.loadVideoById(\"" + vId + "\");";
    }

    public static String playVideoScript() {
        return "javascript:player.playVideo();";
    }

    public static String pauseVideoScript() {
        return "javascript:player.pauseVideo();";
    }

    public static String initializePlayerScript(String playerId) {
        return "javascript:var player = document.getElementById(\"" + playerId + "\");" +
                "player.addEventListener(\"onStateChange\", \"onPlayerStateChange\");"+
                "function onPlayerStateChange(event) {\n" +
                "      window.HtmlViewer.showPlayerState(player.getPlayerState());\n" +
                "  }";
    }

    public static String getHtmlScript() {
        return "javascript:window.HtmlViewer.showHTML" +
                "('&lt;body&gt;'+document.getElementsByTagName('body')[0].innerHTML+'&lt;/body&gt;', player);";
    }

    public static String loadPlaylistScript(String pId) {
        return "javascript:player.loadPlaylist({list:\"" + pId + "\"});";
    }

    public static String nextVideo() {
        return "javascript:player.nextVideo()";
    }

    public static String prevVideo() {
        return "javascript:player.previousVideo()";
    }

    public static String getVidUpdateNotiContent() {
        return "javascript:window.HtmlViewer.showVID(player.getVideoData()['video_id']);";
    }

    public static String seekToZero() {
        return "javascript:player.seekTo(0)";
    }

    public static String setLoopPlaylist() {
        return "javascript:player.setLoop(true)";
    }

    public static String unsetLoopPlaylist() {
        return "javascript:player.setLoop(false)";
    }

    public static String replayPlaylistScript() {
        return "javascript:player.playVideoAt(0)";
    }

    public static String isPlaylistEnded() {
        return "javascript:window.HtmlViewer.playlistItems(player.getPlaylist());" +
                "window.HtmlViewer.currVidIndex(player.getPlaylistIndex());";
    }
    public static String CurrVidIndex() {
        return "javascript:window.HtmlViewer.currVidIndex(player.getPlaylistIndex());";
    }

    public static String getTime() {
        return "javascript:window.HtmlViewer.currTime(player.getCurrentTime());";
    }
}
