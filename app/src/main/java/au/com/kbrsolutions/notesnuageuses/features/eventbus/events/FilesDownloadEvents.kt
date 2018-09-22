package au.com.kbrsolutions.notesnuageuses.features.eventbus.events

import com.google.android.gms.drive.DriveId

class FilesDownloadEvents(
        var request: Events,
        var msgContents: String,
        var textContents: String,
        var mimeType: String,
        var fileName: String,
        var downloadedFileDriveId: DriveId,
        var fileItemId: Long,
        var idxInTheFolderFilesList: Int) {

    enum class Events {
        FILE_DOWNLOADING,
        FILE_DOWNLOADED,
        CANCEL_CREATE,
        SAVE_TEXT_NOTE,
        DO_NOT_SAVE_TEXT_NOTE,
        SHOW_MESSAGE,
        CREATE_FILE_DIALOG_CANCELLED,
        CREATE_TEXT_NOTE,
    }

    class Builder(private var request: Events) {
        private lateinit var msgContents: String
        private lateinit var textContents: String
        private lateinit var downloadedFileDriveId: DriveId
        private lateinit var mimeType: String
        private lateinit var fileName: String
        private var fileItemId: Long = -1
        private var idxInTheFolderFilesList: Int = -1

        fun msgContents(msgContents: String) = apply { this.msgContents = msgContents }

        fun textContents(textContents: String) = apply { this.textContents = textContents }

        fun downloadedFileDriveId(downloadedFileDriveId: DriveId) =
                apply { this.downloadedFileDriveId = downloadedFileDriveId }

        fun mimeType(mimeType: String) = apply { this.mimeType = mimeType }

        fun fileName(fileName: String) = apply { this.fileName = fileName }

        fun idxInTheFolderFilesList(idxInTheFolderFilesList: Int) =
                apply { this.idxInTheFolderFilesList = idxInTheFolderFilesList }

        fun fileItemId(fileItemId: Long) =
                apply { this.fileItemId = fileItemId }

        fun build() = FilesDownloadEvents(
                request,
                msgContents,
                textContents,
                mimeType,
                fileName,
                downloadedFileDriveId,
                fileItemId,
                idxInTheFolderFilesList)
    }

}

