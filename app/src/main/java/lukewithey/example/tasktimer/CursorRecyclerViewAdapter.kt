package lukewithey.example.tasktimer

import android.database.Cursor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.task_list_items.*
import java.lang.IllegalStateException
import kotlin.system.measureNanoTime

class TaskViewHolder ( override val containerView: View) :
        RecyclerView.ViewHolder(containerView),
        LayoutContainer {

    fun bind(task : Task, listener: CursorRecyclerViewAdapter.OnTaskClickListener) {
        tli_name.text = task.name
        tli_description.text = task.description
        tli_edit.visibility = View.VISIBLE
        tli_delete.visibility = View.VISIBLE

        tli_edit.setOnClickListener {
            listener.onEditClick(task)
        }

        tli_delete.setOnClickListener {
            listener.onDeleteClick(task)
        }

        containerView.setOnLongClickListener {
            listener.onTaskLongClick(task)
            true
        }
    }
}
// EXPERIMENTAL USE FIND VIEW BY ID IN PRODUCTION CODE
// GO TO FLICKR BROWSER APP FOR findViewByID examples

private const val TAG = "CursorRecyclerViewAdapt"


class CursorRecyclerViewAdapter(private var cursor: Cursor?, private val listener: OnTaskClickListener) :
        RecyclerView.Adapter<TaskViewHolder>() {

    interface OnTaskClickListener {
        fun onEditClick(task: Task)
        fun onDeleteClick(task: Task)
        fun onTaskLongClick(task: Task)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        Log.d(TAG, "onCreateViewHolder: new view requested")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_list_items, parent, false)
        return  TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val cursor = cursor // avoid problem with smart cast

        if(cursor == null || cursor.count == 0) {
            Log.d(TAG, "onBindViewHolder: providing instructions")
            holder.tli_name.setText(R.string.instructions_heading)
            holder.tli_description.setText(R.string.instructions)
            holder.tli_edit.visibility = View.GONE
            holder.tli_delete.visibility = View.GONE
        } else {
            if(!cursor.moveToPosition(position)) {
                throw IllegalStateException("couldn;t move cursor to position $position")
            }

            val task = Task(
                cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_NAME)),
                cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_DESCRIPTION)),
                cursor.getInt(cursor.getColumnIndex(TasksContract.Columns.TASK_SORT_ORDER)))

            // Remember that the id isn't set in the constructor

            task.id = cursor.getLong(cursor.getColumnIndex(TasksContract.Columns.ID))

        holder.bind(task, listener)

        }
    }

    override fun getItemCount(): Int {
        val cursor = cursor
        val count = if(cursor == null || cursor.count == 0) {
            1 // fib, because we opulate a single ViewHolder with instructions
        } else {
            cursor.count
        }
        Log.d(TAG, "returning $count")
        return count
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.
     * The returned old Cursor is *not* closed
     *
     * @param newCursor The new cursor is to be used
     * @return Returns the previously set Cursor, or null if there wasn't one.
     *
     */
    fun swapCursor(newCursor: Cursor?): Cursor? {
        Log.d(TAG, "swapCursor: used")
        if (newCursor === cursor) {
            return null
        }
        val numItems = itemCount
        val oldCursor = cursor
        cursor = newCursor
        if (newCursor != null) {
            // notify the observers about the new cursor
            notifyDataSetChanged()
        } else {
            // notify the observers about the lack of a data set
            notifyItemRangeRemoved(0, numItems)
        }
        return oldCursor
    }

}