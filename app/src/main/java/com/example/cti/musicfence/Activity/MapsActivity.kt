package com.example.cti.musicfence.Activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.example.cti.musicfence.Model.geoFence
import com.example.cti.musicfence.R
import com.example.cti.musicfence.Service.GeoFenceTransitionsIntentService
import com.example.cti.musicfence.Util.dbFunc
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.lang.Float

class MapsActivity : FragmentActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {
    private var mMap: GoogleMap? = null
    private val googleApiClient: GoogleApiClient? = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
    var func: dbFunc? = null
    var nomeMusica: String? = null
    private var button: Button? = null
    private var geofencingClient: GeofencingClient? = null
    private val duracaoGeofence = (60 * 60 + 1000).toLong()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapView: MapView = findViewById(R.id.mapView)
        mapView.onResume()
        mapView.getMapAsync(this)
        googleApiClient?.connect()
        val intent = intent
        nomeMusica = intent.getStringExtra("nomeMusica")
        Log.d("Musica", nomeMusica!!)
        func = dbFunc(this)
        button = findViewById<View>(R.id.bDeleteFence) as Button
        geofencingClient = LocationServices.getGeofencingClient(this)
    }

    private fun readMyCurrentCoordenadas() {
        val locationListener = meuLocationListener()
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        var location: Location? = null
        var latitude = 0.0
        var longitude = 0.0
        if (!isGPSEnable && !isNetworkEnabled) {
            Log.i("Erro", "Necessita de GPS e Internet")
        } else {
            if (isNetworkEnabled) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0f, locationListener)
                Log.d("Internet", "Network Ativo")
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                }
            }
            if (isGPSEnable) {
                if (location == null) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0f, locationListener)
                    Log.d("GPS", "GPS Ativo")
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                    }
                }
            }
        }
        val minhaPos = LatLng(latitude, longitude)
        mMap!!.addMarker(MarkerOptions().position(minhaPos).title("Sua Posicao")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(minhaPos, 15f))
        Log.i("Posicao", "Lat: $latitude|Long: $longitude")
    }

    fun callAcessLocation() {
        Log.i("Chamada", "Funcao Ativa")
        readMyCurrentCoordenadas()
    }

    override fun onStop() {
        super.onStop()
        pararConexao()
    }

    fun pararConexao() {
        if (googleApiClient!!.isConnected) {
            googleApiClient!!.disconnect()
        }
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
                if (func!!.adicionar(latLng.latitude, latLng.longitude, raio[0], nomeMusica) == true) {
                    val g = geoFence()
                    val geofence = createGeofence(latLng, raio[0])
                    val geofencingRequest = geofencingRequest(geofence)
                    addGeo(geofencingRequest)
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
        //LatLng suaPosicao = new LatLng(latitude, longitude);
        //mMap.addMarker(new MarkerOptions().position(suaPosicao).title("Sua posicao"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(suaPosicao));
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

    // Add a marker in Sydney and move the camera
    /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    private inner class meuLocationListener : LocationListener {
        override fun onLocationChanged(location: Location) {}
        override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
        override fun onProviderEnabled(s: String) {}
        override fun onProviderDisabled(s: String) {}
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        pararConexao()
    }

    override fun onConnected(bundle: Bundle?) {}
    override fun onConnectionSuspended(i: Int) {}
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
            val circle = mMap!!.addCircle(circleOptions)
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeo(request: GeofencingRequest) {
        Log.d("Geo Add: ", "Adicionada.")
        LocationServices.GeofencingApi.addGeofences(googleApiClient, request, CriargeoPendingIntent()
        ).setResultCallback(this)
    }

    private fun createGeofence(latLng: LatLng, radius: Double): Geofence {
        val g = geoFence()
        Log.d("Criar geofence", "Criada.")
        return Geofence.Builder()
                .setRequestId(g.requestId)
                .setCircularRegion(latLng.latitude, latLng.longitude, radius.toFloat())
                .setExpirationDuration(duracaoGeofence)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
    }

    private fun geofencingRequest(geofence: Geofence): GeofencingRequest {
        Log.d("GeoRequest ", "Request")
        return GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()
    }

    private val geoPendingIntent: PendingIntent? = null
    private fun CriargeoPendingIntent(): PendingIntent {
        Log.d("Criar Pending Intent", "Criado.")
        if (geoPendingIntent != null) return geoPendingIntent
        val intent = Intent(this, GeoFenceTransitionsIntentService::class.java)
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}