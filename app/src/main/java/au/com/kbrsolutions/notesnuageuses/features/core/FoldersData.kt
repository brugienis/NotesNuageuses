package au.com.kbrsolutions.notesnuageuses.features.core

import android.util.Log
import com.google.android.gms.drive.DriveId
import java.util.*

object FoldersData {
    private var currFolderLevel = -1
    private var foldersData: MutableList<FolderData> = ArrayList()
    private var foldersDriveIdsList: MutableList<DriveId> = ArrayList()
    private var foldersTitlesList: MutableList<String> = ArrayList()
    private var foldersMetadataArrayInfoList: MutableList<ArrayList<FileMetadataInfo>>
            = ArrayList()
    private var foldersFilesTitlesList: MutableList<ArrayList<String>> = ArrayList()
    private var foldersFilesIdsMapList: MutableList<HashMap<Long, ArrayList<FileMetadataInfo>>>
            = ArrayList()
    private var foldersFilesIdMap = ArrayList<HashMap<Long, FileMetadataInfo>>()

    private val TAG = FoldersData::class.java.simpleName

    fun init() {
        currFolderLevel = -1
        foldersData = ArrayList()
        foldersDriveIdsList = ArrayList()
        foldersTitlesList = ArrayList()
        foldersMetadataArrayInfoList = ArrayList()
        foldersFilesTitlesList = ArrayList()
        foldersFilesIdsMapList = ArrayList()
        foldersFilesIdMap = ArrayList()
        verifyDataStructure()
    }

    @Synchronized
    fun addFolderData(folderData: FolderData) {
        if (currFolderLevel > -1 && folderData.folderLevel > -1) {
        }
        verifyDataStructure()
        if (currFolderLevel > -1 && currFolderLevel < folderData.folderLevel) {
            return
        } else if (currFolderLevel > -1
                && foldersDriveIdsList[folderData.folderLevel] !=
                folderData.fileParentFolderDriveId) {
            verifyDataStructure()
            return
        }
        foldersData.add(folderData)

        foldersDriveIdsList.add(folderData.newFolderDriveId)

        foldersTitlesList.add(folderData.newFolderTitle)
        processFolderMetadata(folderData.filesMetadataInfoList, false)
        verifyDataStructure()
    }

    fun getCurrParentDriveId(level: Int): DriveId {
        return foldersDriveIdsList[level]
    }

    @Synchronized
    fun refreshFolderData(
            folderLevel: Int,
            fileParentFolderDriveId: DriveId?,
            trashedFilesCnt: Int,
            foldersMetadatasInfo: ArrayList<FileMetadataInfo>) {
        if (currFolderLevel > -1 && currFolderLevel < folderLevel) {
            verifyDataStructure()
            return
        } else if (currFolderLevel > -1 && fileParentFolderDriveId != null &&
                foldersDriveIdsList[folderLevel] != fileParentFolderDriveId) {
            verifyDataStructure()
            return
        }
        var foldersDataAtLevel = foldersData[folderLevel]
        foldersDataAtLevel.trashedFilesCnt = trashedFilesCnt
        processFolderMetadata(foldersMetadatasInfo, true)
        verifyDataStructure()
    }

    fun currFolderIsEmptyOrAllFilesAreTrashed(): Boolean {
        if (currFolderLevel < 0) {
            return true
        }
        val currFolderData = getCurrFolderData()
        return currFolderData.isEmptyOrAllFilesTrashed
    }

    @Synchronized
    fun removeMostRecentFolderData() {
        if (currFolderLevel < 0) {
            // fixme: do not throw exception in release version
            throw RuntimeException("FoldersData - removeMostRecentFolderData should NOT be called")
        }
        foldersData.removeAt(currFolderLevel)
        foldersDriveIdsList.removeAt(currFolderLevel)
        foldersTitlesList.removeAt(currFolderLevel)
        foldersMetadataArrayInfoList.removeAt(currFolderLevel)
        foldersFilesTitlesList.removeAt(currFolderLevel)
        currFolderLevel--
        verifyDataStructure()
    }

    @Synchronized
    fun insertFolderItemView(
            fileItemId: Long,
            folderLevel: Int,
            fileParentFolderDriveId: DriveId,
            position: Int,
            folderMetadataInfo: FileMetadataInfo) {
        if (currFolderLevel < folderLevel) {
            verifyDataStructure()
            return
        } else if (foldersDriveIdsList[folderLevel] != fileParentFolderDriveId) {
            verifyDataStructure()
            return
        }
        val folderFilesTitles = foldersFilesTitlesList[folderLevel]
        folderFilesTitles.add(position, folderMetadataInfo.fileTitle)
        val folderMetadatasInfoList = foldersMetadataArrayInfoList[folderLevel]
        folderMetadatasInfoList.add(position, folderMetadataInfo)
        val oneFolderFilesIdMap = foldersFilesIdMap[folderLevel]
        oneFolderFilesIdMap[fileItemId] = folderMetadataInfo
        verifyDataStructure()
    }

