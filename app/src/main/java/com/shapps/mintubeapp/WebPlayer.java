package com.shapps.mintubeapp;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.shapps.mintubeapp.CustomViews.CircularImageView;

import java.util.Map;

/**
 * Created by shyam on 15/3/16.
 */
public class WebPlayer {

    static WebView player;
    Context context;

    //Player movement when hidden
    int initialXP, initialYP;
    float initialTouchXP, initialTouchYP, finalTouchXP, finalTouchYP;

    public WebPlayer(Context context) {
        this.player = new WebView(context);
        this.context = context;
    }

    public void setupPlayer() {
        player.getSettings().setJavaScriptEnabled(true);

//         For debugging using chrome on PC
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                player.setWebContentsDebuggingEnabled(true);
//            }

        player.setWebChromeClient(new WebChromeClient());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            player.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }
        player.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 6.2; Win64; x64; rv:21.0.0) Gecko/20121011 Firefox/21.0.0");

        //----------------------------To get Player Id-------------------------------------------

        player.addJavascriptInterface(new GetHtmlInterface((PlayerService) context), "HtmlViewer");
        player.setWebViewClient(new WebViewClient() {
                                    @Override
                                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                        return true;
                                    }

                                    @Override
                                    public void onPageFinished(WebView view, String url) {
                                        player.loadUrl(JavaScript.getHtmlScript());
                                    }
                                }
        );
    }

    public void loadUrl(String s, Map hashMap) {
        player.loadUrl(s, hashMap);
    }

    public static void loadScript(String s) {
        player.loadUrl(s);
    }

    public static WebView getPlayer() {
        return player;
    }

    public void destroy() {
        player.destroy();
    }

    public void setOnTouchListener(final CircularImageView closeImage, final RelativeLayout closeImageLayout, final int closeImgSize, final int closeImageLayoutSize, final LinearLayout playerView, final WindowManager windowManager, final LinearLayout serviceClose, final LinearLayout serviceCloseBackground, final RelativeLayout viewToHide, final int playerHeadSize, final int scrnWidth, final int scrnHeight) {
        final boolean needToShow[] = {true};
        player.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                WindowManager.LayoutParams param_player = (WindowManager.LayoutParams) playerView.getLayoutParams();
                serviceCloseBackground.setVisibility(View.VISIBLE);
                final Handler handleLongTouch = new Handler();
                final Runnable setVisible = new Runnable() {
                    @Override
                    public void run() {
                        if (needToShow[0]) {
                            serviceClose.setVisibility(View.VISIBLE);
                        }
                    }
                };
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialXP = param_player.x;
                        initialYP = param_player.y;
                        initialTouchXP = event.getRawX();
                        initialTouchYP = event.getRawY();
                        needToShow[0] = true;
                        handleLongTouch.postDelayed(setVisible, 100);
                        return true;
                    case MotionEvent.ACTION_UP:
                        finalTouchXP = event.getRawX();
                        finalTouchYP = event.getRawY();
                        needToShow[0] = false;
                        handleLongTouch.removeCallbacksAndMessages(null);
                        serviceCloseBackground.setVisibility(View.GONE);
                        serviceClose.setVisibility(View.GONE);
                        if (isClicked(initialTouchXP, finalTouchXP, initialTouchYP, finalTouchYP)) {
                            PlayerService.makePlayerVisibleAgain();
                        } else {
                            //stop if inside the close Button
                            if(PlayerService.isInsideClose){
                                PlayerService.stopThings();
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int newX, newY;
                        newX = initialXP + (int) (event.getRawX() - initialTouchXP);
                        newY = initialYP + (int) (event.getRawY() - initialTouchYP);
                        if (newX < 0) {
                            param_player.x = 0;
                        } else if ((playerHeadSize * 4 / 3) + newX > scrnWidth) {
                            param_player.x = scrnWidth - (playerHeadSize * 4 / 3);
                        } else {
                            param_player.x = newX;
                        }
                        if (newY < 0) {
                            param_player.y = 0;
                        } else if (newY + playerHeadSize > scrnHeight) {
                            param_player.y = scrnHeight - playerHeadSize;
                        } else {
                            param_player.y = newY;
                        }
                        windowManager.updateViewLayout(playerView, param_player);

                        int [] t = new int[2];
                        closeImageLayout.getLocationOnScreen(t);
                        PlayerService.updateIsInsideClose(param_player.x, param_player.y, t);
                        if(PlayerService.isInsideClose){
                            param_player.x = t[0] + closeImageLayoutSize / 10;
                            param_player.y = t[1] - PlayerService.getStatusBarHeight() + closeImageLayoutSize / 5;
                            param_player.width = closeImageLayoutSize;
                            param_player.height = closeImageLayoutSize;
                            if(closeImage.getLayoutParams().width == closeImgSize){
                                closeImage.getLayoutParams().width = closeImgSize * 2;
                                closeImage.getLayoutParams().height = closeImgSize * 2;
                                closeImage.requestLayout();
                            }
                        }
                        else{
                            param_player.width = playerHeadSize * 4 / 3;
                            param_player.height = playerHeadSize;
                            if(closeImage.getLayoutParams().width > closeImgSize){
                                closeImage.getLayoutParams().width = closeImgSize;
                                closeImage.getLayoutParams().height = closeImgSize;
                                closeImage.requestLayout();
                            }
                        }
                        windowManager.updateViewLayout(playerView, param_player);
                        return true;
            }
            return false;
        }

            private boolean isClicked(float startX, float endX, float startY, float endY) {
                float differenceX = Math.abs(startX - endX);
                float differenceY = Math.abs(startY - endY);
                if (differenceX >= 5 || differenceY >= 5) {
                    return false;
                }
                return true;
            }
        });
    }

    public void removeTouchListener() {
        player.setOnTouchListener(null);
    }
}
