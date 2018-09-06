package au.com.kbrsolutions.notesnuageuses.features.events

import com.google.android.gms.drive.DriveId
import java.util.*

class FilesEvents(
        var request: Events,
        var msgContents: String,
        var textContents: String,
        var fileItemId: Long,
        var selectedFileDriveId: DriveId,
        var mimeType: String,
        var folderLevel: Int,
        var parentFileName: String,
        var fileName: String,
        var newFileName: String?,
        var currFolderDriveId: DriveId,
        var createDt: Date,
        var updateDt: Date) {

    enum class Events {
        TEXT_UPLOADING,
        TEXT_UPLOADED,
        FILE_DOWNLOADING,
        FILE_DOWNLOADED,
        UPLOAD_PROBLEMS,
        CREATE_FOLDER,
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
        SCHEDULE_FILE_RESEND,
        FILE_DOWNLOAD_PROBLEMS,
        FILE_DOWNLOAD_DECRYPT_PROBLEMS,
        DELETE_FILE_PROBLEMS,
        DELETE_FILE_FINISHED,
        DELETE_FILE,
        DELETE_FILE_START
    }

    class Builder(private var request: Events) {
        private lateinit var msgContents: String
        private lateinit var textContents: String
        private var fileItemId: Long = 0
        private lateinit var selectedFileDriveId: DriveId
        private lateinit var mimeType: String
        private var folderLevel: Int = 0
        private lateinit var parentFileName: String
        private lateinit var fileName: String
        private var newFileName: String? = null
        private lateinit var currFolderDriveId: DriveId
        private lateinit var createDate: Date
        private lateinit var updateDate: Date

        fun msgContents(msgContents: String) = apply { this.msgContents = msgContents }

        fun textContents(textContents: String) = apply { this.textContents = textContents }

        fun fileItemId(fileItemId: Long) = apply { this.fileItemId = fileItemId }

        fun selectedFileDriveId(selectedFileDriveId: DriveId) =
                apply { this.selectedFileDriveId = selectedFileDriveId }

        fun mimeType(mimeType: String) = apply { this.mimeType = mimeType }

        fun folderLevel(folderLevel: Int) = apply { this.folderLevel = folderLevel }

        fun fileName(fileName: String) = apply { this.fileName = fileName }

        fun parentFileName(parentFileName: String) = apply { this.parentFileName = parentFileName }

        fun newFileName(newFileName: String) = apply { this.newFileName = newFileName }

        fun currFolderDriveId(currFolderDriveId: DriveId) =
                apply { this.currFolderDriveId = currFolderDriveId }

        fun createDate(createDate: Date) = apply { this.createDate = createDate }

        fun updateDate(updateDate: Date) = apply { this.updateDate = updateDate }

        fun build() = FilesEvents(
                request,
                msgContents,
                textContents,
                fileItemId,
                selectedFileDriveId,
                mimeType,
                folderLevel,
                parentFileName,
                fileName,
                newFileName,
                currFolderDriveId,
                createDate,
                updateDate)
    }

}

