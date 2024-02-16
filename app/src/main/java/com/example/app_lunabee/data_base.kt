package com.example.app_lunabee

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.util.Log


class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {

        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "DBMuscu"
        private const val KEY_ID = "id"

        private const val TABLE_SEANCE = "Seance"
        private const val NAME_SEANCE = "name_seance"
        private const val IMAGE_COLUMN = "image_seance"

        private const val TABLE_EXERCICE = "exercice"
        private const val NAME_EXO = "exercice"
        private const val SEANCE_ID = "seance_id"
        private const val REPET_EXO = "repet"

        private var instance: DatabaseHelper? = null
        private var fonct_add = PageAdd()

        @Synchronized
        fun getInstance(context: Context): DatabaseHelper {
            if (instance == null) {
                instance = DatabaseHelper(context.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createSeanceTableQuery =
            "CREATE TABLE $TABLE_SEANCE ($KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT, $NAME_SEANCE TEXT, $IMAGE_COLUMN INT)"
        db.execSQL(createSeanceTableQuery)

        val createExerciceTableQuery =
            "CREATE TABLE $TABLE_EXERCICE ($KEY_ID INTEGER PRIMARY KEY, $NAME_EXO TEXT, $REPET_EXO INT, $SEANCE_ID INTEGER, FOREIGN KEY($SEANCE_ID) REFERENCES $TABLE_SEANCE($KEY_ID))"
        db.execSQL(createExerciceTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_SEANCE ADD COLUMN $NAME_SEANCE TEXT")
        }
    }

    fun deleteExercice(exerciceId: Long): Boolean {
        val db = this.writableDatabase

        return try {
            val result = db.delete(TABLE_EXERCICE, "$KEY_ID=?", arrayOf(exerciceId.toString()))
            result > 0
        } finally {
            db.close()
        }
    }

    @SuppressLint("Range")
    fun getExerciceId(nomExercice: String, seanceId: Long): Long {
        val db = this.readableDatabase
        val query = "SELECT $KEY_ID FROM $TABLE_EXERCICE WHERE $NAME_EXO = ? AND $SEANCE_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(nomExercice, seanceId.toString()))
        var exerciceId: Long = -1

        try {
            if (cursor.moveToFirst()) {
                exerciceId = cursor.getLong(cursor.getColumnIndex(KEY_ID))
            }
        } finally {
            cursor.close()
        }

        return exerciceId
    }

    fun addSeance(nomSeance: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(NAME_SEANCE, nomSeance)
        }
        db.insert(TABLE_SEANCE, null, values)
        db.close()
    }

    fun supSeance(nomSeance: String) {
        val db = this.writableDatabase
        db.beginTransaction()

        try {
            db.delete(TABLE_EXERCICE, "$SEANCE_ID = ?", arrayOf(nomSeance))
            db.delete(TABLE_SEANCE, "$NAME_SEANCE = ?", arrayOf(nomSeance))

            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la suppression de la séance et de ses exercices", e)
        } finally {
            db.endTransaction()
        }
    }

    fun addImageToSeance(nomSeance: String, imageUri: Int) {
        val db = this.writableDatabase
        val existingSeance = getSeanceId(nomSeance)

        if (existingSeance == -1L) {
            addSeance(nomSeance)
        }
        val values = ContentValues().apply {
            put(IMAGE_COLUMN, imageUri)
        }
        try {
            db.update(TABLE_SEANCE, values, "$NAME_SEANCE = ?", arrayOf(nomSeance))
        } finally {
            db.close()
        }
    }


    fun addExerciceToSeance(nomSeance: String, nomExercice: String, nbrRep: String) {
        val db = this.writableDatabase
        val seanceId = getSeanceId(nomSeance)

        try {
            if (seanceId != -1L) {
                val values = ContentValues().apply {
                    put(NAME_EXO, nomExercice)
                    put(SEANCE_ID, seanceId)
                    put(REPET_EXO, nbrRep.toInt())
                }
                val rowId = db.insert(TABLE_EXERCICE, null, values)
                Log.d("Database", "Inserted exercice with ID: $rowId")
            }
        } catch (e: Exception) {
            Log.e("Database", "Error inserting exercice", e)
        } finally {
            db.close()
        }
    }

    @SuppressLint("Range")
    fun getSeanceId(nomSeance: String): Long {
        val db = this.readableDatabase
        val query = "SELECT $KEY_ID FROM $TABLE_SEANCE WHERE $NAME_SEANCE = ?"
        val cursor = db.rawQuery(query, arrayOf(nomSeance))

        var seanceId: Long = -1

        try {
            if (cursor.moveToFirst()) {
                seanceId = cursor.getLong(cursor.getColumnIndex(KEY_ID))
            }
        } finally {
            cursor.close()
        }
        return seanceId
    }

    @SuppressLint("Range")
    fun getAllSeance(): List<Seance> {
        val seanceList = mutableListOf<Seance>()

        val query = "SELECT * FROM $TABLE_SEANCE"
        val db = this.readableDatabase
        val cursor = db.rawQuery(query, null)

        try {
            if (cursor != null && cursor.moveToFirst()) {
                val seanceIdColumnIndex = cursor.getColumnIndex(KEY_ID)
                val nomSeanceColumnIndex = cursor.getColumnIndex(NAME_SEANCE)

                do {
                    if (nomSeanceColumnIndex != -1 && seanceIdColumnIndex != -1) {
                        val seanceId = cursor.getLong(seanceIdColumnIndex)
                        val nomSeance = cursor.getString(nomSeanceColumnIndex)
                        val seance = Seance(seanceId, nomSeance)
                        seanceList.add(seance)
                    }
                } while (cursor.moveToNext())
            }
        } finally {
            cursor.close()
        }
        return seanceList
    }

    fun getAllSeanceNames(): List<String> {
        val seanceNames = mutableListOf<String>()
        val db = this.readableDatabase
        val query = "SELECT $NAME_SEANCE FROM $TABLE_SEANCE"
        val cursor = db.rawQuery(query, null)

        cursor.use {
            while (cursor.moveToNext()) {
                val seanceName = cursor.getString(cursor.getColumnIndex(NAME_SEANCE))
                seanceNames.add(seanceName)
            }
        }

        return seanceNames
    }

    @SuppressLint("Range")
    fun getImageResourceForSeance(seanceName: String): Int {
        val db = this.readableDatabase
        val query = "SELECT $IMAGE_COLUMN FROM $TABLE_SEANCE WHERE $NAME_SEANCE = ?"
        val cursor = db.rawQuery(query, arrayOf(seanceName))
        var imageResource = -1

        try {
            if (cursor.moveToFirst()) {
                imageResource = cursor.getInt(cursor.getColumnIndex(IMAGE_COLUMN))
            }
        } finally {
            cursor.close()
        }
        return imageResource
    }

    fun isImageDefinedForSeance(seanceName: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT $IMAGE_COLUMN FROM $TABLE_SEANCE WHERE $NAME_SEANCE = ?"
        val selectionArgs = arrayOf(seanceName)

        val cursor = db.rawQuery(query, selectionArgs)

        try {
            return cursor.moveToFirst() && !cursor.isNull(cursor.getColumnIndex(IMAGE_COLUMN))
        } finally {
            cursor.close()
        }
    }

    @SuppressLint("Range")
    fun getExercicesForSeance(seanceId: Long): List<String> {
        val exercicesList = mutableListOf<String>()

        val db = this.readableDatabase
        val query = "SELECT $NAME_EXO FROM $TABLE_EXERCICE WHERE $SEANCE_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(seanceId.toString()))

        try {
            if (cursor.moveToFirst()) {
                do {
                    val exerciceName = cursor.getString(cursor.getColumnIndex(NAME_EXO))
                    exercicesList.add(exerciceName)
                } while (cursor.moveToNext())
            }
        } finally {
            cursor.close()
        }
        return exercicesList
    }

    @SuppressLint("Range")
    fun getRepetAndExo(nomSeance: String): List<Exercise> {
        val exercises = mutableListOf<Exercise>()
        var seanceId = getSeanceId(nomSeance)
        val db = this.readableDatabase
        val query = "SELECT $NAME_EXO, $REPET_EXO FROM $TABLE_EXERCICE WHERE $SEANCE_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(seanceId.toString()))

        try {
            if (cursor.moveToFirst()) {
                do {
                    val exerciseName = cursor.getString(cursor.getColumnIndex(NAME_EXO))
                    val repetitions = cursor.getInt(cursor.getColumnIndex(REPET_EXO))
                    val exercise = Exercise(exerciseName, repetitions)
                    exercises.add(exercise)
                } while (cursor.moveToNext())
            }
        } finally {
            cursor.close()
        }
        return exercises
    }

    fun seanceNameExists(nomSeance: String): Boolean {
        val seanceList = DatabaseManager.dbHelper.getAllSeance()
        return seanceList.any { it.nomSeance == nomSeance }
    }
}