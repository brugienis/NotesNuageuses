package au.com.kbrsolutions.notesnuageuses.features.core

import android.util.Log
import com.google.android.gms.drive.DriveId
import java.util.*

object FoldersData {
    private var currFolderLevel = -1
    private var foldersTrashedFilesCnt: MutableList<Int> = ArrayList()
    private var foldersData: MutableList<FolderData> = ArrayList()
    private var foldersDriveIdsList: MutableList<DriveId> = ArrayList()
    private var foldersTitlesList: MutableList<String> = ArrayList()
    private var foldersMetadataArrayInfoList: MutableList<ArrayList<FileMetadataInfo>> = ArrayList()
    private var foldersFilesTitlesList: MutableList<ArrayList<String>> = ArrayList()
    //	private List<HashMap<Long, Integer>> foldersFilesIdsMapList = new ArrayList<HashMap<Long, Integer>>();
    private var foldersFilesIdsMapList: MutableList<HashMap<Long, ArrayList<FileMetadataInfo>>> = ArrayList<HashMap<Long, ArrayList<FileMetadataInfo>>>()
    private var foldersFilesIdMap = ArrayList<HashMap<Long, FileMetadataInfo>>()
//	protected List<ArrayList<Metadata>> trashFoldersMetadatasList = new ArrayList<ArrayList<Metadata>>();

    private val TAG = "xyz" + FoldersData::class.java.simpleName

    fun init() {
        currFolderLevel = -1
        foldersTrashedFilesCnt = ArrayList()
        foldersData = ArrayList<FolderData>()
        foldersDriveIdsList = ArrayList()
        foldersTitlesList = ArrayList()
        foldersMetadataArrayInfoList = ArrayList<ArrayList<FileMetadataInfo>>()
        foldersFilesTitlesList = ArrayList()
        foldersFilesIdsMapList = ArrayList<HashMap<Long, ArrayList<FileMetadataInfo>>>()
        foldersFilesIdMap = ArrayList<HashMap<Long, FileMetadataInfo>>()
        verifyDataStructure()
    }

    private fun showFoldersDataInfo() {
        Log.v(TAG, "showFoldersDataInfo - " +
                " currFolderLevel: " + currFolderLevel +
                "; foldersTrashedFilesCnt size: " + foldersTrashedFilesCnt.size +
                "; foldersData size: " + foldersData.size +
                "; foldersDriveIdsList size: " + foldersDriveIdsList.size +
                "; foldersTitlesList size: " + foldersTitlesList.size +
                "; foldersMetadataArrayInfoList size: " + foldersMetadataArrayInfoList.size +
                "; foldersFilesTitlesList size: " + foldersFilesTitlesList.size +
                "; foldersFilesIdsMapList size: " + foldersFilesIdsMapList.size +
                "; foldersFilesIdMap size: " + foldersFilesIdMap.size
        )
    }

    @Synchronized
    fun addFolderData(folderData: FolderData) {
//        showFoldersDataInfo()
//        Log.v(TAG, """
//            addFolderData -  start - currFolderLevel: $currFolderLevel
//            folderData.folderLevel: ${folderData.folderLevel}
//             newFolderTitle:${folderData.newFolderTitle}
//             newFolderDriveId: ${folderData.newFolderDriveId}
//             fileParentFolderDriveId: ${folderData.fileParentFolderDriveId}
//             """)
        if (currFolderLevel > -1 && folderData.folderLevel > -1) {
//            Log.v("FoldersData", """addFolderData -
//                | folderData fileParentFolderDriveId: ${folderData.fileParentFolderDriveId}
//                | array foldersDriveIdsList: ${foldersDriveIdsList[folderData.folderLevel]}
//                | """.trimMargin())
        }
        verifyDataStructure()
        if (currFolderLevel > -1 && currFolderLevel < folderData.folderLevel) {
            return
        } else if (currFolderLevel > -1 && folderData.fileParentFolderDriveId != null && foldersDriveIdsList[folderData.folderLevel] != folderData.fileParentFolderDriveId) {
            verifyDataStructure()
            return
        }
        foldersTrashedFilesCnt.add(folderData.trashedFilesCnt)
        foldersData.add(folderData)

        foldersDriveIdsList.add(folderData.newFolderDriveId)
//        foldersDriveIdsList.add(folderData.filesMetadatasInfo[0].fileDriveId)

        foldersTitlesList.add(folderData.newFolderTitle)
        processFolderMetadata(folderData.filesMetadatasInfo, false)
        verifyDataStructure()
//        Log.v("FoldersData", "addFolderData - folderData.filesMetadatasInfo: ${folderData.filesMetadatasInfo} ")
//        Log.v(TAG, "addFolderData -  end   - currFolderLevel: $currFolderLevel array foldersDriveIdsList: ${foldersDriveIdsList[currFolderLevel]} newFolderTitle: ${folderData.newFolderTitle}")
    }

