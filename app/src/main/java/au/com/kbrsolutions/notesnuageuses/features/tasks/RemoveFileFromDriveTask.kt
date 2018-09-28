package au.com.kbrsolutions.notesnuageuses.features.tasks

import android.content.Context
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.core.FoldersData
import au.com.kbrsolutions.notesnuageuses.features.eventbus.events.DriveAccessEvents
import au.com.kbrsolutions.notesnuageuses.features.eventbus.events.FileDeleteEvents
import com.google.android.gms.drive.DriveId
import com.google.android.gms.drive.DriveResource
import com.google.android.gms.drive.DriveResourceClient
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import org.greenrobot.eventbus.EventBus
import java.util.*
import java.util.concurrent.Callable

/*
        https://developers.google.com/drive/android/trash
 */

data class RemoveFileFromDriveTask(
        var context: Context,
        var eventBus: EventBus,
        var driveResourceClient: DriveResourceClient,
        val thisFileDriveId: DriveId,
        val parentFolderDriveId: DriveId,
        val idxInTheFolderFilesList: Int,
        val thisFileFolderLevel: Int,
        val deleteThisFile: Boolean) : Callable<String> {

    private val folderMetadataArrayInfo = FoldersData.getCurrFolderMetadataInfo()
    private val folderMetadataInfo = folderMetadataArrayInfo!![idxInTheFolderFilesList]

//    private val thisFileDriveId = folderMetadataInfo.fileDriveId
    private val fileItemId = folderMetadataInfo.fileItemId
    private val parentFileName = FoldersData.getFolderTitle(thisFileFolderLevel)
    private val currName = folderMetadataInfo.fileTitle
    private val isFolder = folderMetadataInfo.isFolder
    private val mimeType = folderMetadataInfo.mimeType
    private val createDate = folderMetadataInfo.createDt
    private val updateDateBeforeRename = folderMetadataInfo.updateDt
    private var isTrashed: Boolean = folderMetadataInfo.isTrashed

    override fun call(): String {

        try {
            var success = false
            if (deleteThisFile) {
                val deleteFileTask = driveResourceClient.delete(thisFileDriveId.asDriveResource())
                Tasks.await(deleteFileTask)
                success = deleteFileTask.isSuccessful
            } else {
                val getMetadataTask = driveResourceClient.getMetadata(
                        thisFileDriveId.asDriveResource())
                Tasks.await(getMetadataTask)

                if (getMetadataTask.isSuccessful) {
                    val metadata = getMetadataTask.result

                    val driveResource: DriveResource = metadata.driveId.asDriveResource()

                    lateinit var toggleTrashTask: Task<Void>

                    if (metadata.isTrashed) {
                        toggleTrashTask = driveResourceClient.untrash(driveResource)
                        isTrashed = false
                    } else {
                        toggleTrashTask = driveResourceClient.trash(driveResource)
                        isTrashed = true
                    }
                    Tasks.await(toggleTrashTask)
                    success = toggleTrashTask.isSuccessful
                }
            }

            if (!success) {
                sendProblemEvent(
                        context.resources.getString(
                                R.string.file_delete_problem, currName))
            } else {
                val msg = if (deleteThisFile) "File was deleted" else "File is trashed: $isTrashed"
                postProgressEvent(
                        FileDeleteEvents.Events.TRASH_FILE_FINISHED,
                        msg,
                        Date())
            }

        } catch (e: IllegalStateException) {
            postProgressEvent(FileDeleteEvents.Events.TRASH_FILE_PROBLEMS,
                    context.resources.
                    getString(R.string.file_delete_connection_problem, currName + "; " + e),
                    updateDateBeforeRename)
        } catch (e: Exception) {            // added to test if any exception is handled properly
            postProgressEvent(FileDeleteEvents.Events.TRASH_FILE_PROBLEMS,
                    context.resources.
                    getString(R.string.file_delete_problem, currName + "; " + e),
                    updateDateBeforeRename)
        }

        return "Fini"
    }

    private fun postProgressEvent(
            event: FileDeleteEvents.Events,
            msg: String?,
            updateDate: Date) {
        eventBus.post(FileDeleteEvents.Builder(event)
                .msgContents(msg)
                .parentFileName(parentFileName!!)
                .fileName(currName)
                .idxInTheFolderFilesList(idxInTheFolderFilesList)
                .fileItemId(fileItemId)
                .mimeType(mimeType)
                .isFolder(isFolder)
                .createDate(createDate)
                .updateDate(updateDate)
                .thisFileFolderLevel(thisFileFolderLevel)
                .thisFileDriveId(thisFileDriveId)
                .parentFolderDriveId(parentFolderDriveId)
                .isTrashed(isTrashed)
                .isFileDeleted(deleteThisFile)
                .build())
    }

    private fun sendProblemEvent(
            fileNameAndException: String) {
        eventBus.post(DriveAccessEvents.Builder(DriveAccessEvents.Events.MESSAGE)
                .msgContents(fileNameAndException)
                .isProblem(true)
                .build())
    }

    class Builder {

        private lateinit var context: Context
        private lateinit var eventBus: EventBus
        private lateinit var driveResourceClient: DriveResourceClient
        private lateinit var thisFileDriveId: DriveId
        private lateinit var parentFolderDriveId: DriveId
        private var idxInTheFolderFilesList: Int = 0
        private var thisFileFolderLevel: Int = 0
        private var deleteThisFile: Boolean = false

        fun context(context: Context) = apply { this.context = context }

        fun eventBus(eventBus: EventBus) = apply { this.eventBus = eventBus }

        fun driveResourceClient(driveResourceClient: DriveResourceClient) =
                apply { this.driveResourceClient = driveResourceClient }

        fun parentFolderDriveId(parentFolderDriveId: DriveId) =
                apply { this.parentFolderDriveId = parentFolderDriveId }

        fun thisFileDriveId(thisFileDriveId: DriveId) =
                apply { this.thisFileDriveId = thisFileDriveId }

        fun idxInTheFolderFilesList(idxInTheFolderFilesList: Int) =
                apply { this.idxInTheFolderFilesList = idxInTheFolderFilesList }

        fun thisFileFolderLevel(thisFileFolderLevel: Int) =
                apply { this.thisFileFolderLevel = thisFileFolderLevel }

        fun deleteThisFile(deleteThisFile: Boolean) =
                apply { this.deleteThisFile = deleteThisFile }

        fun build() = RemoveFileFromDriveTask (
                context,
                eventBus,
                driveResourceClient,
                thisFileDriveId,
                parentFolderDriveId,
                idxInTheFolderFilesList,
                thisFileFolderLevel,
                deleteThisFile)
    }
}