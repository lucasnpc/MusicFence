package com.example.cti.musicfence.musicPlayer.utils

import android.media.MediaPlayer
import com.example.cti.musicfence.musicPlayer.enum.MusicPlayerAction
import com.example.cti.musicfence.musicPlayer.model.Music
import kotlinx.coroutines.flow.MutableStateFlow

object MusicPlayer {

    val mediaPlayer: MediaPlayer by lazy {
        MediaPlayer()
    }

    val musicPlayerTrigger: MutableStateFlow<MusicPlayerAction> =
        MutableStateFlow(MusicPlayerAction.STOP)

    var playlist: ArrayList<Music> = arrayListOf()

    var musicIndex = 0

}