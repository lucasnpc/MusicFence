package com.example.cti.musicfence.Model

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Created by laboratorio on 29/11/17.
 */
class dbHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {}

    companion object {
        private const val DATABASE_NAME = "musicFence.db"
        private const val DATABASE_VERSION = 1
        private const val CREATE_TABLE = "CREATE TABLE geoFence (" +
                "latitude DOUBLE," +
                "longitude DOUBLE," +
                "raio DOUBLE," +
                "music VARCHAR);"
    }
}