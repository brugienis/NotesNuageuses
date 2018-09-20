package au.com.kbrsolutions.notesnuageuses.features.tasks

import android.content.Context
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.core.FoldersData
import au.com.kbrsolutions.notesnuageuses.features.events.FileRenameEvents
import au.com.kbrsolutions.notesnuageuses.features.events.FileRenameEvents.Events.*
import com.google.android.gms.drive.DriveId
import com.google.android.gms.drive.DriveResourceClient
import com.google.android.gms.drive.MetadataChangeSet
import com.google.android.gms.tasks.Tasks
import org.greenrobot.eventbus.EventBus
import java.util.*
import java.util.concurrent.Callable

data class RenameFileOnDriveTask(
        var context: Context,
        var eventBus: EventBus,
        var driveResourceClient: DriveResourceClient,
        val thisFileDriveId: DriveId,
        val newFileName: String,
        val idxInTheFolderFilesList: Int,
        val thisFileFolderLevel: Int,
        val thisFileFolderDriveId: DriveId) : Callable<String> {

    private val selectedFileDriveId: DriveId
    private val parentFileName: String
    private val currName: String
    val fileItemId: Long
    private val isFolder: Boolean
    private val mimeType: String
    private val createDate: Date
    private val updateDateBeforeRename: Date

    init {
        val folderMetadatasInfo = FoldersData.getCurrFolderMetadataInfo()
        val folderMetadataInfo = folderMetadatasInfo!!.get(idxInTheFolderFilesList)
        this.selectedFileDriveId = folderMetadataInfo.fileDriveId!!
        this.fileItemId = folderMetadataInfo.fileItemId
        this.parentFileName = FoldersData.getFolderTitle(thisFileFolderLevel)!!
        this.currName = folderMetadataInfo.fileTitle
        this.isFolder = folderMetadataInfo.isFolder
        this.mimeType = folderMetadataInfo.mimeType
        this.createDate = folderMetadataInfo.createDt
        this.updateDateBeforeRename = folderMetadataInfo.updateDt
    }

    override fun call(): String {
        try {
            postProgressEvent(RENAME_FILE_START, null, Date())

            val changeSet = MetadataChangeSet.Builder()
                    .setTitle(newFileName)
                    .build()

            val updateMetadataTask =
                    driveResourceClient.updateMetadata(thisFileDriveId.asDriveResource(), changeSet)
            Tasks.await(updateMetadataTask)

            if (updateMetadataTask.isSuccessful) {
                postProgressEvent(RENAME_FILE_FINISHED, null, Date())
            } else {
                postProgressEvent(RENAME_FILE_PROBLEMS,

                        context.resources.getString(R.string.base_handler_rename_problem, currName),
                        updateDateBeforeRename)
            }
        } catch (e: IllegalStateException) {
            postProgressEvent(RENAME_FILE_PROBLEMS,

                    context.resources.getString(R.string.base_handler_rename_connection_problem, currName), updateDateBeforeRename)
        } catch (e: Exception) {
            postProgressEvent(RENAME_FILE_PROBLEMS,

                    context.resources.getString(R.string.base_handler_rename_problem, currName), updateDateBeforeRename)
        }

        return ""
    }

    private fun postProgressEvent(event: FileRenameEvents.Events, msg: String?, updateDate: Date) {
        eventBus.post(FileRenameEvents.Builder(event)
                .msgContents(msg)
                .parentFileName(parentFileName)
                .fileName(currName)
                .newFileName(newFileName)
                .idxInTheFolderFilesList(idxInTheFolderFilesList)
                .fileItemId(fileItemId)
                .mimeType(mimeType)
                .isFolder(isFolder)
                .createDate(createDate)
                .updateDate(updateDate)
                .thisFileFolderLevel(thisFileFolderLevel)
                .currFolderDriveId(thisFileFolderDriveId)
                .thisFileDriveId(selectedFileDriveId)
                .build())
    }

    class Builder {

        private lateinit var context: Context
        private lateinit var eventBus: EventBus
        private lateinit var driveResourceClient: DriveResourceClient
        private lateinit var thisFileDriveId: DriveId
        private lateinit var newFileName: String
        private var idxInTheFolderFilesList: Int = 0
        private var thisFileFolderLevel: Int = 0
        private lateinit var thisFileFolderDriveId: DriveId

        fun context(context: Context) = apply { this.context = context }

        fun eventBus(eventBus: EventBus) = apply { this.eventBus = eventBus }

        fun driveResourceClient(driveResourceClient: DriveResourceClient) =
                apply { this.driveResourceClient = driveResourceClient }

        fun thisFileDriveId(thisFileDriveId: DriveId) =
                apply { this.thisFileDriveId = thisFileDriveId }

        fun newFileName(newFileName: String) =
                apply { this.newFileName = newFileName }

        fun idxInTheFolderFilesList(idxInTheFolderFilesList: Int) =
                apply { this.idxInTheFolderFilesList = idxInTheFolderFilesList }

        fun thisFileFolderLevel(thisFileFolderLevel: Int) =
                apply { this.thisFileFolderLevel = thisFileFolderLevel }

        fun thisFileFolderDriveId(thisFileFolderDriveId: DriveId) =
                apply { this.thisFileFolderDriveId = thisFileFolderDriveId }

        fun build() = RenameFileOnDriveTask (
                context,
                eventBus,
                driveResourceClient,
                thisFileDriveId,
                newFileName,
                idxInTheFolderFilesList,
                thisFileFolderLevel,
                thisFileFolderDriveId)
    }
}