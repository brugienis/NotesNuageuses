package au.com.kbrsolutions.notesnuageuses.features.main.fragments

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.Utilities
import com.google.android.gms.drive.DriveId
import kotlinx.android.synthetic.main.fragment_text_viewer.view.*

class FileFragment : Fragment() {

    private lateinit var listener: OnFileFragmentInteractionListener

    private var mArgsProcessed = false
    private var mFileName: String = "Unknown"
    private var imm: InputMethodManager? = null
    private var textET: EditText? = null
    private var textContents: String? = null
    private var driveId: DriveId? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFileFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() +
                    " must implement OnFileFragmentInteractionListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        retainInstance = true

        if (!mArgsProcessed) {
            arguments?.let {
                mFileName = it.getString(ARG_FILE_NAME_KEY)
            }
            mArgsProcessed = true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        val rootView = inflater.inflate(R.layout.fragment_text_viewer, container, false)

        textET = rootView.textId
        rootView.textId.setText("")
        return rootView
    }

    fun setFileName(fileName: String) {
        mFileName = fileName
    }

    private fun handleSaveMenuItemClicked() {
        Log.v("FileFragment", """handleSaveMenuItemClicked - handleSaveMenuItemClicked: start """)
        hideKeyboard()
        listener.sendTextFileToDrive(
                driveId,
                mFileName,
                textET!!.text.toString().toByteArray())
        cleanup("quickSaveClicked")
    }

    fun cleanup(source: String) {
        textContents = null
        textET!!.setText("")
        hideKeyboard()
    }

    private fun hideKeyboard() {
        // A_MUST: during monkey test got NullPointer Exception
        val view = view
        if (view != null && view.windowToken != null && imm != null) {
            imm!!.hideSoftInputFromWindow(getView()!!.windowToken, 0)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.text_fragment_menu, menu)
        Log.v("FileFragment", """onCreateOptionsMenu - called """)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val menuItem: MenuItem = menu.findItem(R.id.menuSaveOpenedFile)
        menuItem.isVisible = true
        menuItem.isEnabled = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.v("FileFragment", """onOptionsItemSelected - item: $item """)
        Log.v("FileFragment", """onOptionsItemSelected -
            | menuItem itemId: ${item.itemId}
            | hex id: ${Utilities.getClassHashCode(item.itemId)}
            | menuItem menuInfo: ${item.menuInfo}
            |
            | """.trimMargin())
        when (item.itemId) {

            android.R.id.home -> onUpButtonPressed()

            R.id.menuSaveOpenedFile -> handleSaveMenuItemClicked()
        }
        return true
    }

    private fun onUpButtonPressed() {
        Log.v("FileFragment", """onUpButtonPressed - onUpButtonPressed: start """)
        listener.onUpButtonPressedInFragment()
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the context and potentially other fragments contained in that
     * context.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFileFragmentInteractionListener {
        fun sendTextFileToDrive(
                existingFileDriveId: DriveId?,
                fileName: String,
                fileContents: ByteArray)
        fun onUpButtonPressedInFragment()
    }

    companion object {

        private const val ARG_FILE_NAME_KEY = "arg_file_name_key"

        @JvmStatic
        fun newInstance(fileName: String) =
                FileFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_FILE_NAME_KEY, fileName)
                    }
                    Log.v("FileFragment", "newInstance - arguments: $arguments ")
                }

    }
}