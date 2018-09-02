package au.com.kbrsolutions.notesnuageuses.features.tasks

import android.content.Context
import android.util.Log
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.events.FilesEvents
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

data class GetFileFromDriveTask(
        var context: Context,
        var eventBus: EventBus,
        var driveResourceClient: DriveResourceClient,
        val selectedDriveId: DriveId,
        val fileName: String,
        val mimeType: String): Callable<String> {

    private var decryptMillis: Long = 0

    override fun call(): String? {
        val startMillis = System.currentTimeMillis()
        val fileContents: String

        val openFileTask = driveResourceClient.openFile(
                selectedDriveId.asDriveFile(),
                DriveFile.MODE_READ_ONLY)
        Tasks.await(openFileTask)

        val contents = openFileTask.result
        val builder = StringBuilder()

        try {

            eventBus.post(FilesEvents.Builder(FilesEvents.Events.FILE_DOWNLOADING)
                    .mimeType(mimeType)
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
                eventBus.post(FilesEvents.Builder(FilesEvents.Events.FILE_DOWNLOADED)
                        .msgContents(msg)
                        .fileName(fileName)
                        .selectedFileDriveId(selectedDriveId)
                        .textContents(fileContents)
                        .mimeType(mimeType)
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
                            R.string.base_handler_download_problem, fileName + "; " + e))
        } catch (e: IllegalStateException) {
            postDownloadProblemEvent(
                    context.resources.getString(
                            R.string.base_handler_download_no_connection, fileName + "; " + e))
        } catch (e: Exception) {                                                                    // added to test if any exception is handled properly
            postDownloadProblemEvent(
                    context.resources.getString(
                            R.string.base_handler_download_problem, fileName + "; " + e))
        }

        return "Getting file done"
    }

    private fun postDownloadProblemEvent(msg: String) {
        eventBus.post(FilesEvents.Builder(FilesEvents.Events.FILE_DOWNLOAD_PROBLEMS)
                .msgContents(msg)
                .mimeType(mimeType)
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
                mimeType
        )
    }

}