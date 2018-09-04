package au.com.kbrsolutions.notesnuageuses.features.main.fragments

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import au.com.kbrsolutions.notesnuageuses.R
import com.google.android.gms.drive.DriveId
import kotlinx.android.synthetic.main.fragment_text_viewer.view.*
import java.util.*

class TextFragment : Fragment() {

    private lateinit var listener: OnTextFragmentInteractionListener

    private var mArgsProcessed = false

    private var textET: EditText? = null
    private var enableTextET = true
    private var imm: InputMethodManager? = null
    private var fragmentActive: Boolean = false
    private var creatingNewNote: Boolean = false
    private var mFileName: String = "Unknown"
    private var driveId: DriveId? = null
    private var createDt: Date? = null
    private var textContents: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnTextFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() +
                    " must implement OnTextFragmentInteractionListener")
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

    override fun onResume() {
        super.onResume()
        fragmentActive = true
        if (textContents != null) {
            textET!!.setText(textContents)
        }
    }

    override fun onPause() {
        super.onPause()
        fragmentActive = false
    }

    private fun handleSaveMenuItemClicked() {
        Log.v("TextFragment", """handleSaveMenuItemClicked - handleSaveMenuItemClicked: start """)
        hideKeyboard()
        listener.sendTextFileToDrive(
                driveId,
                mFileName,
                textET!!.text.toString().toByteArray())
        cleanup("quickSaveClicked")
    }

    fun setFileName(fileName: String) {
        mFileName = fileName
    }

    fun setDownloadProgressText(msg: String) {
        textET!!.setText(msg)
    }

    fun showDownloadedTextNote(
            createDt: Date?,
            fileName: String?,
            driveId: DriveId?,
            contents: String?) {

        textContents = contents
        this.createDt = createDt
        this.mFileName = fileName ?: "Got null"
        this.driveId = driveId

        if (!fragmentActive) {
            return
        }
        textET!!.setText(contents)
        textET?.let {
            it.setText(contents)
            it.isEnabled = true
        }
    }

    fun handleDownloadProblems(msg: String) {
        textET!!.setText(msg)
        hideKeyboard()
    }

    private fun hideKeyboard() {
        // A_MUST: during monkey test got NullPointer Exception
        val view = view
        if (view != null && view.windowToken != null && imm != null) {
            imm!!.hideSoftInputFromWindow(getView()!!.windowToken, 0)
        }
    }

    fun setEnableTextView(enableTextView: Boolean) {
        enableTextET = enableTextView
    }

    fun setCreatingNewNote() {
        creatingNewNote = true
    }

    fun cleanup(source: String) {
        textContents = null
        textET!!.setText("")
        hideKeyboard()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.text_fragment_menu, menu)
        Log.v("TextFragment", """onCreateOptionsMenu - called """)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val menuItem: MenuItem = menu.findItem(R.id.menuSaveOpenedFile)
        menuItem.isVisible = true
        menuItem.isEnabled = true
        Log.v("TextFragment", """onPrepareOptionsMenu - menuItem: $menuItem """)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.v("TextFragment", """onOptionsItemSelected - item: $item """)
        when (item.itemId) {
            R.id.menuSaveOpenedFile -> handleSaveMenuItemClicked()
        }
        return true
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
    interface OnTextFragmentInteractionListener {
        fun sendTextFileToDrive(
                existingFileDriveId: DriveId?,
                fileName: String,
                fileContents: ByteArray)
    }

    companion object {

        private const val ARG_FILE_NAME_KEY = "arg_file_name_key"

        @JvmStatic
        fun newInstance(fileName: String) =
                TextFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_FILE_NAME_KEY, fileName)
                    }
                    Log.v("TextFolderFragment", "newInstance - arguments: $arguments ")
                }

    }

}