    @Synchronized
    fun updateFolderItemView(
            fileItemId: Long?,
            folderLevel: Int,
            fileParentFolderDriveId: DriveId,
            idxInTheFolderFilesList: Int,
            fileMetadataInfo: FileMetadataInfo) {

        /* If an update is coming for the folder level that was already removed - ignore it */
        if (currFolderLevel < folderLevel) {
            verifyDataStructure()
            return
            /* Ignore update request if the folder's 'drive is' on the 'folderLevel' has changed */
        } else if (foldersDriveIdsList[folderLevel] != fileParentFolderDriveId) {
            verifyDataStructure()
            return
        }
        val folderMetadataArrayInfoListAtLevel = foldersMetadataArrayInfoList[folderLevel]

        var fileItemIdPos = -1
        if (folderMetadataArrayInfoListAtLevel[idxInTheFolderFilesList].fileItemId == fileItemId) {
            fileItemIdPos = idxInTheFolderFilesList
        } else {
            folderMetadataArrayInfoListAtLevel.withIndex().forEach {
                if (it.value.fileItemId == fileItemId) {
                    fileItemIdPos = it.index
                    return@forEach
                }
            }
        }

        // fixme: throw exception in test only
        if (fileItemIdPos == -1) {
            throw RuntimeException("updateFolderItemView - fileItemId not found")
        }

        val folderFilesTitles = foldersFilesTitlesList[folderLevel]
        folderFilesTitles[fileItemIdPos] = fileMetadataInfo.fileTitle

        val filesMetadataInfo = foldersData[folderLevel].filesMetadataInfoList

        val isTrashedCurr = filesMetadataInfo[fileItemIdPos].isTrashed
        val isTrashedNewValue = fileMetadataInfo.isTrashed

        val foldersDataAtLevel = foldersData[folderLevel]
        if (isTrashedCurr != isTrashedNewValue) {
//            var trashedFilesCnt = foldersTrashedFilesCnt[folderLevel]
            var trashedFilesCnt = foldersDataAtLevel.trashedFilesCnt
            if (isTrashedNewValue) {
                foldersDataAtLevel.trashedFilesCnt = ++trashedFilesCnt
            } else {
                foldersDataAtLevel.trashedFilesCnt = --trashedFilesCnt
            }
        }
        folderMetadataArrayInfoListAtLevel[fileItemIdPos] = fileMetadataInfo
        verifyDataStructure()
    }

    @Synchronized
    fun updateFolderItemViewAfterFileDelete(
            fileItemId: Long?,
            folderLevel: Int,
            fileParentFolderDriveId: DriveId,
            idxInTheFolderFilesList: Int,
            fileMetadataInfo: FileMetadataInfo) {

        if (currFolderLevel < folderLevel) {
            verifyDataStructure()
            return
        } else if (foldersDriveIdsList[folderLevel] != fileParentFolderDriveId) {
            verifyDataStructure()
            return
        }
        val folderMetadataArrayInfoListAtLevel = foldersMetadataArrayInfoList[folderLevel]

        var fileItemIdPos = -1
        if (folderMetadataArrayInfoListAtLevel[idxInTheFolderFilesList].fileItemId == fileItemId) {
            fileItemIdPos = idxInTheFolderFilesList
        } else {
            folderMetadataArrayInfoListAtLevel.withIndex().forEach {
                if (it.value.fileItemId == fileItemId) {
                    fileItemIdPos = it.index
                    return@forEach
                }
            }
        }
        // fixme: throw exception in test only
        if (fileItemIdPos == -1) {
            throw RuntimeException("updateFolderItemViewAfterFileDelete - fileItemId not found")
        } else {
            folderMetadataArrayInfoListAtLevel.removeAt(fileItemIdPos)
            if (fileMetadataInfo.isTrashed) {
                val trashedFilesCnt = getCurrentFolderTrashedFilesCnt() - 1
                var foldersDataAtLevel = foldersData[folderLevel]
                foldersDataAtLevel.trashedFilesCnt = trashedFilesCnt
            }
        }
        verifyDataStructure()
    }

    @Synchronized
    fun getCurrFolderLevel(): Int {
        return currFolderLevel
    }

    @Synchronized
    fun getCurrentFolderTrashedFilesCnt(): Int =
            if (currFolderLevel == -1) {
                -1
            } else {
                foldersData[currFolderLevel].trashedFilesCnt
    }

