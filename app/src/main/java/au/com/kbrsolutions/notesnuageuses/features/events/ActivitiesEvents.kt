package au.com.kbrsolutions.notesnuageuses.features.events

import android.graphics.Bitmap
import com.google.android.gms.drive.DriveId
import java.util.*

class ActivitiesEvents(
        var request: HomeEvents,
        var msgContents: String?,
        var textContents: String?,
        var bitmapContents: Bitmap?,
        var decryptedContentsBytes: ByteArray?,
        var fileItemId: Long?,
        var setSelectedFileDriveId: DriveId?,
        var isFolder: Boolean?,
        var mimeType: String?,
        var isEncryptedFile: Boolean?,
        var isTrashed: Boolean?,
        var passwords: Array<String>?,
        var showLockTime: Boolean?,
        var encryptPassword: String?,
        var folderLevel: Int?,
        var idxInTheFolderFilesList: Int?,
        var parentFileName: String?,
        var fileName: String?,
        var newFileName: String?,
        var currFolderDriveId: DriveId?,
        var createDt: Date?,
        var updateDt: Date?,
        var maxContinuesIncorrectPassword: Int?,
        var lockMillis: Int?
//        var foldersAddData: FolderData?
) {

    enum class HomeEvents {
        CONNECTED_TO_GOOGLE_DRIVE,
        DISCONNECTED_FROM_GOOGLE_DRIVE,
        GOOGLE_DRIVE_ACCESSOR_SERVICE_STARTED,
        GOOGLE_DRIVE_ACCESSOR_SERVICE_STOPPED,
        TEXT_ENCRYPTING,
        TEXT_UPLOADING,
        TEXT_UPLOADED,
        FILE_DOWNLOADING,
        FILE_DECRYPTING,
        FILE_DECRYPTED,
        CREATE_FOLDER,
        FOLDER_CREATED,
        CREATE_FOLDER_PROBLEMS,
        CANCEL_CREATE,
        SAVE_TEXT_NOTE,
        DO_NOT_SAVE_TEXT_NOTE,
        SHOW_MESSAGE,
        CREATE_FILE_DIALOG_CANCELLED,
        CREATE_TEXT_NOTE,
        RENAME_FILE,
        RENAME_FILE_START,
        RENAME_FILE_FINISHED,
        RENAME_FILE_PROBLEMS,
        CREATE_PHOTO_NOTE,
        FOLDER_DATA_RETRIEVED,
        FOLDER_DATA_RETRIEVE_PROBLEM,
        PROCESSING_PHOTO,
        PHOTO_UPLOADED,
        PHOTO_ENCRYPTING,
        PHOTO_UPLOADING,
        UPLOAD_PROBLEMS,
        SCHEDULE_FILE_RESEND,
        FILE_DDOWNLOAD_PROBLEMS,
        FILE_DDOWNLOAD_DECRYPT_PROBLEMS,
        TRASH_FILE,
        TRASH_FILE_FINISHED, TRASH_FILE_PROBLEMS, DELETE_FILE_PROBLEMS, DELETE_FILE_FINISHED, DELETE_FILE, DELETE_FILE_START
    }


//    private fun ActivitiesEvents(

    class Builder(private var request: HomeEvents) {
        private var msgContents: String? = null
        private var textContents: String? = null
        private var bitmapContents: Bitmap? = null
        private var decryptedContentsBytes: ByteArray? = null
        private var fileItemId: Long = 0
        private var selectedFileDriveId: DriveId? = null
        private var isFolder: Boolean = false
        private var mimeType: String? = null
        private var isEncryptedFile: Boolean = false
        private var isTrashed: Boolean = false
        private var passwords: Array<String>? = null
        var showLockTime: Boolean = false
        private var encryptPassword: String? = null
        private var folderLevel: Int = 0
        private var idxInTheFolderFilesList: Int = 0
        private var parentFileName: String? = null
        private var fileName: String? = null
        private var newFileName: String? = null
        private var currFolderDriveId: DriveId? = null
        private var createDate: Date? = null
        private var updateDate: Date? = null
        private var maxContinuesIncorrectPassword: Int = 0
        private var lockMillis: Int = 0
//        private var foldersAddData: FolderData? = null

        fun msgContents(msgContents: String) = apply { this.msgContents = msgContents }

        fun textContents(textContents: String) = apply { this.textContents = textContents }

        fun bitmapContents(bitmapContents: Bitmap) = apply { this.bitmapContents = bitmapContents }

        fun decryptedContentsBytes(decryptedContentsBytes: ByteArray) =
                apply { this.decryptedContentsBytes = decryptedContentsBytes }

        fun fileItemId(fileItemId: Long) = apply { this.fileItemId = fileItemId }

        fun selectedFileDriveId(selectedFileDriveId: DriveId) =
                apply { this.selectedFileDriveId = selectedFileDriveId }

        fun encryptPassword(encryptPassword: String) =
                apply { this.encryptPassword = encryptPassword }

        fun mimeType(mimeType: String) = apply { this.mimeType = mimeType }

        fun isEncryptedFile(isEncryptedFile: Boolean) =
                apply { this.isEncryptedFile = isEncryptedFile }

        fun isTrashed(isTrashed: Boolean) = apply { this.isTrashed = isTrashed }

        fun passwords(passwords: Array<String>) = apply { this.passwords = passwords }

        fun showLockTime(showLockTime: Boolean) = apply { this.showLockTime = showLockTime }

        fun isFolder(isFolder: Boolean) = apply { this.isFolder = isFolder }

        fun folderLevel(folderLevel: Int) = apply { this.folderLevel = folderLevel }

        fun idxInTheFolderFilesList(idxInTheFolderFilesList: Int) =
                apply { this.idxInTheFolderFilesList = idxInTheFolderFilesList }

        fun parentFileName(parentFileName: String) = apply { this.parentFileName = parentFileName }

        fun newFileName(newFileName: String) = apply { this.newFileName = newFileName }

        fun currFolderDriveId(currFolderDriveId: DriveId) =
                apply { this.currFolderDriveId = currFolderDriveId }

        fun createDate(createDate: Date) = apply { this.createDate = createDate }

        fun updateDate(updateDate: Date) = apply { this.updateDate = updateDate }

        fun maxContinuesIncorrectPassword(maxContinuesIncorrectPassword: Int) =
                apply { this.maxContinuesIncorrectPassword = maxContinuesIncorrectPassword }

        fun lockMillis(lockMillis: Int) = apply { this.lockMillis = lockMillis }

//        fun foldersAddData(foldersAddData: FolderData) =
//                apply { this.foldersAddData = foldersAddData }

        fun build() = ActivitiesEvents(
                    request,
                    msgContents,
                    textContents,
                    bitmapContents,
                    decryptedContentsBytes,
                    fileItemId,
                    selectedFileDriveId,
                    isFolder,
                    mimeType,
                    isEncryptedFile,
                    isTrashed,
                    passwords,
                    showLockTime,
                    encryptPassword,
                    folderLevel,
                    idxInTheFolderFilesList,
                    parentFileName,
                    fileName,
                    newFileName,
                    currFolderDriveId,
                    createDate,
                    updateDate,
                    maxContinuesIncorrectPassword,
                    lockMillis)
//                    foldersAddData)
        }
    }

