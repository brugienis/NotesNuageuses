package au.com.kbrsolutions.notesnuageuses.features.core

import android.util.Log
import au.com.kbrsolutions.notesnuageuses.features.main.dialogs.RenameFileDialog.Companion.LOC_CAT_TAG
import com.google.android.gms.drive.DriveId
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*

class FoldersDataTest {

    @Before
    fun setUp() {
        FoldersData.init()
    }

    @After
    fun tearDown() {
    }

    @Test
    fun addFolderData() {
    }

    @Test
    fun testInit() {
        val currFolderLevel = FoldersData.getCurrFolderLevel()
        val expectedCurrFolderLevel = -1
        Assert.assertEquals("incorrect currFolderLevel", expectedCurrFolderLevel, currFolderLevel)
        verifyDataStructure()
    }

    @Test
    fun testAddFolderData_fstTime() {
        var folderLevel: Int
        var thisDriveId: DriveId
        var parentDriveId: DriveId?
        val parentTitle: String
        var title: String
        var fileDriveId: DriveId
        var isFolder: Boolean?
        var mimeType: String
        var createDate: Date
        var updateDate: Date

        // this should be OK
        folderLevel = 2            // folderLevel is ignored the first time the 'add is executed - it doesn't matter what value will be passed
        parentDriveId = null        //DriveId.decodeFromString("DriveId:CAESHDBCN3VRMnJDUU0wZVFUV3R6VDFNeWVEWkhXbFUYxFUg-sbQ7YpR");
        thisDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        parentTitle = "parT"
        title = "Title"
        fileDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        isFolder = true
        mimeType = "text/plain"
        createDate = Date()
        updateDate = Date()
        var foldersMetadatasInfo = ArrayList<FileMetadataInfo>()
        //(String parentTitle, String fileTitle, DriveId fileDriveId, boolean isFolder, String mimeType, Date createDate, Date updateDate, long fileItemId)
        var folderMetadataInfo = FileMetadataInfo(parentTitle, title, fileDriveId, isFolder, mimeType, createDate, updateDate, System.currentTimeMillis(), true, false)
        foldersMetadatasInfo.add(folderMetadataInfo)
//        var folderData = FolderData(thisDriveId, title, folderLevel, parentDriveId, true, 0, foldersMetadatasInfo)
        var folderData = FolderData(thisDriveId, title, folderLevel, thisDriveId, true, 0, foldersMetadatasInfo)
        FoldersData.addFolderData(folderData)
        //        FoldersData.addFolderData(thisDriveId, title, folderLevel, parentDriveId, 0, foldersMetadatasInfo);
        var currFolderLevel = FoldersData.getCurrFolderLevel()
        var expectedCurrFolderLevel = 0
        Assert.assertEquals("incorrect currFolderLevel", expectedCurrFolderLevel, currFolderLevel)

        // folderLevel = 3; - this case will not work - after the previous step it should be folderLevel = 0;
        folderLevel = 3
        parentDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        thisDriveId = DriveId.decodeFromString("DriveId:CAESHDBCN3VRMnJDUU0wZVFUV3R6VDFNeWVEWkhXbFUYxFUg-sbQ7YpR")
        title = "Title"
        fileDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        isFolder = true
        mimeType = "text/plain"
        createDate = Date()
        updateDate = Date()
        foldersMetadatasInfo = ArrayList()
        folderMetadataInfo = FileMetadataInfo(parentTitle, title, fileDriveId, isFolder, mimeType, createDate, updateDate, System.currentTimeMillis(), true, false)
        foldersMetadatasInfo.add(folderMetadataInfo)
        //        FolderData folderData = new FolderData(thisDriveId, title, folderLevel, parentDriveId, true, 0, foldersMetadatasInfo);
        FoldersData.addFolderData(folderData)
        //        FoldersData.addFolderData(thisDriveId, title, folderLevel, parentDriveId, 0, foldersMetadatasInfo);
        currFolderLevel = FoldersData.getCurrFolderLevel()
        expectedCurrFolderLevel = 0
        Assert.assertEquals("incorrect currFolderLevel", expectedCurrFolderLevel, currFolderLevel)

        // this should be OK
        folderLevel = 0
        parentDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        thisDriveId = DriveId.decodeFromString("DriveId:CAESHDBCN3VRMnJDUU0wZVFUV3R6VDFNeWVEWkhXbFUYxFUg-sbQ7YpR")
        title = "Title"
        fileDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        isFolder = true
        mimeType = "text/plain"
        createDate = Date()
        updateDate = Date()
        foldersMetadatasInfo = ArrayList()
        folderMetadataInfo = FileMetadataInfo(parentTitle, title, fileDriveId, isFolder, mimeType, createDate, updateDate, System.currentTimeMillis(), true, false)
        foldersMetadatasInfo.add(folderMetadataInfo)
        folderData = FolderData(thisDriveId, title, folderLevel, parentDriveId, true, 0, foldersMetadatasInfo)
        FoldersData.addFolderData(folderData)
        //        FoldersData.addFolderData(thisDriveId, title, folderLevel, parentDriveId, 0, foldersMetadatasInfo);
        currFolderLevel = FoldersData.getCurrFolderLevel()
        expectedCurrFolderLevel = folderLevel + 1
        Assert.assertEquals("incorrect currFolderLevel", expectedCurrFolderLevel, currFolderLevel)

        // this should be OK
        folderLevel = 1
        parentDriveId = DriveId.decodeFromString("DriveId:CAESHDBCN3VRMnJDUU0wZVFUV3R6VDFNeWVEWkhXbFUYxFUg-sbQ7YpR")
        thisDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        title = "Title"
        fileDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        isFolder = true
        mimeType = "text/plain"
        createDate = Date()
        updateDate = Date()
        foldersMetadatasInfo = ArrayList()
        folderMetadataInfo = FileMetadataInfo(parentTitle, title, fileDriveId, isFolder, mimeType, createDate, updateDate, System.currentTimeMillis(), true, false)
        foldersMetadatasInfo.add(folderMetadataInfo)
        folderData = FolderData(thisDriveId, title, folderLevel, parentDriveId, true, 0, foldersMetadatasInfo)
        FoldersData.addFolderData(folderData)
        //        FoldersData.addFolderData(thisDriveId, title, folderLevel, parentDriveId, 0, foldersMetadatasInfo);
        currFolderLevel = FoldersData.getCurrFolderLevel()
        expectedCurrFolderLevel = folderLevel + 1
        Assert.assertEquals("incorrect currFolderLevel", expectedCurrFolderLevel, currFolderLevel)
        verifyDataStructure()
    }

