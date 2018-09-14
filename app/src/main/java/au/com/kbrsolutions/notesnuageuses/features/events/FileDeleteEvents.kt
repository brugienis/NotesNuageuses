package au.com.kbrsolutions.notesnuageuses.features.events

import com.google.android.gms.drive.DriveId
import java.util.*

class FileDeleteEvents(
        var request: Events,
        var msgContents: String?,
        var fileItemId: Long?,
        var selectedFileDriveId: DriveId?,
        var isFolder: Boolean?,
        var mimeType: String?,
        var isTrashed: Boolean?,
        var thisFileFolderLevel: Int,
        var idxInTheFolderFilesList: Int?,
        var parentFileName: String?,
        var fileName: String?,
        var currFolderDriveId: DriveId?,
        var createDt: Date?,
        var updateDt: Date?) {

    enum class Events {
        TRASH_FILE,
        TRASH_FILE_FINISHED, TRASH_FILE_PROBLEMS, DELETE_FILE_PROBLEMS, DELETE_FILE_FINISHED, DELETE_FILE, DELETE_FILE_START
    }

    class Builder(private var request: FileDeleteEvents.Events) {
        private var msgContents: String? = null
        private var thisFileFolderLevel: Int = 0
        private var fileItemId: Long = 0
        private var selectedFileDriveId: DriveId? = null
        private var isFolder: Boolean = false
        private var mimeType: String? = null
        private var isTrashed: Boolean = false
        private var passwords: Array<String>? = null
        var showLockTime: Boolean = false
        private var encryptPassword: String? = null
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

        fun msgContents(msgContents: String?) = apply { this.msgContents = msgContents }

        fun fileItemId(fileItemId: Long) = apply { this.fileItemId = fileItemId }

        fun selectedFileDriveId(selectedFileDriveId: DriveId) =
                apply { this.selectedFileDriveId = selectedFileDriveId }

        fun mimeType(mimeType: String) = apply { this.mimeType = mimeType }

        fun isTrashed(isTrashed: Boolean) = apply { this.isTrashed = isTrashed }

        fun isFolder(isFolder: Boolean) = apply { this.isFolder = isFolder }

        fun thisFileFolderLevel(thisFileFolderLevel: Int) =
                apply { this.thisFileFolderLevel = thisFileFolderLevel }

        fun idxInTheFolderFilesList(idxInTheFolderFilesList: Int) =
                apply { this.idxInTheFolderFilesList = idxInTheFolderFilesList }

        fun parentFileName(parentFileName: String) = apply { this.parentFileName = parentFileName }

        fun fileName(fileName: String) = apply { this.fileName = fileName }

        fun currFolderDriveId(currFolderDriveId: DriveId) =
                apply { this.currFolderDriveId = currFolderDriveId }

        fun createDate(createDate: Date) = apply { this.createDate = createDate }

        fun updateDate(updateDate: Date) = apply { this.updateDate = updateDate }

        fun build() = FileDeleteEvents(
                    request,
                    msgContents,
                    fileItemId,
                    selectedFileDriveId,
                    isFolder,
                    mimeType,
                    isTrashed,
                    thisFileFolderLevel,
                    idxInTheFolderFilesList,
                    parentFileName,
                    fileName,
                    currFolderDriveId,
                    createDate,
                    updateDate)
        }
    }

