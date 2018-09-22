package au.com.kbrsolutions.notesnuageuses.features.tasks

import android.content.Context
import android.util.Log
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.eventbus.events.DriveAccessEvents
import au.com.kbrsolutions.notesnuageuses.features.eventbus.events.FilesUploadEvents
import com.google.android.gms.drive.*
import com.google.android.gms.tasks.Tasks
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.io.OutputStream
import java.util.*
import java.util.concurrent.Callable

data class SendFileToDriveTask(
        var context: Context,
        var eventBus: EventBus,
        var driveResourceClient: DriveResourceClient,
        val parentFolderLevel: Int,
        val parentFolderDriveId: DriveId,
        val existingFileDriveId: DriveId?,
        val fileName: String,
        val mimeType: String,
        val fileContents: ByteArray,
        val parentFileName: String,
        val fileItemId: Long,
        val idxInTheFolderFilesList: Int): Callable<String> {

    private val createDt: Date = Date()
//    private val fileItemId = createDt.time
    private var msg: String = ""
    private var thisFileDriveId: DriveId? = null
    private var encryptMillis: Long = 0

    override fun call(): String {
//        Log.v("SendFileToDriveTask", """call -
//            |fileContents: ${String(fileContents)} """
//                .trimMargin())
        val startMillis = System.currentTimeMillis()

        var outputStream: OutputStream? = null
        val fileNameWithExtension = fileName + (context.resources
                        .getString(R.string.base_handler_encrypted_text_file_extension))
//        msg = null

        try {

            var uploadSuccessful = false

            if (existingFileDriveId != null) {
                thisFileDriveId = existingFileDriveId
                sendUpdateEvent(FilesUploadEvents.Events.TEXT_UPLOADING, msg, fileNameWithExtension)

                val openTask = driveResourceClient.openFile(
                        existingFileDriveId.asDriveFile(), DriveFile.MODE_WRITE_ONLY)
                Tasks.await(openTask)

                val driveContents: DriveContents = openTask.result

                outputStream = driveContents.outputStream

                // fixLater: Sep 01, 2018 - test finally
                outputStream.write(fileContents)
                Log.v("SendFileToDriveTask", """call before - fileContents: $fileContents """)
                // groovyScript("_1 ?: '<top>'", kotlinClassName())
                // groovyScript("_1.take(Math.min(23, _1.length()));", className())
//                if (true) throw RuntimeException("BR test - after write")

                Log.v("SendFileToDriveTask", """call after  - fileContents: $fileContents """)

                val commitTask = driveResourceClient.commitContents(driveContents, null)
                Tasks.await(commitTask)

                if (commitTask.isSuccessful) {
                    uploadSuccessful = true
                }

            } else {
                sendUpdateEvent(FilesUploadEvents.Events.TEXT_UPLOADING, msg, fileNameWithExtension)

                val createContentsTask = driveResourceClient.createContents()

                Tasks.await(createContentsTask)

                val contents = createContentsTask.result
                outputStream = contents.outputStream

                outputStream.write(fileContents)

                Log.v("SendFileToDriveTask", """call -
                    |fileContents: ${String(fileContents)}
                    | ; after write
                    |""".trimMargin())

//                OutputStreamWriter(outputStream).use {
//                    writer -> writer.write("")
////                    throw RuntimeException("BR - after write")
//                }

                val changeSet = MetadataChangeSet.Builder()
                        .setTitle(fileName)
                        .setMimeType("text/plain")
                        .setStarred(true)
                        .build()

                val driveFileTask = driveResourceClient.createFile(
                        parentFolderDriveId.asDriveFolder(),
                        changeSet,
                        contents
                )

                Tasks.await(driveFileTask)

                Log.v("SendFileToDriveTask", """call - driveFileTask.isSuccessful: ${driveFileTask.isSuccessful} """)
                if (driveFileTask.isSuccessful) {
                    thisFileDriveId = driveFileTask.result.driveId
                    uploadSuccessful = true
                } else {
                    msg = "$fileName${driveFileTask.exception.toString()}"
                }
            }

            if (uploadSuccessful) {
                msg = context.resources.getString(R.string.base_handler_upload_time_details,
                        fileName, (System.currentTimeMillis() - startMillis) / 1000f,
                        encryptMillis / 1000f)
                sendUpdateEvent(FilesUploadEvents.Events.TEXT_UPLOADED, msg, fileNameWithExtension)
            } else {
                sendProblemEvent(msg)
            }
        } catch (e: IllegalStateException) {
            sendProblemEvent(fileName + e)
        } catch (e: IOException) {
            sendProblemEvent(fileName + e)
        } catch (e: Exception) {                                    // added to handle any exception
            sendProblemEvent(fileName + e)
        } finally {
            Log.v("SendFileToDriveTask", """call - finally -
                |outputStream: $outputStream """.trimMargin())
            if (outputStream != null) {
                try {
                    outputStream.close()
                } catch (e: IOException) {
                    // nothing can be done
                }

            }
        }
        return "SendFileToDriveTask - successful end"
    }

    private fun sendProblemEvent(
            fileNameAndException: String) {
        eventBus.post(DriveAccessEvents.Builder(DriveAccessEvents.Events.MESSAGE)
                .msgContents(fileNameAndException)
                .isProblem(true)
                .build())
    }

    private fun sendUpdateEvent(event: FilesUploadEvents.Events, msg: String, fileName: String) {
        Log.v("SendFileToDriveTask", """sendUpdateEvent - before
            |fileName: $fileName
            |thisFileDriveId: $thisFileDriveId
            |""".trimMargin())
        eventBus.post(FilesUploadEvents.Builder(event)
                .msgContents(msg)
                .parentFileName(parentFileName)
                .fileName(fileName)
                .createDate(createDt)
                .updateDate(Date())
                .fileItemId(fileItemId)
                .currFolderDriveId(parentFolderDriveId)
                .thisFileDriveId(thisFileDriveId)
                .mimeType(mimeType)
                .folderLevel(parentFolderLevel)
                .idxInTheFolderFilesList(idxInTheFolderFilesList)
                .build())
    }

    class Builder {

        private lateinit var context: Context
        private lateinit var eventBus: EventBus
        private lateinit var driveResourceClient: DriveResourceClient
        private var parentFolderLevel: Int = 0
        private lateinit var parentFolderDriveId: DriveId
        private var existingFileDriveId: DriveId? = null
        private lateinit var fileName: String
        private lateinit var mimeType: String
        private lateinit var contents: ByteArray
        private lateinit var parentFileName: String
        private var fileItemId: Long = -1
        private var idxInTheFolderFilesList: Int = -1

        fun context(context: Context) = apply { this.context = context }

        fun eventBus(eventBus: EventBus) = apply { this.eventBus = eventBus }

        fun driveResourceClient(driveResourceClient: DriveResourceClient) =
                apply { this.driveResourceClient = driveResourceClient }

        fun parentFolderLevel(parentFolderLevel: Int) =
                apply { this.parentFolderLevel = parentFolderLevel }

        fun parentFolderDriveId(parentFolderDriveId: DriveId) =
                apply { this.parentFolderDriveId = parentFolderDriveId }

        fun existingFileDriveId(existingFileDriveId: DriveId?) =
                apply { this.existingFileDriveId = existingFileDriveId }

        fun fileName(fileName: String) = apply { this.fileName = fileName }

        fun mimeType(mimeType: String) = apply { this.mimeType = mimeType }

        fun contents(contents: ByteArray) = apply { this.contents = contents }

        fun parentFileName(parentFileName: String) = apply { this.parentFileName = parentFileName }

        fun idxInTheFolderFilesList(idxInTheFolderFilesList: Int) =
                apply { this.idxInTheFolderFilesList = idxInTheFolderFilesList }

        fun fileItemId(fileItemId: Long) =
                apply { this.fileItemId = fileItemId }

        fun build() = SendFileToDriveTask (
                context,
                eventBus,
                driveResourceClient,
                parentFolderLevel,
                parentFolderDriveId,
                existingFileDriveId,
                fileName,
                mimeType,
                contents,
                parentFileName,
                fileItemId,
                idxInTheFolderFilesList)
    }

}
