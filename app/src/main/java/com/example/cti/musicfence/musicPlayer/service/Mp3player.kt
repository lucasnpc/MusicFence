package com.example.cti.musicfence.musicPlayer.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import com.example.cti.musicfence.musicPlayer.`interface`.PlayerInterface
import com.example.cti.musicfence.musicPlayer.utils.MusicPlayer

class Mp3player : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    var isPlay = false
    var isPaused = false
    private var changeMusic = false

    inner class PlayerBinder : Binder(), PlayerInterface {
        override val musicName: String?
            get() = MusicPlayer.playlist[MusicPlayer.musicIndex].title

        override fun play() {
            try {
                if (!isPlay || changeMusic) {
                    MusicPlayer.mediaPlayer.reset()
                    MusicPlayer.mediaPlayer.setDataSource(MusicPlayer.playlist[MusicPlayer.musicIndex].path)
                    MusicPlayer.mediaPlayer.prepare()
                }
                isPlay = true
                isPaused = false
                changeMusic = false
                MusicPlayer.seekBar?.max = MusicPlayer.mediaPlayer.duration
                MusicPlayer.musicaAtual?.text = MusicPlayer.playlist[MusicPlayer.musicIndex].title
                Thread {
                    while (MusicPlayer.mediaPlayer.isPlaying) {
                        MusicPlayer.seekBar?.progress = MusicPlayer.mediaPlayer.currentPosition
                    }
                }.start()
                MusicPlayer.mediaPlayer.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun pause() {
            MusicPlayer.mediaPlayer.pause()
            isPaused = true
        }

        override fun stop() {
            MusicPlayer.mediaPlayer.stop()
            MusicPlayer.seekBar?.progress = 0
            isPlay = false
        }

        override fun next() {
            if (MusicPlayer.musicIndex < MusicPlayer.playlist.size - 1) {
                MusicPlayer.musicIndex += 1
            } else {
                MusicPlayer.musicIndex = 0
            }
            changeMusic = true
            this.play()
        }

        override fun previous() {
            if (MusicPlayer.musicIndex > 0) {
                MusicPlayer.musicIndex -= 1
            } else {
                MusicPlayer.musicIndex = MusicPlayer.playlist.size - 1
            }
            changeMusic = true
            this.play()
        }

        override fun playMusic(index: Int) {
            if (index < MusicPlayer.playlist.size) {
                MusicPlayer.musicIndex = index
            }
            changeMusic = true
            this.play()
        }

        override fun getDuration(): Int {
            return MusicPlayer.mediaPlayer.duration
        }

        override fun getCurrentPosition(): Int {
            return MusicPlayer.mediaPlayer.currentPosition
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.action) {
            ACTION_PLAY -> {
                MusicPlayer.mediaPlayer.apply {
                    setOnPreparedListener(this@Mp3player)
                    prepareAsync()
                }
            }
        }

        MusicPlayer.musicIndex = 0
        MusicPlayer.mediaPlayer.setOnCompletionListener(this)
        try {
            MusicPlayer.mediaPlayer.setDataSource(MusicPlayer.playlist[MusicPlayer.musicIndex].path)
            MusicPlayer.mediaPlayer.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder {
        return PlayerBinder()
    }

    override fun onCompletion(mp: MediaPlayer) {
        if (MusicPlayer.musicIndex < MusicPlayer.playlist.size - 1) {
            MusicPlayer.musicIndex += 1
        } else {
            MusicPlayer.musicIndex = 0
        }
        changeMusic = true
        play()
    }

    private fun play() {
        try {
            if (changeMusic) {
                MusicPlayer.mediaPlayer.reset()
                MusicPlayer.mediaPlayer.setDataSource(MusicPlayer.playlist[MusicPlayer.musicIndex].path)
                MusicPlayer.mediaPlayer.prepare()
            }
            MusicPlayer.seekBar?.max = MusicPlayer.mediaPlayer.duration
            Thread {
                while (MusicPlayer.mediaPlayer.isPlaying) {
                    MusicPlayer.seekBar?.progress = MusicPlayer.mediaPlayer.currentPosition
                }
            }.start()
            MusicPlayer.mediaPlayer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        MusicPlayer.mediaPlayer.start()
    }

    private companion object {
        const val ACTION_PLAY: String = "com.example.action.PLAY"
    }
}