package com.example.cti.musicfence.musicPlayer.model

import java.io.Serializable

data class Music(
    private var id: Int,
    private var artista: String?,
    var title: String?,
    var path: String?,
    val nome: String?,
    private var duracao: Int
) : Serializable