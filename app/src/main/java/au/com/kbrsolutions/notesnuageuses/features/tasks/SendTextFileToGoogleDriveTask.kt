package au.com.kbrsolutions.notesnuageuses.features.tasks

import android.content.Context
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.core.FoldersData
import au.com.kbrsolutions.notesnuageuses.features.events.ActivitiesEvents
import com.google.android.gms.drive.*
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.io.OutputStream
import java.util.*
import java.util.concurrent.Callable

data class SendTextToGoogleDriveCallable(
        var context: Context,
        var eventBus: EventBus,
        var driveResourceClient: DriveResourceClient,
        var createDt: Date? = null,
        val parentFolderLevel: Int,
        val parentFolderDriveId: DriveId,
        val existingFileDriveId: DriveId?,
        val encryptPasswords: Array<String>,
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
        val startMillis = System.currentTimeMillis()
        val contentToSave: ByteArray
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
            sendUpdateEvent(ActivitiesEvents.HomeEvents.TEXT_UPLOADING, msg, fileNameWithExtension)

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
                    outputStream!!.write(contentToSave)
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
                if (driveContentsResult!!.status.isSuccess) {
                    outputStream = driveContentsResult.driveContents.outputStream
                    outputStream!!.write(contentToSave)
                    val metadataChangeSet = MetadataChangeSet.Builder()
                            .setMimeType(mimeType)
                            .setTitle(fileNameWithExtension!!)
                            .build()
                    // fixLater - uncomment below line and use new Drive API
                    val folder: DriveFolder? = null
                    // fixLater - uncomment below line and use new Drive API
                    //						folder = Drive.DriveApi.getFolder(mGoogleApiClient, parentFolderDriveId);
                    val driveFileResult: DriveFolder.DriveFileResult? = null
                    //						driveFileResult = folder.createFile(mGoogleApiClient, metadataChangeSet, driveContentsResult.getDriveContents()).await();
                    // FIXME: add code to verify status
                    if (driveFileResult!!.status.isSuccess) {
                        thisFileDriveId = driveFileResult.driveFile.driveId
                        uploadSuccessful = true
                    } else {
                        uploadSuccessful = false
                    }
                } else {
                    uploadSuccessful = false
                }
            }

            if (uploadSuccessful) {
                msg = context.resources.getString(R.string.base_handler_upload_time_details, fileName, (System.currentTimeMillis() - startMillis) / 1000f, encryptMillis / 1000f)
                sendUpdateEvent(ActivitiesEvents.HomeEvents.TEXT_UPLOADED, msg, fileNameWithExtension)
            } else {
                sendProblemEvent(ActivitiesEvents.HomeEvents.UPLOAD_PROBLEMS, true, fileName)
            }
        } catch (e: IllegalStateException) {
            sendProblemEvent(ActivitiesEvents.HomeEvents.UPLOAD_PROBLEMS, true, fileName)
        } catch (e: IOException) {
            sendProblemEvent(ActivitiesEvents.HomeEvents.UPLOAD_PROBLEMS, false, fileName)
        } catch (e: Exception) {                                                                            // added to handle any exception
            sendProblemEvent(ActivitiesEvents.HomeEvents.UPLOAD_PROBLEMS, false, fileName)
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close()
                } catch (e: IOException) {
                    // nothing can be done
                }

            }
        }
        return "SendTextToGoogleDriveCallable - successful end"
    }

    private fun sendProblemEvent(
            event: ActivitiesEvents.HomeEvents,
            willTryAgain: Boolean, fileName: String) {
        val msg = if (willTryAgain) {
            context.resources.getString(R.string.base_handler_upload_problem_will_try_later,
                    fileName)
        } else {
            context.resources.getString(R.string.base_handler_upload_problem, fileName)
        }
        sendUpdateEvent(event, msg, fileName)
    }

    private fun sendUpdateEvent(event: ActivitiesEvents.HomeEvents, msg: String?, fileName: String) {
        eventBus.post(ActivitiesEvents.Builder(event)
                .setMsgContents(msg)
                .setParentFileName(parentFileName)
                .setFileName(fileName)
                .setCreateDate(createDt)
                .setUpdateDate(Date())
                .setFileItemId(fileItemId)
                .setCurrFolderDriveId(parentFolderDriveId)
                .setSelectedFileDriveId(thisFileDriveId)
                .setIsFolder(false)
                .setMimeType(mimeType)
                .setIsEncryptedFile(encryptFile)
                .setCurrFolderLevel(parentFolderLevel)
                .setIdxInTheFolderFilesList(idxInTheFolderFilesList)
                .build())
    }

    class Builder {

        private lateinit var context: Context
        private lateinit var eventBus: EventBus
        private lateinit var driveResourceClient: DriveResourceClient
        private var createDt: Date? = null
        private var parentFolderLevel: Int = 0
        private lateinit var parentFolderDriveId: DriveId
        private var existingFileDriveId: DriveId? = null
        private lateinit var fileName: String
        private lateinit var mimeType: String
        private lateinit var encryptionPasswords: Array<String>
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

        fun existingFileDriveId(existingFileDriveId: DriveId) =
                apply { this.existingFileDriveId = existingFileDriveId }

        fun fileName(fileName: String) = apply { this.fileName = fileName }

        fun mimeType(mimeType: String) = apply { this.mimeType = mimeType }

        fun encryptionPasswords(encryptionPasswords: Array<String>) =
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

        fun build() = SendTextToGoogleDriveCallable (
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
