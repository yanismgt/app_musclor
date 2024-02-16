package com.example.app_lunabee

import android.content.Context

object DatabaseManager {
    lateinit var dbHelper: DatabaseHelper
        private set

    fun initialize(context: Context) {
        dbHelper = DatabaseHelper.getInstance(context.applicationContext)
    }

    fun closeDatabase() {
        dbHelper.close()
    }
}