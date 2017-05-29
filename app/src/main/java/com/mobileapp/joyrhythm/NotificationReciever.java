package com.mobileapp.joyrhythm;

/**
 * Created by soojin on 2017-05-25.
 */

        import android.app.NotificationManager;
        import android.app.PendingIntent;
        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.support.v4.app.NotificationCompat;

        import com.mobileapp.joyrhythm.kakao.LoginActivity;

public class NotificationReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent1 = new Intent(context,LoginActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,100,intent1,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context).
                setSmallIcon(R.drawable.icon).
                setContentIntent(pendingIntent).
                setContentText("It's time to play!").
                setContentTitle("Joy Rhythm").
        setAutoCancel(true);
        notificationManager.notify(100,builder.build());

    }
}