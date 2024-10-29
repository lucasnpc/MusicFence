package com.example.cti.musicfence.musicPlayer.`interface`

import com.example.cti.musicfence.musicPlayer.model.Music

/**
 * Created by Cti on 13/11/2017.
 */
interface PlayerBinderInterface {
    val musicName: String?
    fun play()
    fun pause()
    fun stop()
    fun next()
    fun previous()
    fun playMusic(index: Int)
    fun getDuration(): Int
    fun getCurrentPosition(): Int
    fun handleSeekBarChange(progress: Int, fromUser: Boolean)
    fun seekAndStart(progress: Int)
    fun submitPlaylist(list: ArrayList<Music>)
    fun getPlaylist(): List<Music>
}