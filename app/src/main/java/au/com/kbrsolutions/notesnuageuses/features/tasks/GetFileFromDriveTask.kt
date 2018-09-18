package au.com.kbrsolutions.notesnuageuses.features.tasks

import android.content.Context
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.events.DriveAccessEvents
import au.com.kbrsolutions.notesnuageuses.features.events.FilesDownloadEvents
import com.google.android.gms.drive.DriveFile
import com.google.android.gms.drive.DriveId
import com.google.android.gms.drive.DriveResourceClient
import com.google.android.gms.tasks.Tasks
import org.greenrobot.eventbus.EventBus
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.Callable

data class GetFileFromDriveTask(
        var context: Context,
        var eventBus: EventBus,
        var driveResourceClient: DriveResourceClient,
        val selectedDriveId: DriveId,
        val fileName: String,
        val mimeType: String,
        val fileItemId: Long,
        var idxInTheFolderFilesList: Int): Callable<String> {

    private var decryptMillis: Long = 0

    override fun call(): String? {
        val startMillis = System.currentTimeMillis()
        val fileContents: String

        val openFileTask = driveResourceClient.openFile(
                selectedDriveId.asDriveFile(),
                DriveFile.MODE_READ_ONLY)
        Tasks.await(openFileTask)

        if (!openFileTask.isSuccessful) {
            postDownloadProblemEvent(
                    context.resources.getString(
                            R.string.base_handler_download_problem,
                            fileName + "; " + openFileTask.exception))
        } else {
            val contents = openFileTask.result
            val builder = StringBuilder()

            try {

                eventBus.post(DriveAccessEvents.Builder(DriveAccessEvents.Events.MESSAGE)
                        .msgContents(context.getString(R.string.file_download_starts))
                        .build())

                BufferedReader(
                        InputStreamReader(contents.inputStream)).use { reader ->
                    var line = reader.readLine()
                    while (line != null) {
                        builder.append(line).append("\n")
                        line = reader.readLine()
                    }
                }

                fileContents = builder.toString()

                val discardTask = driveResourceClient.discardContents(contents)
                Tasks.await(discardTask)

                if (discardTask.isSuccessful) {
                    val msg = context.resources.
                            getString(
                                    R.string.base_handler_download_time_details, fileName, (System.currentTimeMillis() - startMillis) / 1000f, decryptMillis / 1000f)

                    eventBus.post(FilesDownloadEvents.Builder(FilesDownloadEvents.Events.FILE_DOWNLOADED)
                            .msgContents(msg)
                            .fileName(fileName)
                            .downloadedFileDriveId(selectedDriveId)
                            .textContents(fileContents)
                            .mimeType(mimeType)
                            .fileItemId(fileItemId)
                            .idxInTheFolderFilesList(idxInTheFolderFilesList)
                            .build())
                } else {
                    postDownloadProblemEvent(
                            context.resources.getString(
                                    R.string.base_handler_download_problem,
                                    fileName + "; " + discardTask.exception))
                }
            } catch (e: IOException) {
                postDownloadProblemEvent(
                        context.resources.getString(
                                R.string.base_handler_download_problem, "$fileName; $e"))
            } catch (e: IllegalStateException) {
                postDownloadProblemEvent(
                        context.resources.getString(
                                R.string.base_handler_download_no_connection, "$fileName; $e"))
            } catch (e: Exception) {        // added to test if any exception is handled properly
                postDownloadProblemEvent(
                        context.resources.getString(
                                R.string.base_handler_download_problem, "$fileName; $e"))
            }
        }

        return "Getting file done"
    }

    private fun postDownloadProblemEvent(msg: String) {
        eventBus.post(DriveAccessEvents.Builder(DriveAccessEvents.Events.MESSAGE)
                .msgContents(msg)
                .build())
    }

    class Builder {

        private lateinit var context: Context
        private lateinit var eventBus: EventBus
        private lateinit var driveResourceClient: DriveResourceClient
        private lateinit var selectedDriveId: DriveId
        private lateinit var fileName: String
        private lateinit var mimeType: String
        private var fileItemId: Long = -1
        private var idxInTheFolderFilesList: Int = -1

        fun context(context: Context) = apply { this.context = context }

        fun eventBus(eventBus: EventBus) = apply { this.eventBus = eventBus }

        fun driveResourceClient(driveResourceClient: DriveResourceClient) =
                apply { this.driveResourceClient = driveResourceClient }

        fun selectedDriveId(selectedDriveId: DriveId) =
                apply { this.selectedDriveId = selectedDriveId }

        fun fileName(fileName: String) = apply { this.fileName = fileName }

        fun mimeType(mimeType: String) = apply { this.mimeType = mimeType }

        fun fileItemId(fileItemId: Long) =
                apply { this.fileItemId = fileItemId }

        fun idxInTheFolderFilesList(idxInTheFolderFilesList: Int) =
                apply { this.idxInTheFolderFilesList = idxInTheFolderFilesList }

        fun build() = GetFileFromDriveTask(
                context,
                eventBus,
                driveResourceClient,
                selectedDriveId,
                fileName,
                mimeType,
                fileItemId,
                idxInTheFolderFilesList)
    }

}