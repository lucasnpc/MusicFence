package com.example.cti.musicfence.model

import com.google.android.gms.location.Geofence
import java.util.UUID


/**
 * Created by laboratorio on 01/12/17.
 */
data class GeofenceModel(
    private var requestId: String = UUID.randomUUID().toString(),
    private val latitude: Double,
    private val longitude: Double,
    private val radius: Float,
    private val expirationTime: Long = (60 * 60 + 1000).toLong(),
    private val transitionTypes: Int = Geofence.GEOFENCE_TRANSITION_ENTER or
            Geofence.GEOFENCE_TRANSITION_EXIT,
    val musicName: String = ""
) : Geofence {
    override fun getRequestId(): String {
        return requestId
    }

    override fun getTransitionTypes(): Int {
        return transitionTypes
    }

    override fun getExpirationTime(): Long {
        return expirationTime
    }

    override fun getLatitude(): Double {
        return latitude
    }

    override fun getLongitude(): Double {
        return longitude
    }

    override fun getRadius(): Float {
        return radius
    }

    override fun getNotificationResponsiveness(): Int {
        TODO("Not yet implemented")
    }

    override fun getLoiteringDelay(): Int {
        TODO("Not yet implemented")
    }
}