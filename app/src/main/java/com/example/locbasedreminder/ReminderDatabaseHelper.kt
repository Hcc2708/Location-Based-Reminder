package com.example.locbasedreminder

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.android.gms.maps.model.LatLng

class ReminderDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "reminder.db"
        const val DATABASE_VERSION = 1

        const val TABLE_NAME = "reminders"
        const val COLUMN_ID = "_id"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_LONGITUDE = "longitude"
        const val COLUMN_TASK = "task"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_LATITUDE REAL," +
                "$COLUMN_LONGITUDE REAL," +
                "$COLUMN_TASK TEXT)"

        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addReminder(latitude: Double, longitude: Double, task: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_LATITUDE, latitude)
            put(COLUMN_LONGITUDE, longitude)
            put(COLUMN_TASK, task)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getReminders(): MutableList<Reminder> {
        val reminders = mutableListOf<Reminder>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        if (cursor.moveToFirst()) {
            do {
                val latitudeColumnIndex = cursor.getColumnIndex(COLUMN_LATITUDE)
                val longitudeColumnIndex = cursor.getColumnIndex(COLUMN_LONGITUDE)
                val taskColumnIndex = cursor.getColumnIndex(COLUMN_TASK)
                if(latitudeColumnIndex >= 0 && longitudeColumnIndex >= 0 && taskColumnIndex >= 0) {
                    val latitude = cursor.getDouble(latitudeColumnIndex)
                    val longitude = cursor.getDouble(longitudeColumnIndex)
                    val task = cursor.getString(taskColumnIndex)
                    reminders.add(Reminder(LatLng(latitude, longitude), task))
                }
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return reminders
    }
    fun removeReminderByTask(task: String) {
        val db = writableDatabase
        val whereClause = "$COLUMN_TASK = ?"
        val whereArgs = arrayOf(task)
        db.delete(TABLE_NAME, whereClause, whereArgs)
        db.close()
    }
}
