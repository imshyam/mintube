package com.shapps.ytube;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by shyam on 18/2/16.
 */
class GetHtmlInterface {
    Context context;
    static String PlayerId = "Null Yet.";
    static boolean foundPlayerId;
    static Handler handlerForJavascriptInterface = new Handler();
    public GetHtmlInterface(PlayerService playerService) {
        this.context = playerService;
    }

    public static String getPlayerId() {
        return PlayerId;
    }

    @JavascriptInterface
    public void showHTML(final String html)
    {
        handlerForJavascriptInterface.post(new Runnable()
                                           {

                                               @Override
                                               public void run()
                                               {
                                                   Pattern pattern = Pattern.compile(
                                                           ".*\\n.*(player_uid_\\d+_1).*\\n.*",Pattern.CASE_INSENSITIVE);
                                                   Matcher matcher = pattern.matcher(html);
                                                   if (matcher.matches()) {
                                                       PlayerId = matcher.group(1);
                                                       foundPlayerId = true;
                                                   }
                                                   Log.e("Player Id ", PlayerId);
                                               }
                                           }
        );
    }

    public static boolean foundPlayerId() {
        return foundPlayerId;
    }
}