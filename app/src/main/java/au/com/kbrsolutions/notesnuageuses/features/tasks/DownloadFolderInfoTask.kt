package au.com.kbrsolutions.notesnuageuses.features.tasks

import android.app.Activity
import android.util.Log
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.core.FileMetadataInfo
import au.com.kbrsolutions.notesnuageuses.features.core.FolderData
import au.com.kbrsolutions.notesnuageuses.features.core.FoldersData
import au.com.kbrsolutions.notesnuageuses.features.events.FoldersEvents
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
        var activity: Activity,
        var eventBus: EventBus,
        var driveResourceClient: DriveResourceClient,
        var mSelectedFolderTitle: String,
        var mSelectedFolderDriveId: DriveId?,
        var mParentFolderLevel: Int,
        var mParentFolderDriveId: DriveId?,
        var mCurrentFolderDriveId: DriveId?,
        var mFoldersData: FoldersData
//        var processingNewFolder: Boolean = false
        ): Callable<String> {

    private val TAG = DownloadFolderInfoTask::class.java.simpleName

    private var processingNewFolder: Boolean = false

    override fun call(): String {
        processingNewFolder =
                mCurrentFolderDriveId == null ||
                mSelectedFolderDriveId != mCurrentFolderDriveId
        Log.v("DownloadFolderInfoTask", "call - processingNewFolder: $processingNewFolder ")

        try {
            val foldersAddData: FolderData = getFolderFilesList(driveResourceClient, mSelectedFolderDriveId)

//            Log.v("DownloadFolderInfoTask", "call - falling asleep ")
            Thread.sleep(1000)  // delay at development time
//            Log.v("DownloadFolderInfoTask", "call - waking up ")

            eventBus.post(FoldersEvents.Builder(FoldersEvents.Events.FOLDER_DATA_RETRIEVED)
                    .foldersAddData(foldersAddData)
                    .build())

        } catch (e: Exception) {
            eventBus.post(FoldersEvents.Builder(FoldersEvents.Events.FOLDER_DATA_RETRIEVE_PROBLEM)
                    .msgContents(activity
                            .resources
                            .getString(
                                    R.string.base_handler_retrieve_folder_problem,
                                    mSelectedFolderTitle, e))
                    .build())
        }

        return "RetrievingRootDriveFolderInfoCallable"
    }

    private fun getFolderFilesList(
            mDriveResourceClient: DriveResourceClient?,
            selectedFolderDriveId: DriveId?): FolderData {

        // https://developers.google.com/android/guides/tasks
        val selectedDriveFolder: DriveFolder
//        Variable 'foldersAddData' must be initialized
        lateinit var foldersAddData: FolderData
        try {
            selectedDriveFolder = if (selectedFolderDriveId == null) {
                // Task<DriveFolder> appFolderTask = driveResourceClient.getAppFolder();
                val appFolderTask = mDriveResourceClient!!.rootFolder
                Tasks.await(appFolderTask)
            } else {
                selectedFolderDriveId.asDriveFolder()
            }
            val query = Query.Builder()
                    // .addFilter(Filters.eq(SearchableField.MIME_TYPE, "text/plain"))
                    .build()

            val queryTask = mDriveResourceClient!!
                    .queryChildren(selectedDriveFolder, query)

            val metadataBuffer = Tasks.await(queryTask)

            var trashedFilesCnt = 0
            val foldersMetadata = ArrayList<Metadata>()
            val foldersMetadatasInfo = ArrayList<FileMetadataInfo>()
            var folderMetadataInfo: FileMetadataInfo
            for (metadata in metadataBuffer) {
                if (metadata.isTrashed) {
                    trashedFilesCnt++
                }
                run {
                    folderMetadataInfo = FileMetadataInfo(mSelectedFolderTitle, metadata)
                    foldersMetadatasInfo.add(folderMetadataInfo)
                    foldersMetadata.add(metadata)
                }
            }
            metadataBuffer.release()

            when {
                selectedFolderDriveId == null ->
                    foldersAddData = FolderData(
                        selectedDriveFolder.driveId,
                        mSelectedFolderTitle,
                        -1,
                        selectedDriveFolder.driveId,
                        true,
                        trashedFilesCnt,
                        foldersMetadatasInfo)

                processingNewFolder ->
                    foldersAddData = FolderData(
                        selectedDriveFolder.driveId,
                        mSelectedFolderTitle,
                        mParentFolderLevel,
                        mParentFolderDriveId!!,
                        true,
                        trashedFilesCnt,
                        foldersMetadatasInfo)

                else -> mFoldersData.refreshFolderData(
                        mParentFolderLevel,
                        selectedDriveFolder.driveId,
                        trashedFilesCnt,
                        foldersMetadatasInfo)
            }
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        Log.v("DownloadFolderInfoTask", "getFolderFilesList - mParentFolderDriveId: $mParentFolderDriveId foldersAddData: $foldersAddData ")
        return foldersAddData
    }

    class Builder {

        private lateinit var activity: Activity
        private lateinit var eventBus: EventBus
        private lateinit var driveResourceClient: DriveResourceClient
        private lateinit var selectedFolderTitle: String
        private var selectedFolderDriveId: DriveId? = null
        private var parentFolderLevel: Int = -1
        private var parentFolderDriveId: DriveId? = null
        private var currentFolderDriveId: DriveId? = null
        private lateinit var foldersData: FoldersData

        fun activity(activity: Activity) = apply { this.activity = activity }

        fun selectedFolderTitle(selectedFolderTitle: String) =
                apply { this.selectedFolderTitle = selectedFolderTitle }

        fun eventBus(eventBus: EventBus) = apply { this.eventBus = eventBus }

        fun driveResourceClient(driveResourceClient: DriveResourceClient) =
                apply { this.driveResourceClient = driveResourceClient }

        fun selectedFolderDriveId(selectedFolderDriveId: DriveId) =
                apply { this.selectedFolderDriveId = selectedFolderDriveId }

        fun parentFolderLevel(parentFolderLevel: Int) =
                apply { this.parentFolderLevel = parentFolderLevel }

        fun parentFolderDriveId(parentFolderDriveId: DriveId) =
                apply { this.parentFolderDriveId = parentFolderDriveId }

        fun currentFolderDriveId(currentFolderDriveId: DriveId) =
                apply { this.currentFolderDriveId = currentFolderDriveId }

        fun foldersData(foldersData: FoldersData) = apply { this.foldersData = foldersData }

        fun build() = DownloadFolderInfoTask (
                activity,
                eventBus,
                driveResourceClient,
                selectedFolderTitle,
                selectedFolderDriveId,
                parentFolderLevel,
                parentFolderDriveId,
                currentFolderDriveId,
                foldersData)
    }

}