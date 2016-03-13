package com.shapps.ytube;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.shapps.ytube.YouTube.ApiKey;

/**
 * Created by shyam on 13/3/16.
 */
public class YouTubeStandAloneActivity extends Activity{

    String VIDEO_ID, PLIST_ID;
    int startAt;
    public static int RESULT_FINISHED = 12345;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if(Constants.linkType == 0) {
                VIDEO_ID = bundle.getString("VID");
                startAt = bundle.getInt("START_AT");
                Intent intent = YouTubeStandalonePlayer.createVideoIntent(this, ApiKey.API_KEY, VIDEO_ID,
                        startAt * 1000, true, false);
                startActivityForResult(intent, RESULT_FINISHED);
                finish();
            }
            else{
                PLIST_ID = bundle.getString("PID");
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_FINISHED){
            Log.e("FINI", "SH");
        }
    }
}