    @Test
    fun testAddFolderData_oneFile_thenTrashFile() {
        val folderLevel: Int
        val thisDriveId: DriveId
        val parentDriveId: DriveId?
        val parentTitle: String
        val title: String
        val fileDriveId: DriveId
        val isFolder: Boolean?
        val mimeType: String
        val createDate: Date
        val updateDate: Date

        // this should be OK
        folderLevel = 0            // folderLevel is ignored the first time the 'add is executes - it doesn't matter what value will be passed
        parentDriveId = DriveId.decodeFromString("DriveId:CAESHDBCN3VRMnJDUU0wZVFUV3R6VDFNeWVEWkhXbFUYxFUg-sbQ7YpR");
        thisDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        parentTitle = "parT"
        title = "Trash1File"
        fileDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        isFolder = true
        mimeType = "text/plain"
        createDate = Date()
        updateDate = Date()
        var isTrashed = false
        val foldersMetadatasInfo = ArrayList<FileMetadataInfo>()
        //(String parentTitle, String fileTitle, DriveId fileDriveId, boolean isFolder, String mimeType, Date createDate, Date updateDate, long fileItemId)
        val fileItemId = System.currentTimeMillis()
        var folderMetadataInfo = FileMetadataInfo(parentTitle, title, fileDriveId, isFolder, mimeType, createDate, updateDate, fileItemId, true, isTrashed)
        foldersMetadatasInfo.add(folderMetadataInfo)
        var folderData = FolderData(thisDriveId, title, folderLevel, parentDriveId, true, 0, foldersMetadatasInfo)

        /* add folder */
        FoldersData.addFolderData(folderData)
        //        FoldersData.addFolderData(thisDriveId, title, folderLevel, parentDriveId, 0, foldersMetadatasInfo);
        val currFolderLevel = FoldersData.getCurrFolderLevel()
        val expectedCurrFolderLevel = 0
        Assert.assertEquals("incorrect currFolderLevel", expectedCurrFolderLevel, currFolderLevel)
        Assert.assertEquals("not all files in the folder are trashed", false, FoldersData.currFolderIsEmptyOrAllFilesAreTrashed())

        /* change file status to trashed */
        isTrashed = true
        folderMetadataInfo = FileMetadataInfo(parentTitle, title, fileDriveId, isFolder, mimeType, createDate, updateDate, fileItemId, true, isTrashed)
        FoldersData.updateFolderItemView(
                fileItemId,
                folderLevel,
                thisDriveId,
                0,
                folderMetadataInfo)

        folderData = FoldersData.getCurrFolderData()
        val filesMetadataInfo = folderData.filesMetadatasInfo
        Assert.assertEquals("wrong filesMetadataInfo size", 1, filesMetadataInfo.size)

        //        ArrayList<FileMetadataInfo> folderMetadataInfo1 = FoldersData.getCurrFolderMetadataInfo();

        Assert.assertEquals("wrong trashedFilesCnt", 1, FoldersData.getCurrentFolderTrashedFilesCnt())
        Assert.assertEquals("all files in the folder are trashed", true, FoldersData.currFolderIsEmptyOrAllFilesAreTrashed())

        /* change name - the status is still trashed */
        val newTitle = title + "new"
        folderMetadataInfo = FileMetadataInfo(parentTitle, newTitle, fileDriveId, isFolder, mimeType, createDate, updateDate, fileItemId, true, isTrashed)
        FoldersData.updateFolderItemView(
                fileItemId,
                folderLevel,
                thisDriveId,
                0,
                folderMetadataInfo)

        Assert.assertEquals("wrong trashedFilesCnt", 1, FoldersData.getCurrentFolderTrashedFilesCnt())
        Assert.assertEquals(
                "wrong trashedFilesCnt",
                1,
                FoldersData.getCurrFolderData().trashedFilesCnt)
    }

