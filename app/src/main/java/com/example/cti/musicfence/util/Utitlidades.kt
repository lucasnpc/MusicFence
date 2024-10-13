package com.example.cti.musicfence.util

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.cti.musicfence.model.DBGateway
import com.example.cti.musicfence.model.GeofenceModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class CalcDistancia {
    companion object {
        @JvmStatic
        fun distance(
            lat1: Double, lat2: Double, lon1: Double, lon2: Double,
            el1: Double, el2: Double
        ): Double {
            val earthRadius = 6371 // Radius of the earth
            val latDistance = deg2rad(lat2 - lat1)
            val lonDistance = deg2rad(lon2 - lon1)
            val a = (sin(latDistance / 2) * sin(latDistance / 2)
                    + (cos(deg2rad(lat1)) * cos(deg2rad(lat2))
                    * sin(lonDistance / 2) * sin(lonDistance / 2)))
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            var distance = earthRadius * c * 1000 // convert to meters
            val height = el1 - el2
            distance = distance.pow(2.0) + height.pow(2.0)
            return sqrt(distance)
        }

        @JvmStatic
        fun deg2rad(deg: Double): Double {
            return deg * Math.PI / 180.0
        }
    }
}

class DatabaseFunc(context: Context?) {
    private val tabela = "geoFence"
    private val gateway: DBGateway = context?.let { DBGateway.getInstance(it) }!!
    fun adicionar(latitude: Double, longitude: Double, raio: Double, musica: String?): Boolean {
        val contentValues = ContentValues()
        contentValues.put("latitude", latitude)
        contentValues.put("longitude", longitude)
        contentValues.put("raio", raio)
        contentValues.put("music", musica)
        return gateway.database.insert(tabela, null, contentValues) > 0
    }

    fun remover(latitude: Double, longitude: Double): Boolean {
        val cv = arrayOf("" + latitude, "" + longitude)
        return gateway.database.delete(tabela, "latitude=? and longitude=?", cv) > 0
    }

    @SuppressLint("Recycle")
    fun listar(): ArrayList<GeofenceModel> {
        val cursor = gateway.database.rawQuery("SELECT * FROM geoFence", null)
        val GeofenceModels = ArrayList<GeofenceModel>()
        while (cursor.moveToNext()) {
            val g = GeofenceModel()
            g.latitude = cursor.getDouble(cursor.getColumnIndex("latitude"))
            g.longitude = cursor.getDouble(cursor.getColumnIndex("longitude"))
            g.raio = cursor.getDouble(cursor.getColumnIndex("raio"))
            g.musica = cursor.getString(cursor.getColumnIndex("music"))
            GeofenceModels.add(g)
        }
        return GeofenceModels
    }

//    @SuppressLint("Recycle")
//    fun retornaMusicFence(latLng: LatLng): String {
//        val cursor = gateway.database.rawQuery(
//            "SELECT * FROM geoFence WHERE latitude=" +
//                    latLng.latitude + " and longitude=" + latLng.longitude,
//            null
//        )
//        val music = Music()
//        while (cursor.moveToNext()) {
//            music.title = cursor.getString(cursor.getColumnIndex("music"))
//        }
//        return music.title.toString()
//    }

}

class LerCoordenadaAtual() {

    companion object {
        @JvmStatic
        @SuppressLint("MissingPermission")
        fun lerCoordenadas(
            context: Context?,
            locationListener: LocationListener,
            mMap: GoogleMap?
        ) {
            val locationListener = locationListener
            val locationManager =
                context?.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
            val isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled =
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            var location: Location? = null
            var latitude = 0.0
            var longitude = 0.0
            if (!isGPSEnable && !isNetworkEnabled) {
                Log.i("Erro", "Necessita de GPS e Internet")
            } else {
                if (isNetworkEnabled) {

                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        2000,
                        0f,
                        locationListener
                    )
                    Log.d("Internet", "Network Ativo")
                    location =
                        locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                    }
                }
                if (isGPSEnable) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            2000,
                            0f,
                            locationListener
                        )
                        Log.d("GPS", "GPS Ativo")
                        location =
                            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        if (location != null) {
                            latitude = location.latitude
                            longitude = location.longitude
                        }
                    }
                }
            }

            val minhaPos = LatLng(latitude, longitude)
            mMap!!.addMarker(
                MarkerOptions().position(minhaPos).title("Sua Posicao")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(minhaPos, 15f))
            Log.i("Posicao", "Lat: $latitude|Long: $longitude")
        }
    }
}