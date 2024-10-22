package com.example.cti.musicfence.musicPlayer.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import com.example.cti.musicfence.musicPlayer.`interface`.PlayerInterface
import com.example.cti.musicfence.musicPlayer.utils.MusicPlayer

class Mp3player : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    inner class PlayerBinder : Binder(), PlayerInterface {
        override val musicName: String?
            get() = MusicPlayer.playlist[MusicPlayer.musicIndex].title

        override fun play(changeMusic: Boolean) {
            try {
                if (!MusicPlayer.mediaPlayer.isPlaying || changeMusic) {
                    MusicPlayer.mediaPlayer.reset()
                    MusicPlayer.mediaPlayer.setDataSource(MusicPlayer.playlist[MusicPlayer.musicIndex].path)
                    MusicPlayer.mediaPlayer.prepareAsync()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun pause() {
            if (MusicPlayer.mediaPlayer.isPlaying) {
                MusicPlayer.mediaPlayer.pause()
            }
        }

        override fun stop() {
            if (MusicPlayer.mediaPlayer.isPlaying) {
                MusicPlayer.mediaPlayer.stop()
                MusicPlayer.seekBar?.progress = 0
            }
        }

        override fun next() {
            if (MusicPlayer.musicIndex < MusicPlayer.playlist.size - 1) {
                MusicPlayer.musicIndex += 1
            } else {
                MusicPlayer.musicIndex = 0
            }
            this.play(changeMusic = true)
        }

        override fun previous() {
            if (MusicPlayer.musicIndex > 0) {
                MusicPlayer.musicIndex -= 1
            } else {
                MusicPlayer.musicIndex = MusicPlayer.playlist.size - 1
            }
            this.play(changeMusic = true)
        }

        override fun playMusic(index: Int) {
            if (index < MusicPlayer.playlist.size) {
                MusicPlayer.musicIndex = index
                this.play(changeMusic = true)
            }
        }

        override fun getDuration(): Int {
            return if (MusicPlayer.mediaPlayer.isPlaying) MusicPlayer.mediaPlayer.duration else 0
        }

        override fun getCurrentPosition(): Int {
            return if (MusicPlayer.mediaPlayer.isPlaying) MusicPlayer.mediaPlayer.currentPosition else 0
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        MusicPlayer.mediaPlayer.setOnPreparedListener(this)
        MusicPlayer.mediaPlayer.setOnCompletionListener(this)
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
        playOnCompletion()
    }

    private fun playOnCompletion() {
        try {
            MusicPlayer.mediaPlayer.reset()
            MusicPlayer.mediaPlayer.setDataSource(MusicPlayer.playlist[MusicPlayer.musicIndex].path)
            MusicPlayer.mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        MusicPlayer.mediaPlayer.start()
        MusicPlayer.seekBar?.max = MusicPlayer.mediaPlayer.duration
        MusicPlayer.musicaAtual?.text = MusicPlayer.playlist[MusicPlayer.musicIndex].title

        Thread {
            while (MusicPlayer.mediaPlayer.isPlaying) {
                MusicPlayer.seekBar?.progress = MusicPlayer.mediaPlayer.currentPosition
                try {
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()
    }
}