    // fixLater: Sep 28, 2018 - add test: add trashed file and check counts

    @Test
    fun testAddFolderData_oneFile_thenDeleteFile() {
        val folderLevel: Int
        val thisDriveId: DriveId
        val parentDriveId: DriveId?
        val parentTitle: String
        val title: String
        val fileDriveId: DriveId
        val isFolder: Boolean?
        val mimeType: String
        val createDate: Date
        val updateDate: Date

        // this should be OK
        folderLevel = 0            // folderLevel is ignored the first time the 'add is executes - it doesn't matter what value will be passed
        parentDriveId = DriveId.decodeFromString("DriveId:CAESHDBCN3VRMnJDUU0wZVFUV3R6VDFNeWVEWkhXbFUYxFUg-sbQ7YpR");
        thisDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        parentTitle = "parT"
        title = "Trash1File"
        fileDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        isFolder = true
        mimeType = "text/plain"
        createDate = Date()
        updateDate = Date()
        var isTrashed = false
        val foldersMetadatasInfo = ArrayList<FileMetadataInfo>()
        //(String parentTitle, String fileTitle, DriveId fileDriveId, boolean isFolder, String mimeType, Date createDate, Date updateDate, long fileItemId)
        val fileItemId = System.currentTimeMillis()
        var folderMetadataInfo = FileMetadataInfo(parentTitle, title, fileDriveId, isFolder, mimeType, createDate, updateDate, fileItemId, true, isTrashed)
        foldersMetadatasInfo.add(folderMetadataInfo)
        var folderData = FolderData(thisDriveId, title, folderLevel, parentDriveId, true, 0, foldersMetadatasInfo)
        FoldersData.addFolderData(folderData)
        //        FoldersData.addFolderData(thisDriveId, title, folderLevel, parentDriveId, 0, foldersMetadatasInfo);
        val currFolderLevel = FoldersData.getCurrFolderLevel()
        val expectedCurrFolderLevel = 0
        Assert.assertEquals("incorrect currFolderLevel", expectedCurrFolderLevel, currFolderLevel)
        Assert.assertEquals("not all files in the folder are trashed", false, FoldersData.currFolderIsEmptyOrAllFilesAreTrashed())

        isTrashed = true
        folderMetadataInfo = FileMetadataInfo(parentTitle, title, fileDriveId, isFolder, mimeType, createDate, updateDate, fileItemId, true, isTrashed)
        FoldersData.updateFolderItemView(
                fileItemId,
                folderLevel,
                thisDriveId,
                0,
                folderMetadataInfo)

        folderData = FoldersData.getCurrFolderData()
        val filesMetadataInfo = folderData.filesMetadatasInfo
        Assert.assertEquals("wrong filesMetadataInfo size", 1, filesMetadataInfo.size)

        //        ArrayList<FileMetadataInfo> folderMetadataInfo1 = FoldersData.getCurrFolderMetadataInfo();

        Assert.assertEquals("wrong trashedFilesCnt", 1, FoldersData.getCurrentFolderTrashedFilesCnt())
        Assert.assertEquals("all files in the folder are trashed", true, FoldersData.currFolderIsEmptyOrAllFilesAreTrashed())

        // pretend file was deleted

        FoldersData.updateFolderItemViewAfterFileDelete(
                fileItemId,
                folderLevel,
                thisDriveId,
                0,
                folderMetadataInfo)

        Assert.assertEquals("wrong trashedFilesCnt", 0, FoldersData.getCurrentFolderTrashedFilesCnt())
        val foldersMetadatasInfoAfterDelete = FoldersData.getCurrFolderMetadataInfo()
        //        FileMetadataInfo folderMetadataInfoAfterDelete = foldersMetadatasInfoAfterDelete.get(0);
        Assert.assertEquals("There should be no files after this delete", 0, foldersMetadatasInfoAfterDelete!!.size)
        Assert.assertTrue("There should be no files in the current folder", FoldersData.currFolderIsEmptyOrAllFilesAreTrashed())
    }

