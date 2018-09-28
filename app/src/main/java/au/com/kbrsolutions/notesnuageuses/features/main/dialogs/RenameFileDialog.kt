package au.com.kbrsolutions.notesnuageuses.features.main.dialogs

import android.app.DialogFragment
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import au.com.kbrsolutions.notesnuageuses.R
import com.google.android.gms.drive.DriveId
import org.greenrobot.eventbus.EventBus

class RenameFileDialog : DialogFragment() {

    private var headerTv: TextView? = null
    private var fileNameEt: EditText? = null
    private var cancelBtn: Button? = null
    private var okBtn: Button? = null
    private var eventBus: EventBus? = null
    private var idxInTheFolderFilesList: Int = 0
    private var encodedSelectedDriveId: String? = null
    private var currFileName: String? = null
    private var isFolder: Boolean? = null
    private var mimeType: String? = null
    private var currFolderLevel: Int = 0
    private var encodedCurrFolderDriveId: String? = null
    private var currFileNameNoExtension: String? = null
    private var fileExtension = ""
    private var fileExtensionStartIdx = -1

    private var mArgsProcessed = false
    private var listener: OnRenameFileDialogInteractionListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnRenameFileDialogInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() +
                    " must implement OnRenameFileDialogInteractionListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog)
        if (!mArgsProcessed) {
            arguments?.let {
                val bundle: Bundle = it.getBundle(BUNDLE_KEY)
                idxInTheFolderFilesList = bundle.getInt(POSITION_IN_FOLDER_FILES_LIST)
                encodedSelectedDriveId = bundle.getString(FILE_DRIVE_ID)
                currFileName = bundle.getString(CURR_FILE_NAME)
                isFolder = bundle.getBoolean(RenameFileDialog.IS_FOLDER)
                mimeType = bundle.getString(MIME_TYPE)
                currFolderLevel = bundle.getInt(CURR_FOLDER_LEVEL)
                encodedCurrFolderDriveId = bundle.getString(CURR_FOLDER_DRIVE_ID)
                if (isFolder!!) {
                    currFileNameNoExtension = currFileName
                } else {
                    fileExtensionStartIdx = currFileName!!.lastIndexOf(".")
                    fileExtension = if (fileExtensionStartIdx == -1) {
                        ""
                    } else {
                        currFileName!!.substring(fileExtensionStartIdx)
                    }
                    if (fileExtensionStartIdx > -1) {
                        currFileNameNoExtension = currFileName!!.substring(0, fileExtensionStartIdx)
                    }
                }
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {

        val dialog = dialog
        if (isFolder!!) {
            dialog.setTitle(R.string.rename_file_folder)
        } else {
            dialog.setTitle(R.string.rename_file_file)
        }

        val v = inflater.inflate(R.layout.fragment_rename_file_dalog, container, false)

        headerTv = v.findViewById(R.id.renameFileTextHeaderId)

        // fixLater: Sep 28, 2018 - remove findViewById(...) calls
        fileNameEt = v.findViewById(R.id.renameFileTextEtId)
        fileNameEt!!.setText(currFileNameNoExtension)
        fileNameEt!!.setHint(R.string.rename_dialog_file_name)
        fileNameEt!!.setSelectAllOnFocus(true)

        cancelBtn = v.findViewById(R.id.renameFileCancelBtnId)
        cancelBtn!!.isEnabled = true
        cancelBtn!!.setOnClickListener { cancelButtonClicked() }

        okBtn = v.findViewById(R.id.renameFileOkBtnId)
        okBtn!!.isEnabled = true
        okBtn!!.setOnClickListener {
            okButtonClicked()
            Log.i(LOC_CAT_TAG, "OK clicked")
        }

        getDialog().window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        return v
    }

    private fun okButtonClicked() {
        val fileName = fileNameEt!!.text.toString().trim()
        if (fileName.isEmpty()) {
            headerTv!!.setText(R.string.rename_dialog_file_required)
            fileNameEt!!.setHint(R.string.rename_dialog_file_name)
            fileNameEt!!.setHintTextColor(Color.RED)
        } else {
            listener!!.startRenameFile(
                    DriveId.decodeFromString(encodedSelectedDriveId!!),
                    fileName + fileExtension,
                    idxInTheFolderFilesList,
                    currFolderLevel,
                    DriveId.decodeFromString(encodedCurrFolderDriveId!!))
            dismiss()
        }
    }

    private fun cancelButtonClicked() {
        dismiss()
    }

    override fun onResume() {
        super.onResume()
        dialog.setOnKeyListener { dialog, keyCode, event ->
            if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
                // To dismiss the fragment when the back-button is pressed.
                dismiss()
                true
            } else
                false// Otherwise, do nothing else
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the context and potentially other fragments contained in that
     * context.
     *
     *
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnRenameFileDialogInteractionListener {
        fun startRenameFile(thisFileDriveId: DriveId,
                            newFileName: String,
                            idxInTheFolderFilesList: Int,
                            thisFileFolderLevel: Int,
                            thisFileFolderDriveId: DriveId)

    }

    companion object {
        const val BUNDLE_KEY = "bundle_key"
        const val FILE_DRIVE_ID = "selectedDriveId"
        const val CURR_FILE_NAME = "currFileName"
        const val POSITION_IN_FOLDER_FILES_LIST = "posInFolder"
        const val FILE_ITEM_ID = "fileItemId"
        const val IS_FOLDER = "isFilder"
        const val MIME_TYPE = "mimeType"
        const val CREATE_DATE = "createDate"
        const val UPDATE_DATE = "updateDate"
        const val DIALOG_TITLE = "dialogTitle"
        const val CURR_FOLDER_LEVEL = "currFolderLevel"
        const val CURR_FOLDER_DRIVE_ID = "currFolderDriveId"
        const val LOC_CAT_TAG = "RenameFileDialogFragmen"

        @JvmStatic
        fun newInstance(args: Bundle)=
                RenameFileDialog().apply {
                    arguments = Bundle().apply {
                        putBundle(BUNDLE_KEY, args)
                    }
                }
    }
}
