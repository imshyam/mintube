package com.shapps.ytube;

import android.content.Context;
import android.content.SharedPreferences;

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

    //Repeat
    //if repeatType = 0  --> no repeatType
    //if repeatType = 1  --> repeatType complete
    //if repeatType = 2  --> repeatType single
    public  static  int repeatType = 0;
    public  static  int noOfRepeats = 0;
    //if repeatType is 2 then Video id to repeatType
    public  static  String vIdToRepeat = "";


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
