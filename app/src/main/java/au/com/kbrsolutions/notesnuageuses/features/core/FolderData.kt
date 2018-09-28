package au.com.kbrsolutions.notesnuageuses.features.core

import android.util.Log
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
    // fixLater: Sep 28, 2018 - clean up the code
//    var isEmptyOrAllFilesTrashed =
//            filesMetadatasInfo.size == 0 || filesMetadatasInfo.size == trashedFilesCnt
    var isEmptyOrAllFilesTrashed: Boolean = false
        get() {
            Log.v("FolderData", """ -
                |filesMetadatasInfo.size: $trashedFilesCnt
                |filesMetadatasInfo.size: ${filesMetadatasInfo.size}
                |filesMetadatasInfo.size == trashedFilesCnt: ${filesMetadatasInfo.size == trashedFilesCnt}
                |""".trimMargin())
        return filesMetadatasInfo.size == 0 || filesMetadatasInfo.size == trashedFilesCnt
    }
}