    @Test
    fun testAllCurrFolderFilesTrashed() {
        val folderLevel = 0
        val thisDriveId: DriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        val parentDriveId: DriveId = DriveId.decodeFromString("DriveId:CAESHDBCN3VRMnJDUU0wZVFUV3R6VDFNeWVEWkhXbFUYxFUg-sbQ7YpR")
        val parentTitle = "parT"
        val title = "Trash1File"
        val fileDriveId: DriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        val isFolder: Boolean?
        val mimeType = "text/plain"
        val createDate = Date()
        val updateDate = Date()

        isFolder = true
        var isTrashed = false

        // test before any folder added
        Assert.assertEquals(
                "There should be no files in the folder",
                true,
                FoldersData.currFolderIsEmptyOrAllFilesAreTrashed())

        // adding new folder - one un trashed file
        val foldersMetadatasInfo = ArrayList<FileMetadataInfo>()
        val fileItemId = System.currentTimeMillis()

        var folderMetadataInfo = FileMetadataInfo(
                parentTitle,
                title,
                fileDriveId,
                isFolder,
                mimeType,
                createDate,
                updateDate,
                fileItemId,
                true,
                isTrashed)

        foldersMetadatasInfo.add(folderMetadataInfo)
        var folderData = FolderData(
                thisDriveId,
                title,
                folderLevel,
                parentDriveId,
                true,
                0,
                foldersMetadatasInfo)

        FoldersData.addFolderData(folderData)
        Assert.assertEquals(
                "not all files in the folder are trashed",
                false,
                FoldersData.currFolderIsEmptyOrAllFilesAreTrashed())

        // changing file's mIsTrashed to true
        isTrashed = true
        folderMetadataInfo = FileMetadataInfo(
                parentTitle,
                title,
                fileDriveId,
                isFolder,
                mimeType,
                createDate,
                updateDate,
                fileItemId,
                true,
                isTrashed)

        FoldersData.updateFolderItemView(
                fileItemId,
                folderLevel,
                thisDriveId,
                0,
                folderMetadataInfo)

        folderData = FoldersData.getCurrFolderData()
        val filesMetadataInfo = folderData.filesMetadatasInfo

        Assert.assertEquals(
                "Wrong files count in the folder",
                1,
                FoldersData.getCurrFolderData().filesMetadatasInfo.size)

        Assert.assertEquals(
                "Wrong trashedFilesCnt",
                1,
                FoldersData.getCurrentFolderTrashedFilesCnt())

        Assert.assertEquals(
                "All files in the folder are trashed",
                true,
                FoldersData.currFolderIsEmptyOrAllFilesAreTrashed())
    }

    fun testRefreshFolderData() {
        Assert.fail("Not yet implemented")
    }

    fun testRemoveMostRecentFolderData() {
        Assert.fail("Not yet implemented")
    }

    /*
     * folderLevel passed in the FoldersData.insertFolderItemView(...) is higher than the currFolderLevel - user clicked back button.
     * The insert should be ignored.
     */
    fun testInsertFolderItemView_currFolderLowerThanFolderLevel() {
        Assert.fail("Not yet implemented")
    }

    /*
     * folderLevel passed in the FoldersData.insertFolderItemView(...) is lower than the currFolderLevel - user clicked on another folder in the folder to which insert is
     * supposed to be executed. The insert should proceed.
     */
    fun testInsertFolderItemView_currFolderHigherThanFolderLevel() {
        Assert.fail("Not yet implemented")
    }

    @Test
    fun testInsertFolderItemView_currFolderDidNotChanged() {
        val parentTitle = ""
        val title: String
        val fileDriveId: DriveId
        val isFolder: Boolean?
        val mimeType: String
        val fileParentFolderDriveId = addTopFolderDetailsWithOneFile()
        title = "File : xxxx encrypting"
        fileDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        isFolder = true
        mimeType = "text/plain"
        val fileItemId = System.currentTimeMillis()
        val folderLevel = 0
        val position = 0
        val currFolderLevel = FoldersData.getCurrFolderLevel()
        val expectedContainerSize = currFolderLevel + 1
        val foldersMetadatasInfoListSizeBeforeInsert = FoldersData.getFoldersMetadatasInfoList().size
        val currFolderMetadataInfoBeforeInsert = FoldersData.getCurrFolderMetadataInfo()
        val currFolderMetadataInfoBeforeInsertSize = currFolderMetadataInfoBeforeInsert!!.size
        Log.i(LOC_CAT_TAG, "@@testInsertFolderItemView - currFolderMetadataInfoBeforeInsert: $currFolderMetadataInfoBeforeInsert")
        // . . . .
        val folderMetadataInfo = FileMetadataInfo(parentTitle, title, fileDriveId, isFolder, mimeType, Date(), Date(), System.currentTimeMillis(), true, false)
        FoldersData.insertFolderItemView(fileItemId, folderLevel, fileParentFolderDriveId, position, folderMetadataInfo)
        // . . . .
        val foldersMetadatasInfoListSizeAfterInsert = FoldersData.getFoldersMetadatasInfoList().size
        val currFolderMetadataInfoAfterInsert = FoldersData.getCurrFolderMetadataInfo()
        Log.i(LOC_CAT_TAG, "@@testInsertFolderItemView - currFolderMetadataInfoAfterInsert: $currFolderMetadataInfoAfterInsert")
        Assert.assertEquals("foldersMetadatasInfoList size must be: $expectedContainerSize", expectedContainerSize, foldersMetadatasInfoListSizeBeforeInsert)
        Assert.assertEquals("foldersMetadatasInfoList size must be: $expectedContainerSize", foldersMetadatasInfoListSizeAfterInsert, foldersMetadatasInfoListSizeBeforeInsert)
        // below DO NOT try to compare currFolderMetadataInfoBeforeInsert.size() with currFolderMetadataInfoAfterInsert.size() - they point to exactly the same container
        Assert.assertEquals("wrong value of currFolderMetadataInfoAfterInsert", currFolderMetadataInfoBeforeInsertSize + 1, currFolderMetadataInfoAfterInsert!!.size)
        val (_, fileTitle) = currFolderMetadataInfoAfterInsert.get(position)
        Assert.assertEquals("wrong value of folderMetadataInfoInserted", title, fileTitle)
    }

