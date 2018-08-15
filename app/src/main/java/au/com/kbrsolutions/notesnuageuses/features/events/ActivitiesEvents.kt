package au.com.kbrsolutions.notesnuageuses.features.events

import android.graphics.Bitmap
import au.com.kbrsolutions.notesnuageuses.features.core.FolderData
import com.google.android.gms.drive.DriveId
import java.util.*

class ActivitiesEvents {

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
        //		SAVE_NOTE_OPTIONS,
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

    var request: HomeEvents
    var msg: String
    var textContents: String
    var bitmapContents: Bitmap
    var decryptedContentsBytes: ByteArray
    var fileItemId: Long
    //	public final DriveId driveId;
    var setSelectedFileDriveId: DriveId
    var isFolder: Boolean
    var mimeType: String
    var isEncryptedFile: Boolean
    var isTrashed: Boolean
    var passwords: Array<String>
    var showLockTime: Boolean
    var encryptPassword: String
    var folderLevel: Int
    var idxInTheFolderFilesList: Int
    var parentFileName: String
    var fileName: String
    var newFileName: String
    var currFolderDriveId: DriveId
    var createDt: Date
    var updateDt: Date
    var maxContinuesIncorrectPassword: Int
    var lockMillis: Int
    var foldersAddData: FolderData

    private fun ActivitiesEvents(
            event: HomeEvents,
            msg: String,
            textContents: String,
            bitmapContents: Bitmap,
            decryptedContentsBytes: ByteArray,
            fileItemId: Long,
            setSelectedFileDriveId: DriveId,
            isFolder: Boolean,
            mimeType: String,
            isEncryptedFile: Boolean,
            isTrashed: Boolean,
            passwords: Array<String>,
            showLockTime: Boolean,
            encryptPassword: String,
            folderLevel: Int,
            idxInTheFolderFilesList: Int,
            parentFileName: String,
            fileName: String,
            newFileName: String,
            currFolderDriveId: DriveId,
            createDate: Date,
            updateDate: Date,
            maxContinuesIncorrectPassword: Int,
            lockMillis: Int,
            foldersAddData: FolderData): ??? {
        this.request = event
        this.msg = msg
        this.textContents = textContents
        this.bitmapContents = bitmapContents
        this.decryptedContentsBytes = decryptedContentsBytes
        this.fileItemId = fileItemId
        this.setSelectedFileDriveId = setSelectedFileDriveId
        this.isFolder = isFolder
        this.mimeType = mimeType
        this.folderLevel = folderLevel
        this.isEncryptedFile = isEncryptedFile
        this.isTrashed = isTrashed
        this.passwords = passwords
        this.showLockTime = showLockTime
        this.encryptPassword = encryptPassword
        this.idxInTheFolderFilesList = idxInTheFolderFilesList
        this.parentFileName = parentFileName
        this.fileName = fileName
        this.newFileName = newFileName
        this.currFolderDriveId = currFolderDriveId
        this.createDt = createDate
        this.updateDt = updateDate
        this.maxContinuesIncorrectPassword = maxContinuesIncorrectPassword
        this.lockMillis = lockMillis
        this.foldersAddData = foldersAddData

    }

//    fun getRequest(): HomeEvents {
//        return request
//    }
//
//    fun getMsgContents(): String {
//        return msg
//    }
//
//    fun getBitmapContents(): Bitmap {
//        return bitmapContents
//    }
//
//    fun getFileItemId(): Long {
//        return fileItemId
//    }
//
//    fun getSelectedFileDriveId(): DriveId {
//        return setSelectedFileDriveId
//    }
//
//    fun isFolder(): Boolean {
//        return isFolder
//    }
//
//    fun getMimeType(): String {
//        return mimeType
//    }
//
//    fun isEncryptedFile(): Boolean {
//        return isEncryptedFile
//    }
//
//    fun getFolderLevel(): Int {
//        return folderLevel
//    }
//
//    fun getIdxInTheFolderFilesList(): Int {
//        return idxInTheFolderFilesList
//    }
//
//    fun getFileName(): String {
//        return fileName
//    }
//
//    fun getNewFileName(): String {
//        return newFileName
//    }
//
//    fun getCurrFolderDriveId(): DriveId {
//        return currFolderDriveId
//    }
//
//    fun getEncryptPassword(): String {
//        return encryptPassword
//    }
//
//    fun getFoldersAddData(): FolderData {
//        return foldersAddData
//    }

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
        private var foldersAddData: FolderData? = null

        fun setMsgContents(msgContents: String): Builder {
            this.msgContents = msgContents
            return this
        }

        fun setTextContents(textContents: String): Builder {
            this.textContents = textContents
            return this
        }

        fun setBitmapContents(bitmapContents: Bitmap): Builder {
            this.bitmapContents = bitmapContents
            return this
        }

        fun setDecryptedContentsBytes(decryptedContentsBytes: ByteArray): Builder {
            this.decryptedContentsBytes = decryptedContentsBytes
            return this
        }

        fun setFileItemId(fileItemId: Long): Builder {
            this.fileItemId = fileItemId
            return this
        }

        fun setSelectedFileDriveId(driveId: DriveId): Builder {
            this.selectedFileDriveId = driveId
            return this
        }

        fun setEncryptPassword(encryptPassword: String): Builder {
            this.encryptPassword = encryptPassword
            return this
        }

        fun setMimeType(mimeType: String): Builder {
            this.mimeType = mimeType
            return this
        }

        fun setIsEncryptedFile(isEncryptedFile: Boolean): Builder {
            this.isEncryptedFile = isEncryptedFile
            return this
        }

        fun setIsTrashed(isTrashed: Boolean): Builder {
            this.isTrashed = isTrashed
            return this
        }

        fun setPasswords(passwords: Array<String>): Builder {
            this.passwords = passwords
            return this
        }

        fun setShowLockTime(showLockTime: Boolean): Builder {
            this.showLockTime = showLockTime
            return this
        }

        fun setIsFolder(isFolder: Boolean): Builder {
            this.isFolder = isFolder
            return this
        }

        fun setCurrFolderLevel(folderLevel: Int): Builder {
            this.folderLevel = folderLevel
            return this
        }

        fun setIdxInTheFolderFilesList(idxInTheFolderFilesList: Int): Builder {
            this.idxInTheFolderFilesList = idxInTheFolderFilesList
            return this
        }

        fun setParentFileName(parentFileName: String): Builder {
            this.parentFileName = parentFileName
            return this
        }

        fun setFileName(currFileName: String): Builder {
            this.fileName = currFileName
            return this
        }

        fun setNewFileName(newFileName: String): Builder {
            this.newFileName = newFileName
            return this
        }

        fun setCurrFolderDriveId(currFolderDriveId: DriveId): Builder {
            this.currFolderDriveId = currFolderDriveId
            return this
        }

        fun setCreateDate(createDate: Date): Builder {
            this.createDate = createDate
            return this
        }

        fun setUpdateDate(updateDate: Date): Builder {
            this.updateDate = updateDate
            return this
        }

        fun setMaxContinuesIncorrectPassword(maxContinuesIncorrectPassword: Int): Builder {
            this.maxContinuesIncorrectPassword = maxContinuesIncorrectPassword
            return this
        }

        fun setLockMillis(lockMillis: Int): Builder {
            this.lockMillis = lockMillis
            return this
        }

        fun setFoldersAddData(foldersAddData: FolderData): Builder {
            this.foldersAddData = foldersAddData
            return this
        }

        fun build(): ActivitiesEvents {
            return ActivitiesEvents(
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
                    lockMillis,
                    foldersAddData)
        }
    }
}
