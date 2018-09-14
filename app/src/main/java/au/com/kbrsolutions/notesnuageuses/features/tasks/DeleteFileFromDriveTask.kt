package au.com.kbrsolutions.notesnuageuses.features.tasks

import android.content.Context
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.core.FoldersData
import au.com.kbrsolutions.notesnuageuses.features.events.ActivitiesEvents
import au.com.kbrsolutions.notesnuageuses.features.events.DriveAccessEvents
import au.com.kbrsolutions.notesnuageuses.features.events.FileDeleteEvents
import com.google.android.gms.common.api.Status
import com.google.android.gms.drive.DriveId
import com.google.android.gms.drive.DriveResource
import com.google.android.gms.drive.DriveResourceClient
import org.greenrobot.eventbus.EventBus
import java.util.*
import java.util.concurrent.Callable

data class DeleteFileFromDriveTask(
        var context: Context,
        var eventBus: EventBus,
        var driveResourceClient: DriveResourceClient,
        val selectedDriveId: DriveId,
        val idxInTheFolderFilesList: Int,
        val thisFileFolderLevel: Int,
        val thisFileFolderDriveId: DriveId) : Callable<String> {

    private val folderMetadatasInfo = FoldersData.getCurrFolderMetadataInfo()
    private val folderMetadataInfo = folderMetadatasInfo!![idxInTheFolderFilesList]

    private val selectedFileDriveId = folderMetadataInfo.fileDriveId
    private val fileItemId = folderMetadataInfo.fileItemId
    private val parentFileName = FoldersData.getFolderTitle(thisFileFolderLevel)
    private val currName = folderMetadataInfo.fileTitle
    private val isFolder = folderMetadataInfo.isFolder
    private val mimeType = folderMetadataInfo.mimeType
    private val createDate = folderMetadataInfo.createDt
    private val updateDateBeforeRename = folderMetadataInfo.updateDt
    private val isTrashable = folderMetadataInfo.isTrashable
    private val isTrashed = folderMetadataInfo.isTrashed

    private var successfullyTrashedOrUntrashed = false

    override fun call(): String {

        try {

            val driveResource: DriveResource? = null
            if (selectedFileDriveId.resourceType == DriveId.RESOURCE_TYPE_FOLDER) {
                // fixLater - uncomment below line and use new Drive API
                //					driveResource = Drive.DriveApi.getFolder(mGoogleApiClient, selectedFileDriveId);
            } else {
                // fixLater - uncomment below line and use new Drive API
                //					driveResource = Drive.DriveApi.getFile(mGoogleApiClient, selectedFileDriveId);
            }

            // If a DriveResource is a folder it will only be trashed if all of its children
            // are also accessible to this app.
            val status: Status? = null
            if (isTrashable) {
                if (isTrashed) {
                    // fixLater - uncomment below line and use new Drive API
                    //						status = driveResource.untrash(mGoogleApiClient).await();
                    //								.setResultCallback(trashStatusCallback);
                    successfullyTrashedOrUntrashed = true
                } else {
                    // fixLater - uncomment below line and use new Drive API
                    //						status = driveResource.trash(mGoogleApiClient).await();
                    //								.setResultCallback(trashStatusCallback);
                    successfullyTrashedOrUntrashed = true
                }
            }

            //				DriveFile selectedFileDrive = Drive.DriveApi.getFile(mGoogleApiClient, selectedFileDriveId);

            if (status != null && !status.isSuccess) {
                sendProblemEvent(
                        context.resources.
                                getString(
                                        R.string.file_delete_problem, currName))
            }
            postProgressEvent(FileDeleteEvents.Events.TRASH_FILE_FINISHED, null, Date())
            // TODO: 29/06/2015 not tested yet
        } catch (e: IllegalStateException) {
            postProgressEvent(FileDeleteEvents.Events.TRASH_FILE_PROBLEMS,
                    context.resources.
                    getString(R.string.file_delete_connection_problem, currName),
                    updateDateBeforeRename)
        } catch (e: Exception) {                                                                                                    // added to test if any exception is handled properly
            postProgressEvent(FileDeleteEvents.Events.TRASH_FILE_PROBLEMS,
                    context.resources.
                    getString(R.string.file_delete_problem, currName),
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
                .currFolderDriveId(thisFileFolderDriveId)
                .selectedFileDriveId(selectedFileDriveId!!)
                .isTrashed(if (successfullyTrashedOrUntrashed) !isTrashed else isTrashed)
                .build())
    }

    private fun sendProblemEvent(
            fileNameAndException: String) {
        eventBus.post(DriveAccessEvents.Builder(DriveAccessEvents.Events.MESSAGE)
                .msgContents(fileNameAndException)
                .isProblem(true)
                .build())
    }
}