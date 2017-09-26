package com.behealthy.gincos.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import com.behealthy.gincos.R;
import com.behealthy.gincos.activities.MainActivity;

public class NotificationReceiver extends BroadcastReceiver {

    private static final int DAILY_REGISTER_RC = 654;

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent dailyRegisterIntent = new Intent(context,MainActivity.class);
        dailyRegisterIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pi = PendingIntent.getActivity(context,DAILY_REGISTER_RC,dailyRegisterIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentIntent(pi)
                .setContentTitle(context.getString(R.string.notification_title))
                .setAutoCancel(true)
                .setContentText(context.getString(R.string.notification_description))
                .setSmallIcon(R.drawable.ic_notification);

        nm.notify(DAILY_REGISTER_RC,builder.build());
    }
}
