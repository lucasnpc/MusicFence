package com.example.cti.musicfence.model

import android.content.Context
import android.database.sqlite.SQLiteDatabase

/**
 * Created by laboratorio on 30/11/17.
 */
class DBGateway private constructor(context: Context) {
    val database: SQLiteDatabase

    companion object {
        private var gateway: DBGateway? = null
        fun getInstance(context: Context): DBGateway? {
            if (gateway == null) gateway = DBGateway(context)
            return gateway
        }
    }

    init {
        val helper = DBHelper(context)
        database = helper.writableDatabase
    }
}