package lukewithey.example.tasktimer

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

private const val TAG = "MainActivity"
private const val DIALOG_ID_CANCEL_EDIT = 1

class MainActivity : AppCompatActivity(),
    AddEditFragment.OnSaveClicked,
    MainActivityFragment.OnTaskEdit {

    // Whether or not the activity is in two pane mode
    // ie running in landscape or on a tablet.
    private var mTwoPane = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        mTwoPane = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        val fragment = supportFragmentManager.findFragmentById(R.id.task_details_container)
        if (fragment != null) {
            // There was an existing fragment to edit a task, make sure the panes are set correctly.
            showEditPane()
        } else {
            task_details_container.visibility = if (mTwoPane) View.INVISIBLE else View.GONE
            mainFragment.view?.visibility = View.VISIBLE
        }

    }

    private fun showEditPane() {
        task_details_container.visibility = View.VISIBLE
        //Hide the left hand pane if in signle pane view.
        mainFragment.view?.visibility = if(mTwoPane) View.VISIBLE else View.GONE
    }

    private fun removeEditPane(fragment: Fragment? = null){
        Log.d(TAG, "removeEditPane: called")
        if(fragment != null){
            supportFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
        }

        //set the visibility of the right hand pane
        task_details_container.visibility = if(mTwoPane) View.INVISIBLE else View.GONE
        // show left hand pane
        mainFragment.view?.visibility = View.VISIBLE

        supportActionBar?.setDisplayHomeAsUpEnabled(false)

    }

    override fun onSaveClicked() {
        Log.d(TAG, "onSaveClicked: called")
        val fragment = supportFragmentManager.findFragmentById(R.id.task_details_container)
        removeEditPane(fragment)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.menumain_addtask -> taskEditRequest(null)
//            R.id.menumain_settings -> true
            android.R.id.home -> {
                Log.d(TAG,"onOptionsItemSelected up button pressed")
                val fragment = supportFragmentManager.findFragmentById(R.id.task_details_container)
//                removeEditPane(fragment)
                if ((fragment is AddEditFragment) && fragment.isDirty()) {
                    showConfirmationDialog(DIALOG_ID_CANCEL_EDIT,
                    getString(R.string.cancelEditDiag_message),
                    R.string.cancelEditDiag_positiveCaption,
                    R.string.cancelEditDiag_negativeCaption)
                } else {
                    removeEditPane(fragment)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onTaskEdit(task: Task) {
        taskEditRequest(task)
    }

    private fun taskEditRequest(task: Task?) {
        Log.d(TAG, "taskEditRequest: starts")

        // Create a new fragment to edit the task
        val newFragment = AddEditFragment.newInstance(task)
        supportFragmentManager.beginTransaction()
            .replace(R.id.task_details_container, newFragment)
            .commit()

        showEditPane()

        Log.d(TAG, "taskEditRequest: ends")

    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.task_details_container)
        if (fragment == null || mTwoPane) {
            super.onBackPressed()
        } else {
            removeEditPane(fragment)
        }

        if ((fragment is AddEditFragment) && fragment.isDirty()) {
            showConfirmationDialog(DIALOG_ID_CANCEL_EDIT,
                getString(R.string.cancelEditDiag_message),
                R.string.cancelEditDiag_positiveCaption,
                R.string.cancelEditDiag_negativeCaption)
        } else {
            removeEditPane(fragment)
        }

    }

}
