package com.example.cti.musicfence.activity

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.cti.musicfence.R
import com.example.cti.musicfence.activity.utils.handlePermissionAboveApi33
import com.example.cti.musicfence.activity.utils.handlePermissionBeforeApi33
import com.example.cti.musicfence.model.Musica
import com.example.cti.musicfence.service.GeofenceBroadcastReceiver
import com.example.cti.musicfence.service.Mp3player
import com.example.cti.musicfence.service.Mp3player.PlayerBinder
import com.example.cti.musicfence.util.CalcDistancia
import com.example.cti.musicfence.util.DatabaseFunc
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

class MainActivity : AppCompatActivity(), ServiceConnection, ResultCallback<Status>,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private var conexao: ServiceConnection? = null
    private var listaViewMusicas: ListView? = null
    private val duracaoGeofence = 60 * 60 + 1000.toLong()
    private var geofencingClient: GeofencingClient? =
        context?.let { LocationServices.getGeofencingClient(it) }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)
        seekBar = findViewById(R.id.music_progress)
        listaViewMusicas = findViewById<View>(R.id.lista_musicas) as ListView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            handlePermissionAboveApi33 {
                configurarLista()
            }
        } else {
            handlePermissionBeforeApi33 {
                configurarLista()
            }
        }

        musicaAtual = findViewById<View>(R.id.textView2) as TextView
        val intentGeofence = Intent(".GeoFenceTransitionsIntentService")
        intentGeofence.setPackage("com.example.cti.")
        startService(intentGeofence)
        val dbFunc = DatabaseFunc(this)
        for (g in dbFunc.listar()) {
            val g2 = createGeofence(g)
            val geofencingRequest = geofencingRequest(g2)
            geofencingClient?.addGeofences(geofencingRequest, criarGeoPendingIntent())
                ?.addOnSuccessListener(this) { Log.d("Status", "sucesso.") }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_READ_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    configurarLista()
                }
            }
        }
    }

    private fun createGeofence(g: com.example.cti.musicfence.model.GeofenceModel): com.google.android.gms.location.Geofence {
        Log.d("Criar geofence", "Criada.")
        return Geofence.Builder()
            .setRequestId("0")
            .setCircularRegion(g.latitude, g.longitude, g.raio.toFloat())
            .setExpirationDuration(duracaoGeofence)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or
                        Geofence.GEOFENCE_TRANSITION_EXIT
            )
            .build()
    }

    private val geoPendingIntent: PendingIntent? = null

    private fun criarGeoPendingIntent(): PendingIntent {
        Log.d("Criar Pending Intent", "Criado.")
        if (geoPendingIntent != null) return geoPendingIntent
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun geofencingRequest(geofence: com.google.android.gms.location.Geofence): GeofencingRequest {
        Log.d("GeoRequest ", "Request")
        return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
    }

    private fun configurarLista() {
        listaMusic = allMusic
        val intentService =
            Intent("com.example.cti.musicfence.SERVICE_PLAYER_2").putParcelableArrayListExtra(
                "listaMusicas",
                listaMusic
            )
        intentService.setPackage("com.example.cti.")
        startService(intentService)
        val adapter = ArrayAdapter(this, R.layout.lista_titulo_sumario_texto, listaMusic!!)
        listaViewMusicas!!.adapter = adapter
        listaViewMusicas!!.onItemClickListener =
            OnItemClickListener { parent, view, position, id -> binder!!.playMusic(position) }
        listaViewMusicas!!.onItemLongClickListener =
            OnItemLongClickListener { adapterView, view, i, l ->
                val intent = Intent(this@MainActivity, MapsActivity::class.java)
                intent.putExtra("nomeMusica", (view as TextView).text.toString())
                startActivity(intent)
                false
            }
        conexao = this
        if (binder == null || !binder!!.isBinderAlive) {
            val intentPlayer = Intent(this, Mp3player::class.java)
            bindService(intentPlayer, conexao as MainActivity, BIND_AUTO_CREATE)
            startService(intentPlayer)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val intentService = Intent("com.example.cti.musicfence.SERVICE_PLAYER_2")
        intentService.setPackage("com.example.cti.")
        stopService(intentService)
        val intentGeofence = Intent(".GeoFenceTransitionsIntentService")
        intentGeofence.setPackage("com.example.cti.")
        stopService(intentGeofence)
    }

    fun playMusic(view: View?) {
        binder?.play()
    }

    fun pauseMusic(view: View?) {
        binder?.pause()
    }

    fun stopMusic(view: View?) {
        binder?.stop()
    }

    fun nextMusic(view: View?) {
        binder?.next()
    }

    fun previousMusic(view: View?) {
        binder?.previous()
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        binder = service as PlayerBinder
        //this.musicas.setText(binder.getPath());
        Mp3player.playlist = listaMusic
        try {
            seekBar!!.max = binder!!.getDuration()
            seekBar!!.progress = binder!!.getCurrentPosition()
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        binder = null
    }

    private val allMusic: ArrayList<Musica>
        get() {
            val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION
            )
            val cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection,
                null,
                null
            )
            val songs = ArrayList<Musica>()
            if (cursor != null) while (cursor.moveToNext()) {
                val musica = Musica(
                    cursor.getInt(0), cursor.getString(1), cursor.getString(2),
                    cursor.getString(3), cursor.getString(4), cursor.getInt(5)
                )
                songs.add(musica)
            }
            return songs
        }

    override fun onResult(status: Status) {
        if (status.isSuccess) Log.e("Tag", "O sistema esta monitorando") else Log.e(
            "Tag",
            "o SISTEMA NAO ESTA MONITORANDO"
        )
    }

    override fun onConnected(bundle: Bundle?) {}
    override fun onConnectionSuspended(i: Int) {}
    override fun onConnectionFailed(connectionResult: ConnectionResult) {}

    companion object {
        @JvmField
        var seekBar: SeekBar? = null
        private var binder: PlayerBinder? = null
        var listaMusic: ArrayList<Musica>? = null

        @JvmField
        var musicaAtual: TextView? = null
        private val context: Context? = null
        const val MY_PERMISSIONS_READ_EXTERNAL_STORAGE = 1

        @JvmStatic
        fun entradaGeofence(latLng: LatLng) {
            Log.d("Id Geofence", "entrou em uma geofence")
            val dbFunc = DatabaseFunc(context)
            for (geo in dbFunc.listar()) {
                Log.d(geo.musica, geo.latitude.toString() + " - " + geo.longitude)
                if (CalcDistancia.distance(
                        latLng.latitude,
                        geo.latitude,
                        latLng.longitude,
                        geo.longitude,
                        1.0,
                        1.0
                    ) < geo.raio
                ) {
                    val nomeMusica = geo.musica
                    Log.i("Musica Geo", nomeMusica.toString())
                    var index = 0
                    for (m in listaMusic!!) {
                        if (m.titulo!!.contains(nomeMusica.toString())) {
                            Log.d("teste", "Play music")
                            binder!!.playMusic(index)
                            binder!!.play()
                        }
                        index++
                    }
                }
            }
        }

    }
}