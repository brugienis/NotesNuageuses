package au.com.kbrsolutions.notesnuageuses.features.eventbus.events

import com.google.android.gms.drive.DriveId
import java.util.*

class FileDeleteEvents(
        var request: Events,
        var msgContents: String,
        var fileItemId: Long,
        var thisFileDriveId: DriveId,
        var isFolder: Boolean,
        var mimeType: String,
        var isTrashed: Boolean,
        var thisFileFolderLevel: Int,
        var idxInTheFolderFilesList: Int,
        var parentFileName: String,
        var fileName: String,
        var parentFolderDriveId: DriveId,
        var createDt: Date,
        var updateDt: Date,
        var isFileDeleted: Boolean) {

    enum class Events {
        REMOVE_FILE_FINISHED,
        REMOVE_FILE_PROBLEMS
    }

    class Builder(private var request: FileDeleteEvents.Events) {
        private lateinit var msgContents: String
        private var thisFileFolderLevel: Int = 0
        private var fileItemId: Long = 0
        private lateinit var thisFileDriveId: DriveId
        private var isFolder: Boolean = false
        private lateinit var mimeType: String
        private var isTrashed: Boolean = false
        private var idxInTheFolderFilesList: Int = 0
        private lateinit var parentFileName: String
        private lateinit var fileName: String
        private lateinit var parentFolderDriveId: DriveId
        private lateinit var createDate: Date
        private lateinit var updateDate: Date
        private var isFileDeleted: Boolean = false

        fun msgContents(msgContents: String) = apply { this.msgContents = msgContents }

        fun fileItemId(fileItemId: Long) = apply { this.fileItemId = fileItemId }

        fun thisFileDriveId(thisFileDriveId: DriveId) =
                apply { this.thisFileDriveId = thisFileDriveId }

        fun mimeType(mimeType: String) = apply { this.mimeType = mimeType }

        fun isTrashed(isTrashed: Boolean) = apply { this.isTrashed = isTrashed }

        fun isFolder(isFolder: Boolean) = apply { this.isFolder = isFolder }

        fun thisFileFolderLevel(thisFileFolderLevel: Int) =
                apply { this.thisFileFolderLevel = thisFileFolderLevel }

        fun idxInTheFolderFilesList(idxInTheFolderFilesList: Int) =
                apply { this.idxInTheFolderFilesList = idxInTheFolderFilesList }

        fun parentFileName(parentFileName: String) = apply { this.parentFileName = parentFileName }

        fun fileName(fileName: String) = apply { this.fileName = fileName }

        fun parentFolderDriveId(parentFolderDriveId: DriveId) =
                apply { this.parentFolderDriveId = parentFolderDriveId }

        fun createDate(createDate: Date) = apply { this.createDate = createDate }

        fun updateDate(updateDate: Date) = apply { this.updateDate = updateDate }

        fun isFileDeleted(isFileDeleted: Boolean) = apply { this.isFileDeleted = isFileDeleted }

        fun build() = FileDeleteEvents(
                request,
                msgContents,
                fileItemId,
                thisFileDriveId,
                isFolder,
                mimeType,
                isTrashed,
                thisFileFolderLevel,
                idxInTheFolderFilesList,
                parentFileName,
                fileName,
                parentFolderDriveId,
                createDate,
                updateDate,
                isFileDeleted)
        }
    }

