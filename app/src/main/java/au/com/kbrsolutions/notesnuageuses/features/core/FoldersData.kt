package au.com.kbrsolutions.notesnuageuses.features.core

import android.util.Log
import com.google.android.gms.drive.DriveId
import java.util.*

object FoldersData {
    private var currFolderLevel = -1
    private var foldersTrashedFilesCnt: MutableList<Int> = ArrayList()
    private var foldersData: MutableList<FolderData> = ArrayList<FolderData>()
    private var foldersDriveIdsList: MutableList<DriveId> = ArrayList()
    private var foldersTitlesList: MutableList<String> = ArrayList()
    private var foldersMetadatasInfoList: MutableList<ArrayList<FileMetadataInfo>> = ArrayList<ArrayList<FileMetadataInfo>>()
    private var foldersFilesTitlesList: MutableList<ArrayList<String>> = ArrayList()
    //	private List<HashMap<Long, Integer>> foldersFilesIdsMapList = new ArrayList<HashMap<Long, Integer>>();
    private var foldersFilesIdsMapList: MutableList<HashMap<Long, ArrayList<FileMetadataInfo>>> = ArrayList<HashMap<Long, ArrayList<FileMetadataInfo>>>()
    internal var foldersFilesIdMap = ArrayList<HashMap<Long, FileMetadataInfo>>()
//	protected List<ArrayList<Metadata>> trashFoldersMetadatasList = new ArrayList<ArrayList<Metadata>>();

    private val TAG = "xyz" + FoldersData::class.java.simpleName

