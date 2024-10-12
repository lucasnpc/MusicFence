package com.example.cti.musicfence.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import com.example.cti.musicfence.`interface`.PlayerInterface
import com.example.cti.musicfence.util.MusicPlayer.mediaPlayer
import com.example.cti.musicfence.util.MusicPlayer.musicIndex
import com.example.cti.musicfence.util.MusicPlayer.musicaAtual
import com.example.cti.musicfence.util.MusicPlayer.playlist
import com.example.cti.musicfence.util.MusicPlayer.seekBar

private const val ACTION_PLAY: String = "com.example.action.PLAY"

class Mp3player : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    var isPlay = false
    var isPaused = false
    private var changeMusic = false

    inner class PlayerBinder : Binder(), PlayerInterface {
        override val musicName: String?
            get() = playlist[musicIndex].titulo

        override fun play() {
            try {
                if (!isPlay || changeMusic) {
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(playlist[musicIndex].path)
                    mediaPlayer.prepare()
                }
                isPlay = true
                isPaused = false
                changeMusic = false
                seekBar?.max = mediaPlayer.duration
                musicaAtual?.text = playlist[musicIndex].titulo
                Thread {
                    while (mediaPlayer.isPlaying) {
                        seekBar?.progress = mediaPlayer.currentPosition
                    }
                }.start()
                mediaPlayer.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun pause() {
            mediaPlayer.pause()
            isPaused = true
        }

        override fun stop() {
            mediaPlayer.stop()
            seekBar?.progress = 0
            isPlay = false
        }

        override fun next() {
            if (musicIndex < playlist.size - 1) {
                musicIndex += 1
            } else {
                musicIndex = 0
            }
            changeMusic = true
            this.play()
        }

        override fun previous() {
            if (musicIndex > 0) {
                musicIndex -= 1
            } else {
                musicIndex = playlist.size - 1
            }
            changeMusic = true
            this.play()
        }

        override fun playMusic(index: Int) {
            if (index < playlist.size) {
                musicIndex = index
            }
            changeMusic = true
            this.play()
        }

        override fun getDuration(): Int {
            return mediaPlayer.duration
        }

        override fun getCurrentPosition(): Int {
            return mediaPlayer.currentPosition
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.action) {
            ACTION_PLAY -> {
                mediaPlayer.apply {
                    setOnPreparedListener(this@Mp3player)
                    prepareAsync()
                }
            }
        }

        musicIndex = 0
        mediaPlayer.setOnCompletionListener(this)
        try {
            mediaPlayer.setDataSource(playlist[musicIndex].path)
            mediaPlayer.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder {
        return PlayerBinder()
    }

    override fun onCompletion(mp: MediaPlayer) {
        if (musicIndex < playlist.size - 1) {
            musicIndex += 1
        } else {
            musicIndex = 0
        }
        changeMusic = true
        play()
    }

    private fun play() {
        try {
            if (changeMusic) {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(playlist[musicIndex].path)
                mediaPlayer.prepare()
            }
            seekBar?.max = mediaPlayer.duration
            Thread {
                while (mediaPlayer.isPlaying) {
                    seekBar?.progress = mediaPlayer.currentPosition
                }
            }.start()
            mediaPlayer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mediaPlayer.start()
    }
}