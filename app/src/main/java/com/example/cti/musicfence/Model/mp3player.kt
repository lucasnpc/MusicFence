package com.example.cti.musicfence.Model

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.example.cti.musicfence.Activity.MainActivity
import com.example.cti.musicfence.Interface.PlayerInterface
import java.util.*

class mp3player : Service(), OnCompletionListener {
    private var mediaPlayer: MediaPlayer? = null
    private var MusicIndex = 0
    var isPlay = false
    var isPaused = false
    private var changeMusic = false

    inner class PlayerBinder : Binder(), PlayerInterface {
        override val musicName: String?
            get() = playlist!![MusicIndex].titulo
        override val duration: Int
            get() = mediaPlayer!!.duration
        override val currentPosition: Int
            get() = mediaPlayer!!.currentPosition

        override fun play() {
            try {
                if (!isPlay || changeMusic) {
                    mediaPlayer!!.reset()
                    mediaPlayer.setDataSource(playlist!![MusicIndex].path)
                    mediaPlayer!!.prepare()
                }
                isPlay = true
                isPaused = false
                changeMusic = false
                MainActivity.seekBar!!.max = mediaPlayer!!.duration
                MainActivity.musicaAtual.setText(playlist!![MusicIndex].titulo)
                if (mediaPlayer != null) {
                    Thread {
                        while (mediaPlayer!!.isPlaying) {
                            MainActivity.seekBar!!.progress = mediaPlayer!!.currentPosition
                        }
                    }.start()
                }
                mediaPlayer!!.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun pause() {
            mediaPlayer!!.pause()
            isPaused = true
        }

        override fun stop() {
            mediaPlayer!!.stop()
            MainActivity.seekBar!!.progress = 0
            isPlay = false
        }

        override fun next() {
            if (MusicIndex < playlist!!.size - 1) {
                MusicIndex += 1
            } else {
                MusicIndex = 0
            }
            changeMusic = true
            this.play()
        }

        override fun previous() {
            if (MusicIndex > 0) {
                MusicIndex -= 1
            } else {
                MusicIndex = playlist!!.size - 1
            }
            changeMusic = true
            this.play()
        }

        override fun playMusic(Index: Int) {
            if (Index < playlist!!.size) {
                MusicIndex = Index
            }
            changeMusic = true
            this.play()
        }
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        playlist = intent.getParcelableArrayListExtra("listaMusicas")
        if (playlist != null) {
            MainActivity.seekBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    mediaPlayer!!.seekTo(MainActivity.seekBar!!.progress)
                    mediaPlayer!!.start()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onProgressChanged(seekBar: SeekBar, progress: Int,
                                               fromUser: Boolean) {
                    // TODO Auto-generated method stub
                }
            })
            MusicIndex = 0
            mediaPlayer = MediaPlayer()
            mediaPlayer!!.setOnCompletionListener(this)
            try {
                mediaPlayer.setDataSource(playlist.get(MusicIndex).getPath())
                mediaPlayer!!.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            Log.d("error musicfence", "playslist null")
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return PlayerBinder()
    }

    override fun onCompletion(mp: MediaPlayer) {
        if (MusicIndex < playlist.size - 1) {
            MusicIndex += 1
        } else {
            MusicIndex = 0
        }
        changeMusic = true
        play()
    }

    fun play() {
        try {
            if (changeMusic) {
                mediaPlayer!!.reset()
                mediaPlayer.setDataSource(playlist.get(MusicIndex).getPath())
                mediaPlayer!!.prepare()
            }
            MainActivity.seekBar!!.max = mediaPlayer!!.duration
            if (mediaPlayer != null) {
                Thread {
                    while (mediaPlayer!!.isPlaying) {
                        MainActivity.seekBar!!.progress = mediaPlayer!!.currentPosition
                    }
                }.start()
                mediaPlayer!!.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        var playlist: ArrayList<Musica>? = null
    }
}