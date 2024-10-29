package com.example.cti.musicfence.musicPlayer.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import com.example.cti.musicfence.musicPlayer.enum.MusicPlayerAction
import com.example.cti.musicfence.musicPlayer.`interface`.PlayerBinderInterface
import com.example.cti.musicfence.musicPlayer.model.Music
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class Mp3player : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private val mediaPlayer: MediaPlayer by lazy {
        MediaPlayer()
    }

    private val _musicPlayerState: MutableStateFlow<MusicPlayerAction> =
        MutableStateFlow(MusicPlayerAction.STOP)

    private val playlist: ArrayList<Music> = arrayListOf()

    private var currentMusicIndex = -1

    inner class PlayerBinder : Binder(), PlayerBinderInterface {
        override val musicName: String?
            get() = playlist[currentMusicIndex].title

        val musicPlayerState: StateFlow<MusicPlayerAction> = _musicPlayerState

        override fun play() {
            when (_musicPlayerState.value) {
                MusicPlayerAction.PLAYING -> Unit

                MusicPlayerAction.PAUSED -> resumePlayer()

                MusicPlayerAction.STOP -> changeMusicOrReset()

                MusicPlayerAction.CHANGEMUSIC -> changeMusicOrReset()

            }
        }

        override fun pause() {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                _musicPlayerState.value = MusicPlayerAction.PAUSED
            }
        }

        override fun stop() {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
                _musicPlayerState.value = MusicPlayerAction.STOP
            }
        }

        override fun next() {
            if (currentMusicIndex < 0) return
            if (currentMusicIndex < playlist.size - 1) {
                currentMusicIndex += 1
            } else {
                currentMusicIndex = 0
            }
            _musicPlayerState.value = MusicPlayerAction.CHANGEMUSIC
            this.play()
        }

        override fun previous() {
            if (currentMusicIndex < 0) return
            if (currentMusicIndex > 0) {
                currentMusicIndex -= 1
            } else {
                currentMusicIndex = playlist.size - 1
            }
            _musicPlayerState.value = MusicPlayerAction.CHANGEMUSIC
            this.play()
        }

        override fun playMusic(index: Int) {
            if (index != currentMusicIndex) {
                currentMusicIndex = index
                _musicPlayerState.value = MusicPlayerAction.CHANGEMUSIC
            }
            this.play()
        }

        override fun getDuration(): Int {
            return mediaPlayer.duration
        }

        override fun getCurrentPosition(): Int {
            return mediaPlayer.currentPosition
        }

        override fun handleSeekBarChange(progress: Int, fromUser: Boolean) {
            if (fromUser) {
                mediaPlayer.seekTo(progress)
            }
        }

        override fun seekAndStart(progress: Int) {
            mediaPlayer.seekTo(progress)
            mediaPlayer.start()
        }

        override fun submitPlaylist(list: ArrayList<Music>) {
            playlist.addAll(list)
        }

        override fun getPlaylist(): List<Music> {
            return playlist
        }
    }

    private fun resumePlayer() {
        mediaPlayer.start()
        _musicPlayerState.value = MusicPlayerAction.PLAYING
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
        if (currentMusicIndex < playlist.size - 1) {
            currentMusicIndex += 1
        } else {
            currentMusicIndex = 0
        }
        _musicPlayerState.value = MusicPlayerAction.CHANGEMUSIC
        changeMusicOrReset()
    }

    private fun changeMusicOrReset() {
        mediaPlayer.reset()
        mediaPlayer.setDataSource(playlist[currentMusicIndex].path)
        mediaPlayer.prepareAsync()
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mediaPlayer.start()
        _musicPlayerState.value = MusicPlayerAction.PLAYING
    }
}
