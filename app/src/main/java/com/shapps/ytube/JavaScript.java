package com.shapps.ytube;

import android.util.Log;

/**
 * Created by shyam on 19/2/16.
 */
public class JavaScript {

    public static String loadPlayerScript(String vId){;
        return "player.loadVideoById(\"" + vId + "\");";
    }

    public static String playVideoScript() {
        return "player.playVideo();";
    }

    public static String pauseVideoScript() {
        return "player.pauseVideo();";
    }

    public static String initializePlayerScript(String playerId) {
        return "var player = document.getElementById(\"" + playerId + "\");" +
                "player.addEventListener(\"onStateChange\", \"onPlayerStateChange\");"+
                "function onPlayerStateChange(event) {\n" +
                "      window.HtmlViewer.showToast(player.getPlayerState());\n" +
                "  }";
    }

    public static String getHtmlScript() {
        return "javascript:window.HtmlViewer.showHTML" +
                "('&lt;body&gt;'+document.getElementsByTagName('body')[0].innerHTML+'&lt;/body&gt;', player);";
    }
}
