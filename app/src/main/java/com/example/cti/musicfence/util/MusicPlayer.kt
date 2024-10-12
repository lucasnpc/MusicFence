package com.example.cti.musicfence.util

import android.media.MediaPlayer
import android.widget.SeekBar
import android.widget.TextView
import com.example.cti.musicfence.model.Musica

object MusicPlayer {

    val mediaPlayer: MediaPlayer by lazy {
        MediaPlayer()
    }

    var playlist: ArrayList<Musica> = arrayListOf()

    var musicIndex = 0

    var seekBar: SeekBar? = null

    var musicaAtual: TextView? = null
}