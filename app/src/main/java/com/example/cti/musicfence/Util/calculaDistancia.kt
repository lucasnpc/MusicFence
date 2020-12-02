package com.example.cti.musicfence.Util

/**
 * Created by laboratorio on 15/12/17.
 */
object calculaDistancia {
    fun distance(lat1: Double, lat2: Double, lon1: Double, lon2: Double,
                 el1: Double, el2: Double): Double {
        val R = 6371 // Radius of the earth
        val latDistance = deg2rad(lat2 - lat1)
        val lonDistance = deg2rad(lon2 - lon1)
        val a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + (Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)))
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        var distance = R * c * 1000 // convert to meters
        val height = el1 - el2
        distance = Math.pow(distance, 2.0) + Math.pow(height, 2.0)
        return Math.sqrt(distance)
    }

    fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }
}