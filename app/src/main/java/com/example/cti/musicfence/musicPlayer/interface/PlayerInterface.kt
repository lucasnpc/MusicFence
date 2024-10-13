package com.example.cti.musicfence.musicPlayer.`interface`

/**
 * Created by Cti on 13/11/2017.
 */
interface PlayerInterface {
    val musicName: String?
    fun play()
    fun pause()
    fun stop()
    operator fun next()
    fun previous()
    fun playMusic(index: Int)
    fun getDuration(): Int
    fun getCurrentPosition(): Int
}