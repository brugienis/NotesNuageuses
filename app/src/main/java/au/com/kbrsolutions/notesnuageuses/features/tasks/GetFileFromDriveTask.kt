package au.com.kbrsolutions.notesnuageuses.features.tasks

import android.content.Context
import android.util.Log
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
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.Callable

// fixLater: Sep 06, 2018 - remove folderLevel
data class GetFileFromDriveTask(
        var context: Context,
        var eventBus: EventBus,
        var driveResourceClient: DriveResourceClient,
        val selectedDriveId: DriveId,
        val fileName: String,
        val mimeType: String): Callable<String> {

    private var decryptMillis: Long = 0

    override fun call(): String? {
        Log.v("GetFileFromDriveTask", """call start - fileName: $fileName """)
        val startMillis = System.currentTimeMillis()
        val fileContents: String

        val openFileTask = driveResourceClient.openFile(
                selectedDriveId.asDriveFile(),
                DriveFile.MODE_READ_ONLY)
        Tasks.await(openFileTask)

        Log.v("GetFileFromDriveTask", """call - Tasks.await(openFileTask): after """)

        if (!openFileTask.isSuccessful) {
            postDownloadProblemEvent(
                    context.resources.getString(
                            R.string.base_handler_download_problem,
                            fileName + "; " + openFileTask.exception))
        } else {
            val contents = openFileTask.result
            val builder = StringBuilder()

            try {
                Log.v("GetFileFromDriveTask", """call - eventBus.post(FilesUploadEvents.Builder(FilesUploadEvents.Events.FILE_DOWNLOADING): before """)

                eventBus.post(DriveAccessEvents.Builder(DriveAccessEvents.Events.MESSAGE)
                        .msgContents(context.getString(R.string.file_download_starts))
                        .build())
                Log.v("GetFileFromDriveTask", """call - eventBus.post(FilesUploadEvents.Builder(FilesUploadEvents.Events.FILE_DOWNLOADING): after """)

                BufferedReader(
                        InputStreamReader(contents.inputStream)).use { reader ->
                    var line = reader.readLine()
                    while (line != null) {
                        builder.append(line).append("\n")
                        line = reader.readLine()
                    }
                }

                fileContents = builder.toString()
                Log.v("GetFileFromDriveTask", """call -
                |fileContents: ${fileContents} """.trimMargin())

                val discardTask = driveResourceClient.discardContents(contents)
                Tasks.await(discardTask)
                Log.v("GetFileFromDriveTask", """call -
                |discardTask isSuccessful: ${discardTask.isSuccessful}
                | isCanceled: ${discardTask.isCanceled}
                | isComplete: ${discardTask.isComplete}
                |"""
                        .trimMargin())

                if (discardTask.isSuccessful) {
                    val msg = context.resources.
                            getString(
                                    R.string.base_handler_download_time_details, fileName, (System.currentTimeMillis() - startMillis) / 1000f, decryptMillis / 1000f)
                    Log.v("GetFileFromDriveTask", """call success - msg: $msg """)
                    eventBus.post(FilesDownloadEvents.Builder(FilesDownloadEvents.Events.FILE_DOWNLOADED)
                            .msgContents(msg)
                            .fileName(fileName)
                            .downloadedFileDriveId(selectedDriveId)
                            .textContents(fileContents)
                            .mimeType(mimeType)
                            .build())
                } else {
                    Log.v("GetFileFromDriveTask", """call problem - msg: ${discardTask.exception} """)
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

    private fun getDecryptedBytes(inputStream: InputStream?): String {
        val reader = BufferedReader(InputStreamReader(inputStream!!))
        val builder = StringBuilder()
        val downloadedContents: String
        var line = reader.readLine()
        while (line != null) {
            builder.append(line)
            line = reader.readLine()
        }
        downloadedContents = builder.toString()
        return downloadedContents
    }

    class Builder {

        private lateinit var context: Context
        private lateinit var eventBus: EventBus
        private lateinit var driveResourceClient: DriveResourceClient
        private lateinit var selectedDriveId: DriveId
        private lateinit var fileName: String
        private lateinit var mimeType: String

        fun context(context: Context) = apply { this.context = context }

        fun eventBus(eventBus: EventBus) = apply { this.eventBus = eventBus }

        fun driveResourceClient(driveResourceClient: DriveResourceClient) =
                apply { this.driveResourceClient = driveResourceClient }

        fun selectedDriveId(selectedDriveId: DriveId) =
                apply { this.selectedDriveId = selectedDriveId }

        fun fileName(fileName: String) = apply { this.fileName = fileName }

        fun mimeType(mimeType: String) = apply { this.mimeType = mimeType }

        fun build() = GetFileFromDriveTask(
                context,
                eventBus,
                driveResourceClient,
                selectedDriveId,
                fileName,
                mimeType)
    }

}