package au.com.kbrsolutions.notesnuageuses.features.tasks

import android.content.Context
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.core.FileMetadataInfo
import au.com.kbrsolutions.notesnuageuses.features.core.FolderData
import au.com.kbrsolutions.notesnuageuses.features.core.FoldersData
import au.com.kbrsolutions.notesnuageuses.features.eventbus.events.FoldersEvents
import com.google.android.gms.drive.DriveFolder
import com.google.android.gms.drive.DriveId
import com.google.android.gms.drive.DriveResourceClient
import com.google.android.gms.drive.Metadata
import com.google.android.gms.drive.query.Query
import com.google.android.gms.tasks.Tasks
import org.greenrobot.eventbus.EventBus
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException

data class DownloadFolderInfoTask(
        var activity: Context,
        var eventBus: EventBus,
        var driveResourceClient: DriveResourceClient,
        var selectedFolderTitle: String,
        var selectedFolderDriveId: DriveId?,
        var parentFolderLevel: Int,
        var parentFolderDriveId: DriveId?): Callable<String> {

    override fun call(): String {

        try {
            val foldersAddData: FolderData = getFolderFilesList()

            eventBus.post(FoldersEvents.Builder(FoldersEvents.Events.FOLDER_DATA_RETRIEVED)
                    .foldersAddData(foldersAddData)
                    .build())

        } catch (e: Exception) {
            eventBus.post(FoldersEvents.Builder(FoldersEvents.Events.FOLDER_DATA_RETRIEVE_PROBLEM)
                    .msgContents(activity
                            .resources
                            .getString(
                                    R.string.base_handler_retrieve_folder_problem,
                                    selectedFolderTitle,
                                    e))
                    .build())
        }

        return "DownloadFolderInfoTask done"
    }

    private fun getDriveFolder(): DriveFolder {
        val appFolderTask = driveResourceClient.rootFolder
        Tasks.await(appFolderTask)
        return appFolderTask.result
    }

    private fun getFolderFilesList(): FolderData {

        val processingNewFolder =
                selectedFolderDriveId == null || selectedFolderDriveId != parentFolderDriveId

        // https://developers.google.com/android/guides/tasks

        lateinit var foldersAddData: FolderData
        try {
            val selectedDriveFolder = selectedFolderDriveId?.asDriveFolder() ?: getDriveFolder()

            val query = Query.Builder()
                    .build()

            val queryTask = driveResourceClient
                    .queryChildren(selectedDriveFolder, query)

            val metadataBuffer = Tasks.await(queryTask)

            var trashedFilesCnt = 0
            val foldersMetadata = ArrayList<Metadata>()
            val folderMetadataArrayInfo = ArrayList<FileMetadataInfo>()
            var folderMetadataInfo: FileMetadataInfo
            for (metadata in metadataBuffer) {
                if (metadata.isTrashed) {
                    trashedFilesCnt++
                }
                run {
                    folderMetadataInfo = FileMetadataInfo(selectedFolderTitle, metadata)
                    folderMetadataArrayInfo.add(folderMetadataInfo)
                    foldersMetadata.add(metadata)
                }
            }
            metadataBuffer.release()

            when {
                selectedFolderDriveId == null ->
                    foldersAddData = FolderData(
                        selectedDriveFolder.driveId,
                        selectedFolderTitle,
                        -1,
                        selectedDriveFolder.driveId,
                        true,
                        trashedFilesCnt,
                        folderMetadataArrayInfo)

                processingNewFolder ->
                    foldersAddData = FolderData(
                        selectedDriveFolder.driveId,
                        selectedFolderTitle,
                        parentFolderLevel,
                        parentFolderDriveId!!,
                        true,
                        trashedFilesCnt,
                        folderMetadataArrayInfo)

                else -> FoldersData.refreshFolderData(
                        parentFolderLevel,
                        selectedDriveFolder.driveId,
                        trashedFilesCnt,
                        folderMetadataArrayInfo)
            }
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        return foldersAddData
    }

    class Builder {

        private lateinit var context: Context
        private lateinit var eventBus: EventBus
        private lateinit var driveResourceClient: DriveResourceClient
        private lateinit var selectedFolderTitle: String
        private var selectedFolderDriveId: DriveId? = null
        private var parentFolderLevel: Int = -1
        private var parentFolderDriveId: DriveId? = null

        fun context(context: Context) = apply { this.context = context }

        fun selectedFolderTitle(selectedFolderTitle: String) =
                apply { this.selectedFolderTitle = selectedFolderTitle }

        fun eventBus(eventBus: EventBus) = apply { this.eventBus = eventBus }

        fun driveResourceClient(driveResourceClient: DriveResourceClient) =
                apply { this.driveResourceClient = driveResourceClient }

        fun selectedFolderDriveId(selectedFolderDriveId: DriveId?) =
                apply { this.selectedFolderDriveId = selectedFolderDriveId }

        fun parentFolderLevel(parentFolderLevel: Int) =
                apply { this.parentFolderLevel = parentFolderLevel }

        fun parentFolderDriveId(parentFolderDriveId: DriveId) =
                apply { this.parentFolderDriveId = parentFolderDriveId }

        fun build() = DownloadFolderInfoTask (
                context,
                eventBus,
                driveResourceClient,
                selectedFolderTitle,
                selectedFolderDriveId,
                parentFolderLevel,
                parentFolderDriveId)
    }

}