    @Test
    fun testUpdateFolderItemView_00() {
        var title: String
        val fileDriveId: DriveId
        val isFolder = false
        val mimeType = "text/plain"

        //		DriveId fileParentFolderDriveId = addTopFolderDetailsWithOneFile();
        val fileParentFolderDriveId = addEmptyTopFolderDetails()
        title = "File0 encrypting"
        val fileItemId0 = insertNewFileDetails(fileParentFolderDriveId, title)
        val currFolderMetadataInfo = FoldersData.getCurrFolderMetadataInfo()
        val folderMetadataInfoArrayBefore = currFolderMetadataInfo!!.toTypedArray()
        val currFoldersFilesArrayBefore = FoldersData.getCurrFoldersFilesList().toArray(arrayOfNulls<String>(0))
        run {
            var i = 0
            val cnt = folderMetadataInfoArrayBefore.size
            while (i < cnt) {
                Log.i(LOC_CAT_TAG, "@@testUpdateFolderItemView:    " + i + ": " + folderMetadataInfoArrayBefore[i].fileTitle + "/" + folderMetadataInfoArrayBefore[i].isFolder)
                i++
            }
        }
        Log.i(LOC_CAT_TAG, "@@testUpdateFolderItemView:    -------------")

        fileDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        title = "File0 updating"
        val folderLevel = 0
        val position = 0
        val folderMetadataInfo = FileMetadataInfo("", title, fileDriveId, isFolder, mimeType, Date(), Date(), System.currentTimeMillis(), true, false)
        FoldersData.updateFolderItemView(
                fileItemId0,
                folderLevel,
                fileParentFolderDriveId,
                0,
                folderMetadataInfo)
        // . . . .
        val folderMetadataInfoArrayAfter = currFolderMetadataInfo.toTypedArray()
        run {
            var i = 0
            val cnt = folderMetadataInfoArrayAfter.size
            while (i < cnt) {
                Assert.assertEquals("i: $i", title, folderMetadataInfoArrayAfter[i].fileTitle)
                Log.i(LOC_CAT_TAG, "@@testUpdateFolderItemView:    " + i + ": " + folderMetadataInfoArrayBefore[i].fileTitle + "/" + folderMetadataInfoArrayAfter[i].fileTitle)
                i++
            }
        }
        val currFoldersFilesArrayAfter = FoldersData.getCurrFoldersFilesList().toArray(arrayOfNulls<String>(0))
        Log.i(LOC_CAT_TAG, "@@testUpdateFolderItemView: " + currFoldersFilesArrayAfter.size + " files in folder")
        var i = 0
        val cnt = currFoldersFilesArrayAfter.size
        while (i < cnt) {
            Log.i(LOC_CAT_TAG, "i/fileTitle: " + title + "/" + currFoldersFilesArrayAfter.size + " files in folder")
            if (i == position) {
                Assert.assertEquals("i: $i", title, currFoldersFilesArrayAfter[i])
            } else {
                Assert.assertEquals("i: $i", currFoldersFilesArrayBefore[i], currFoldersFilesArrayAfter[i])
            }
            Log.i(LOC_CAT_TAG, "@@testUpdateFolderItemView:    " + i + ": " + folderMetadataInfoArrayBefore[i] + "/" + currFoldersFilesArrayAfter[i])
            i++
        }
        // . . . .
    }