    @Synchronized
    fun getCurrFolderData(): FolderData {
        return foldersData[currFolderLevel]
    }

    @Synchronized
    fun getCurrFolderDriveId(): DriveId? {
        return getFolderDriveId(currFolderLevel)
    }

    @Synchronized
    fun getFolderDriveId(folderLevel: Int): DriveId? {
        if (folderLevel == -1) {
            verifyDataStructure()
            return null
        }
        return foldersDriveIdsList[folderLevel]
    }

    @Synchronized
    fun getFolderTitle(folderLevel: Int): String? {
        if (folderLevel == -1) {
            verifyDataStructure()
            return null
        }
        return foldersTitlesList[folderLevel]
    }

    @Synchronized
    fun getCurrFolderMetadataInfo(): ArrayList<FileMetadataInfo>? {
        // added check for 'currFolderLevel == -1' because it crashed monkey test
        Log.v("FoldersData", """getCurrFolderMetadataInfo -
            |currFolderLevel: ${currFolderLevel}
            |currFolderLevel: ${foldersMetadataArrayInfoList[currFolderLevel]}
            |""".trimMargin())
        return if (currFolderLevel == -1) null else foldersMetadataArrayInfoList[currFolderLevel]
    }

    @Synchronized
    fun getCurrFoldersFilesList(): ArrayList<String> {
        return foldersFilesTitlesList[currFolderLevel]
    }

    private fun processFolderMetadata(
            foldersMetadatasInfo: ArrayList<FileMetadataInfo>,
            refreshFilesInfo: Boolean) {
        val folderFilesList = ArrayList<String>()
        val foldersFilesIdsPositionMap = HashMap<Long, Int>()
        val folderFilesIdsMap = HashMap<Long, ArrayList<FileMetadataInfo>>()

        val oneFolderFilesIdMap = HashMap<Long, FileMetadataInfo>()
        var fileCreateTs: Long?
        val calendar = Calendar.getInstance()
        var pos = 0
        for (folderMetadataInfo in foldersMetadatasInfo) {
            folderFilesList.add(folderMetadataInfo.fileTitle)
            calendar.time = folderMetadataInfo.createDt
            fileCreateTs = calendar.timeInMillis
            foldersFilesIdsPositionMap[fileCreateTs] = pos++
        }
        if (refreshFilesInfo) {
            foldersMetadataArrayInfoList[currFolderLevel] = foldersMetadatasInfo
            foldersFilesTitlesList[currFolderLevel] = folderFilesList
        } else {
            foldersFilesTitlesList.add(folderFilesList)
            foldersMetadataArrayInfoList.add(foldersMetadatasInfo)
            foldersFilesIdsMapList.add(folderFilesIdsMap)
            foldersFilesIdMap.add(oneFolderFilesIdMap)
            currFolderLevel++
        }
        verifyDataStructure()
    }

    private fun verifyDataStructure() {
        if (foldersDriveIdsList.size != currFolderLevel + 1) {
            throw RuntimeException("FoldersData - verifyDataStructure - foldersDriveIdsList/currFolderLevel: " + foldersDriveIdsList.size + "/" + currFolderLevel)
        }
        if (foldersTitlesList.size != currFolderLevel + 1) {
            throw RuntimeException("FoldersData - verifyDataStructure - foldersTitlesList/currFolderLevel: " + foldersTitlesList.size + "/" + currFolderLevel)
        }
        if (foldersMetadataArrayInfoList.size != currFolderLevel + 1) {
            throw RuntimeException("FoldersData - verifyDataStructure - foldersMetadataArrayInfoList/currFolderLevel: " + foldersMetadataArrayInfoList.size + "/" + currFolderLevel)
        }
        if (foldersFilesTitlesList.size != currFolderLevel + 1) {
            throw RuntimeException("FoldersData - verifyDataStructure - foldersFilesTitlesList/currFolderLevel: " + foldersFilesTitlesList.size + "/" + currFolderLevel)
        }
    }

    //- - - - - - - - - - - for UnitTest - - - - -

    fun getFoldersDriveIdsList(): List<DriveId> {
        return foldersDriveIdsList
    }

    fun getFoldersTitlesList(): List<String> {
        return foldersTitlesList
    }

    fun getFoldersMetadatasInfoList(): List<ArrayList<FileMetadataInfo>> {
        return foldersMetadataArrayInfoList
    }

    fun getFoldersFilesTitlesList(): List<ArrayList<String>> {
        return foldersFilesTitlesList
    }

    fun getFoldersFilesIdsMapList(): List<HashMap<Long, ArrayList<FileMetadataInfo>>> {
        return foldersFilesIdsMapList
    }
}