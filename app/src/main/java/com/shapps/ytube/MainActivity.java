package com.shapps.ytube;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.shapps.ytube.YouTube.YoutubePlayer;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    WebView youtubeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        youtubeView = (WebView) findViewById(R.id.youtube_view);
        youtubeView.getSettings().setJavaScriptEnabled(true);
        youtubeView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String str, Bitmap bitmap) {
                super.onPageStarted(view, str, bitmap);
                Log.d("Main Page Loading ", str);
            }

            @Override
            public void onPageFinished(WebView view, String str) {
                super.onPageFinished(view, str);
                Log.d("Main Page Finished", str);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (String.valueOf(request.getUrl()).contains("http://m.youtube.com/watch?") || String.valueOf(request.getUrl()).contains("https://m.youtube.com/watch?")) {
                        Log.e("Yay ", "Catches!!!!");
                        String url = String.valueOf(request.getUrl());
                        String VID = url.substring(url.indexOf("&v=") + 3, url.length());
                        Log.e("VID ", VID);
                        if (isServiceRunning(PlayerService.class)) {
                            Log.e("Service : ", "Already Running!");
                            PlayerService.startVid(VID);
                        }
                        else {
                            Intent i = new Intent(MainActivity.this, PlayerService.class);
                            i.putExtra("VID_ID", VID);
                            i.setAction(Constants.ACTION.STARTFOREGROUND_WEB_ACTION);
                            startService(i);
                        }
                        Handler handler = new Handler(getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                youtubeView.stopLoading();
                                youtubeView.goBack();

                            }
                        });
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }
        });
        youtubeView.canGoBack();
        youtubeView.loadUrl("http://m.youtube.com");

    }
    private boolean isServiceRunning(Class<PlayerService> playerServiceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (playerServiceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}