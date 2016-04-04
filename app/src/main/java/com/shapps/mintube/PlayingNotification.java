package com.shapps.mintube;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

public class PlayingNotification {

    private static final String NOTIFICATION_TAG = "Playing";
    private static Notification buildNotification;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void notify(final Context context,
                              final String exampleString, final int number) {
        final Resources res = context.getResources();

        final Bitmap picture = BitmapFactory.decodeResource(res, R.drawable.thumbnail);

        RemoteViews viewBig = new RemoteViews(
                context.getPackageName(),
                R.layout.notification_large
        );

        RemoteViews viewSmall = new RemoteViews(
                context.getPackageName(),
                R.layout.notification_small
        );

        viewBig.setImageViewResource(R.id.thumbnail, R.drawable.thumbnail);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)

                .setSmallIcon(R.drawable.thumbnail)

                .setVisibility(Notification.VISIBILITY_PUBLIC)

                .setContent(viewSmall)


                        // Automatically dismiss the notification when it is touched.
                .setAutoCancel(false);
        buildNotification = builder.build();

        buildNotification.bigContentView = viewBig;

        notify(context, buildNotification);
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private static void notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.notify(NOTIFICATION_TAG, 0, notification);
        } else {
            nm.notify(NOTIFICATION_TAG.hashCode(), notification);
        }
    }

    /**
     * Cancels any notifications of this type previously shown using
     * {@link #notify(Context, String, int)}.
     */
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public static void cancel(final Context context) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.cancel(NOTIFICATION_TAG, 0);
        } else {
            nm.cancel(NOTIFICATION_TAG.hashCode());
        }
    }

    public static Notification getBuildNotification() {
        return buildNotification;
    }
}
