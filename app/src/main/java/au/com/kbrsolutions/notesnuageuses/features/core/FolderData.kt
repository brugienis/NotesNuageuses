package au.com.kbrsolutions.notesnuageuses.features.core

import com.google.android.gms.drive.DriveId
import java.util.*

data class FolderData(
        val newFolderDriveId: DriveId,
        val newFolderTitle: String,
        val folderLevel: Int,
        val fileParentFolderDriveId: DriveId,
        val newFolderData: Boolean,
        var trashedFilesCnt: Int,
        val filesMetadatasInfo: ArrayList<FileMetadataInfo>) {

    var isEmptyOrAllFilesTrashed: Boolean = false
        get() {
            return filesMetadatasInfo.size == 0 || filesMetadatasInfo.size == trashedFilesCnt
        }
}