package com.example.cti.musicfence.Util

import android.content.ContentValues
import android.content.Context
import com.example.cti.musicfence.Model.Musica
import com.example.cti.musicfence.Model.dbGateway
import com.example.cti.musicfence.Model.geoFence
import com.google.android.gms.maps.model.LatLng
import java.util.*

/**
 * Created by laboratorio on 30/11/17.
 */
class dbFunc(context: Context?) {
    private val tabela = "geoFence"
    private val gateway: dbGateway
    fun adicionar(latitude: Double, longitude: Double, raio: Double, musica: String?): Boolean {
        val contentValues = ContentValues()
        contentValues.put("latitude", latitude)
        contentValues.put("longitude", longitude)
        contentValues.put("raio", raio)
        contentValues.put("music", musica)
        return gateway.database.insert(tabela, null, contentValues) > 0
    }

    fun remover(latitude: Double, longitude: Double): Boolean {
        val cv = arrayOf("" + latitude, "" + longitude)
        return gateway.database.delete(tabela, "latitude=? and longitude=?", cv) > 0
    }

    fun listar(): ArrayList<geoFence> {
        val cursor = gateway.database.rawQuery("SELECT * FROM geoFence", null)
        val geoFences = ArrayList<geoFence>()
        while (cursor.moveToNext()) {
            val g = geoFence()
            g.latitude = cursor.getDouble(cursor.getColumnIndex("latitude"))
            g.longitude = cursor.getDouble(cursor.getColumnIndex("longitude"))
            g.raio = cursor.getDouble(cursor.getColumnIndex("raio"))
            g.musica = cursor.getString(cursor.getColumnIndex("music"))
            geoFences.add(g)
        }
        return geoFences
    }

    fun retornaMusicFence(latLng: LatLng): String {
        val cursor = gateway.database.rawQuery("SELECT * FROM geoFence WHERE latitude=" +
                latLng.latitude + " and longitude=" + latLng.longitude,
                null)
        val musica = Musica()
        while (cursor.moveToNext()) {
            musica.titulo = cursor.getString(cursor.getColumnIndex("music"))
        }
        return musica.titulo
    }

    init {
        gateway = dbGateway.getInstance(context)
    }
}