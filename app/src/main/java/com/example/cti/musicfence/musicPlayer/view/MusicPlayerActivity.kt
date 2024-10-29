package com.example.cti.musicfence.musicPlayer.view

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cti.musicfence.R
import com.example.cti.musicfence.activity.MapsActivity
import com.example.cti.musicfence.activity.utils.getAllMusics
import com.example.cti.musicfence.activity.utils.getPermissionsAboveApi33
import com.example.cti.musicfence.activity.utils.getPermissionsUnderApi33
import com.example.cti.musicfence.databinding.ActivityInicioBinding
import com.example.cti.musicfence.model.GeofenceModel
import com.example.cti.musicfence.musicPlayer.enum.MusicPlayerAction
import com.example.cti.musicfence.musicPlayer.service.Mp3player
import com.example.cti.musicfence.musicPlayer.service.Mp3player.PlayerBinder
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MusicPlayerActivity : AppCompatActivity(), ResultCallback<Status>,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private val binding: ActivityInicioBinding by lazy {
        ActivityInicioBinding.inflate(layoutInflater)
    }

    private val musicService by lazy {
        Intent(this, Mp3player::class.java)
    }

    private var updateJob: Job? = null

    private var playerBinder: PlayerBinder? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            startMusicService()
        } else {
            Toast.makeText(this, "Permissão negada", Toast.LENGTH_SHORT).show()
        }
    }

    private var isServiceConnected = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            playerBinder = service as PlayerBinder
            isServiceConnected = true

            setupPlayList()
            setupMusicPlayerCollect()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceConnected = false
            playerBinder = null
        }
    }

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
        setupSeekBarListener()

        val intentGeofence = Intent(".GeoFenceTransitionsIntentService")
        intentGeofence.setPackage("com.example.cti.")
        startService(intentGeofence)
        val dbFunc = DatabaseFunc(this)
        for (g in dbFunc.listar()) {
            val g2 = createGeofence(g)
            val geofencingRequest = geofencingRequest(g2)
            geofencingClient.addGeofences(geofencingRequest, criarGeoPendingIntent())
                .addOnSuccessListener(this) { Log.d("Status", "sucesso.") }
        }
    }

    override fun onStart() {
        super.onStart()
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getPermissionsAboveApi33()
        } else {
            getPermissionsUnderApi33()
        }
        permissionLauncher.launch(permissions)
    }

    private fun setupMusicPlayerCollect() {
        lifecycleScope.launch {
            playerBinder?.musicPlayerState?.collect { action ->
                when (action) {
                    MusicPlayerAction.PLAYING -> {
                        binding.currentMusicPlaying.text = playerBinder?.musicName
                        binding.musicProgress.max = playerBinder?.getDuration() ?: 0
                        startUpdatingProgress()
                    }

                    MusicPlayerAction.PAUSED -> {
                        stopUpdatingProgress()
                    }

                    MusicPlayerAction.STOP -> {
                        stopUpdatingProgress()
                        binding.currentMusicPlaying.text = null
                        binding.musicProgress.max = 0
                        binding.musicProgress.progress = 0
                    }

                    MusicPlayerAction.CHANGEMUSIC -> Unit
                }
            }
        }
    }

    private fun startMusicService() {
        bindService(musicService, serviceConnection, BIND_AUTO_CREATE)
        startService(musicService)
    }

    private fun setClickListeners() {
        binding.run {
            buttonPlay.setOnClickListener {
                playerBinder?.playMusic(0)
            }
            buttonPause.setOnClickListener {
                playerBinder?.pause()
            }
            buttonStop.setOnClickListener {
                binding.musicProgress.progress = 0
                playerBinder?.stop()
            }
            buttonNext.setOnClickListener {
                playerBinder?.next()
            }
            buttonPrevious.setOnClickListener {
                playerBinder?.previous()
            }
        }
    }

    private fun setupSeekBarListener() {
        binding.run {
            musicProgress.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    playerBinder?.seekAndStart(musicProgress.progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onProgressChanged(
                    seekBar: SeekBar, progress: Int,
                    fromUser: Boolean
                ) {
                    playerBinder?.handleSeekBarChange(progress, fromUser)
                }
            })
        }
    }

    private fun setupPlayList() {
        getAllMusics().also { musics ->
            playerBinder?.submitPlaylist(musics)
            binding.listaMusicas.apply {
                adapter = ArrayAdapter(
                    this@MusicPlayerActivity,
                    R.layout.lista_titulo_sumario_texto,
                    musics.map { music -> music.title }
                )
                setOnItemClickListener { _, _, position, _ ->
                    playerBinder?.playMusic(position)
                }
                setOnItemLongClickListener { _, view, _, _ ->
                    val intent = Intent(this@MusicPlayerActivity, MapsActivity::class.java)
                    intent.putExtra("nomeMusica", (view as TextView).text.toString())
                    startActivity(intent)
                    false
                }
            }
        }
    }

    private fun createGeofence(g: GeofenceModel): Geofence {
        return GeofenceModel(
            latitude = g.latitude,
            longitude = g.longitude,
            radius = g.radius,
        )
    }

    private val geoPendingIntent: PendingIntent? = null

    private fun criarGeoPendingIntent(): PendingIntent {
        if (geoPendingIntent != null) return geoPendingIntent
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun geofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
    }

    override fun onStop() {
        super.onStop()
        if (isServiceConnected) {
            unbindService(serviceConnection)
            stopService(musicService)
        }
    }

    private fun startUpdatingProgress() {
        updateJob = lifecycleScope.launch {
            while (playerBinder?.musicPlayerState?.value == MusicPlayerAction.PLAYING) {
                binding.musicProgress.progress = playerBinder?.getCurrentPosition() ?: 0
                delay(150)
            }
        }
    }

    private fun stopUpdatingProgress() {
        updateJob?.cancel()
        updateJob = null
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
            Log.d(geo.musicName, geo.latitude.toString() + " - " + geo.longitude)
            if (CalcDistancia.distance(
                    latLng.latitude,
                    geo.latitude,
                    latLng.longitude,
                    geo.longitude,
                    1.0,
                    1.0
                ) < geo.radius
            ) {
                val nomeMusica = geo.musicName
                Log.i("Musica Geo", nomeMusica)
                playerBinder?.run {
                    for ((index, i) in getPlaylist().indices.withIndex()) {
                        if (getPlaylist()[i].title?.contains(nomeMusica) == true) {
                            Log.d("teste", "Play music")
                            playerBinder?.playMusic(index)
                            playerBinder?.play()
                        }
                    }
                }
            }
        }
    }
}