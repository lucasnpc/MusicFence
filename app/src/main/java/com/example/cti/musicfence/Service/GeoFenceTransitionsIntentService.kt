package com.example.cti.musicfence.Service

import android.app.IntentService
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.example.cti.musicfence.Activity.MainActivity.Companion.entradaGeofence
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.maps.model.LatLng

/**
 * Created by laboratorio on 04/12/17.
 */
class GeoFenceTransitionsIntentService : IntentService(TAG) {
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    override fun onHandleIntent(intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        val location = geofencingEvent.triggeringLocation
        val latLng = LatLng(location.latitude, location.longitude)
        val geoFenceTransition = geofencingEvent.geofenceTransition
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            entradaGeofence(latLng)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private fun sendNotification(msg: String) {
        Log.i("Notificao", "Geofence $msg")
        //Intent notificationIntent = MainActivity.makeNotificationIntent(getApplicationContext(),msg);

        //TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        //stackBuilder.addParentStack(MainActivity.class);
        //stackBuilder.addNextIntent(notificationIntent);
        /*PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0,criarNotificacao(msg,notificationPendingIntent));*/
    }

    private fun criarNotificacao(msg: String, notifyPendingIntent: PendingIntent): Notification {
        val notificationBuilder = NotificationCompat.Builder(this)
        notificationBuilder.setColor(Color.BLUE)
                .setContentTitle(msg)
                .setContentText("Notificacao Geofence!!")
                .setContentIntent(notifyPendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE or Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
        return notificationBuilder.build()
    }

    companion object {
        private val TAG = GeoFenceTransitionsIntentService::class.java.simpleName
    }
}