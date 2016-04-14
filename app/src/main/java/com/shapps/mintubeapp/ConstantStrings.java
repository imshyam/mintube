package com.shapps.mintubeapp;

/**
 * Created by shyam on 14/4/16.
 */
public class ConstantStrings {

    public  static String VID = "";
    public static String PLIST = "";

    public static void setVid(String vid) {
        ConstantStrings.VID = vid;
    }

    public static String getVideoHTML() {
        return  "<!DOCTYPE HTML>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <script src=\"https://www.youtube.com/iframe_api\"></script>\n" +
                "    <style type=\"text/css\">\n" +
                "        html, body {\n" +
                "            margin: 0px;\n" +
                "            padding: 0px;\n" +
                "            border: 0px;\n" +
                "            width: 100%;\n" +
                "            height: 100%;\n" +
                "        }\n" +
                "    </style>" +
                "  </head>\n" +
                "\n" +
                "  <body>\n" +
                "    <iframe style=\"display: block;\" id=\"player\" frameborder=\"0\"  width=\"100%\" height=\"100%\" " +
                "       src=\"https://www.youtube.com/embed/" + VID +
                       "?enablejsapi=1&autoplay=1&iv_load_policy=3&fs=0&rel=0\">" +
                "    </iframe>\n" +
                "    <script type=\"text/javascript\">\n" +
                "      var tag = document.createElement('script');\n" +
                "      tag.src = \"https://www.youtube.com/iframe_api\";\n" +
                "      var firstScriptTag = document.getElementsByTagName('script')[0];\n" +
                "      firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);\n" +
                "      var player;\n" +
                "      function onYouTubeIframeAPIReady() {\n" +
                "          player = new YT.Player('player', {\n" +
                "              events: {\n" +
                "                  'onReady': onPlayerReady\n" +
                "              }\n" +
                "          });\n" +
                "      }\n" +
                "      function onPlayerReady(event) {\n" +
                "          player.setPlaybackQuality(\"" + Constants.getPlaybackQuality() + "\");\n" +
                "      }\n" +
                "    </script>\n" +
                "\n" +
                "</body>\n" +
                "</html>";
    }

    public static void setPList(String PList) {
        ConstantStrings.PLIST = PList;
    }

    public static String getPlayListHTML() {
        return "<!DOCTYPE HTML>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <script src=\"https://www.youtube.com/iframe_api\"></script>\n" +
                "    <style type=\"text/css\">\n" +
                "        html, body {\n" +
                "            margin: 0px;\n" +
                "            padding: 0px;\n" +
                "            border: 0px;\n" +
                "            width: 100%;\n" +
                "            height: 100%;\n" +
                "        }\n" +
                "    </style>" +
                "  </head>\n" +
                "\n" +
                "  <body>\n" +
                "    <iframe style=\"display: block;\" id=\"player\" frameborder=\"0\" width=\"100%\" height=\"100%\" " +
                "       src=\"https://www.youtube.com/embed/" +
                        "?list=" + PLIST +
                        "&enablejsapi=1&autoplay=1&iv_load_policy=3&fs=0&rel=0\">" +
                "    </iframe>\n" +
                "    <script type=\"text/javascript\">\n" +
                "      var tag = document.createElement('script');\n" +
                "\n" +
                "      tag.src = \"https://www.youtube.com/iframe_api\";\n" +
                "      var firstScriptTag = document.getElementsByTagName('script')[0];\n" +
                "      firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);\n" +
                "      var player;\n" +
                "      function onYouTubeIframeAPIReady() {\n" +
                "          player = new YT.Player('player', {\n" +
                "              events: {\n" +
                "                  'onReady': onPlayerReady\n" +
                "              }\n" +
                "          });\n" +
                "      }\n" +
                "      function onPlayerReady(event) {\n" +
                "          player.setPlaybackQuality(\""+ Constants.getPlaybackQuality() +"\");\n" +
                "      }\n" +
                "    </script>\n" +
                "\n" +
                "</body>\n" +
                "</html>";
    }
}
