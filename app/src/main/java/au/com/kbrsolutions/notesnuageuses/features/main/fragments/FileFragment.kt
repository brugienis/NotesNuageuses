package au.com.kbrsolutions.notesnuageuses.features.main.fragments

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
    private lateinit var mTextEt: EditText
    private var mTextContents: String? = null
    private lateinit var mThisFileDriveId: DriveId
    private var source = "0"

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
        Log.v("FileFragment", """onCreate - start""")
        setHasOptionsMenu(true)
        retainInstance = false

        if (!mArgsProcessed) {
            Log.v("FileFragment", """onCreate - processing arguments""")
            arguments?.let {
                mFileName = it.getString(ARG_FILE_NAME_KEY)
                mFileContents = it.getString(ARG_FILE_CONTENTS_KEY)
                mThisFileDriveId = DriveId.decodeFromString(
                        it.getString(ARG_THIS_FILE_DRIVE_ID_KEY))
            }
            mArgsProcessed = true
        }
        Log.v("FileFragment", """onCreate - end""")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        val rootView = inflater.inflate(R.layout.fragment_text_viewer, container, false)

        Log.v("FileFragment", """onCreateView - savedInstanceState: $savedInstanceState """)
        mTextEt = rootView.fileFragmentTextId

        source = "1"
        mTextEt.afterTextChanged (source) {
            Log.v("FileFragment", """onCreateView.afterTextChanged -
                |source: $source
                |mTextEt: $it
                |""".trimMargin())
//            if (source == "4") {
//                throw RuntimeException("BR afterTextChanged")
//            }
        }

        source = "2"
//        rootView.fileFragmentTextId.setText(mFileContents)
        mTextEt.setText(mFileContents)
        source = "3"

        val hash = this.hashCode()
        val hexHash = Utilities.getClassHashCode(hash)
        Log.v("FileFragment", """onCreateView - hexHash: $hexHash mTextEt: ${mTextEt.text} """)
        Log.v("FileFragment", """onCreateView - hexHash: $hexHash rootView.fileFragmentTextId: ${rootView.fileFragmentTextId.text} """)
        source = "4"
        return rootView
    }

    override fun onResume() {
        super.onResume()
        Log.v("FileFragment", """onResume - mFileContents: ${mFileContents} """)
        source = "8"
        mTextEt.setText(mFileContents)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
    }



    private fun EditText.afterTextChanged(source: String, afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                afterTextChanged.invoke(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        })
    }

    fun setFileDetails(fileName: String, fileContents: String, thisFileDriveId: DriveId) {
        source = "5"
        Log.v("FileFragment before", """setFileDetails -
            |fileName: $fileName
            |mFileContents: $mFileContents
            |fileContents: $fileContents
            |""".trimMargin())
        mFileName = fileName
        mFileContents = fileContents
        mThisFileDriveId = thisFileDriveId
        mTextEt.setText(mFileContents)
        source = "6"
        Log.v("FileFragment after ", """setFileDetails -
            |mTextEt: ${mTextEt.text}
            |mFileContents: $mFileContents
            |fileContents: $fileContents
            |""".trimMargin())
    }

    private fun handleSaveMenuItemClicked() {
        Log.v("FileFragment", """handleSaveMenuItemClicked - handleSaveMenuItemClicked: start """)
        hideKeyboard()
        listener.sendTextFileToDrive(
                mThisFileDriveId,
                mFileName,
                mTextEt.text.toString().toByteArray())
        cleanup()
    }

    private fun cleanup() {
        source = "7"
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
                }
    }
}