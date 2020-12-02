package com.example.cti.musicfence.Interface

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
    fun playMusic(Index: Int)
    fun getDuration(): Int
    fun getCurrentPosition(): Int
}