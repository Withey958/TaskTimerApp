package lukewithey.example.tasktimer

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.lang.IllegalStateException

/** Basic database class for the application.
 * The only class that should use this is the [AppProvider]
 * */
private const val TAG = "AppDatabase"

private const val DATABASE_NAME = "TaskTimer.db"
private const val DATABASE_VERSION = 1

internal class AppDatabase private constructor(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // Don't do this in production apps!!!
    init {
        Log.d(TAG, "AppDatabase initialising")
    }

    override fun onCreate(db: SQLiteDatabase) {

        // CREATE TABLE Tasks(_id INTEGER PRIMARY KEY NOT NULL, Name TEXT NOT NULL, Description TEXT, SortOrder INTEGER)
        // Implementing above ^ SQL in android using TasksContract object.

        Log.d(TAG, "onCreate: starts")
        val sSQL = """CREATE TABLE ${TasksContract.TABLE_NAME} (
            ${TasksContract.Columns.ID} INTEGER PRIMARY KEY NOT NULL,
            ${TasksContract.Columns.TASK_NAME} TEXT NOT NULL,
            ${TasksContract.Columns.TASK_DESCRIPTION} TEXT,
            ${TasksContract.Columns.TASK_SORT_ORDER} INTEGER);""".replaceIndent(" ")
// Create second table Timings. Check second column...
//        """CREATE TABLE ${TimingsContract.TABLE_NAME} (
//            ${TimingsContract.Columns.ID} INTEGER PRIMARY KEY NOT NULL,
//            ${TasksContract.Columns.ID} INTEGER NOT NULL,
//            ${TimingsContract.Columns.TIMING_START_TIME} INTEGER NOT NULL,
//            ${TimingsContract.Columns.TIMING_DURATION} INTEGER INTEGER NOT NULL);""".replaceIndent(" ")

        Log.d(TAG, sSQL)

        db.execSQL(sSQL)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "onUpgrade starts")
        when (oldVersion) {
            1 -> {
                // upgrade logic from version 1
            }
            else -> throw IllegalStateException("onUpgrade() with unknown newVersion $newVersion")
        }
    }

    companion object : SingletonHolder<AppDatabase, Context>(::AppDatabase)


}