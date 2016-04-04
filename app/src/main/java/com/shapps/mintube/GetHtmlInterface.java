package com.shapps.mintube;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by shyam on 18/2/16.
 */
class GetHtmlInterface {
    Context context;
    static String PlayerId = "";
    static boolean foundPlayerId;
    static Handler handlerForJavascriptInterface = new Handler();
    public GetHtmlInterface(PlayerService playerService) {
        this.context = playerService;
    }

    @JavascriptInterface
    public void showHTML(final String html, WebView player) {
        Pattern pattern = Pattern.compile(
                ".*\\n.*(player_uid_\\d+_1).*\\n.*",Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        if (matcher.matches()) {
            PlayerId = matcher.group(1);
            Log.e("Player Id ", PlayerId);
            foundPlayerId = true;
            Session.setPlayerId(PlayerId);
            handlerForJavascriptInterface.post(new Runnable() {
                @Override
                public void run() {
                    PlayerService.InitializePlayer();
                }
            });
        }
        else {
            handlerForJavascriptInterface.post(new Runnable() {
                @Override
                public void run() {
                    PlayerService.tryAgainForPlayerID();
                }
            });
        }

    }
    @JavascriptInterface
    public void showPlayerState (final int status) {
        Log.e("Player Status ", String.valueOf(status));
        handlerForJavascriptInterface.post(new Runnable() {
            @Override
            public void run() {
                PlayerService.setPlayingStatus(status);
            }
        });
    }
    @JavascriptInterface
    public void showVID (final String vId) {
        Log.e("New Video Id ", vId);
        handlerForJavascriptInterface.post(new Runnable() {
            @Override
            public void run() {
                PlayerService.setImageTitleAuthor(vId);
            }
        });
    }
    @JavascriptInterface
    public void playlistItems (final String[] items) {
        Log.e("Playlist Items", String.valueOf(items.length));
        PlayerService.setNoItemsInPlaylist(items.length);
        PlayerService.compare();
    }
    @JavascriptInterface
    public void currVidIndex (final int index) {
        Log.e("Current Video Index ", String.valueOf(index));
        PlayerService.setCurrVideoIndex(index);
        PlayerService.compare();
    }
}