package com.example.cti.musicfence.Model

import android.content.Context
import android.database.sqlite.SQLiteDatabase

/**
 * Created by laboratorio on 30/11/17.
 */
class dbGateway private constructor(context: Context) {
    val database: SQLiteDatabase

    companion object {
        private var gateway: dbGateway? = null
        fun getInstance(context: Context): dbGateway? {
            if (gateway == null) gateway = dbGateway(context)
            return gateway
        }
    }

    init {
        val helper = dbHelper(context)
        database = helper.writableDatabase
    }
}