package lukewithey.example.tasktimer

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_main.*
import java.lang.AssertionError
import java.lang.RuntimeException

/**
 * A simple [Fragment] subclass.
 */

private const val TAG = "MainActivityFragment"
private const val DIALOG_ID_DELETE = 1
private const val DIALOG_TASK_ID = "task_id"

class MainActivityFragment : Fragment(),
CursorRecyclerViewAdapter.OnTaskClickListener,
AppDialog.DialogEvents{

    interface OnTaskEdit {
        fun onTaskEdit(task: Task)
    }

    private val viewModel by lazy { ViewModelProviders.of(requireActivity()).get(TaskTimerViewModel::class.java) }
    private val mAdapter = CursorRecyclerViewAdapter(null, this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: called")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if(context !is OnTaskEdit) {
            throw RuntimeException("${context.toString()} must implement OnTaskEdit")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.cursor.observe(this, Observer {cursor -> mAdapter.swapCursor(cursor)?.close()})
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        task_list.layoutManager = LinearLayoutManager(context)
        task_list.adapter = mAdapter
    }

    override fun onEditClick(task: Task) {
        (activity as OnTaskEdit?)?.onTaskEdit(task)
    }

    override fun onDeleteClick(task: Task) {
        val args = Bundle().apply {
            putInt(DIALOG_ID, DIALOG_ID_DELETE)
            putString(DIALOG_MESSAGE, getString(R.string.deldiag_message))
            putInt(DIALOG_POSITIVE_RID, R.string.deldiag_positive_action)
            putLong(DIALOG_TASK_ID, task.id) // pass the id in the arguments, so we can retrieve it when we get a callback
        }

        val dialog = AppDialog()
        dialog.arguments = args
        dialog.show(childFragmentManager, null)
    }

    override fun onTaskLongClick(task: Task) {
        TODO("Not yet implemented")
    }

    override fun onPositiveDialogResult(dialogId: Int, args: Bundle) {
        Log.d(TAG, "onPositiveDialogueResult: called with id $dialogId")

        if (dialogId == DIALOG_ID_DELETE) {
            val taskId = args.getLong(DIALOG_TASK_ID)
            if (BuildConfig.DEBUG && taskId == 0L) throw AssertionError("Task ID is zero")
            viewModel.deleteTask(taskId)
        }

    }
}
