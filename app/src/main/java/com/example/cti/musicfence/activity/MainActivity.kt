package com.example.cti.musicfence.activity

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.cti.musicfence.R
import com.example.cti.musicfence.activity.utils.getAllMusics
import com.example.cti.musicfence.activity.utils.handlePermissionAboveApi33
import com.example.cti.musicfence.activity.utils.handlePermissionBeforeApi33
import com.example.cti.musicfence.databinding.ActivityInicioBinding
import com.example.cti.musicfence.musicPlayer.service.Mp3player
import com.example.cti.musicfence.musicPlayer.service.Mp3player.PlayerBinder
import com.example.cti.musicfence.musicPlayer.utils.MusicPlayer.mediaPlayer
import com.example.cti.musicfence.musicPlayer.utils.MusicPlayer.musicaAtual
import com.example.cti.musicfence.musicPlayer.utils.MusicPlayer.playlist
import com.example.cti.musicfence.musicPlayer.utils.MusicPlayer.seekBar
import com.example.cti.musicfence.service.GeofenceBroadcastReceiver
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

    private val binding: ActivityInicioBinding by lazy {
        ActivityInicioBinding.inflate(layoutInflater)
    }

    private var binder: PlayerBinder? = null

    private var conexao: ServiceConnection? = null
    private val duracaoGeofence = 60 * 60 + 1000.toLong()
    private val geofencingClient: GeofencingClient by lazy {
        LocationServices.getGeofencingClient(
            this
        )
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setClickListeners()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            handlePermissionAboveApi33 {
                setupPlayList()
            }
        } else {
            handlePermissionBeforeApi33 {
                setupPlayList()
            }
        }

        musicaAtual = binding.currentMusicPlaying
        seekBar = binding.musicProgress
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
                    setupPlayList()
                }
            }
        }
    }

    private fun createGeofence(g: com.example.cti.musicfence.model.GeofenceModel): Geofence {
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

    private fun geofencingRequest(geofence: Geofence): GeofencingRequest {
        Log.d("GeoRequest ", "Request")
        return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
    }

    private fun setupPlayList() {
        playlist = getAllMusics()
        val intentService = Intent("com.example.cti.musicfence.SERVICE_PLAYER_2")
        intentService.setPackage("com.example.cti.")
        startService(intentService)
        binding.listaMusicas.apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                R.layout.lista_titulo_sumario_texto,
                playlist.map { it.title }
            )
            setOnItemClickListener { _, _, position, _ ->
                binder?.playMusic(position)

            }
            setOnItemLongClickListener { _, view, _, _ ->
                val intent = Intent(this@MainActivity, MapsActivity::class.java)
                intent.putExtra("nomeMusica", (view as TextView).text.toString())
                startActivity(intent)
                false
            }
        }
        conexao = this
        if (binder == null || binder?.isBinderAlive == false) {
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

    private fun setClickListeners() {
        binding.run {
            buttonPlay.setOnClickListener { playMusic() }
            buttonPause.setOnClickListener { pauseMusic() }
            buttonStop.setOnClickListener { stopMusic() }
            buttonNext.setOnClickListener { nextMusic() }
            buttonPrevious.setOnClickListener { previousMusic() }
            musicProgress.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    mediaPlayer.seekTo(musicProgress.progress)
                    mediaPlayer.start()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onProgressChanged(
                    seekBar: SeekBar, progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        mediaPlayer.seekTo(progress)
                    }
                }
            })
        }
    }

    private fun playMusic() {
        binder?.play()
    }

    private fun pauseMusic() {
        binder?.pause()
    }

    private fun stopMusic() {
        binder?.stop()
    }

    private fun nextMusic() {
        binder?.next()
    }

    private fun previousMusic() {
        binder?.previous()
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        binder = service as PlayerBinder
        try {
            binding.musicProgress.max = binder?.getDuration() ?: 0
            binding.musicProgress.progress = binder?.getCurrentPosition() ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        binder = null
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

    fun entradaGeofence(latLng: LatLng) {
        Log.d("Id Geofence", "entrou em uma geofence")
        val dbFunc = DatabaseFunc(this)
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
                for ((index, i) in playlist.indices.withIndex()) {
                    if (playlist[i].title?.contains(nomeMusica.toString()) == true) {
                        Log.d("teste", "Play music")
                        binder?.playMusic(index)
                        binder?.play()
                    }
                }
            }
        }
    }

    companion object {
        const val MY_PERMISSIONS_READ_EXTERNAL_STORAGE = 1
    }
}