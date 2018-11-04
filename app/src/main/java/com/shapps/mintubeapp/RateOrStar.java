package com.shapps.mintubeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class RateOrStar extends AppCompatActivity implements View.OnClickListener {

    Button rateOnPlayStore, starOnGitHub, issue, later, never;
    Intent browserIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_or_star);
        rateOnPlayStore = findViewById(R.id.rate_on_playstore);
        starOnGitHub = findViewById(R.id.star_on_GitHub);
        issue = findViewById(R.id.issue);
        later = findViewById(R.id.later);
        never = findViewById(R.id.never);

        rateOnPlayStore.setOnClickListener(this);
        starOnGitHub.setOnClickListener(this);
        issue.setOnClickListener(this);
        later.setOnClickListener(this);
        never.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.rate_on_playstore:
                //For Play Store : =
//                Uri uri = Uri.parse("market://details?id=" + this.getPackageName());
//                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
//                // To count with Play market backstack, After pressing back button,
//                // to taken back to our application, we need to add following flags to intent.
//                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
//                        Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
//                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
//                try {
//                    startActivity(goToMarket);
//                } catch (ActivityNotFoundException e) {
//                    startActivity(new Intent(Intent.ACTION_VIEW,
//                            Uri.parse("http://play.google.com/store/apps/details?id=" + this.getPackageName())));
//                }

                //For Amazon App Store
                String MARKET_AMAZON_URL = "amzn://apps/android?p=";
                String WEB_AMAZON_URL = "http://www.amazon.com/gp/mas/dl/android?p=";
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_AMAZON_URL + this.getPackageName()));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException anfe) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(WEB_AMAZON_URL + this.getPackageName()));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                this.finish();
                break;

            case R.id.star_on_GitHub:
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/imshyam/mintube"));
                startActivity(browserIntent);
                this.finish();
                break;
            case R.id.issue:
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/imshyam/mintube/issues"));
                startActivity(browserIntent);
                this.finish();
                break;
            case R.id.later:
                this.finish();
                break;
            case R.id.never:
                SharedPreferences.Editor editor =
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                editor.putInt(getString(R.string.count), 20);
                editor.apply();
                this.finish();
                break;
        }

    }

    void showToastMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT)
                .show();
    }
}
