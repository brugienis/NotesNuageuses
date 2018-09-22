package au.com.kbrsolutions.notesnuageuses.features.eventbus.events

import com.google.android.gms.drive.DriveId
import java.util.*

class FilesUploadEvents(
        var request: Events,
        var msgContents: String,
        var fileItemId: Long,
        var thisFileDriveId: DriveId? = null,
        var mimeType: String,
        var folderLevel: Int,
        var parentFileName: String,
        var fileName: String,
        var currFolderDriveId: DriveId,
        var createDt: Date,
        var updateDt: Date,
        var idxInTheFolderFilesList: Int) {

    enum class Events {
        TEXT_UPLOADING,
        TEXT_UPLOADED,
        UPLOAD_PROBLEMS,
        CANCEL_CREATE,
        SAVE_TEXT_NOTE,
        DO_NOT_SAVE_TEXT_NOTE,
        SHOW_MESSAGE,
        CREATE_FILE_DIALOG_CANCELLED,
        CREATE_TEXT_NOTE
    }

    class Builder(private var request: Events) {
        private lateinit var msgContents: String
        private var fileItemId: Long = 0
        private var thisFileDriveId: DriveId? = null
        private lateinit var mimeType: String
        private var folderLevel: Int = 0
        private lateinit var parentFileName: String
        private lateinit var fileName: String
        private lateinit var currFolderDriveId: DriveId
        private lateinit var createDate: Date
        private lateinit var updateDate: Date
        private var idxInTheFolderFilesList: Int = -1

        fun msgContents(msgContents: String) = apply { this.msgContents = msgContents }

        fun fileItemId(fileItemId: Long) = apply { this.fileItemId = fileItemId }

        fun thisFileDriveId(thisFileDriveId: DriveId?) =
                apply { this.thisFileDriveId = thisFileDriveId }

        fun mimeType(mimeType: String) = apply { this.mimeType = mimeType }

        fun folderLevel(folderLevel: Int) = apply { this.folderLevel = folderLevel }

        fun fileName(fileName: String) = apply { this.fileName = fileName }

        fun parentFileName(parentFileName: String) = apply { this.parentFileName = parentFileName }

        fun currFolderDriveId(currFolderDriveId: DriveId) =
                apply { this.currFolderDriveId = currFolderDriveId }

        fun createDate(createDate: Date) = apply { this.createDate = createDate }

        fun updateDate(updateDate: Date) = apply { this.updateDate = updateDate }

        fun idxInTheFolderFilesList(idxInTheFolderFilesList: Int) =
                apply { this.idxInTheFolderFilesList = idxInTheFolderFilesList }

        fun build() = FilesUploadEvents(
                request,
                msgContents,
                fileItemId,
                thisFileDriveId,
                mimeType,
                folderLevel,
                parentFileName,
                fileName,
                currFolderDriveId,
                createDate,
                updateDate,
                idxInTheFolderFilesList)
    }

}

