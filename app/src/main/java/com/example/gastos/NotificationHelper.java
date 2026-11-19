package com.example.gastos;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class NotificationHelper {
    private static final String CHANNEL_ID = "gastos_channel";

    @SuppressLint("MissingPermission")
    public static void show(Context ctx, String title, String text) {
        // Android 13+: sin permiso concedido no se notifica
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // Android 8+: las notificaciones necesitan un canal
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Gastos", NotificationManager.IMPORTANCE_DEFAULT);
            ctx.getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true);
        NotificationManagerCompat.from(ctx).notify((int) System.currentTimeMillis(), b.build());
    }
}