    fun init() {
        currFolderLevel = -1
        foldersTrashedFilesCnt = ArrayList()
        foldersData = ArrayList<FolderData>()
        foldersDriveIdsList = ArrayList()
        foldersTitlesList = ArrayList()
        foldersMetadatasInfoList = ArrayList<ArrayList<FileMetadataInfo>>()
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
                "; foldersMetadatasInfoList size: " + foldersMetadatasInfoList.size +
                "; foldersFilesTitlesList size: " + foldersFilesTitlesList.size +
                "; foldersFilesIdsMapList size: " + foldersFilesIdsMapList.size +
                "; foldersFilesIdMap size: " + foldersFilesIdMap.size
        )
    }

    @Synchronized
    fun addFolderData(folderData: FolderData) {
        showFoldersDataInfo()
        Log.v(TAG, """
            addFolderData -  start - currFolderLevel: $currFolderLevel
            folderData.folderLevel: ${folderData.folderLevel}
             newFolderTitle:${folderData.newFolderTitle}
             newFolderDriveId: ${folderData.newFolderDriveId}
             fileParentFolderDriveId: ${folderData.fileParentFolderDriveId}
             """)
        if (folderData.folderLevel > -1) {
            Log.v("FoldersData", "addFolderData - folderData fileParentFolderDriveId: ${folderData.fileParentFolderDriveId} array foldersDriveIdsList: ${foldersDriveIdsList[folderData.folderLevel]}")
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
        Log.v("FoldersData", "addFolderData - folderData.filesMetadatasInfo: ${folderData.filesMetadatasInfo} ")
        Log.v(TAG, "addFolderData -  end   - currFolderLevel: $currFolderLevel array foldersDriveIdsList: ${foldersDriveIdsList[currFolderLevel]} newFolderTitle: ${folderData.newFolderTitle}")
    }
    /*

folderData.filesMetadatasInfo: [FileMetadataInfo(
        parentTitle=App folder,
        fileTitle=Sopot new,
        fileDriveId=DriveId:CAESITFFQ3RtRjBXT0pYZHo1YkQzLVJHbHRBNEF6SjJBaTJTaRh-IKTY8qOtWSgB,
        isFolder=true,
        mimeType=application/vnd.google-apps.folder,
        createDt=Fri Aug 24 14:54:21 GMT+10:00 2018,
        updateDt=Fri Aug 24 14:54:21 GMT+10:00 2018,
        fileItemId=1535086461484, isTrashable=true, isTrashed=false)]
end   - currFolderLevel: 0 array foldersDriveIdsList: DriveId:CAESBHJvb3QYBCCk2PKjrVkoAQ== newFolderTitle: App folder
     */

    fun getParentDriveId(level: Int): DriveId {
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
        foldersMetadatasInfoList.removeAt(currFolderLevel)
        foldersFilesTitlesList.removeAt(currFolderLevel)
        //		foldersFilesTitlesList.remove(currFolderLevel);
        currFolderLevel--
        verifyDataStructure()
    }

    @Synchronized
    fun insertFolderItemView(fileItemId: Long, folderLevel: Int, fileParentFolderDriveId: DriveId, position: Int, folderMetadataInfo: FileMetadataInfo) {
        // A_MUST: check if folderLevel and position have correct value. ALSO add folder DriverId
        // in case folder was removed and another opened on the same level
        showFoldersDataInfo()
        Log.v(TAG, "insertFolderItemView - fileParentFolderDriveId: $fileParentFolderDriveId")
        Log.v(TAG, "insertFolderItemView - foldersDriveIdsList.get(folderLevel): " + foldersDriveIdsList[folderLevel])
        if (currFolderLevel < folderLevel) {
            verifyDataStructure()
            return
        } else if (foldersDriveIdsList[folderLevel] != fileParentFolderDriveId) {
            verifyDataStructure()
            return
        }
        val folderFilesTitles = foldersFilesTitlesList[folderLevel]
        folderFilesTitles.add(position, folderMetadataInfo.fileTitle)
        val folderMetadatasInfoList = foldersMetadatasInfoList[folderLevel]
        folderMetadatasInfoList.add(position, folderMetadataInfo)
        val oneFolderFilesIdMap = foldersFilesIdMap[folderLevel]
        oneFolderFilesIdMap[fileItemId] = folderMetadataInfo
        verifyDataStructure()
        //		Log.i(LOG_TAG, "@@insertFolderItemView - done - currFolderLevel/folderMetadatasInfoList size: " + currFolderLevel + "/" + folderMetadatasInfoList.size());
    }

    @Synchronized
    fun updateFolderItemView(fileItemId: Long?, folderLevel: Int, fileParentFolderDriveId: DriveId, fileMetadataInfo: FileMetadataInfo) {
        // TODO: check if folderLevel and position have correct value. ALSO add folder DriverId in case folder was removed and another opened on the same level
        if (currFolderLevel < folderLevel) {
            verifyDataStructure()
            return
        } else if (foldersDriveIdsList[folderLevel] != fileParentFolderDriveId) {
            verifyDataStructure()
            return
        }
        val folderMetadatasInfoList = foldersMetadatasInfoList[folderLevel]
        var fileItemIdIdx = -1
        var oneFolderMetadataInfo: FileMetadataInfo
        var i = 0
        val cnt = folderMetadatasInfoList.size
        while (i < cnt) {
            oneFolderMetadataInfo = folderMetadatasInfoList[i]
            //			Log.i(LOC_CAT_TAG, "@@updateFolderItemView - fileItemId/oneFolderMetadataInfo.fileItemId: " + fileItemId + "/" + oneFolderMetadataInfo.fileItemId);
            //			if (oneFolderMetadataInfo.fileItemId != null && oneFolderMetadataInfo.fileItemId.equals(fileItemId)) {
            if (oneFolderMetadataInfo.fileItemId === fileItemId) {
                fileItemIdIdx = i
                break
            }
            i++
        }
        // fixme: throw exception in test only
        if (fileItemIdIdx == -1) {
            throw RuntimeException("updateFolderItemView - fileItemId not found")
        }
        /*
		from time to time I am getting - will have to investigate

12-04 19:51:11.752 22980-22980/au.com.kbrsolutions.privatecloudnotes I/Choreographer: Skipped 32 frames!  The application may be doing too much work on its main thread.
12-04 19:51:28.063 22980-22980/au.com.kbrsolutions.privatecloudnotes E/Event: Could not dispatch event: class au.com.kbrsolutions.privatecloudnotes.events.ActivitiesEvents to subscribing class class au.com.kbrsolutions.privatecloudnotes.core.HomeActivity
                                                                              java.lang.RuntimeException: updateFolderItemView - fileItemId not found
                                                                                  at au.com.kbrsolutions.privatecloudnotes.core.FoldersData.updateFolderItemView(FoldersData.java:151)
                                                                                  at au.com.kbrsolutions.privatecloudnotes.core.HomeActivity.onEventMainThread(HomeActivity.java:1704)
                                                                                  at java.lang.reflect.Method.invoke(Native Method)
                                                                                  at java.lang.reflect.Method.invoke(Method.java:372)
                                                                                  at de.greenrobot.event.EventBus.invokeSubscriber(EventBus.java:498)
		 */

        val folderFilesTitles = foldersFilesTitlesList[folderLevel]
        folderFilesTitles[fileItemIdIdx] = fileMetadataInfo.fileTitle

        val folderData = foldersData[folderLevel]
        val filesMetadataInfo = foldersData[folderLevel].filesMetadatasInfo
        // TODO: 1/07/2015 something wrong with the logic below - before file is deleted, there is a call to this method to update file name - indicate that delete is going to start
        //                 it is decreasing trashed file count - and it SHOULDN'T
        val isTrashedCurr = filesMetadataInfo.get(fileItemIdIdx).isTrashed
        val isTrashedNewValue = fileMetadataInfo.isTrashed
        //		Log.v(LOG_TAG, "updateFolderItemView - before - " + allCurrFolderFilesTrashedOrThereAreNoFiles() + "/" + getCurrentFolderTrashedFilesCnt());
        //		Log.v(LOG_TAG, "updateFolderItemView - before - isTrashedCurr/isTrashedNewValue: " + isTrashedCurr + "/" + isTrashedNewValue);
        if (isTrashedCurr != isTrashedNewValue) {
            var trashedFilesCnt = getCurrentFolderTrashedFilesCnt()
            if (isTrashedNewValue) {
                foldersTrashedFilesCnt[folderLevel] = ++trashedFilesCnt
            } else {
                foldersTrashedFilesCnt[folderLevel] = --trashedFilesCnt
            }
            val newFolderData = FolderData(folderData.newFolderDriveId, folderData.newFolderTitle, folderData.folderLevel, folderData.fileParentFolderDriveId, folderData.newFolderData, trashedFilesCnt, folderData.filesMetadatasInfo)
            foldersData[folderLevel] = newFolderData
        }
        foldersData[folderLevel].filesMetadatasInfo.set(fileItemIdIdx, fileMetadataInfo)
        //		Log.v(LOG_TAG, "updateFolderItemView - after - " + allCurrFolderFilesTrashedOrThereAreNoFiles() + "/" + getCurrentFolderTrashedFilesCnt());


        folderMetadatasInfoList[fileItemIdIdx] = fileMetadataInfo
        verifyDataStructure()
    }

    // TODO: 30/06/2015 add Unit test
    @Synchronized
    fun updateFolderItemViewAfterFileDelete(fileItemId: Long?, folderLevel: Int, fileParentFolderDriveId: DriveId, fileMetadataInfo: FileMetadataInfo) {
        // TODO: check if folderLevel and position have correct value. ALSO add folder DriverId in case folder was removed and another opened on the same level
        if (currFolderLevel < folderLevel) {
            verifyDataStructure()
            return
        } else if (foldersDriveIdsList[folderLevel] != fileParentFolderDriveId) {
            verifyDataStructure()
            return
        }
        val folderMetadatasInfoList = foldersMetadatasInfoList[folderLevel]
        var fileItemIdIdx = 0
        //		}
        //		int fileIdx = 0;
        for (oneFolderMetadataInfo in folderMetadatasInfoList) {
            //			Log.v(LOG_TAG, "updateFolderItemViewAfterFileDelete - oneFolderMetadataInfo.fileItemId/fileItemId: " + oneFolderMetadataInfo.fileItemId + "/" + fileItemId);
            if (oneFolderMetadataInfo.fileItemId === fileItemId) {
                folderMetadatasInfoList.removeAt(fileItemIdIdx)
                break
            }
            fileItemIdIdx++
        }
        // fixme: throw exception in test only
        if (fileItemIdIdx == -1) {
            throw RuntimeException("updateFolderItemView - fileItemId not found")
        } else {
            //			Log.v(LOG_TAG, "updateFolderItemViewAfterFileDelete - before - " + allCurrFolderFilesTrashedOrThereAreNoFiles() + "/" + getCurrentFolderTrashedFilesCnt());
            val trashedFilesCnt = getCurrentFolderTrashedFilesCnt() - 1
            val folderData = foldersData[folderLevel]
            val newFolderData = FolderData(folderData.newFolderDriveId, folderData.newFolderTitle, folderData.folderLevel, folderData.fileParentFolderDriveId, folderData.newFolderData, trashedFilesCnt, folderData.filesMetadatasInfo)
            foldersData[folderLevel] = newFolderData
            foldersTrashedFilesCnt[folderLevel] = trashedFilesCnt
            //			foldersData.get(folderLevel).filesMetadatasInfo.set(fileItemIdIdx, fileMetadataInfo);
            //			Log.v(LOG_TAG, "updateFolderItemViewAfterFileDelete - after  - " + allCurrFolderFilesTrashedOrThereAreNoFiles() + "/" + getCurrentFolderTrashedFilesCnt());
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
        return if (currFolderLevel == -1) null else foldersMetadatasInfoList[currFolderLevel]
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
            foldersMetadatasInfoList[currFolderLevel] = foldersMetadatasInfo
            foldersFilesTitlesList[currFolderLevel] = folderFilesList
        } else {
            foldersFilesTitlesList.add(folderFilesList)
            foldersMetadatasInfoList.add(foldersMetadatasInfo)
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
        if (foldersMetadatasInfoList.size != currFolderLevel + 1) {
            throw RuntimeException("FoldersData - verifyDataStructure - foldersMetadatasInfoList/currFolderLevel: " + foldersMetadatasInfoList.size + "/" + currFolderLevel)
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
        return foldersMetadatasInfoList
    }

    fun getFoldersFilesTitlesList(): List<ArrayList<String>> {
        return foldersFilesTitlesList
    }

    fun getFoldersFilesIdsMapList(): List<HashMap<Long, ArrayList<FileMetadataInfo>>> {
        return foldersFilesIdsMapList
    }
}