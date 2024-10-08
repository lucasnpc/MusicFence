package com.example.cti.musicfence.model

import com.google.android.gms.location.Geofence

/**
 * Created by laboratorio on 01/12/17.
 */
class GeofenceModel : Geofence {
    var latitude = 0.0
    var longitude = 0.0
    var raio = 0.0
    var musica: String? = null
    private var id = 0

    override fun getRequestId(): String {
        return id.toString()
    }

}