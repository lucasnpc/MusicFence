package com.example.cti.musicfence.musicPlayer.utils

import android.media.MediaPlayer
import android.widget.SeekBar
import android.widget.TextView
import com.example.cti.musicfence.musicPlayer.model.Music

object MusicPlayer {

    val mediaPlayer: MediaPlayer by lazy {
        MediaPlayer()
    }

    var playlist: ArrayList<Music> = arrayListOf()

    var musicIndex = 0

    var seekBar: SeekBar? = null

    var musicaAtual: TextView? = null
}