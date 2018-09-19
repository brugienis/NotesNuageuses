package au.com.kbrsolutions.notesnuageuses.features.tasks

import android.content.Context
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.core.FoldersData
import au.com.kbrsolutions.notesnuageuses.features.events.FileRenameEvents
import au.com.kbrsolutions.notesnuageuses.features.events.FileRenameEvents.Events.*
import com.google.android.gms.drive.DriveId
import com.google.android.gms.drive.DriveResourceClient
import com.google.android.gms.drive.MetadataChangeSet
import org.greenrobot.eventbus.EventBus
import java.util.*
import java.util.concurrent.Callable

data class RenameFileOnGoogleDriveCallable(
        var context: Context,
        var eventBus: EventBus,
        var driveResourceClient: DriveResourceClient,
        val selectedDriveId: DriveId,
        val newName: String,
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

        /* no need to check if there is connection to the Google Drive - this task could not be submitted if there wasn't one */

        try {
            postProgressEvent(RENAME_FILE_START, null, Date())

            //				Thread.sleep(5000);
            //				if (true) mGoogleApiClient.disconnect();
            //			if (true) throw new IllegalStateException("Just a test");
            //			if (true) throw new Exception("Just a test");
            //			if (true) throw new RuntimeException("Just a test - Runtime");

            // fixLater - uncomment below line and use new Drive API
            //				DriveFile selectedFileDrive = Drive.DriveApi.getFile(mGoogleApiClient, selectedFileDriveId);

            val changeSet = MetadataChangeSet.Builder()
                    .setTitle(newName)
                    .build()

            // fixLater - uncomment below line and use new Drive API
            //				MetadataResult metadataResult = selectedFileDrive.updateMetadata(getGoogleApiClient(), changeSet).await();

            // fixLater - uncomment below lines and use new Drive API
            //				if (!metadataResult.getStatus().isSuccess()) {
            //					postProgressEvent(HomeEvents.RENAME_FILE_FINISHED, getString(R.string.base_handler_rename_problem, currName), updateDateBeforeRename);
            //					return null;
            //				}
            postProgressEvent(RENAME_FILE_FINISHED, null, Date())
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
                .newFileName(newName)
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
}