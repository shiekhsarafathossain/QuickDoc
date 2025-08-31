package com.ewucsesummer25.quickdoc;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class AppointmentNotificationReceiver extends BroadcastReceiver {

    public static final String ACTION_SHOW_APPOINTMENT_REMINDER = "com.ewucsesummer25.quickdoc.SHOW_APPOINTMENT_REMINDER";
    private static final String CHANNEL_ID = "APPOINTMENT_CHANNEL";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get appointment details from the original intent
        String doctorName = intent.getStringExtra("DOCTOR_NAME");
        String appointmentTime = intent.getStringExtra("APPOINTMENT_TIME");
        int notificationId = intent.getIntExtra("NOTIFICATION_ID", 0);

        Intent localIntent = new Intent(ACTION_SHOW_APPOINTMENT_REMINDER);
        localIntent.putExtra("DOCTOR_NAME", doctorName);
        localIntent.putExtra("APPOINTMENT_TIME", appointmentTime);
        localIntent.putExtra("NOTIFICATION_ID", notificationId);
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Appointment Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notifications for upcoming appointments");
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Appointment Reminder")
                .setContentText("You have an appointment with " + doctorName + " at " + appointmentTime)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify(notificationId, builder.build());
    }
}