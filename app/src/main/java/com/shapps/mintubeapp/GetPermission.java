package com.shapps.mintubeapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;

/**
 * Created by shyam on 4/3/16.
 */
public class GetPermission extends AppCompatActivity {

    public static int OVERLAY_PERMISSION_REQ_CODE = 12345;
    public static int OVERLAY_PERMISSION_REQ_BACK_TO_ACT_CODE = 23456;
    String vId, pId;
    int permissionCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_get_permission);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            vId = extras.getString("VID");
            pId = extras.getString("PID");
            permissionCode = OVERLAY_PERMISSION_REQ_CODE;
        } else {
            permissionCode = OVERLAY_PERMISSION_REQ_BACK_TO_ACT_CODE;
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button getPermission = findViewById(R.id.get_permission);
        getPermission.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && !Settings.canDrawOverlays(GetPermission.this)) {
                Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(i, permissionCode);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    needPermissionDialog(requestCode);
                } else {
                    Intent i = new Intent(this, PlayerService.class);
                    i.putExtra("VID_ID", vId);
                    i.putExtra("PLAYLIST_ID", pId);
                    i.setAction(Constants.ACTION.STARTFOREGROUND_WEB_ACTION);
                    startService(i);
                    finish();
                }
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    needPermissionDialog(requestCode);
                } else {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
            }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void needPermissionDialog(final int requestCode) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("You need to grant the permission.");
            builder.setPositiveButton("OK",
                    (dialog, which) -> {
                        Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(i, OVERLAY_PERMISSION_REQ_CODE);
                    });
            builder.setNegativeButton("Cancel", (dialog, which) -> {

            });
            builder.setCancelable(false);
            builder.show();
        }
    }
}