package au.com.kbrsolutions.notesnuageuses.features.tasks

import android.app.Activity
import android.util.Log
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.core.FileMetadataInfo
import au.com.kbrsolutions.notesnuageuses.features.core.FoldersData
import au.com.kbrsolutions.notesnuageuses.features.eventbus.events.FoldersEvents
import com.google.android.gms.drive.DriveFolder
import com.google.android.gms.drive.DriveId
import com.google.android.gms.drive.DriveResourceClient
import com.google.android.gms.drive.MetadataChangeSet
import com.google.android.gms.tasks.Tasks
import org.greenrobot.eventbus.EventBus
import java.util.*
import java.util.concurrent.Callable

data class CreateDriveFolderTask(
        val activity: Activity,
        val eventBus: EventBus,
        val driveResourceClient: DriveResourceClient,
        val parentFolderLevel: Int,
        val parentFolderDriveId: DriveId?,
        val newFolderName: String?) : Callable<String> {


    private fun getDriveFolder(): DriveId {
        val appFolderTask = driveResourceClient.rootFolder
        Tasks.await(appFolderTask)
        return appFolderTask.result.driveId
    }

    override fun call(): String {
        Log.v(TAG, "call - parentFolderLevel/parentFolderDriveId: $parentFolderLevel/$parentFolderDriveId")
        try {
            val currFolderDriveId = parentFolderDriveId ?: getDriveFolder()

            val changeSet = MetadataChangeSet.Builder()
                    .setTitle(newFolderName!!)
                    .setMimeType(DriveFolder.MIME_TYPE)
                    .setStarred(false)
                    .build()

            val newDriverFolderTask = driveResourceClient.createFolder(
                    currFolderDriveId.asDriveFolder(), changeSet)

            val newDriveFolder = Tasks.await(newDriverFolderTask)

            val fileItemId = System.currentTimeMillis()
            FoldersData.insertFolderItemView(
                    fileItemId,
                    parentFolderLevel,
                    currFolderDriveId,
                    0,
                    FileMetadataInfo(
                            newFolderName,
                            newFolderName,
                            newDriveFolder.driveId,
                            true,
                            DriveFolder.MIME_TYPE,
                            Date(),
                            Date(),
                            fileItemId,
                            true,
                            false))

            eventBus.post(FoldersEvents.Builder(FoldersEvents.Events.FOLDER_CREATED)
                    .newFileName(newFolderName)
                    .build())
        } catch (e: IllegalStateException) {
            postCreateFolderProblemEvent(
                    activity.resources
                            .getString(R.string.base_handler_create_folder_no_connection, newFolderName))
        } catch (e: Exception) {
            //            Log.v(TAG, "call - e: " + e);// handle any exception
            e.printStackTrace()
            postCreateFolderProblemEvent(
                    activity.resources
                            .getString(R.string.base_handler_create_folder_problems,
                                    newFolderName))
        }

        return "CreatingDriveFolderCallable successfully finished"
    }

    private fun postCreateFolderProblemEvent(msg: String) {
        eventBus.post(FoldersEvents.Builder(FoldersEvents.Events.CREATE_FOLDER_PROBLEMS)
                .msgContents(msg)
                .build())
    }

    class Builder {

        private lateinit var activity: Activity
        private lateinit var eventBus: EventBus
        private lateinit var driveResourceClient: DriveResourceClient
        private var newFolderName: String? = null
        private var parentFolderLevel: Int = 0
        private var parentFolderDriveId: DriveId? = null

        fun activity(mActivity: Activity) = apply { this.activity = mActivity }

        fun eventBus(eventBus: EventBus) = apply { this.eventBus = eventBus }

        fun driveResourceClient(driveResourceClient: DriveResourceClient) =
                apply { this.driveResourceClient = driveResourceClient }

        fun parentFolderLevel(parentFolderLevel: Int) =
            apply { this.parentFolderLevel = parentFolderLevel }

        fun parentFolderDriveId(parentFolderDriveId: DriveId?) =
                apply { this.parentFolderDriveId = parentFolderDriveId }

        fun newFolderName(newFolderName: String) = apply { this.newFolderName = newFolderName }

        fun build() = CreateDriveFolderTask (
                activity,
                eventBus,
                driveResourceClient,
                parentFolderLevel,
                parentFolderDriveId,
                newFolderName
        )

    }

    companion object {

        private val TAG = CreateDriveFolderTask::class.java.simpleName
    }

}
