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
import au.com.kbrsolutions.notesnuageuses.features.eventbus.events.ActivitiesEvents
import com.google.android.gms.drive.DriveId
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RenameFileDialog : DialogFragment() {

    private var headerTv: TextView? = null
    private var fileNameEt: EditText? = null
    private var cancelBtn: Button? = null
    private var okBtn: Button? = null
    private var eventBus: EventBus? = null
    //	private HomeActivity mActivity;
    private var idxInTheFolderFilesList: Int = 0
    //	private long fileItemId;
    private var encodedSelectedDriveId: String? = null
    private var currFileName: String? = null
    private var isFolder: Boolean? = null
    private var mimeType: String? = null
    //	private long createDateMillis;
    //	private long updateDateMillis;
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
//        if (eventBus == null) {
//            eventBus = EventBus.getDefault()
//            eventBus!!.register(this)
//        }
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog)
        if (!mArgsProcessed) {
            arguments?.let {
                val bundle: Bundle = it.getBundle(BUNDLE_KEY)
                idxInTheFolderFilesList = bundle.getInt(POSITION_IN_FOLDER_FILES_LIST)
                encodedSelectedDriveId = bundle.getString(FILE_DRIVE_ID)
                currFileName = bundle.getString(CURR_FILE_NAME)
                Log.v("RenameFileDialog", """onCreate - currFileName: $currFileName """)
                isFolder = bundle.getBoolean(RenameFileDialog.IS_FOLDER)
                mimeType = bundle.getString(MIME_TYPE)
                //		createDateMillis = this.getArguments().getLong(CREATE_DATE);
                //		updateDateMillis = this.getArguments().getLong(UPDATE_DATE);
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
        Log.i(LOC_CAT_TAG, "onCreateView - start")

        val dialog = dialog
        if (isFolder!!) {
            dialog.setTitle(R.string.rename_file_folder)
        } else {
            dialog.setTitle(R.string.rename_file_file)
        }

        val v = inflater.inflate(R.layout.fragment_rename_file_dalog, container, false)

        headerTv = v.findViewById(R.id.renameFileTextHeaderId)

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
        Log.i(LOC_CAT_TAG, "okButtonClicked - fileName.length: " + fileName.length)
        if (fileName.length == 0) {
            Log.i(LOC_CAT_TAG, "okButtonClicked - fileName.length zero: " + fileName.length)
            headerTv!!.setText(R.string.rename_dialog_file_required)
            fileNameEt!!.setHint(R.string.rename_dialog_file_name)
            fileNameEt!!.setHintTextColor(Color.RED)
        } else {
            Log.i(LOC_CAT_TAG, "#@#okButtonClicked - " +
                    "currFileName/newFileName: $currFileName/$fileName$fileExtension")
            listener!!.startRenameFile(
                    DriveId.decodeFromString(encodedSelectedDriveId!!),
                    fileName + fileExtension,
                    idxInTheFolderFilesList,
                    currFolderLevel,
                    DriveId.decodeFromString(encodedCurrFolderDriveId!!))

//            eventBus!!.post(ActivitiesEvents.Builder(ActivitiesEvents.HomeEvents.RENAME_FILE)
//                    .setSelectedFileDriveId(DriveId.decodeFromString(encodedSelectedDriveId!!))
//                    .setNewFileName(fileName + fileExtension)
//                    .setIdxInTheFolderFilesList(idxInTheFolderFilesList)
//                    .setCurrFolderLevel(currFolderLevel)
//                    .setCurrFolderDriveId(DriveId.decodeFromString(encodedCurrFolderDriveId!!))
//                    .build())
            dismiss()
        }
        Log.i(LOC_CAT_TAG, "OK clicked")
    }

    private fun cancelButtonClicked() {
        Log.i(LOC_CAT_TAG, "#@#cancelClicked - end")
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ActivitiesEvents) {
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

    /*

                    .setSelectedFileDriveId(DriveId.decodeFromString(encodedSelectedDriveId!!))
                    .setNewFileName(fileName + fileExtension)
                    .setIdxInTheFolderFilesList(idxInTheFolderFilesList)
                    .setCurrFolderLevel(currFolderLevel)
                    .setCurrFolderDriveId(DriveId.decodeFromString(encodedCurrFolderDriveId!!))
     */

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
