package au.com.kbrsolutions.notesnuageuses.features.tasks

import android.content.Context
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.core.FoldersData
import au.com.kbrsolutions.notesnuageuses.features.events.FilesEvents
import com.google.android.gms.drive.*
import com.google.android.gms.tasks.Tasks
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.util.*
import java.util.concurrent.Callable




data class SendTextFileToGoogleDriveTask(
        var context: Context,
        var eventBus: EventBus,
        var driveResourceClient: DriveResourceClient,
        var createDt: Date,
        val parentFolderLevel: Int,
        val parentFolderDriveId: DriveId,
        val existingFileDriveId: DriveId?,
        val encryptPasswords: Array<String>?,
        val encryptKeyLength: Int,
        val encryptIterationCount: Int,
        val fileName: String,
        val mimeType: String,
        val replaceFile: Boolean,
        val encryptFile: Boolean,
        val idxInTheFolderFilesList: Int,
        val contents: ByteArray,
        val maxContinuesIncorrectPassword: Int,
        val lockMillis: Int,
        val showLockTime: Boolean,
        val foldersData: FoldersData): Callable<String> {

    val parentFileName: String? = foldersData.getFolderTitle(parentFolderLevel)
    var fileItemId: Long = 0
    var fileNameWithExtension: String? = null
    var msg: String? = null
    var thisFileDriveId: DriveId? = null
    var encryptMillis: Long = 0

    override fun call(): String {
//        Log.v("SendTextFileToGoogleDriveTask", """call -
//            |contents: ${String(contents)} """
//                .trimMargin())
        val startMillis = System.currentTimeMillis()
        if (!replaceFile || existingFileDriveId == null) {
            //				createDt = updateDt = new Date();
            createDt = Date()
        } else {
            thisFileDriveId = existingFileDriveId
        }
        fileItemId = createDt!!.time
        var outputStream: OutputStream? = null
        val fileNameWithExtension = fileName +
                (context.resources
                        .getString(R.string.base_handler_encrypted_text_file_extension))
        msg = null
        try {
            sendUpdateEvent(FilesEvents.Events.TEXT_UPLOADING, msg, fileNameWithExtension)

            val driveContentsResult: DriveApi.DriveContentsResult? = null
            var uploadSuccessful = false

            if (replaceFile && existingFileDriveId != null) {
                //https://github.com/googledrive/android-demos/blob/master/src/com/google/android/gms/drive/sample/demo/EditContentsActivity.java
                // fixLater - uncomment below line and use new Drive API
                val file: DriveFile? = null
                //					file = Drive.DriveApi.getFile(mGoogleApiClient, existingFileDriveId);
                //                // fixLater - uncomment below line and use new Drive API
                //                    driveContentsResult = file.open(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null).await();
                if (driveContentsResult!!.status.isSuccess) {
                    val driveContents = driveContentsResult.driveContents
                    outputStream = driveContents.outputStream
                    outputStream!!.write(contents)
                    val changeSet = MetadataChangeSet.Builder()
                            .setLastViewedByMeDate(Date()).build()
                    // fixLater - uncomment below line and use new Drive API
                    //                        Status status = driveContents.commit(getGoogleApiClient(), changeSet).await();  // commitAndCloseContents(getGoogleApiClient(), driveContentsResult.getContents()).await();
                    // FIXME: add code to verify status
                    uploadSuccessful = true
                } else {
                    uploadSuccessful = false
                }
            } else {
                // fixLater - uncomment below line and use new Drive API
                //                    driveContentsResult = Drive.DriveApi.newDriveContents(mGoogleApiClient).await();

                val createContentsTask = driveResourceClient.createContents()

                Tasks.await(createContentsTask)

                val contents = createContentsTask.getResult()
                val outputStream = contents.getOutputStream()
                OutputStreamWriter(outputStream).use { writer -> writer.write("Hello World!") }

                val changeSet = MetadataChangeSet.Builder()
                        .setTitle("HelloWorld.txt")
                        .setMimeType("text/plain")
                        .setStarred(true)
                        .build()

                val driveFileTask = driveResourceClient.createFile(parentFolderDriveId.asDriveFolder(), changeSet, contents)

                Tasks.await(driveFileTask)

                uploadSuccessful = true

//                if (driveContentsResult!!.status.isSuccess) {
//                    outputStream = driveContentsResult.driveContents.outputStream
//                    outputStream!!.write(contents)
//                    val metadataChangeSet = MetadataChangeSet.Builder()
//                            .setMimeType(mimeType)
//                            .setTitle(fileNameWithExtension!!)
//                            .build()
//                    // fixLater - uncomment below line and use new Drive API
//                    val folder: DriveFolder? = null
//                    // fixLater - uncomment below line and use new Drive API
//                    //						folder = Drive.DriveApi.getFolder(mGoogleApiClient, parentFolderDriveId);
//                    val driveFileResult: DriveFolder.DriveFileResult? = null
//                    //						driveFileResult = folder.createFile(mGoogleApiClient, metadataChangeSet, driveContentsResult.getDriveContents()).await();
//                    // FIXME: add code to verify status
//                    if (driveFileResult!!.status.isSuccess) {
//                        thisFileDriveId = driveFileResult.driveFile.driveId
//                        uploadSuccessful = true
//                    } else {
//                        uploadSuccessful = false
//                    }
//                } else {
//                    uploadSuccessful = false
//                }
            }

            if (uploadSuccessful) {
                msg = context.resources.getString(R.string.base_handler_upload_time_details,
                        fileName, (System.currentTimeMillis() - startMillis) / 1000f,
                        encryptMillis / 1000f)
                sendUpdateEvent(FilesEvents.Events.TEXT_UPLOADED, msg, fileNameWithExtension)
            } else {
                sendProblemEvent(FilesEvents.Events.UPLOAD_PROBLEMS, true, fileName)
            }
        } catch (e: IllegalStateException) {
            sendProblemEvent(FilesEvents.Events.UPLOAD_PROBLEMS, true, fileName + e)
        } catch (e: IOException) {
            sendProblemEvent(FilesEvents.Events.UPLOAD_PROBLEMS, false, fileName + e)
        } catch (e: Exception) {                                                                            // added to handle any exception
            sendProblemEvent(FilesEvents.Events.UPLOAD_PROBLEMS, false, fileName + e)
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close()
                } catch (e: IOException) {
                    // nothing can be done
                }

            }
        }
        return "SendTextFileToGoogleDriveTask - successful end"
    }

    private fun sendProblemEvent(
            event: FilesEvents.Events,
            willTryAgain: Boolean, fileName: String) {
        val msg = if (willTryAgain) {
            context.resources.getString(R.string.base_handler_upload_problem_will_try_later,
                    fileName)
        } else {
            context.resources.getString(R.string.base_handler_upload_problem, fileName)
        }
        sendUpdateEvent(event, msg, fileName)
    }

    private fun sendUpdateEvent(event: FilesEvents.Events, msg: String?, fileName: String) {
        eventBus.post(FilesEvents.Builder(event)
                .msgContents(msg)
                .parentFileName(parentFileName)
                .fileName(fileName)
                .createDate(createDt)
                .updateDate(Date())
                .fileItemId(fileItemId)
                .currFolderDriveId(parentFolderDriveId)
                .selectedFileDriveId(thisFileDriveId)
                .mimeType(mimeType)
                .folderLevel(parentFolderLevel)
                .build())
    }

    class Builder {

        private lateinit var context: Context
        private lateinit var eventBus: EventBus
        private lateinit var driveResourceClient: DriveResourceClient
        private lateinit var createDt: Date
        private var parentFolderLevel: Int = 0
        private lateinit var parentFolderDriveId: DriveId
        private var existingFileDriveId: DriveId? = null
        private lateinit var fileName: String
        private lateinit var mimeType: String
        private var encryptionPasswords: Array<String>? = null
        private var encryptionKeyLength: Int = 0
        private var encryptionIterationCount: Int = 0
        private var replaceFile: Boolean = false
        private var encryptFile: Boolean = false
        private var idxInTheFolderFilesList: Int = 0
        private lateinit var contents: ByteArray
        private var maxContinuesIncorrectPassword: Int = 0
        private var lockMillis: Int = 0
        private var showLockTime: Boolean = false
        private lateinit var foldersData: FoldersData

        fun context(context: Context) = apply { this.context = context }

        fun eventBus(eventBus: EventBus) = apply { this.eventBus = eventBus }

        fun driveResourceClient(driveResourceClient: DriveResourceClient) =
                apply { this.driveResourceClient = driveResourceClient }

        fun createDt(createDt: Date) = apply { this.createDt = createDt }

        fun parentFolderLevel(parentFolderLevel: Int) =
                apply { this.parentFolderLevel = parentFolderLevel }

        fun parentFolderDriveId(parentFolderDriveId: DriveId) =
                apply { this.parentFolderDriveId = parentFolderDriveId }

        fun existingFileDriveId(existingFileDriveId: DriveId?) =
                apply { this.existingFileDriveId = existingFileDriveId }

        fun fileName(fileName: String) = apply { this.fileName = fileName }

        fun mimeType(mimeType: String) = apply { this.mimeType = mimeType }

        fun encryptionPasswords(encryptionPasswords: Array<String>?) =
                apply { this.encryptionPasswords = encryptionPasswords }

        fun encryptionKeyLength(encryptionKeyLength: Int) =
                apply { this.encryptionKeyLength = encryptionKeyLength }

        fun encryptionIterationCount(encryptionIterationCount: Int) =
                apply { this.encryptionIterationCount = encryptionIterationCount }

        fun replaceFile(replaceFile: Boolean) = apply { this.replaceFile = replaceFile }


        fun encryptFile(encryptFile: Boolean) = apply { this.encryptFile = encryptFile }

        fun idxInTheFolderFilesList(idxInTheFolderFilesList: Int) =
                apply { this.idxInTheFolderFilesList = idxInTheFolderFilesList }

        fun contents(contents: ByteArray) = apply { this.contents = contents }

        fun maxContinuesIncorrectPassword(maxContinuesIncorrectPassword: Int) =
                apply { this.maxContinuesIncorrectPassword = maxContinuesIncorrectPassword }

        fun lockMillis(lockMillis: Int) = apply { this.lockMillis = lockMillis }

        fun showLockTime(showLockTime: Boolean) = apply { this.showLockTime = showLockTime }

        fun foldersData(foldersData: FoldersData) = apply { this.foldersData = foldersData }

        fun build() = SendTextFileToGoogleDriveTask (
                context,
                eventBus,
                driveResourceClient,
                createDt,
                parentFolderLevel,
                parentFolderDriveId,
                existingFileDriveId,
                encryptionPasswords,
                encryptionKeyLength,
                encryptionIterationCount,
                fileName,
                mimeType,
                replaceFile,
                encryptFile,
                idxInTheFolderFilesList,
                contents,
                maxContinuesIncorrectPassword,
                lockMillis,
                showLockTime,
                foldersData)
    }

}
