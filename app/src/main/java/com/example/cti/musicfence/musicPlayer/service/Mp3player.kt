package com.example.cti.musicfence.musicPlayer.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import com.example.cti.musicfence.musicPlayer.enum.MusicPlayerAction
import com.example.cti.musicfence.musicPlayer.`interface`.PlayerInterface
import com.example.cti.musicfence.musicPlayer.utils.MusicPlayer
import com.example.cti.musicfence.musicPlayer.utils.MusicPlayer.mediaPlayer
import com.example.cti.musicfence.musicPlayer.utils.MusicPlayer.musicPlayerTrigger

class Mp3player : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    inner class PlayerBinder : Binder(), PlayerInterface {
        override val musicName: String?
            get() = MusicPlayer.playlist[MusicPlayer.musicIndex].title

        override fun play() {
            when (musicPlayerTrigger.value) {
                MusicPlayerAction.STOP -> changeMusicOrReset()

                MusicPlayerAction.PAUSE -> resumePlayer()

                MusicPlayerAction.PLAY -> Unit

                MusicPlayerAction.NEXT -> changeMusicOrReset()

                MusicPlayerAction.PREVIOUS -> changeMusicOrReset()
            }
        }

        override fun pause() {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                musicPlayerTrigger.value = MusicPlayerAction.PAUSE
            }
        }

        override fun stop() {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
                musicPlayerTrigger.value = MusicPlayerAction.STOP
            }
        }

        override fun next() {
            if (MusicPlayer.musicIndex < MusicPlayer.playlist.size - 1) {
                MusicPlayer.musicIndex += 1
            } else {
                MusicPlayer.musicIndex = 0
            }
            musicPlayerTrigger.value = MusicPlayerAction.NEXT
            this.play()
        }

        override fun previous() {
            if (MusicPlayer.musicIndex > 0) {
                MusicPlayer.musicIndex -= 1
            } else {
                MusicPlayer.musicIndex = MusicPlayer.playlist.size - 1
            }
            musicPlayerTrigger.value = MusicPlayerAction.PREVIOUS
            this.play()
        }

        override fun playMusic(index: Int) {
            if (index < MusicPlayer.playlist.size) {
                MusicPlayer.musicIndex = index
                this.play()
            }
        }

        override fun getDuration(): Int {
            return mediaPlayer.duration
        }

        override fun getCurrentPosition(): Int {
            return mediaPlayer.currentPosition
        }
    }

    private fun resumePlayer() {
        mediaPlayer.start()
        musicPlayerTrigger.value = MusicPlayerAction.PLAY
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        mediaPlayer.setOnPreparedListener(this)
        mediaPlayer.setOnCompletionListener(this)
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
        musicPlayerTrigger.value = MusicPlayerAction.NEXT
        changeMusicOrReset()
    }

    private fun changeMusicOrReset() {
        mediaPlayer.reset()
        mediaPlayer.setDataSource(MusicPlayer.playlist[MusicPlayer.musicIndex].path)
        mediaPlayer.prepareAsync()
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mediaPlayer.start()
        musicPlayerTrigger.value = MusicPlayerAction.PLAY
    }
}
