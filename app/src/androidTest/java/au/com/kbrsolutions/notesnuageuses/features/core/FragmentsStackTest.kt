package au.com.kbrsolutions.notesnuageuses.features.core

import android.util.Log
import au.com.kbrsolutions.notesnuageuses.features.main.HomeActivity
import com.google.android.gms.drive.DriveId
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.util.*

class FragmentsStackTest {

    private var fragmentStack: FragmentsStack? = null
    private var foldersData: FoldersData? = null

    private val TAG = FragmentsStackTest::class.java.simpleName

    @Before
    fun setUp() {
        foldersData = FoldersData
        fragmentStack = FragmentsStack
        foldersData!!.init()
        fragmentStack!!.init(true)
    }

    @After
    fun tearDown() {
        fun tearDown() {
            foldersData = null
            fragmentStack = null
        }
    }

    @Test
    fun init() {
    }

    @Test
    fun testInit() {
        Assert.assertTrue("fragmentStack can't be null", fragmentStack != null)
    }

    @Test
    fun testAddFragment() {
        val foldersAddData: FolderData? = null
        fragmentStack!!.addFragment(HomeActivity.FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", foldersAddData)

        assertEquals("wrong fragmentStack size", 1, fragmentStack?.getStackSize())
        assertEquals("wrong currFragment", HomeActivity.FragmentsEnum.ACTIVITY_LOG_FRAGMENT, fragmentStack?.getCurrFragment())
        assertEquals("wrong currFolderLevel", -1, foldersData?.getCurrFolderLevel())
    }

    @Test
    fun testRemoveTopFragment_fromEmptyStack() {
        val fragmentsStackResponse = fragmentStack?.removeTopFragment("testRemoveTopFragment_fromEmptyStack", false)
        assertEquals("wrong fragmentsStackResponse", null, fragmentsStackResponse)
    }

    /*
	 * ACTIVITY_LOG_FRAGMENT
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_00() {
        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        val foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        fragmentStack!!.addFragment(HomeActivity.FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", foldersAddData)
        assertEquals("wrong fragmentStack size", 1, fragmentStack!!.getStackSize())

        val actualFragmentsStackResponse = fragmentStack!!.removeTopFragment("testRemoveTopFragment_topFragmentFolder_00", false)
        Log.v(TAG, "testRemoveTopFragment_topFragmentFolder_00 - actualFragmentsStackResponse: " +
                actualFragmentsStackResponse)

        assertNotNull("actualFragmentsStackResponse cannot be null", actualFragmentsStackResponse)
        assertEquals("wrong fragmentStack size", 0, fragmentStack!!.getStackSize())
        assertEquals("wrong currFragment", HomeActivity.FragmentsEnum.NONE  , fragmentStack!!.getCurrFragment())
        assertEquals("wrong currFolderLevel", -1, foldersData!!.getCurrFolderLevel())
        val expectedFragmentsStackResponse = FragmentsStackResponse(true, HomeActivity.FragmentsEnum.NONE, null, false, false, true)
        val errMsg = validateFragmentsStackResponse(expectedFragmentsStackResponse, actualFragmentsStackResponse!!)
        Assert.assertEquals("wrong fragmentsStackResponse", null, errMsg)

    }

    @Test
    fun replaceCurrFragment() {
    }

    @Test
    fun addFragment() {
    }

    @Test
    fun removeTopFragment() {
    }

    // helper functions

    private fun getFoldersAddData(fileParentFolderDriveId: DriveId, folderLevel: Int): FolderData {
        val newFolderTitle: String
        //		String folderLevel;
        val newFolderDriveId: DriveId
        val isFolder: Boolean?
        val newFolderData: Boolean
        val mimeType: String
        val createDate: Date
        val updateDate: Date
        val trashedFilesCnt = 0

        newFolderTitle = "A"
        //		DriveId fileParentFolderDriveId = addTopFolderDetails();
        //		folderLevel = "File : xxxx encrypting";
        newFolderDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        isFolder = true
        mimeType = "text/plain"
        //		long fileItemId = System.currentTimeMillis();
        //		int folderLevel = 0;
        //		int position = 0;
        newFolderData = true

        createDate = Date()
        updateDate = Date()
        val foldersMetadatasInfo = ArrayList<FileMetadataInfo>()
        val folderMetadataInfo = FileMetadataInfo("", newFolderTitle, newFolderDriveId, isFolder, mimeType, createDate, updateDate, System.currentTimeMillis(), true, false)
        foldersMetadatasInfo.add(folderMetadataInfo)
        //		FoldersAddData foldersAddData = new FoldersAddData(newFolderDriveId, newFolderTitle, folderLevel, fileParentFolderDriveId, newFolderData, filesMetadatasInfo);
        return FolderData(newFolderDriveId, newFolderTitle, folderLevel, fileParentFolderDriveId, newFolderData, trashedFilesCnt, foldersMetadatasInfo)
    }

    private fun addTopFolderDetails(): DriveId {
        val folderLevel: Int
        val thisDriveId: DriveId
        val parentDriveId: DriveId?
        val title: String
        val fileDriveId: DriveId
        val isFolder: Boolean?
        val mimeType: String
        val createDate: Date
        val updateDate: Date

        // this should be OK
        folderLevel = -1            // folderLevel is ignored the first time the 'add is executes - it doesn't matter what value will be passed
        parentDriveId = null        //DriveId.decodeFromString("DriveId:CAESHDBCN3VRMnJDUU0wZVFUV3R6VDFNeWVEWkhXbFUYxFUg-sbQ7YpR");
        thisDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        title = "Title"
        fileDriveId = DriveId.decodeFromString("DriveId:CAESBHJvb3QYpFUg-sbQ7YpR")
        isFolder = true
        mimeType = "text/plain"
        createDate = Date()
        updateDate = Date()
        val foldersMetadatasInfo = ArrayList<FileMetadataInfo>()
        val folderMetadataInfo = FileMetadataInfo("", title, fileDriveId, isFolder, mimeType, createDate, updateDate, System.currentTimeMillis(), true, false)
        foldersMetadatasInfo.add(folderMetadataInfo)
        return thisDriveId
    }

    private fun validateFragmentsStackResponse(expectedFragmentsStackResponse: FragmentsStackResponse, actualFragmentsStackResponse: FragmentsStackResponse): String? {
        val errMsg = StringBuilder()
        if (!expectedFragmentsStackResponse.equals(actualFragmentsStackResponse)) {
            if (expectedFragmentsStackResponse.finishRequired !== actualFragmentsStackResponse.finishRequired) {
                errMsg.append("0   - finishRequired: " + expectedFragmentsStackResponse.finishRequired + ": " + actualFragmentsStackResponse.finishRequired + "\n")
            }
            if (expectedFragmentsStackResponse.fragmentToSet !== actualFragmentsStackResponse.fragmentToSet) {
                errMsg.append("1   - fragmentToSet: " + expectedFragmentsStackResponse.fragmentToSet + " :" + actualFragmentsStackResponse.fragmentToSet + "\n")
            }
            if (expectedFragmentsStackResponse.titleToSet !== actualFragmentsStackResponse.titleToSet) {
                errMsg.append("2   - titleToSet: " + expectedFragmentsStackResponse.titleToSet + ": " + actualFragmentsStackResponse.titleToSet + "\n")
            }
            if (expectedFragmentsStackResponse.updateFolderListAdapterRequired !== actualFragmentsStackResponse.updateFolderListAdapterRequired) {
                errMsg.append("3   - updateFolderListAdapterRequired: " + expectedFragmentsStackResponse.updateFolderListAdapterRequired + ": " + actualFragmentsStackResponse.updateFolderListAdapterRequired + "\n")
            }
            if (expectedFragmentsStackResponse.viewFragmentsCleanupRequired !== actualFragmentsStackResponse.viewFragmentsCleanupRequired) {
                errMsg.append("4   - viewFragmentsCleanupRequired: " + expectedFragmentsStackResponse.viewFragmentsCleanupRequired + ": " + actualFragmentsStackResponse.viewFragmentsCleanupRequired + "\n")
            }
            if (expectedFragmentsStackResponse.menuOptionsChangeRequired !== actualFragmentsStackResponse.menuOptionsChangeRequired) {
                errMsg.append("5   - menuOptionsChangeRequired: " + expectedFragmentsStackResponse.menuOptionsChangeRequired + ": " + actualFragmentsStackResponse.menuOptionsChangeRequired + "\n")
            }
        }
        return if (errMsg.length == 0) null else "\nidx - field : expected : actual\n" + errMsg.toString()
    }
}