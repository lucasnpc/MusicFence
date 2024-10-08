package com.example.cti.musicfence.model

import android.os.Parcel
import android.os.Parcelable

class Musica : Parcelable {
    var id = 0
    var artista: String? = null
    var titulo: String? = null
    var path: String? = null
    var nomeArquivo: String? = null
    var duracao = 0

    constructor() {}
    constructor(id: Int, artista: String?, title: String?, path: String?, nome: String?, duracao: Int) : super() {
        this.id = id
        this.artista = artista
        titulo = title
        this.path = path
        nomeArquivo = nome
        this.duracao = duracao
    }

    override fun toString(): String {
        //return String.format("%d - %s - %s - %s - %s - %d", id, artista, titulo, path, nomeArquivo, duracao);
        return String.format("%s", titulo)
    }

    override fun describeContents(): Int {
        // TODO Auto-generated method stub
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(artista)
        dest.writeString(titulo)
        dest.writeString(path)
        dest.writeString(nomeArquivo)
        dest.writeInt(duracao)
    }

    companion object {
        val CREATOR: Parcelable.Creator<Musica?> = object : Parcelable.Creator<Musica?> {
            override fun createFromParcel(`in`: Parcel): Musica? {
                return Musica(`in`.readInt(), `in`.readString(), `in`.readString(), `in`.readString(), `in`.readString(), `in`.readInt())
            }

            override fun newArray(size: Int): Array<Musica?> {
                return arrayOfNulls(size)
            }
        }
    }
}