    fun getCurrParentDriveId(level: Int): DriveId {
        return foldersDriveIdsList[level]
    }

    // TODO: add code to check before conditions as on addFolderData()
    @Synchronized
    fun refreshFolderData(folderLevel: Int, fileParentFolderDriveId: DriveId?, trashedFilesCnt: Int, foldersMetadatasInfo: ArrayList<FileMetadataInfo>) {
        if (currFolderLevel > -1 && currFolderLevel < folderLevel) {
            verifyDataStructure()
            return
        } else if (currFolderLevel > -1 && fileParentFolderDriveId != null && foldersDriveIdsList[folderLevel] != fileParentFolderDriveId) {
            verifyDataStructure()
            return
        }
        foldersTrashedFilesCnt[currFolderLevel] = trashedFilesCnt
        processFolderMetadata(foldersMetadatasInfo, true)
        verifyDataStructure()
    }

    fun noFoldersAdded(): Boolean {
        return currFolderLevel < 0
    }

    fun allCurrFolderFilesTrashedOrThereAreNoFiles(): Boolean {
        if (currFolderLevel < 0) {
            return true
        }
        val filesMetadataInfo = getCurrFolderData().filesMetadatasInfo
        return filesMetadataInfo.size == getCurrentFolderTrashedFilesCnt()
    }

