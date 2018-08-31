package au.com.kbrsolutions.notesnuageuses.features.tasks

import android.content.Context
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.events.FilesEvents
import com.google.android.gms.drive.DriveApi
import com.google.android.gms.drive.DriveFile
import com.google.android.gms.drive.DriveId
import com.google.android.gms.drive.DriveResourceClient
import org.greenrobot.eventbus.EventBus
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*
import java.util.concurrent.Callable

data class GetFileFromGoogleDriveCallable(
        var context: Context,
        var eventBus: EventBus,
        var driveResourceClient: DriveResourceClient,
        val selectedDriveId: DriveId,
        val createDt: Date,
        val fileName: String,
        val passwords: Array<String>?,
        val keyLength: Int,
        val mimeType: String) : Callable<String> {

    private var callRunToEnd = false
    private var decryptMillis: Long = 0
    //		private byte[] encryptedContents;
    private var decryptedContentsBytes: ByteArray? = null
    //		public String encryptPackage;

    private var maxContinuesIncorrectPassword: Int = 0
    private var lockMillis: Int = 0
    private var showLockTime: Boolean = false

    override fun call(): String? {
        val startMillis = System.currentTimeMillis()

        /* no need to check if there is connection to the Google Drive - this task could not be submitted if there wasn't one */

        var inputStream: InputStream? = null
        val reader: BufferedReader? = null
        try {

            eventBus.post(FilesEvents.Builder(FilesEvents.Events.FILE_DOWNLOADING)
                    .mimeType(mimeType)
                    .build())

            val selectedFileDrive: DriveFile? = null
            // fixLater - uncomment below line and use new Drive API
            //				selectedFileDrive = Drive.DriveApi.getFile(mGoogleApiClient, selectedDriveId);
            // fixLater - uncomment below line and use new Drive API
            val driveContentsResult: DriveApi.DriveContentsResult? = null

            if (!driveContentsResult!!.status.isSuccess) {
                postDownloadProblemEvent(
                        context.resources.
                                getString(R.string.base_handler_download_problem,
                                        fileName))
                return null
            }

            inputStream = driveContentsResult.driveContents.inputStream

            val textContents = getDecryptedBytes(inputStream)

            val msg = context.resources.
                    getString(
                            R.string.base_handler_download_time_details, fileName, (System.currentTimeMillis() - startMillis) / 1000f, decryptMillis / 1000f)
            eventBus.post(FilesEvents.Builder(FilesEvents.Events.FILE_DOWNLOADED)
                    .msgContents(msg)
                    .fileName(fileName)
                    .selectedFileDriveId(selectedDriveId)
                    .textContents(textContents)
                    .mimeType(mimeType)
                    .createDate(createDt)
                    .build())
            callRunToEnd = true
        } catch (e: IOException) {
            postDownloadProblemEvent(
                    context.resources.getString(
                            R.string.base_handler_download_problem, fileName))
        } catch (e: IllegalStateException) {
            postDownloadProblemEvent(
                    context.resources.getString(
                            R.string.base_handler_download_no_connection, fileName))
        } catch (e: Exception) {                                                                    // added to test if any exception is handled properly
            postDownloadProblemEvent(
                    context.resources.getString(
                            R.string.base_handler_download_problem, fileName))
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (nothingCanBeDone: IOException) {
                }

            }
            if (reader != null) {
                try {
                    reader.close()
                } catch (nothingCanBeDone: IOException) {
                }

            }
        }

        return "Getting file done"
    }

    private fun postDownloadProblemEvent(msg: String) {
        eventBus.post(FilesEvents.Builder(FilesEvents.Events.FILE_DOWNLOAD_PROBLEMS)
                .msgContents(msg)
                .mimeType(mimeType)
                .build())
    }

    private fun postDecryptProblemEvent(msg: String) {
        eventBus.post(FilesEvents.Builder(FilesEvents.Events.FILE_DOWNLOAD_DECRYPT_PROBLEMS)
                .msgContents(msg)
                .fileName(fileName)
                .createDate(createDt)
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

}