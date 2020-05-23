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
private const val DATABASE_VERSION = 3

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
        Log.d(TAG, sSQL)
        db.execSQL(sSQL)

        addTimingsTable(db)
        addCurrentTimingView(db)
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "onUpgrade starts")
        when (oldVersion) {
            1 -> {
                addTimingsTable(db) // upgrade logic from version 1
                addCurrentTimingView(db)
            }
            2 -> {
                addCurrentTimingView(db)
            }
            else -> throw IllegalStateException("onUpgrade() with unknown newVersion $newVersion")
        }
    }

    private fun addTimingsTable(db: SQLiteDatabase) {

        // Create second table Timings. Check second column...
        val sSQLTiming = """CREATE TABLE ${TimingsContract.TABLE_NAME} (
            ${TimingsContract.Columns.ID} INTEGER PRIMARY KEY NOT NULL,
            ${TimingsContract.Columns.TIMING_TASK_ID} INTEGER NOT NULL,
            ${TimingsContract.Columns.TIMING_START_TIME} INTEGER,
            ${TimingsContract.Columns.TIMING_DURATION} INTEGER);""".replaceIndent(" ")
        Log.d(TAG, sSQLTiming)
        db.execSQL(sSQLTiming)

        // Create a trigger everytime a task is removed
        val sSQLTrigger = """CREATE TRIGGER Remove_Task
            AFTER DELETE ON ${TasksContract.TABLE_NAME}
            FOR EACH ROW
            BEGIN
            DELETE FROM ${TimingsContract.TABLE_NAME}
            WHERE ${TimingsContract.Columns.TIMING_TASK_ID} = OLD.${TasksContract.Columns.ID};
        END;""".replaceIndent(" ")
        Log.d(TAG, sSQLTrigger)
        db.execSQL(sSQLTrigger)
    }

    private fun addCurrentTimingView (db: SQLiteDatabase) {
        // create the view in our database in

        /*
CREATE VIEW vwCurrentTiming
     AS SELECT Timings._id,
         Timings.TaskId,
         Timings.StartTime,
         Tasks.Name
     FROM Timings
     JOIN Tasks
     ON Timings.TaskId = Tasks._id
     WHERE Timings.Duration = 0
     ORDER BY Timings.StartTime DESC;
 */

        val sSQLCurrentTiming = """CREATE VIEW ${CurrentTimingContract.TABLE_NAME}
        AS SELECT 
            ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.ID},
            ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_TASK_ID},
            ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_START_TIME},
            ${TasksContract.TABLE_NAME}.${TasksContract.Columns.TASK_NAME}
        FROM ${TimingsContract.TABLE_NAME}
        JOIN ${TasksContract.TABLE_NAME}
        ON ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_TASK_ID} = ${TasksContract.TABLE_NAME}.${TasksContract.Columns.ID}
        WHERE ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_DURATION} = 0
        ORDER BY ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_START_TIME} DESC;
        """.replaceIndent(" ")

        Log.d(TAG, sSQLCurrentTiming)
        db.execSQL(sSQLCurrentTiming)
    }

    companion object : SingletonHolder<AppDatabase, Context>(::AppDatabase)


}