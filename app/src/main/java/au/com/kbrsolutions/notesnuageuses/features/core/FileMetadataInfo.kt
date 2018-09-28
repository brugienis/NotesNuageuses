package au.com.kbrsolutions.notesnuageuses.features.core

import com.google.android.gms.drive.DriveId
import com.google.android.gms.drive.Metadata
import java.util.*

data class FileMetadataInfo(
        var parentTitle: String,
        var fileTitle: String,
        var fileDriveId: DriveId?,
        var isFolder: Boolean,
        var mimeType: String,
        var createDt: Date,
        var updateDt: Date,
        var fileItemId: Long,
        var isTrashable: Boolean,
        var isTrashed: Boolean) {

    constructor(parentTitleArg: String, metadata: Metadata): this(
            parentTitleArg,
            metadata.title,
            metadata.driveId,
            metadata.isFolder,
            metadata.mimeType,
            metadata.createdDate,
            metadata.modifiedDate,
            metadata.createdDate.time,
            metadata.isTrashable,
            metadata.isTrashed)

}