package com.shapps.mintubeapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.SearchManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.shapps.mintubeapp.CustomViews.CustomSwipeRefresh;
import com.shapps.mintubeapp.adapter.SuggestionCursorAdapter;
import com.shapps.mintubeapp.viewmodel.SuggestionViewModel;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    Activity mainAct;
    WebView youtubeView;
    String currUrl;
    boolean doubleClickToExit = false;
    //For Result Activity
    public static int OVERLAY_PERMISSION_REQ = 1234;
    String VID, PID;
    //SearchView
    SearchView searchView;
    //Swipe Refresh
    CustomSwipeRefresh swipeRefreshLayout;
    boolean exit = false;

    Button retry, changeSettings, exitApp;

    ViewStub viewStub;

    SuggestionViewModel suggestionViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mainAct = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewStub = findViewById(R.id.view_stub);


        if(isInternetAvailable(mainAct)) {

            viewStub.setLayoutResource(R.layout.content_main);
            viewStub.inflate();

            exit = false;

//            Toolbar toolbar = findViewById(R.id.toolbar);
//            setSupportActionBar(toolbar);

            //Swipe Refresh WebView
            swipeRefreshLayout = findViewById(R.id.swipe_refresh);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    swipeRefreshLayout.setRefreshing(true);
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            youtubeView.loadUrl(youtubeView.getUrl());
                        }
                    });
                }
            });


            // after initialization
            swipeRefreshLayout.setCanChildScrollUpCallback(new CustomSwipeRefresh.CanChildScrollUpCallback() {
                @Override
                public boolean canSwipeRefreshChildScrollUp() {
                    return youtubeView.getScrollY() > 0;
                }
            });

            youtubeView = findViewById(R.id.youtube_view);
            youtubeView.getSettings().setJavaScriptEnabled(true);
            youtubeView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String str, Bitmap bitmap) {
                    super.onPageStarted(view, str, bitmap);
                    Log.d("Main Page Loading ", str);
                    swipeRefreshLayout.setRefreshing(true);

                    currUrl = str;
                }

                @Override
                public void onPageFinished(WebView view, String str) {
                    super.onPageFinished(view, str);
                    swipeRefreshLayout.setRefreshing(false);
                    Log.d("Main Page Finished", str);
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.contains("?app=desktop") && !url.contains("signin?app=desktop")) {
                        Log.d("Url stopped to load : ", url);
                        CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinator_layout);
                        final Snackbar snackbar = Snackbar
                                .make(coordinatorLayout, "Desktop View Unavailable", Snackbar.LENGTH_LONG);
                        //Changing Text Color
                        View snkBar = snackbar.getView();
                        TextView tv = snkBar.findViewById(android.support.design.R.id.snackbar_text);
                        tv.setTextColor(Color.parseColor("#e52d27"));
                        snackbar.show();
                        return true;
                    }
                    return false;
                }

                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (String.valueOf(request.getUrl()).contains("http://m.youtube.com/watch?") ||
                                String.valueOf(request.getUrl()).contains("https://m.youtube.com/watch?")) {
                            String url = String.valueOf(request.getUrl());
                            Log.d("Yay Catches!!!! ", url);
                            //Video Id
                            VID = url.substring(url.indexOf("v=") + 2).split("&")[0];
                            Log.d("VID ", VID);
                            //Playlist Id
                            final String listID = url.substring(url.indexOf("list=") + 5).split("&")[0];
                            Log.d("ListID", listID);
                            PID = "";
                            if (listID.length() > 0 && !listID.contains("m.youtube.com")) {
                                PID = listID;
                                Constants.linkType = 1;
                                Log.d("PlaylistID ", PID);
                            } else {
                                Log.d("Not a ", "Playlist.");
                                PID = null;
                            }
                            Handler handler = new Handler(getMainLooper());
                            final String finalPID = PID;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    youtubeView.stopLoading();
                                    youtubeView.goBack();
                                    if (isServiceRunning(PlayerService.class)) {
                                        Log.d("Service : ", "Already Running!");
                                        PlayerService.startVid(VID, finalPID);
                                    } else {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(MainActivity.this)) {
                                            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                    Uri.parse("package:" + getPackageName()));
                                            startActivityForResult(i, OVERLAY_PERMISSION_REQ);
                                        } else {
                                            Intent i = new Intent(MainActivity.this, PlayerService.class);
                                            i.putExtra("VID_ID", VID);
                                            i.putExtra("PLAYLIST_ID", finalPID);
                                            i.setAction(Constants.ACTION.STARTFOREGROUND_WEB_ACTION);
                                            startService(i);
                                        }
                                    }

                                }
                            });
                        }
                    }
                    return super.shouldInterceptRequest(view, request);
                }
            });
            youtubeView.canGoBack();
            currUrl = "https://m.youtube.com/";
            youtubeView.loadUrl(currUrl);
        }
        else{

            viewStub.setLayoutResource(R.layout.content_main_no_internet);
            viewStub.inflate();

            exit = true;
            retry = findViewById(R.id.retry_internet);
            changeSettings = findViewById(R.id.change_settings);
            exitApp = findViewById(R.id.exit_app);
            retry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mainAct.recreate();
                }
            });
            changeSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0);
                }
            });
            exitApp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    needPermissionDialog(requestCode);
                } else {
                    Intent i = new Intent(this, PlayerService.class);
                    i.putExtra("VID_ID", VID);
                    i.putExtra("PLAYLIST_ID", PID);
                    i.setAction(Constants.ACTION.STARTFOREGROUND_WEB_ACTION);
                    startService(i);
                }
            }
        }
        else if(requestCode == 0) {
            mainAct.recreate();
        }
    }
    private void needPermissionDialog(final int requestCode) {
        if(requestCode == OVERLAY_PERMISSION_REQ) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("You need to grant the permission.");
            builder.setPositiveButton("OK",
                    new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + getPackageName()));
                            startActivityForResult(i, OVERLAY_PERMISSION_REQ);
                        }
                    });
            builder.setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.setCancelable(false);
            builder.show();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Associate searchable configuration with the SearchView
        final SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        if(searchView != null){
            searchView.setSearchableInfo(
                    searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnQueryTextListener(this);
        }
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
            Log.d("Settings", "Act");
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        if(exit){
            super.onBackPressed();
            return;
        }
        Log.d("Curr Url", currUrl);
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

    public static boolean isInternetAvailable(Context context) {
        NetworkInfo info = ((ConnectivityManager)
                Objects.requireNonNull(context.getSystemService(Context.CONNECTIVITY_SERVICE))).getActiveNetworkInfo();

        if (info == null) {
            Log.d("Network Test","no internet connection");
            return false;
        }
        else {
            if(info.isConnected()) {
                Log.d("Network Test"," internet connection available...");
                return true;
            }
            else {
                Log.d("Network Test"," internet connection");
                return true;
            }
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        youtubeView.loadUrl("http://m.youtube.com/results?q="+ query);
        searchView.clearFocus();
        return true;
    }
    @Override
    public boolean onQueryTextChange(String newText) {

        newText = newText.trim();

        if(newText.length() > 0) {

            suggestionViewModel = ViewModelProviders.of(this).get(SuggestionViewModel.class);

            suggestionViewModel.setQuery(newText);

            observeSuggestions(suggestionViewModel);

        }
        return true;
    }

    private void observeSuggestions(SuggestionViewModel suggestionViewModel) {
        suggestionViewModel.getLiveData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if (s != null) {
                    String[] res = s.split("\",\"");
                    if (res.length > 0) {
                        res[0] = res[0].split(",\\[\"")[1];
                        res[res.length - 1] = res[res.length - 1].split("\"")[0];
                        //Cursor Adaptor
                        String[] columnNames = {"_id", "suggestion"};
                        MatrixCursor cursor = new MatrixCursor(columnNames);
                        String[] temp = new String[2];
                        int id = 0;
                        for (String item : res) {
                            if (item != null) {
                                temp[0] = Integer.toString(id++);
                                temp[1] = item;
                                cursor.addRow(temp);
                            }
                        }
                        SuggestionCursorAdapter cursorAdapter = new SuggestionCursorAdapter(getApplicationContext(),
                                cursor, false,
                                searchView);
                        searchView.setSuggestionsAdapter(cursorAdapter);
                    }
                }
            }
        });
    }
}