    @Test
    fun testUpdateFolderItemView_01_two_sequential_inserts_and_updates_01() {
        val isFolder = false
        val mimeType = "text/plain"
        var fstFileTitle: String
        var scndFileTitle: String
        var fstFileDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        val scndFileDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")

        val fileParentFolderDriveId = addEmptyTopFolderDetails()
        val folderLevel = 0
        fstFileTitle = "File0 encrypting"
        // insert the first file details
        val fileItemId0 = insertNewFileDetails(fileParentFolderDriveId, fstFileTitle)

        fstFileDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        fstFileTitle = "File0 updating"
        val folderMetadataInfo0 = FileMetadataInfo("", fstFileTitle, fstFileDriveId, isFolder, mimeType, Date(), Date(), fileItemId0, true, false)
        FoldersData.updateFolderItemView(
                fileItemId0,
                folderLevel,
                fileParentFolderDriveId,
                0,
                folderMetadataInfo0)
        // . . . .

        scndFileTitle = "File1 encrypting"
        // insert the second file details
        val fileItemId1 = insertNewFileDetails(fileParentFolderDriveId, scndFileTitle)

        fstFileDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        scndFileTitle = "File1 updating"
        // . . . .
        val folderMetadataInfo1 = FileMetadataInfo("", scndFileTitle, scndFileDriveId, isFolder, mimeType, Date(), Date(), fileItemId1, true, false)
        FoldersData.updateFolderItemView(
                fileItemId1,
                folderLevel,
                fileParentFolderDriveId,
                0,
                folderMetadataInfo1)
        // . . . .

        val currFolderMetadataInfo = FoldersData.getCurrFolderMetadataInfo()
        val folderMetadataInfoArrayAfter = currFolderMetadataInfo!!.toTypedArray()
        var i = 0
        val cnt = folderMetadataInfoArrayAfter.size
        while (i < cnt) {
            Log.i(LOC_CAT_TAG, "sequential_01 - i/fstFileTitle/scndFileTitle/fileTitle: " + i + " : " + fstFileTitle + "/" + scndFileTitle + "/" + folderMetadataInfoArrayAfter[i].fileTitle)
            when (i) {
                0 -> Assert.assertEquals("i: $i", scndFileTitle, folderMetadataInfoArrayAfter[i].fileTitle)
                1 -> Assert.assertEquals("i: $i", fstFileTitle, folderMetadataInfoArrayAfter[i].fileTitle)

                else -> Assert.fail("no case for i: $i")
            }
            i++
        }
    }

    @Test
    fun testUpdateFolderItemView_01_two_interwoven_inserts_and_updates_02() {
        val isFolder = false
        val mimeType = "text/plain"
        var fstFileTitle: String
        var scndFileTitle: String
        var fstFileDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        val scndFileDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")

        val fileParentFolderDriveId = addEmptyTopFolderDetails()
        val folderLevel = 0
        //		int position = 0;
        fstFileTitle = "File0 encrypting"
        // insert the first file details
        val fileItemId0 = insertNewFileDetails(fileParentFolderDriveId, fstFileTitle)

        fstFileDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        fstFileTitle = "File0 updating"
        // . . . .

        scndFileTitle = "File1 encrypting"
        // insert the second file details
        val fileItemId1 = insertNewFileDetails(fileParentFolderDriveId, scndFileTitle)

        fstFileDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        scndFileTitle = "File1 updating"
        // . . . .
        val folderMetadataInfo0 = FileMetadataInfo(
                "",
                fstFileTitle,
                fstFileDriveId,
                isFolder,
                mimeType,
                Date(),
                Date(),
                fileItemId0,
                true,
                false)

        FoldersData.updateFolderItemView(
                fileItemId0,
                folderLevel,
                fileParentFolderDriveId,
                0,
                folderMetadataInfo0)
        // . . . .
        val folderMetadataInfo1 = FileMetadataInfo(
                "",
                scndFileTitle,
                scndFileDriveId,
                isFolder,
                mimeType,
                Date(),
                Date(),
                fileItemId1,
                true,
                false)

        FoldersData.updateFolderItemView(
                fileItemId1,
                folderLevel,
                fileParentFolderDriveId,
                0,
                folderMetadataInfo1)
        // . . . .

        val currFolderMetadataInfo = FoldersData.getCurrFolderMetadataInfo()
        //		FolderMetadataInfo[] folderMetadataInfoArrayAfter = currFolderMetadataInfo.toArray(new FolderMetadataInfo[0]);
        var folderMetadataInfo: FileMetadataInfo
        var i = 0
        val cnt = currFolderMetadataInfo!!.size
        while (i < cnt) {
            folderMetadataInfo = currFolderMetadataInfo.get(i)
            Log.i(LOC_CAT_TAG, "interwoven_02 - i/fstFileTitle/scndFileTitle-fileItemId/fileTitle: " + i + " : " + fstFileTitle + "/" + scndFileTitle + " - " + folderMetadataInfo.fileItemId + "/" + folderMetadataInfo.fileTitle)
            when (i) {
                0 -> Assert.assertEquals("i: $i", scndFileTitle, folderMetadataInfo.fileTitle)
                1 -> Assert.assertEquals("i: $i", fstFileTitle, folderMetadataInfo.fileTitle)

                else -> Assert.fail("no case for i: $i")
            }
            i++
        }
    }

    @Test
    fun testGetCurrFolderLevel() {
        val currFolderLevel = FoldersData.getCurrFolderLevel()
        val expectedMinCurrFolderLevel = -1
        Assert.assertTrue("currFolderLevel must be > : $currFolderLevel", currFolderLevel >= expectedMinCurrFolderLevel)
    }

