package au.com.kbrsolutions.notesnuageuses.features.main.fragments

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import au.com.kbrsolutions.notesnuageuses.R
import com.google.android.gms.drive.DriveId
import kotlinx.android.synthetic.main.fragment_text_viewer.view.*

class FileFragment : Fragment() {

    private lateinit var listener: OnFileFragmentInteractionListener

    private var mArgsProcessed = false
    private var mFileName: String = "Unknown"
    private var mFileContents: String = "Unknown"
    private var mFileItemId: Long = -1
    private var mIdxInTheFolderFilesList: Int = -1
    private var imm: InputMethodManager? = null
    private lateinit var mTextEt: EditText
    private var mTextContents: String? = null
    private var mThisFileDriveId: DriveId? = null

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
        retainInstance = false

        if (!mArgsProcessed) {
            arguments?.let {
                mFileName = it.getString(ARG_FILE_NAME_KEY)
                mFileContents = it.getString(ARG_FILE_CONTENTS_KEY)
                if (it.containsKey(ARG_THIS_FILE_DRIVE_ID_KEY)) {
                    mThisFileDriveId = DriveId.decodeFromString(
                            it.getString(ARG_THIS_FILE_DRIVE_ID_KEY))
                }
                mFileItemId = it.getLong(ARG_FILE_ITEM_ID_KEY)
                mIdxInTheFolderFilesList = it.getInt(ARG_IDX_IN_THE_FOLDER_FILES_LIST_KEY)
            }
            mArgsProcessed = true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        val rootView = inflater.inflate(R.layout.fragment_text_viewer, container, false)

        mTextEt = rootView.fileFragmentTextId

        return rootView
    }

    override fun onResume() {
        super.onResume()
        mTextEt.setText(mFileContents)
    }

//    override fun onSaveInstanceState(outState: Bundle?) {
//        super.onSaveInstanceState(outState)
//    }

    fun setFileDetails(fileName: String, fileContents: String, thisFileDriveId: DriveId?,
                       fileItemId: Long, idxInTheFolderFilesList: Int) {
        mFileName = fileName
        mFileContents = fileContents
        mThisFileDriveId = thisFileDriveId
        mFileItemId = fileItemId
        mIdxInTheFolderFilesList = idxInTheFolderFilesList
    }

    private fun handleSaveMenuItemClicked() {
        hideKeyboard()
        listener.sendTextFileToDrive(
                mThisFileDriveId,
                mFileName,
                mTextEt.text.toString().toByteArray(),
                mFileItemId,
                mIdxInTheFolderFilesList)
        cleanup()
    }

    private fun cleanup() {
        mTextContents = null
        mTextEt.setText("")
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
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val menuItem: MenuItem = menu.findItem(R.id.menuSaveOpenedFile)
        menuItem.isVisible = true
        menuItem.isEnabled = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            android.R.id.home -> onUpButtonPressed()

            R.id.menuSaveOpenedFile -> handleSaveMenuItemClicked()
        }
        return true
    }

    private fun onUpButtonPressed() {
        listener.onUpButtonPressedInFragment()
    }

    fun getText(): String {
        return mTextEt.text.toString()
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
                fileContents: ByteArray,
                fileItemId: Long,
                idxInTheFolderFilesList: Int)
        fun onUpButtonPressedInFragment()
    }

    companion object {

        private const val ARG_FILE_NAME_KEY = "arg_file_name_key"
        private const val ARG_FILE_CONTENTS_KEY = "arg_file_contents_key"
        private const val ARG_THIS_FILE_DRIVE_ID_KEY = "arg_this_file_drive_id_key"
        private const val ARG_FILE_ITEM_ID_KEY = "arg_file_item_id_key"
        private const val ARG_IDX_IN_THE_FOLDER_FILES_LIST_KEY =
                "arg_idx_in_the_folder_files_list_key"

        @JvmStatic
        fun newInstance(
                fileName: String,
                fileContents: String,
                thisFileDriveId: DriveId?,
                fileItemId: Long,
                idxInTheFolderFilesList: Int) =
                FileFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_FILE_NAME_KEY, fileName)
                        putString(ARG_FILE_CONTENTS_KEY, fileContents)
                        if (thisFileDriveId != null) {
                            putString(ARG_THIS_FILE_DRIVE_ID_KEY, thisFileDriveId.encodeToString())
                        }
                        putLong(ARG_FILE_ITEM_ID_KEY, fileItemId)
                        putInt(ARG_IDX_IN_THE_FOLDER_FILES_LIST_KEY, idxInTheFolderFilesList)
                    }
                }
    }
}