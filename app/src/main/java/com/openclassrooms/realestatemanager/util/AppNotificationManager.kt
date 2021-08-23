package com.openclassrooms.realestatemanager.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.NotificationManagerCompat
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.models.Property
import java.util.*

class AppNotificationManager(var mContext: Context) {

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels: MutableList<NotificationChannel> = ArrayList()
            channels.add(createAppNotificationChanel(
                PROPERTIES_CHANEL_ID,
                mContext.getString(R.string.notification_channel_properties_name),
                mContext.getString(R.string.notification_channel_properties_description),
                NotificationManagerCompat.IMPORTANCE_HIGH))
            channels.add(createAppNotificationChanel(
                APP_CHANEL_ID,
                mContext.getString(R.string.notification_channel_app_name),
                mContext.getString(R.string.notification_channel_app_description),
                NotificationManagerCompat.IMPORTANCE_DEFAULT))
            val notificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannels(channels)
        }
    }

    private fun showNotification(@NonNull notification: Notification, notificationId: Int) {
        val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(mContext)
        notificationManager.notify(notificationId, notification)
    }

    private fun createCustomNotification(message: String, ): Notification {
        return Builder(mContext, PROPERTIES_CHANEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(mContext.getString(R.string.notification_title))
            .setContentText(message)
            .setAutoCancel(true)
            .setGroup(GROUP_KEY_PROPERTIES)
            .build()
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createAppNotificationChanel(
        chanelId: String,
        chanelName: String,
        chanelDescription: String,
        chanelImportance: Int,
    ): NotificationChannel {
        val channel = NotificationChannel(chanelId, chanelName, chanelImportance)
        channel.description = chanelDescription
        return channel
    }

    fun showNotification(property: Property?, message: String) {
        val notificationId = (BASE_NOTIFICATION_ID.toInt() + property.hashCode())
        val notification = createCustomNotification(message)
        showNotification(notification, notificationId)
    }

    companion object {
        private const val APP_PACKAGE = "com.openclassrooms.realestatemanager"
        private const val PROPERTIES_CHANEL_ID = "$APP_PACKAGE.CITIES_CHANNEL"
        private const val APP_CHANEL_ID = "$APP_PACKAGE.APP_CHANNEL"
        private const val GROUP_KEY_PROPERTIES = "$APP_CHANEL_ID.CITIES_GROUP"
        private const val BASE_NOTIFICATION_ID = 100L
    }

}

