package au.com.kbrsolutions.notesnuageuses.features.main.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.base.BaseActivity
import au.com.kbrsolutions.notesnuageuses.features.core.FileMetadataInfo
import au.com.kbrsolutions.notesnuageuses.features.core.FoldersData
import au.com.kbrsolutions.notesnuageuses.features.main.dialogs.RenameFileDialog
import com.google.android.gms.drive.DriveId
import kotlinx.android.synthetic.main.fragment_file_details.view.*
import java.text.DateFormat

class FileDetailsFragment : Fragment() {

    private var openLayout: RelativeLayout? = null
    private var openTv: TextView? = null
    private var renameUntrashLayout: RelativeLayout? = null
    private var renameUntrashTv: TextView? = null
    private var trashDeleteLayout: RelativeLayout? = null
    private var fileDetailRenameUntrashImageId: ImageView? = null
    private var trashDeleteTv: TextView? = null
    private var kindTv: TextView? = null
    private var locationTv: TextView? = null
    private var createdTv: TextView? = null
    private var modifiedTv: TextView? = null

    private var position: Int = 0
    private var fileItemId: Long = 0
    private lateinit var selectedFileDriveId: DriveId
    private var fileName: String? = null
    private var isFolder: Boolean = false
    private var mimeType: String? = null
    private var createDateMillis: Long = 0
    private var updateDateMillis: Long = 0
    private var currFolderLevel: Int = 0
    private lateinit var currFolderDriveId: DriveId
    private var folderMetadataInfo: FileMetadataInfo? = null
    //	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MMM.d hh:mm:ss", Locale.getDefault());
    private val defaultDataFormat = DateFormat.getDateTimeInstance()
    private var listener: OnFileDetailsFragmentInteractionListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnFileDetailsFragmentInteractionListener ?:
                throw RuntimeException(context.toString() +
                        " must implement OnFileDetailsFragmentInteractionListener")

    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_file_details, container,
                false)

        openLayout = rootView.fileDetailOpenLayoutId
        rootView.fileDetailOpenLayoutId.setOnClickListener {
            openTv!!.setBackgroundColor(resources. getColor(R.color.action_view_clicked))
            openFile()
        }

        openTv = rootView.fileDetailOpenId

        val isFileTrashed = FoldersData.getCurrFolderMetadataInfo()!![position].isTrashed

        renameUntrashLayout = rootView.fileDetailRenameUntrashLayoutId

        fileDetailRenameUntrashImageId = rootView.fileDetailRenameUntrashImageId

        renameUntrashLayout!!.setOnClickListener {
            if (isFileTrashed) {
                unTrashFile()
            } else {
                renameFile()
            }
        }

        renameUntrashTv = rootView.fileDetailRenameUntrashId

        if (isFileTrashed) {
            renameUntrashTv!!.text = resources.getText(R.string.file_detail_untrash)
            fileDetailRenameUntrashImageId!!.setImageResource(R.mipmap.ic_restore_black_48dp)
        } else {
            renameUntrashTv!!.text = resources.getText(R.string.file_detail_rename)
            fileDetailRenameUntrashImageId!!.setImageResource(R.mipmap.ic_edit_black_48dp)
        }

        trashDeleteLayout = rootView.fileDetail_TrashOrDelete

        trashDeleteLayout!!.setOnClickListener {
            Log.v("FileDetailsFragment", """onCreateView - trashOrDeleteFile: trashDeleteLayout!!.setOnClickListener """)
            if (isFileTrashed) {
                deleteFile()
            } else {
                trashFile()
            }
        }

        trashDeleteTv = rootView.fileDetailTrashDeleteId

        if (isFileTrashed) {
            trashDeleteTv!!.text = resources.getText(R.string.file_detail_delete)
        } else {
            trashDeleteTv!!.text = resources.getText(R.string.file_detail_trash)
        }

        kindTv = rootView.fileDetailKindDataId

        val fileKind = when {
            folderMetadataInfo!!.isFolder -> getString(R.string.file_type_folder)
            folderMetadataInfo!!.mimeType == BaseActivity.MIME_TYPE_TEXT_FILE -> getString(R.string.file_type_file)
            else -> "Image"
        }

        kindTv!!.text = fileKind

        locationTv = rootView.fileDetailLocationDataId

        val fileLocation = if (folderMetadataInfo!!.parentTitle == null) {
            getString(R.string.app_root_folder_name)
        } else {
            folderMetadataInfo!!.parentTitle
        }

        locationTv!!.text = fileLocation

        createdTv = rootView.fileDetailCreatedDataId

        createdTv!!.text = defaultDataFormat.format(folderMetadataInfo!!.createDt)

        modifiedTv = rootView.fileDetailUpdatedDataId

        modifiedTv!!.text = defaultDataFormat.format(folderMetadataInfo!!.updateDt)

        return rootView
    }

    private fun openFile() {
        listener!!.handleOnFolderOrFileClick(position)
    }

    private fun renameFile() {
        renameUntrashLayout!!.setBackgroundColor(resources.getColor(R.color.action_view_clicked))
        val args = Bundle()
        args.putInt(RenameFileDialog.POSITION_IN_FOLDER_FILES_LIST, position)
        args.putLong(RenameFileDialog.FILE_ITEM_ID, fileItemId)
        args.putString(RenameFileDialog.FILE_DRIVE_ID, selectedFileDriveId!!.encodeToString())
        args.putString(RenameFileDialog.CURR_FILE_NAME, fileName)
        args.putBoolean(RenameFileDialog.IS_FOLDER, isFolder)
        args.putString(RenameFileDialog.MIME_TYPE, mimeType)
        args.putLong(RenameFileDialog.CREATE_DATE, createDateMillis)
        args.putLong(RenameFileDialog.UPDATE_DATE, updateDateMillis)
        args.putInt(RenameFileDialog.CURR_FOLDER_LEVEL, currFolderLevel)
        args.putString(RenameFileDialog.CURR_FOLDER_DRIVE_ID, currFolderDriveId!!.encodeToString())
        listener!!.showRenameFiledialog(args)
    }

    private fun trashFile() {
        trashDeleteTv!!.setBackgroundColor(resources.getColor(R.color.action_view_clicked))
        listener!!.trashOrDeleteFile(
                selectedFileDriveId,
                position,
                currFolderLevel,
                currFolderDriveId,
                false)
    }

    /*
		same logic to trash and un trash file. If the file is trashed, it will be un trashed and vice versa
	 */
    private fun unTrashFile() {
        trashFile()
    }

    private fun deleteFile() {
        trashDeleteTv!!.setBackgroundColor(resources.getColor(R.color.action_view_clicked))
        listener!!.trashOrDeleteFile(
                selectedFileDriveId,
                position,
                currFolderLevel,
                currFolderDriveId,
                true)
    }

    fun resetRenameTvColor() {
        renameUntrashTv!!.setBackgroundColor(resources.getColor(R.color.action_view_not_in_focus))
    }

    fun setSelectedFileInfo(
            position: Int,
            folderMetadataInfo: FileMetadataInfo,
            currFolderLevel: Int,
            currFolderDriveId: DriveId) {
        this.position = position
        this.folderMetadataInfo = folderMetadataInfo
        this.fileItemId = folderMetadataInfo.fileItemId
        this.selectedFileDriveId = folderMetadataInfo.fileDriveId!!
        this.fileName = folderMetadataInfo.fileTitle
        this.isFolder = folderMetadataInfo.isFolder
        this.mimeType = folderMetadataInfo.mimeType
        this.createDateMillis = folderMetadataInfo.createDt.getTime()
        this.updateDateMillis = folderMetadataInfo.updateDt.getTime()
        this.currFolderLevel = currFolderLevel
        this.currFolderDriveId = currFolderDriveId
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
    interface OnFileDetailsFragmentInteractionListener {
        fun handleOnFolderOrFileClick(position: Int)
        fun showRenameFiledialog(args: Bundle)
        fun trashOrDeleteFile(
                selectedFileDriveId: DriveId,
                position: Int,
                currFolderLevel: Int,
                currFolderDriveId: DriveId,
                deleteFile: Boolean)
    }

    companion object {

        private val LOC_CAT_TAG = "FileDetailsFragment"
    }

}
