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
    private var mFileContents: String = "Unknown"
    private var imm: InputMethodManager? = null
    private lateinit var mTextET: EditText
    private var mTextContents: String? = null
    private lateinit var mThisFileDriveId: DriveId
//    private var createDt: Date? = null
    private var fragmentActive: Boolean = false

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
                mFileContents = it.getString(ARG_FILE_CONTENTS_KEY)
                mThisFileDriveId = DriveId.decodeFromString(
                        it.getString(ARG_THIS_FILE_DRIVE_ID_KEY))
            }
            mArgsProcessed = true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        val rootView = inflater.inflate(R.layout.fragment_text_viewer, container, false)

        mTextET = rootView.textId
        rootView.textId.setText(mFileContents)
        return rootView
    }

    fun setFileName(fileName: String, fileContents: String, thisFileDriveId: DriveId) {
        Log.v("FileFragment before", """setFileName -
            |fileName: $fileName
            |fileContents: $fileContents
            |""".trimMargin())
        mFileName = fileName
        mFileContents = fileContents
        mThisFileDriveId = thisFileDriveId
        mTextET.setText(mFileContents)
        Log.v("FileFragment after ", """setFileName -
            |mTextET: ${mTextET.text}
            |fileContents: $fileContents
            |""".trimMargin())
    }

    private fun handleSaveMenuItemClicked() {
        Log.v("FileFragment", """handleSaveMenuItemClicked - handleSaveMenuItemClicked: start """)
        hideKeyboard()
        listener.sendTextFileToDrive(
                mThisFileDriveId,
                mFileName,
                mTextET.text.toString().toByteArray())
        cleanup("quickSaveClicked")
    }

    private fun cleanup(source: String) {
        mTextContents = null
        mTextET.setText("")
        hideKeyboard()
    }

    fun setDownloadProgressText(msg: String) {
        mTextET!!.setText(msg)
    }

//    fun showDownloadedTextNote(
////            createDt: Date?,
//            fileName: String?,
//            driveId: DriveId?,
//            fileContents: String?) {
//
//        mTextContents = fileContents
////        this.createDt = createDt
//        this.mFileName = fileName ?: "Got null"
//        this.mThisFileDriveId = driveId
//        Log.v("FileFragment", """showDownloadedTextNote - fileContents: $fileContents """)
//
//        if (!fragmentActive) {
//            return
//        }
//        mTextET!!.setText(fileContents)
//        mTextET?.let {
//            it.setText(fileContents)
//            it.isEnabled = true
//        }
//    }

    fun handleDownloadProblems(msg: String) {
        mTextET!!.setText(msg)
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

    override fun onResume() {
        super.onResume()
        fragmentActive = true
        if (mTextContents != null) {
            mTextET!!.setText(mTextContents)
        }
    }

    override fun onPause() {
        super.onPause()
        fragmentActive = false
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
        private const val ARG_FILE_CONTENTS_KEY = "arg_file_contents_key"
        private const val ARG_THIS_FILE_DRIVE_ID_KEY = "arg_this_file_drive_id_key"

        @JvmStatic
        fun newInstance(fileName: String, fileContents: String, thisFileDriveId: DriveId) =
                FileFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_FILE_NAME_KEY, fileName)
                        putString(ARG_FILE_CONTENTS_KEY, fileContents)
                        putString(ARG_THIS_FILE_DRIVE_ID_KEY, thisFileDriveId.encodeToString())
                    }
//                    Log.v("FileFragment", "newInstance - arguments: $arguments ")
                }

    }
}