    @Test
    fun testRefreshFolderData_NoFolderDataChange_030() {
        val fileParentFolderDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")    //addEmptyTopFolderDetails();
        val newFolderDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-abQ7YpR")
        val folderLevel = 0
        val newFolderTitle = "new folder"
        val elementsCnt = 3
        val titlePrefix = "folderFile"
        val foldersMetadatasInfo = buildFolderMetadataInfo(fileParentFolderDriveId, titlePrefix, elementsCnt)
        val folderData = FolderData(newFolderDriveId, newFolderTitle, folderLevel, fileParentFolderDriveId, true, 0, foldersMetadatasInfo)
        FoldersData.addFolderData(folderData)
        //        FoldersData.addFolderData(newFolderDriveId, newFolderTitle, folderLevel, fileParentFolderDriveId, 0, foldersMetadatasInfo);
        FoldersData.refreshFolderData(folderLevel, fileParentFolderDriveId, 0, foldersMetadatasInfo)
        // . . . .
        val currFolderMetadataInfo = FoldersData.getCurrFolderMetadataInfo()
        //		FolderMetadataInfo[] folderMetadataInfoArrayAfter = currFolderMetadataInfo.toArray(new FolderMetadataInfo[0]);
        var folderMetadataInfo: FileMetadataInfo
        var i = 0
        val cnt = currFolderMetadataInfo!!.size
        while (i < cnt) {
            folderMetadataInfo = currFolderMetadataInfo.get(i)
            Log.i(LOC_CAT_TAG, "refresh_030 - i/fileTitle: " + i + " : " + folderMetadataInfo.fileTitle)
            //			assertEquals("folderFile" + i, folderMetadataInfo.fileTitle);
            Assert.assertEquals("incorrect file fileTitle at idx: $i", foldersMetadatasInfo[i].fileTitle, folderMetadataInfo.fileTitle)
            i++
        }
    }

    @Test
    fun testRefreshFolderData_AfterFileRemovedFromFolder_031() {
        val fileParentFolderDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")    //addEmptyTopFolderDetails();
        val newFolderDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-abQ7YpR")
        val folderLevel = 0
        val newFolderTitle = "new folder"
        val elementsCnt = 3
        val titlePrefix = "folderFile"
        val foldersMetadatasInfo = buildFolderMetadataInfo(fileParentFolderDriveId, titlePrefix, elementsCnt)
        val folderData = FolderData(newFolderDriveId, newFolderTitle, folderLevel, fileParentFolderDriveId, true, 0, foldersMetadatasInfo)
        FoldersData.addFolderData(folderData)
        //        FoldersData.addFolderData(newFolderDriveId, newFolderTitle, folderLevel, fileParentFolderDriveId, 0, foldersMetadatasInfo);
        // remove element at index 1
        foldersMetadatasInfo.removeAt(1)
        FoldersData.refreshFolderData(folderLevel, fileParentFolderDriveId, 0, foldersMetadatasInfo)
        // . . . .
        val currFolderMetadataInfo = FoldersData.getCurrFolderMetadataInfo()
        //		FolderMetadataInfo[] folderMetadataInfoArrayAfter = currFolderMetadataInfo.toArray(new FolderMetadataInfo[0]);
        var folderMetadataInfo: FileMetadataInfo
        var i = 0
        val cnt = currFolderMetadataInfo!!.size
        while (i < cnt) {
            folderMetadataInfo = currFolderMetadataInfo.get(i)
            Log.i(LOC_CAT_TAG, "refresh_031 - i/fileTitle: " + i + " : " + folderMetadataInfo.fileTitle)
            Assert.assertEquals("incorrect file fileTitle at idx: $i", foldersMetadatasInfo[i].fileTitle, folderMetadataInfo.fileTitle)
            i++
        }
    }

//	public void testGetCurrFolderDriveId() {
//		fail("Not yet implemented");
//	}
//
//	public void testGetFolderDriveId() {
//		fail("Not yet implemented");
//	}
//
//	public void testGetCurrFolderTitle() {
//		fail("Not yet implemented");
//	}
//
//	public void testGetFolderTitle() {
//		fail("Not yet implemented");
//	}

//	public void testGetCurrFolderMetadataInfo() {
//		fail("Not yet implemented");
//	}

//	public void testGetCurrFoldersFilesList() {
//		fail("Not yet implemented");
//	}

    // - - - - - -  internal methods - - - -

    private fun addTopFolderDetailsWithOneFile(): DriveId {
        val folderLevel: Int
        val thisDriveId: DriveId
        val parentDriveId: DriveId?
        val trootFolderTitle: String
        val fileDriveId: DriveId
        val isFolder: Boolean?
        val mimeType: String

        // this should be OK
        folderLevel = -1            // folderLevel is ignored the first time the 'add is executes - it doesn't matter what value will be passed
        parentDriveId = DriveId.decodeFromString("DriveId:CAESHDBCN3VRMnJDUU0wZVFUV3R6VDFNeWVEWkhXbFUYxFUg-sbQ7YpR");
        thisDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        trootFolderTitle = "Root folder"
        fileDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        isFolder = true
        mimeType = "text/plain"
        val foldersMetadatasInfo = ArrayList<FileMetadataInfo>()
        val folderMetadataInfo = FileMetadataInfo("", trootFolderTitle, fileDriveId, isFolder, mimeType, Date(), Date(), System.currentTimeMillis(), true, false)
        foldersMetadatasInfo.add(folderMetadataInfo)
        val folderData = FolderData(thisDriveId, trootFolderTitle, folderLevel, parentDriveId, true, 0, foldersMetadatasInfo)
        FoldersData.addFolderData(folderData)
        //        FoldersData.addFolderData(thisDriveId, trootFolderTitle, folderLevel, parentDriveId, 0, foldersMetadatasInfo);
        val currFolderLevel = FoldersData.getCurrFolderLevel()
        val expectedCurrFolderLevel = 0
        Assert.assertEquals("incorrect currFolderLevel", expectedCurrFolderLevel, currFolderLevel)
        return thisDriveId
    }

