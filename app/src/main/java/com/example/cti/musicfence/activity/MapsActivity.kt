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
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.cti.musicfence.R
import com.example.cti.musicfence.service.GeofenceBroadcastReceiver
import com.example.cti.musicfence.util.LerCoordenadaAtual
import com.example.cti.musicfence.util.DatabaseFunc
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
import java.lang.Float

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, ResultCallback<Status> {
    private var mMap: GoogleMap? = null
    private var func: DatabaseFunc? = null
    var nomeMusica: String? = null
    private var button: Button? = null
    lateinit var geofencingClient: GeofencingClient

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
        func = DatabaseFunc(this)
    }

    fun callAcessLocation() {
        Log.i("Chamada", "Funcao Ativa")
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
        else
            LerCoordenadaAtual.lerCoordenadas(this, meuLocationListener(),mMap)
    }

    fun desenhaGeoFence() {
        for (g in func!!.listar()) {
            val latLng = LatLng(g.latitude, g.longitude)
            val item = g.raio.toString()
            val music = g.musica
            addMarker(latLng, item, music)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        callAcessLocation()
        desenhaGeoFence()
        mMap!!.setOnMapLongClickListener { latLng ->
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            val names = arrayOf("50", "100", "200", "500")
            val raio = DoubleArray(1)
            val alertDialog = AlertDialog.Builder(this@MapsActivity)
                    .create()
            val layoutInflater = layoutInflater
            val convertView = layoutInflater.inflate(R.layout.custom, null) as View
            alertDialog.setView(convertView)
            alertDialog.setTitle("Selecione o Raio")
            val listView = convertView.findViewById<View>(R.id.listView1) as ListView
            val adapter = ArrayAdapter(this@MapsActivity, android.R.layout.simple_list_item_1, names)
            listView.adapter = adapter
            alertDialog.show()
            listView.onItemClickListener = OnItemClickListener { adapterView, view, i, l ->
                val item = (view as TextView).text.toString()
                addMarker(latLng, item, nomeMusica)
                raio[0] = item.toDouble()
                alertDialog.dismiss()
                Log.d("Latitude do Click", latLng.latitude.toString())
                Log.d("Longitude do Click", latLng.longitude.toString())
                Log.d("Raio ", raio[0].toString())
                Log.d("Musica nome", nomeMusica!!)
                if (func!!.adicionar(latLng.latitude, latLng.longitude, raio[0], nomeMusica)) {
                    val geofence = createGeofence(latLng, raio[0])
                    val geofencingRequest = getGeofencingRequest(geofence)
                    addGeo(geofencingRequest, geoPendingIntent)
                    Toast.makeText(this@MapsActivity, "Geofence adicionada com sucesso.", Toast.LENGTH_SHORT).show()
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

    fun deleteFence(latLng: LatLng) {
        func!!.remover(latLng.latitude, latLng.longitude)
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

    fun addMarker(point: LatLng?, item: String, musica: String?) {
        if (point != null) {
            mMap!!.addMarker(MarkerOptions().position(LatLng(point.latitude, point.longitude))
                    .title("Geofence $musica")
                    .snippet("Raio $item"))
            val circleOptions = CircleOptions()
                    .center(LatLng(point.latitude, point.longitude))
                    .radius(Float.valueOf(item).toDouble())
                    .fillColor(0x40ff0000)
                    .strokeColor(Color.TRANSPARENT)
                    .strokeWidth(2f)
            mMap!!.addCircle(circleOptions)
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeo(request: GeofencingRequest, geoPendingIntent: PendingIntent) {
        Log.d("Geo Add: ", "Adicionada.")
        geofencingClient?.addGeofences(request, geoPendingIntent)?.run {
            addOnSuccessListener {  }
            addOnFailureListener {  }
        }
    }

    private fun createGeofence(latLng: LatLng, radius: Double): com.google.android.gms.location.Geofence {
        val g = com.example.cti.musicfence.model.GeofenceModel()
        Log.d("Criar geofence", "Criada.")
        return Geofence.Builder()
                .setRequestId(g.requestId)
                .setCircularRegion(latLng.latitude, latLng.longitude, radius.toFloat())
                .setExpirationDuration((60 * 60 + 1000).toLong())
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
    }

    private fun getGeofencingRequest(geofence: com.google.android.gms.location.Geofence): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofence(geofence)
        }.build()
    }

    private val geoPendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(this, 0 , intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}