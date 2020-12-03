package com.example.cti.musicfence.Model

import android.util.Log
import com.google.android.gms.location.Geofence

/**
 * Created by laboratorio on 01/12/17.
 */
class geoFence : Geofence {
    var latitude = 0.0
    var longitude = 0.0
    var raio = 0.0
    var musica: String? = null
    private var id = 0

    constructor(id: Int, latitude: Double, longitude: Double, raio: Double, musica: String?) {
        this.latitude = latitude
        this.longitude = longitude
        this.raio = raio
        this.musica = musica
        this.id = id
    }

    constructor() {}

    override fun getRequestId(): String {
        return id.toString()
    }

    fun setId(id: Int) {
        Log.d("id geofence", this.id.toString())
        this.id = id
    }
}