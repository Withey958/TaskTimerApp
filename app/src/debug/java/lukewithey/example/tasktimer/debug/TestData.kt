package lukewithey.example.tasktimer.debug

import android.content.ContentResolver
import android.content.ContentValues
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import lukewithey.example.tasktimer.TasksContract
import lukewithey.example.tasktimer.TimingsContract
import java.util.*
import kotlin.math.roundToInt

internal class TestTiming internal constructor(var taskId: Long, date: Long, var duration: Long) {
    var startTime: Long = 0

    init {
        this.startTime = date / 1000
    }
}

object TestData {

    private const val SECS_IN_DAY = 86400
    const val LOWER_BOUND = 100
    const val UPPER_BOUND = 500
    const val MAX_DURATION = SECS_IN_DAY/6

    fun generateTestData(contentResolver: ContentResolver) {

        val projection = arrayOf(TasksContract.Columns.ID)
        val uri = TasksContract.CONTENT_URI
        val cursor = contentResolver.query(uri, projection, null, null,null)

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val taskId = cursor.getLong(cursor.getColumnIndex((TasksContract.Columns.ID)))
                val loopCount = LOWER_BOUND + getRandomInt((UPPER_BOUND - LOWER_BOUND))
                for (i in 0 until loopCount) {
                    // generate random date time
                    val randomDate = randomDateTime()

                    // generate a random duration between 0 & 4 hours
                    val duration = getRandomInt(MAX_DURATION).toLong()

                    // create new testtiming record
                    val testTiming = TestTiming(taskId, randomDate, duration)

                    // add it to database
                    saveCurrentTime(contentResolver, testTiming)

                }
            }
                while (cursor.moveToNext())
            cursor.close()
        }

    }

    private fun getRandomInt(max: Int): Int {
        return (Math.random() * max).roundToInt()
    }

    private fun randomDateTime(): Long {
        val startYear = 2019
        val endYear = 2020

        val sec = getRandomInt(59)
        val min = getRandomInt(59)
        val hour = getRandomInt(23)
        val month = getRandomInt(11)
        val year = startYear + getRandomInt(endYear - startYear)

        val gc = GregorianCalendar(year,month,1)
        val day = 1 + getRandomInt(gc.getActualMaximum(GregorianCalendar.DAY_OF_MONTH)-1)

        gc.set(year, month, day, hour, min, sec)

        return gc.timeInMillis
    }

    private fun saveCurrentTime(contentResolver: ContentResolver, currentTiming: TestTiming) {
        val values = ContentValues()
        values.put(TimingsContract.Columns.TIMING_TASK_ID, currentTiming.taskId)
        values.put(TimingsContract.Columns.TIMING_START_TIME, currentTiming.startTime)
        values.put(TimingsContract.Columns.TIMING_DURATION, currentTiming.duration)

        GlobalScope.launch {
            contentResolver.insert(TimingsContract.CONTENT_URI, values)
        }
    }

}