    @Synchronized
    fun removeMostRecentFolderData() {
        if (currFolderLevel < 0) {
            // fixme: do not throw exception in release version
            throw RuntimeException("FoldersData - removeMostRecentFolderData should NOT be called")
        }
        foldersTrashedFilesCnt.removeAt(currFolderLevel)
        foldersData.removeAt(currFolderLevel)
        foldersDriveIdsList.removeAt(currFolderLevel)
        foldersTitlesList.removeAt(currFolderLevel)
        foldersMetadataArrayInfoList.removeAt(currFolderLevel)
        foldersFilesTitlesList.removeAt(currFolderLevel)
        //		foldersFilesTitlesList.remove(currFolderLevel);
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
        // A_MUST: check if folderLevel and position have correct value. ALSO add folder DriverId
        // in case folder was removed and another opened on the same level
        showFoldersDataInfo()
        Log.v(TAG, "insertFolderItemView - fileParentFolderDriveId: $fileParentFolderDriveId")
        Log.v(TAG, "insertFolderItemView - foldersDriveIdsList.get(folderLevel): " + foldersDriveIdsList[folderLevel])
        Log.v("FoldersData", """insertFolderItemView - currFolderLevel: $currFolderLevel  folderLevel: $folderLevel """)
        if (currFolderLevel < folderLevel) {
            verifyDataStructure()
            return
        } else if (foldersDriveIdsList[folderLevel] != fileParentFolderDriveId) {
            verifyDataStructure()
            return
        }
        Log.v(TAG, "insertFolderItemView - validations passed")
        val folderFilesTitles = foldersFilesTitlesList[folderLevel]
        folderFilesTitles.add(position, folderMetadataInfo.fileTitle)
        val folderMetadatasInfoList = foldersMetadataArrayInfoList[folderLevel]
        folderMetadatasInfoList.add(position, folderMetadataInfo)
        val oneFolderFilesIdMap = foldersFilesIdMap[folderLevel]
        oneFolderFilesIdMap[fileItemId] = folderMetadataInfo
        verifyDataStructure()
        //		Log.i(LOG_TAG, "@@insertFolderItemView - done - currFolderLevel/folderMetadatasInfoList size: " + currFolderLevel + "/" + folderMetadatasInfoList.size());
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
        Log.v("FoldersData", """updateFolderItemView -
            |fileMetadataInfo.fileTitle: ${fileMetadataInfo.fileTitle} """.trimMargin())

        folderMetadataArrayInfoListAtLevel[fileItemIdPos] = fileMetadataInfo
//        val folderData = foldersData[folderLevel]
        val filesMetadataInfo = foldersData[folderLevel].filesMetadatasInfo

        val isTrashedCurr = filesMetadataInfo[fileItemIdPos].isTrashed
        val isTrashedNewValue = fileMetadataInfo.isTrashed
        Log.v("FoldersData", """updateFolderItemView -
            |isTrashedCurr:     $isTrashedCurr
            |isTrashedNewValue: $isTrashedNewValue
            |""".trimMargin())

        if (isTrashedCurr != isTrashedNewValue) {
//            var trashedFilesCnt = getCurrentFolderTrashedFilesCnt()
            var trashedFilesCnt = foldersTrashedFilesCnt[folderLevel]
            if (isTrashedNewValue) {
                foldersTrashedFilesCnt[folderLevel] = ++trashedFilesCnt
            } else {
                foldersTrashedFilesCnt[folderLevel] = --trashedFilesCnt
                Log.v("FoldersData", """updateFolderItemView -
                    |before trashedFilesCnt: $trashedFilesCnt
                    |after  trashedFilesCnt: ${foldersTrashedFilesCnt[folderLevel]}
                    |""".trimMargin())
            }
        }
        verifyDataStructure()
    }

    // TODO: 30/06/2015 add Unit test
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
            throw RuntimeException("updateFolderItemView - fileItemId not found")
        } else {
            folderMetadataArrayInfoListAtLevel.removeAt(fileItemIdPos)
            val trashedFilesCnt = getCurrentFolderTrashedFilesCnt() - 1
            foldersTrashedFilesCnt[folderLevel] = trashedFilesCnt
        }
        verifyDataStructure()
    }

    @Synchronized
    fun getCurrFolderLevel(): Int {
        return currFolderLevel
    }

    @Synchronized
    fun getCurrentFolderTrashedFilesCnt(): Int {
        return if (currFolderLevel == -1) -1 else foldersTrashedFilesCnt[currFolderLevel]
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
            //			Log.i(LOG_TAG, "getFolderDriveId - EXITING - folderLevel: " + folderLevel);
            verifyDataStructure()
            return null
        }
        return foldersDriveIdsList[folderLevel]
    }

    @Synchronized
    fun getCurrFolderTitle(): String? {
        return getFolderTitle(currFolderLevel)
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
        return if (currFolderLevel == -1) null else foldersMetadataArrayInfoList[currFolderLevel]
    }

    @Synchronized
    fun getCurrFoldersFilesList(): ArrayList<String> {
        return foldersFilesTitlesList[currFolderLevel]
    }

    private fun processFolderMetadata(foldersMetadatasInfo: ArrayList<FileMetadataInfo>, refreshFilesInfo: Boolean) {
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
        if (foldersTrashedFilesCnt.size != currFolderLevel + 1) {
            throw RuntimeException("FoldersData - verifyDataStructure - foldersTrashedFilesCnt/currFolderLevel: " + foldersTrashedFilesCnt.size + "/" + currFolderLevel)
        }
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
        //		if (foldersFilesIdsMapList.size() != (currFolderLevel + 1)) {
        //			throw new RuntimeException("FoldersData - verifyDataStructure - foldersFilesIdsMapList/currFolderLevel: " + foldersFilesIdsMapList.size() + "/" + currFolderLevel);
        //		}
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