package com.example.cti.musicfence.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

/**
 * Created by laboratorio on 04/12/17.
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val geofenceEvent = GeofencingEvent.fromIntent(intent)
        if (geofenceEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofenceEvent.errorCode)
            Log.e("Erro geofence", errorMessage)
            return
        }
        val geofenceTransition = geofenceEvent.geofenceTransition

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val triggeringGeofences = geofenceEvent.triggeringGeofences
            val geofenceTransitionDetails = getGeofenceTransitionDetails(
                this,
                geofenceTransition,
                triggeringGeofences
            )
            sendNotification(geofenceTransitionDetails)
            Log.e("Detalhes transition", geofenceTransitionDetails)
        } else
            Log.e("Error transition", "Transicao invalida")
    }

    private fun sendNotification(details: String) {
        TODO("Not yet implemented")
    }

    private fun getGeofenceTransitionDetails(
        receiver: GeofenceBroadcastReceiver,
        transition: Int,
        geofences: List<Geofence>
    ): String {
        TODO("Not yet implemented")
    }
}