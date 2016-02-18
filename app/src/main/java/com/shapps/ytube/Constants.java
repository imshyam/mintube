package com.shapps.ytube;

/**
 * Created by shyam on 16/2/16.
 */
public class Constants {

    //Type of player
    //WebView player = 0
    //Youtube player = 1
    public  static  int playerType = 0;

    //Type of link
    //Single song link = 0
    //Playlist link = 1
    public  static  int linkType = 0;

    //Repeat song or not
    public  static  boolean repeat = false;

    //Actions
    public interface ACTION {
        public static String PREV_ACTION = "com.shapps.ytube.action.prev";
        public static String PAUSE_PLAY_ACTION = "com.shapps.ytube.action.play";
        public static String NEXT_ACTION = "com.shapps.ytube.action.next";
        public static String STARTFOREGROUND_WEB_ACTION = "com.shapps.ytube.action.playingweb";
        public static String STOPFOREGROUND_WEB_ACTION = "com.shapps.ytube.action.stopplayingweb";
        public static String STARTFOREGROUND_YTUBE_ACTION = "com.shapps.ytube.action.playingytube";
        public static String STOPFOREGROUND_YTUBE_ACTION = "com.shapps.ytube.action.stopplayingytube";
    }

    //Notification Id
    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }

}
