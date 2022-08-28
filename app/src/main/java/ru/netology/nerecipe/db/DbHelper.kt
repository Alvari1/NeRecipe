package ru.netology.nerecipe.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(
    context: Context,
    dbVersion: Int,
    dbName: String,
    private val DLLs: Array<String>
) : SQLiteOpenHelper(context, dbName, null, dbVersion) {
    override fun onCreate(db: SQLiteDatabase) {
        DLLs.forEach(db::execSQL)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }
}