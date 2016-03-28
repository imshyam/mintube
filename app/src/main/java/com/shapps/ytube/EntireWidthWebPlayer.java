package com.shapps.ytube;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by shyam on 19/3/16.
 */
public class EntireWidthWebPlayer extends AppCompatActivity {

    static boolean active = false;
    static Activity entWidthAct;

    WebView player;
    ViewGroup parent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.active = true;
        entWidthAct = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fill_entire_width);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RelativeLayout rl = (RelativeLayout) findViewById(R.id.fill_entire_webview);
        player = WebPlayer.getPlayer();

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
        );

        parent = (ViewGroup) player.getParent();
        parent.removeView(player);

        rl.addView(player, params);

        WebPlayer.loadScript(JavaScript.playVideoScript());
    }

    @Override
    public void onBackPressed() {
        active = false;
        ((ViewGroup) player.getParent()).removeView(player);
        parent.addView(player);
        PlayerService.startAgain();
        super.onBackPressed();
    }

}
