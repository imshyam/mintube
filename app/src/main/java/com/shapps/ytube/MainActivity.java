package com.shapps.ytube;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    WebView youtubeView;
    String currUrl;
    boolean doubleClickToExit = false;

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
                currUrl = str;
            }

            @Override
            public void onPageFinished(WebView view, String str) {
                super.onPageFinished(view, str);
                Log.d("Main Page Finished", str);
            }
            @Override
            public boolean shouldOverrideUrlLoading (WebView view, String url){
                if(url.startsWith("http://www.youtube.com/?app=desktop") ||
                        url.startsWith("https://www.youtube.com/?app=desktop")){
                    Log.e("Url stopped to load : ", url);
                    CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
                    final Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, "Desktop View Unavailable", Snackbar.LENGTH_LONG);
                    //Changing Text Color
                    View snkBar = snackbar.getView();
                    TextView tv = (TextView) snkBar.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setTextColor(Color.parseColor("#e52d27"));
                    snackbar.show();
                    return true;
                }
                return false;
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (String.valueOf(request.getUrl()).contains("http://m.youtube.com/watch?")||
                            String.valueOf(request.getUrl()).contains("https://m.youtube.com/watch?")) {
                        Log.e("Yay ", "Catches!!!!");
                        String url = String.valueOf(request.getUrl());
                        //Video Id
                        final String VID = url.substring(url.indexOf("&v=") + 3, url.length());
                        Log.e("VID ", VID);
                        //Playlist Id
                        final String listID = url.substring(url.indexOf("&list=") + 6, url.length());
                        Pattern pattern = Pattern.compile(
                                "([A-Za-z0-9_-]+)&[\\w]+=.*",
                                Pattern.CASE_INSENSITIVE);
                        Matcher matcher = pattern.matcher(listID.toString());
                        Log.e("ListID", listID);
                        String PID = "";
                        if (matcher.matches()) {
                            PID = matcher.group(1);
                        }
                        if(listID.contains("m.youtube.com")){
                            Log.e("Not a ", "Playlist.");
                            PID = null;
                        }
                        else {
                            Constants.setItsAPlaylist();
                            Log.e("PlaylistID ", PID);
                        }
                        Handler handler = new Handler(getMainLooper());
                        final String finalPID = PID;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                youtubeView.stopLoading();
                                youtubeView.goBack();
                                if(isServiceRunning(PlayerService.class)){
                                    Log.e("Service : ", "Already Running!");
                                    PlayerService.startVid(VID, finalPID);
                                }
                                else {
                                    Intent i = new Intent(MainActivity.this, PlayerService.class);
                                    i.putExtra("VID_ID", VID);
                                    i.putExtra("PLAYLIST_ID", finalPID);
                                    i.setAction(Constants.ACTION.STARTFOREGROUND_WEB_ACTION);
                                    startService(i);
                                }

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
    @Override
    public void onBackPressed() {
        if(currUrl.equals("https://m.youtube.com/")) {
            if (doubleClickToExit) {
                super.onBackPressed();
                return;
            }

            this.doubleClickToExit = true;
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleClickToExit = false;
                }
            }, 2000);
        }
        else {
            youtubeView.goBack();
        }
    }
}