    private fun addEmptyTopFolderDetails(): DriveId {
        val folderLevel: Int
        val thisDriveId: DriveId
        val parentDriveId: DriveId?
        val trootFolderTitle: String

        // this should be OK
        folderLevel = -1            // folderLevel is ignored the first time the 'add is executes - it doesn't matter what value will be passed
        parentDriveId = DriveId.decodeFromString("DriveId:CAESHDBCN3VRMnJDUU0wZVFUV3R6VDFNeWVEWkhXbFUYxFUg-sbQ7YpR");
        thisDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        trootFolderTitle = "Root folder"
        val foldersMetadatasInfo = ArrayList<FileMetadataInfo>()
        val folderData = FolderData(thisDriveId, trootFolderTitle, folderLevel, parentDriveId, true, 0, foldersMetadatasInfo)
        FoldersData.addFolderData(folderData)
        //        FoldersData.addFolderData(thisDriveId, trootFolderTitle, folderLevel, parentDriveId, 0, foldersMetadatasInfo);
        val currFolderLevel = FoldersData.getCurrFolderLevel()
        val expectedCurrFolderLevel = 0
        Assert.assertEquals("incorrect currFolderLevel", expectedCurrFolderLevel, currFolderLevel)
        return thisDriveId
    }

    private fun buildFolderMetadataInfo(fileParentFolderDriveId: DriveId, titlePrefix: String, elementsCnt: Int): ArrayList<FileMetadataInfo> {
        val foldersMetadatasInfo = ArrayList<FileMetadataInfo>()
        val fileDriveId: DriveId
        val isFolder: Boolean?
        val mimeType: String
        fileDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        isFolder = false
        mimeType = "text/plain"
        val fileItemId = System.currentTimeMillis()
        var folderMetadataInfo: FileMetadataInfo
        for (i in 0 until elementsCnt) {
            folderMetadataInfo = FileMetadataInfo("", titlePrefix + i, fileDriveId, isFolder, mimeType, Date(), Date(), fileItemId, true, false)
            foldersMetadatasInfo.add(folderMetadataInfo)
        }
        return foldersMetadatasInfo
    }

    private fun insertNewFileDetails(fileParentFolderDriveId: DriveId, title: String): Long {
        //		String fileTitle;
        val fileDriveId: DriveId
        val isFolder: Boolean?
        val mimeType: String
        fileDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        isFolder = false
        mimeType = "text/plain"
        // added sleep because in 'Interwoven' test 2 inserts were done so fast that they got same fileItemId and assert failed - it works fine now
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }

        val fileItemId = System.currentTimeMillis()
        val folderLevel = 0
        val position = 0

        val folderMetadataInfo = FileMetadataInfo("", title, fileDriveId, isFolder, mimeType, Date(), Date(), fileItemId, true, false)
        FoldersData.insertFolderItemView(fileItemId, folderLevel, fileParentFolderDriveId, position, folderMetadataInfo)
        return fileItemId
    }


    private fun verifyDataStructure() {
        val currFolderLevel = FoldersData.getCurrFolderLevel()
        val expectedContainerSize = currFolderLevel + 1
        val foldersDriveIdsListSize = FoldersData.getFoldersDriveIdsList().size
        Assert.assertEquals("foldersDriveIdsList size must be: $expectedContainerSize", expectedContainerSize, foldersDriveIdsListSize)
        val foldersTitlesListSize = FoldersData.getFoldersTitlesList().size
        Assert.assertEquals("foldersTitlesList size must be: $expectedContainerSize", expectedContainerSize, foldersTitlesListSize)
        val foldersMetadatasInfoListSize = FoldersData.getFoldersMetadatasInfoList().size
        Assert.assertEquals("foldersMetadatasInfoList size must be: $expectedContainerSize", expectedContainerSize, foldersMetadatasInfoListSize)
        val foldersFilesTitlesListSize = FoldersData.getFoldersFilesTitlesList().size
        Assert.assertEquals("foldersFilesTitlesList size must be: $expectedContainerSize", expectedContainerSize, foldersFilesTitlesListSize)
        val foldersFilesIdsMapListSize = FoldersData.getFoldersFilesIdsMapList().size
        Assert.assertEquals("FilesIdsMapList size must be: $expectedContainerSize", expectedContainerSize, foldersFilesIdsMapListSize)
    }
}