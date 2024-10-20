package com.example.cti.musicfence.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.cti.musicfence.R
import com.example.cti.musicfence.model.GeofenceModel
import com.example.cti.musicfence.service.GeofenceBroadcastReceiver
import com.example.cti.musicfence.util.DatabaseFunc
import com.example.cti.musicfence.util.LerCoordenadaAtual
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.UUID

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, ResultCallback<Status> {
    private var mMap: GoogleMap? = null
    private val func: DatabaseFunc by lazy {
        DatabaseFunc(this)
    }
    private var nomeMusica: String? = null
    private var button: Button? = null
    private lateinit var geofencingClient: GeofencingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapView: MapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)
        val intent = intent
        nomeMusica = intent.getStringExtra("nomeMusica")
        Log.d("Musica", nomeMusica!!)
        button = findViewById<View>(R.id.bDeleteFence) as Button
        geofencingClient = LocationServices.getGeofencingClient(this)
    }

    private fun callAcessLocation() {
        Log.i("Chamada", "Funcao Ativa")
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        } else
            LerCoordenadaAtual.lerCoordenadas(this, meuLocationListener(), mMap)
    }

    private fun desenhaGeoFence() {
        for (g in func.listar()) {
            addMarker(LatLng(g.latitude, g.longitude), g.radius, g.musicName)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        callAcessLocation()
        desenhaGeoFence()
        mMap!!.setOnMapLongClickListener { latLng ->
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            val geofenceRadiusOptions = arrayOf(50f, 100f, 200f, 500f)
            val raio = FloatArray(1)
            val alertDialog = AlertDialog.Builder(this@MapsActivity)
                .create()
            val layoutInflater = layoutInflater
            val convertView = layoutInflater.inflate(R.layout.custom, null) as View
            alertDialog.setView(convertView)
            alertDialog.setTitle("Selecione o Raio")
            val listView = convertView.findViewById<View>(R.id.listView1) as ListView
            val adapter =
                ArrayAdapter(
                    this@MapsActivity,
                    android.R.layout.simple_list_item_1,
                    geofenceRadiusOptions
                )
            listView.adapter = adapter
            alertDialog.show()
            listView.setOnItemClickListener { parent, view, position, id ->
                view as TextView
                val radius = view.text.toString().toFloat()
                addMarker(latLng, radius, nomeMusica)
                raio[0] = radius
                alertDialog.dismiss()
                if (func.adicionar(
                        latLng.latitude,
                        latLng.longitude,
                        raio[0],
                        nomeMusica
                    )
                ) {
                    val geofence = createGeofence(latLng, raio[0])
                    val geofencingRequest = getGeofencingRequest(geofence)
                    addGeo(geofencingRequest, geoPendingIntent)
                    Toast.makeText(
                        this@MapsActivity,
                        "Geofence adicionada com sucesso.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        mMap!!.setOnMarkerClickListener { marker ->
            val latLng1 = marker.position
            button!!.visibility = View.VISIBLE
            button!!.setOnClickListener {
                deleteFence(latLng1)
                marker.remove()
                button!!.visibility = View.INVISIBLE
            }
            false
        }
    }

    private fun deleteFence(latLng: LatLng) {
        func.remover(latLng.latitude, latLng.longitude)
        Toast.makeText(this, "GeoFence removida com sucesso.", Toast.LENGTH_SHORT).show()
    }

    override fun onResult(status: Status) {
        Log.i("Resultado", "" + status)
        if (status.isSuccess) {
            Log.d("Criacao", "bem sucedida.")
        }
    }

    private inner class meuLocationListener : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.e("Location", location.toString())
        }

        override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
        override fun onProviderEnabled(s: String) {}
        override fun onProviderDisabled(s: String) {}
    }

    private fun addMarker(point: LatLng?, radius: Float, musicName: String?) {
        if (point != null) {
            mMap!!.addMarker(
                MarkerOptions().position(LatLng(point.latitude, point.longitude))
                    .title("Geofence $musicName")
                    .snippet("Raio $radius")
            )
            val circleOptions = CircleOptions()
                .center(LatLng(point.latitude, point.longitude))
                .radius(radius.toDouble())
                .fillColor(0x40ff0000)
                .strokeColor(Color.TRANSPARENT)
                .strokeWidth(2f)
            mMap!!.addCircle(circleOptions)
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeo(request: GeofencingRequest, geoPendingIntent: PendingIntent) {
        Log.d("Geo Add: ", "Adicionada.")
        geofencingClient.addGeofences(request, geoPendingIntent).run {
            addOnSuccessListener { }
            addOnFailureListener { }
        }
    }

    private fun createGeofence(latLng: LatLng, radius: Float): Geofence {
        return GeofenceModel(
            latitude = latLng.latitude,
            longitude = latLng.longitude,
            radius = radius,
        )
    }

    private fun getGeofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofence(geofence)
        }.build()
    }

    private val geoPendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}