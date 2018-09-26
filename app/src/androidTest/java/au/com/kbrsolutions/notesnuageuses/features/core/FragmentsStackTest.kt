package au.com.kbrsolutions.notesnuageuses.features.core

import android.util.Log
import au.com.kbrsolutions.notesnuageuses.features.main.HomeActivity
import au.com.kbrsolutions.notesnuageuses.features.main.HomeActivity.FragmentsEnum
import com.google.android.gms.drive.DriveId
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.util.*

class FragmentsStackTest {

//    private val TAG = FragmentsStackTest::class.java.simpleName

    @Before
    fun setUp() {
        FragmentsStack.initialize(true)
        FoldersData.init()
    }

    @After
    fun tearDown() {
    }

    @Test
    fun testInit() {
    }

    @Test
    fun testAddFragment_notFolder() {

        val foldersAddData: FolderData? = null
        FragmentsStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", foldersAddData)

        assertEquals("wrong testFragmentStack size", 1, FragmentsStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.ACTIVITY_LOG_FRAGMENT, FragmentsStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", -1, FoldersData.getCurrFolderLevel())
    }

    @Test
    fun testAddFragment_folder() {

        var folderLevel = -1
        val fileParentFolderDriveId = addTopFolderDetails()

        val foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        FragmentsStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder", foldersAddData)

        assertEquals("wrong testFragmentStack size", 1, FragmentsStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.FOLDER_FRAGMENT, FragmentsStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 0, FoldersData.getCurrFolderLevel())
    }

    @Test
    fun testRemoveTopFragment_just_one_top_IsFolder() {

        assertEquals("wrong currFolderLevel", -1, FoldersData.getCurrFolderLevel())
        testAddFragment_folder()
        assertEquals("wrong currFolderLevel", 0, FoldersData.getCurrFolderLevel())

        val fragmentsStackResponse = FragmentsStack.removeTopFragment(
                "testRemoveTopFragment_fromEmptyStack", false)

        assertEquals("wrong testFragmentStack size", 0, FragmentsStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.NONE, FragmentsStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", -1, FoldersData.getCurrFolderLevel())
        assertEquals("wrong fragmentsStackResponse", true, fragmentsStackResponse.finishRequired)
    }

    @Test
    fun testRemoveTopFragment_fromEmptyStack() {

        val fragmentsStackResponse = FragmentsStack.removeTopFragment(
                "testRemoveTopFragment_fromEmptyStack", false)
        assertEquals("wrong fragmentsStackResponse", true, fragmentsStackResponse.finishRequired)
    }

    /*
	 * ACTIVITY_LOG_FRAGMENT
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_00() {

        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        val foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        FragmentsStack.addFragment(
                FragmentsEnum.ACTIVITY_LOG_FRAGMENT,
                "Activity log", foldersAddData)
        assertEquals(
                "wrong testFragmentStack size", 1, FragmentsStack.getStackSize())

        val actualFragmentsStackResponse = FragmentsStack.removeTopFragment(
                "testRemoveTopFragment_topFragmentFolder_00", false)

//        Log.v(TAG, "testRemoveTopFragment_topFragmentFolder_00 - actualFragmentsStackResponse: " +
//                actualFragmentsStackResponse)

        assertNotNull("actualFragmentsStackResponse cannot be null",
                actualFragmentsStackResponse)
        assertEquals("wrong testFragmentStack size", 0,
                FragmentsStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.NONE ,
                FragmentsStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", -1,
                FoldersData.getCurrFolderLevel())
        val expectedFragmentsStackResponse = FragmentsStackResponse(
                true,
                FragmentsEnum.NONE,
                null,
                false,
                false,
                true)
        val errMsg = validateFragmentsStackResponse(expectedFragmentsStackResponse,
                actualFragmentsStackResponse!!)
        Assert.assertEquals("wrong fragmentsStackResponse", null, errMsg)
    }

    /*
	 *		ACTIVITY_LOG_FRAGMENT, DOWNLOAD_FRAGMENT, FOLDER_FRAGMENT
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_01() {

        Log.v(TAG, " - testRemoveTopFragment_topFragmentFolder_01 start");
        var folderLevel = -1
//        val folderName = "Folder-"

        val fileParentFolderDriveId = addTopFolderDetails()
        var foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        FragmentsStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log",
                foldersAddData)

        FragmentsStack.addFragment(FragmentsEnum.DOWNLOAD_FRAGMENT,
                "Progress", null)

        foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        FragmentsStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder",
                foldersAddData)

        assertEquals("wrong testFragmentStack size", 3,
                FragmentsStack.getStackSize())

        val actualFragmentsStackResponse = FragmentsStack.removeTopFragment(
                "testRemoveTopFragment_topFragmentFolder_01", false)

        Log.v(TAG, "testRemoveTopFragment_topFragmentFolder_00 - actualFragmentsStackResponse: " +
                actualFragmentsStackResponse)

        assertEquals(
                "wrong testFragmentStack size", 0, FragmentsStack.getStackSize())
        assertEquals(
                "wrong currFragment",
                FragmentsEnum.NONE,
                FragmentsStack.getCurrFragment())
        assertEquals(
                "wrong currFolderLevel", -1, FoldersData.getCurrFolderLevel())
        val expectedFragmentsStackResponse = FragmentsStackResponse(
                true,
                FragmentsEnum.NONE,
                null,
                false,
                false,
                true)
        val errMsg = validateFragmentsStackResponse(
                expectedFragmentsStackResponse, actualFragmentsStackResponse)
        Assert.assertEquals("wrong fragmentsStackResponse", null, errMsg)
    }

    /*
	 * 		ACTIVITY_LOG_FRAGMENT, DOWNLOAD_FRAGMENT, FOLDER_FRAGMENT, DOWNLOAD_FRAGMENT, FOLDER_FRAGMENT
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_02() {
        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        FragmentsStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log",
                null)
        FragmentsStack.addFragment(FragmentsEnum.DOWNLOAD_FRAGMENT,
                "Folder",
                null)
        var foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        FragmentsStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder",
                foldersAddData)
        FragmentsStack.addFragment(FragmentsEnum.DOWNLOAD_FRAGMENT,
                "Folder",
                null)
        foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        FragmentsStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder1",
                foldersAddData)
        assertEquals("wrong FragmentsStack size", 5, FragmentsStack.getStackSize())
        assertEquals("wrong currFolderLevel", 1, FoldersData.getCurrFolderLevel())

        val actualFragmentsStackResponse = FragmentsStack.removeTopFragment(
                "testRemoveTopFragment_topFragmentFolder_02",
                false)

        assertEquals("wrong FragmentsStack size", 3, FragmentsStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.FOLDER_FRAGMENT,
                FragmentsStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 0, FoldersData.getCurrFolderLevel())
        val expectedFragmentsStackResponse = FragmentsStackResponse(
                false,
                FragmentsEnum.NONE,
                "A",
                true,
                false,
                true)
        val errMsg = validateFragmentsStackResponse(expectedFragmentsStackResponse,
                actualFragmentsStackResponse)
        Assert.assertEquals("wrong fragmentsStackResponse", null, errMsg)
    }

    /*
	 * 		DOWNLOAD_FRAGMENT,
	 * 		FOLDER_FRAGMENT,
	 * 		DOWNLOAD_FRAGMENT,
	 * 		FOLDER_FRAGMENT
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_03() {
        var folderLevel = -1
        val folderName = "Folder-"

        val fileParentFolderDriveId = addTopFolderDetails()
        FragmentsStack.addFragment(FragmentsEnum.DOWNLOAD_FRAGMENT,
                "Progress",
                null)

        val titleToSetAfterTopRemoved = folderName + folderLevel
        var foldersAddData = getFoldersAddData(
                fileParentFolderDriveId,
                folderLevel,
                titleToSetAfterTopRemoved)
        FragmentsStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT,
                titleToSetAfterTopRemoved,
                foldersAddData)

        FragmentsStack.addFragment(FragmentsEnum.DOWNLOAD_FRAGMENT,
                "Progress",
                null)

        folderLevel++
        foldersAddData = getFoldersAddData(
                fileParentFolderDriveId,
                folderLevel,
                folderName + folderLevel)
        FragmentsStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT,
                folderName + folderLevel,
                foldersAddData)

        assertEquals("wrong FragmentsStack size", 4, FragmentsStack.getStackSize())
        assertEquals("wrong currFolderLevel", 1, FoldersData.getCurrFolderLevel())

        Log.v("FragmentsStackTest", "testRemoveTopFragment_topFragmentFolder_03 - FragmentsStack: $FragmentsStack ")
        var actualStackFragmentsAfterAdd = FragmentsStack.getFragmentsList()
        Log.v(TAG, " - actualStackFragmentsAfterAdd: ${printCollection("after fragments added  ", actualStackFragmentsAfterAdd)}")

        val actualFragmentsStackResponse = FragmentsStack.removeTopFragment(
                "testRemoveTopFragment_topFragmentFolder_02",
                false)

        actualStackFragmentsAfterAdd = FragmentsStack.getFragmentsList()
        Log.v(TAG, " - actualStackFragmentsAfterAdd: ${printCollection("after fragments removed", actualStackFragmentsAfterAdd)}")

        assertEquals("wrong FragmentsStack size", 2, FragmentsStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.FOLDER_FRAGMENT,
                FragmentsStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 0, FoldersData.getCurrFolderLevel())
        val expectedFragmentsStackResponse = FragmentsStackResponse(
                false,
                FragmentsEnum.NONE,
                "A",
                true,
                false,
                true)
        val errMsg = validateFragmentsStackResponse(expectedFragmentsStackResponse,
                actualFragmentsStackResponse)
        Assert.assertEquals("wrong fragmentsStackResponse", titleToSetAfterTopRemoved, actualFragmentsStackResponse.titleToSet)
    }

    /* Creating text note in non empty folder - after CREATE_FILE_FRAGMENT set, FILE_FRAGMENT is set.
	 * Either user clicked on the Back button or in 'file name and password' dialog clicked on the Cancel button.
	 *
	 *		ACTIVITY_LOG_FRAGMENT, DOWNLOAD_FRAGMENT, FOLDER_FRAGMENT, DOWNLOAD_FRAGMENT, FOLDER_FRAGMENT, CREATE_FILE_FRAGMENT, FILE_FRAGMENT
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_04_create_text_file_in_non_empty_folder_Cancel_clicked() {
        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        FragmentsStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        FragmentsStack.addFragment(FragmentsEnum.DOWNLOAD_FRAGMENT, "progress folder", null)
        var foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        FragmentsStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder", foldersAddData)
        FragmentsStack.addFragment(FragmentsEnum.DOWNLOAD_FRAGMENT, "progress folder", null)
        foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        FragmentsStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder1", foldersAddData)
        //		FragmentsStack.addFragment(HomeActivity.FragmentsEnum.CREATE_FILE_FRAGMENT, "Create file", foldersAddData);
        FragmentsStack.addFragment(FragmentsEnum.FILE_FRAGMENT, "image", foldersAddData)
        assertEquals("wrong FragmentsStack size", 6, FragmentsStack.getStackSize())

        val actionCancelled = true
        Log.i(TAG, "@#testRemoveTopFragment_topFragmentFolder_03 - before removeTopFragment")
        val actualFragmentsStackResponse = FragmentsStack.removeTopFragment("testRemoveTopFragment_topFragmentFolder_03", actionCancelled)
        Log.i(TAG, "@#testRemoveTopFragment_topFragmentFolder_03 - after  removeTopFragment")

        assertEquals("wrong FragmentsStack size", 5, FragmentsStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.FOLDER_FRAGMENT, FragmentsStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 1, FoldersData.getCurrFolderLevel())
        val expectedFragmentsStackResponse = FragmentsStackResponse(false, FragmentsEnum.FOLDER_FRAGMENT, "A", true, true, true)
        val errMsg = validateFragmentsStackResponse(expectedFragmentsStackResponse, actualFragmentsStackResponse)
        Assert.assertEquals("wrong fragmentsStackResponse", null, errMsg)
    }

    /* Creating text note in non empty folder - after CREATE_FILE_FRAGMENT set, FILE_FRAGMENT is set.
	 * Either user clicked on the Back button or in 'file name and password' dialog clicked on the Cancel button.
	 *      DOWNLOAD_FRAGMENT,
	 *      FOLDER_FRAGMENT,
	 *      DOWNLOAD_FRAGMENT,
	 *      FILE_FRAGMENT
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_041_create_text_file_in_non_empty_folder_Cancel_clicked() {
        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        FragmentsStack.addFragment(FragmentsEnum.DOWNLOAD_FRAGMENT, "progress folder", null)
        var foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        FragmentsStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder", foldersAddData)
        FragmentsStack.addFragment(FragmentsEnum.DOWNLOAD_FRAGMENT, "progress folder", null)
        FragmentsStack.addFragment(FragmentsEnum.FILE_FRAGMENT, "image", foldersAddData)
        assertEquals("wrong FragmentsStack size", 4, FragmentsStack.getStackSize())

        val actionCancelled = true
        Log.i(TAG, "@#testRemoveTopFragment_topFragmentFolder_041_create_text_file_in_non_empty_folder_Cancel_clicked - before removeTopFragment")
        val actualFragmentsStackResponse = FragmentsStack.removeTopFragment("testRemoveTopFragment_topFragmentFolder_03", actionCancelled)
        Log.i(TAG, "@#testRemoveTopFragment_topFragmentFolder_041_create_text_file_in_non_empty_folder_Cancel_clicked - after  removeTopFragment")

        assertEquals("wrong FragmentsStack size", 2, FragmentsStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.FOLDER_FRAGMENT, FragmentsStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 0, FoldersData.getCurrFolderLevel())
        val expectedFragmentsStackResponse = FragmentsStackResponse(
                false,
                FragmentsEnum.FOLDER_FRAGMENT,
                "A",
                false,
                true,
                true)
        val errMsg = validateFragmentsStackResponse(expectedFragmentsStackResponse, actualFragmentsStackResponse)
        Assert.assertEquals("wrong fragmentsStackResponse", null, errMsg)
    }

    /* Creating text note in non empty folder - after CREATE_FILE_FRAGMENT set, FILE_FRAGMENT is set.
	 * In 'file name and password' dialog clicked on the Save button.
	 *
	 *		ACTIVITY_LOG_FRAGMENT, DOWNLOAD_FRAGMENT, FOLDER_FRAGMENT, DOWNLOAD_FRAGMENT, FOLDER_FRAGMENT, CREATE_FILE_FRAGMENT, FILE_FRAGMENT
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_04_create_text_file_in_non_empty_folder_Save_clicked() {
        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        FragmentsStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        FragmentsStack.addFragment(FragmentsEnum.DOWNLOAD_FRAGMENT, "progress folder", null)
        var foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        FragmentsStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder", foldersAddData)
        FragmentsStack.addFragment(FragmentsEnum.DOWNLOAD_FRAGMENT, "progress folder", null)
        foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        FragmentsStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder1", foldersAddData)
        //		FragmentsStack.addFragment(HomeActivity.FragmentsEnum.CREATE_FILE_FRAGMENT, "Create file", foldersAddData);
        FragmentsStack.addFragment(FragmentsEnum.FILE_FRAGMENT, "image", foldersAddData)
        assertEquals("wrong FragmentsStack size", 6, FragmentsStack.getStackSize())

        val actionCancelled = false
        Log.i(TAG, "@#testRemoveTopFragment_topFragmentFolder_03 - before removeTopFragment")
        val actualFragmentsStackResponse = FragmentsStack.removeTopFragment("testRemoveTopFragment_topFragmentFolder_03", actionCancelled)
        Log.i(TAG, "@#testRemoveTopFragment_topFragmentFolder_03 - after  removeTopFragment")

        assertEquals("wrong FragmentsStack size", 5, FragmentsStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.FOLDER_FRAGMENT, FragmentsStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 1, FoldersData.getCurrFolderLevel())
        val expectedFragmentsStackResponse = FragmentsStackResponse(false, FragmentsEnum.FOLDER_FRAGMENT, "A", true, true, true)
        val errMsg = validateFragmentsStackResponse(expectedFragmentsStackResponse, actualFragmentsStackResponse)
        Assert.assertEquals("wrong fragmentsStackResponse", null, errMsg)
    }

    /*Creating text note in the empty folder - after CREATE_FILE_FRAGMENT set, FILE_FRAGMENT is set.
	 * Either user clicked on the Back button or in 'file name and password' dialog clicked on the Cancel button.
	 *
	 *		ACTIVITY_LOG_FRAGMENT, DOWNLOAD_FRAGMENT, FOLDER_FRAGMENT, DOWNLOAD_FRAGMENT, EMPTY_FOLDER_FRAGMENT, CREATE_FILE_FRAGMENT, FILE_FRAGMENT
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_04_create_text_file_in_empty_folder_Cancel_clicked() {
        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        FragmentsStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        FragmentsStack.addFragment(FragmentsEnum.DOWNLOAD_FRAGMENT, "Folder", null)
        var foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        FragmentsStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder", foldersAddData)
        FragmentsStack.addFragment(FragmentsEnum.DOWNLOAD_FRAGMENT, "Folder", null)
        foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        FragmentsStack.addFragment(FragmentsEnum.EMPTY_FOLDER_FRAGMENT, "Folder1", foldersAddData)
        //		FragmentsStack.addFragment(HomeActivity.FragmentsEnum.CREATE_FILE_FRAGMENT, "Create file", foldersAddData);
        FragmentsStack.addFragment(FragmentsEnum.FILE_FRAGMENT, "image", foldersAddData)
        assertEquals("wrong FragmentsStack size", 6, FragmentsStack.getStackSize())

        val actionCancelled = true
        val actualFragmentsStackResponse = FragmentsStack.removeTopFragment("testRemoveTopFragment_topFragmentFolder_03", actionCancelled)

        assertEquals("wrong FragmentsStack size", 5, FragmentsStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.EMPTY_FOLDER_FRAGMENT, FragmentsStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 1, FoldersData.getCurrFolderLevel())
        val expectedFragmentsStackResponse = FragmentsStackResponse(false, FragmentsEnum.EMPTY_FOLDER_FRAGMENT, "A", true, true, true)
        val errMsg = validateFragmentsStackResponse(expectedFragmentsStackResponse, actualFragmentsStackResponse)
        Assert.assertEquals("wrong fragmentsStackResponse", null, errMsg)
    }

    // [FOLDER_FRAGMENT, DOWNLOAD_FRAGMENT, FOLDER_FRAGMENT, IMAGE_VIEW_FRAGMENT]

    /*
	 * Either user clicked on the Cancel or Back button in Image view screen.
	 *
	 *		ACTIVITY_LOG_FRAGMENT, DOWNLOAD_FRAGMENT, FOLDER_FRAGMENT, IMAGE_VIEW_FRAGMENT
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_04_picture_view_in_non_empty_folder_Cancel_clicked() {
        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        FragmentsStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        FragmentsStack.addFragment(FragmentsEnum.DOWNLOAD_FRAGMENT, "Folder", null)
        val foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        FragmentsStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder", foldersAddData)
        FragmentsStack.addFragment(FragmentsEnum.IMAGE_VIEW_FRAGMENT, "image", foldersAddData)
        assertEquals("wrong FragmentsStack size", 4, FragmentsStack.getStackSize())

        val actionCancelled = true
        val actualFragmentsStackResponse = FragmentsStack.removeTopFragment("testRemoveTopFragment_topFragmentFolder_03", actionCancelled)

        assertEquals("wrong FragmentsStack size", 3, FragmentsStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.FOLDER_FRAGMENT, FragmentsStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 0, FoldersData.getCurrFolderLevel())
        val expectedFragmentsStackResponse = FragmentsStackResponse(
                false,
                FragmentsEnum.FOLDER_FRAGMENT,
                "A",
                true,
                true,
                true)
        val errMsg = validateFragmentsStackResponse(expectedFragmentsStackResponse, actualFragmentsStackResponse)
        Assert.assertEquals("wrong fragmentsStackResponse", null, errMsg)
    }

    /* Creating text note in the empty folder - after CREATE_FILE_FRAGMENT set, FILE_FRAGMENT is set.
	 * In 'file name and password' dialog clicked on the Save button.
	 *
	 * add ACTIVITY_LOG_FRAGMENT, DOWNLOAD_FRAGMENT, FOLDER_FRAGMENT, DOWNLOAD_FRAGMENT, EMPTY_FOLDER_FRAGMENT, CREATE_FILE_FRAGMENT, FILE_FRAGMENT
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_04_create_text_file_in_empty_folder_Save_clicked() {
        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        FragmentsStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        FragmentsStack.addFragment(FragmentsEnum.DOWNLOAD_FRAGMENT, "Folder", null)
        var foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        FragmentsStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder", foldersAddData)
        FragmentsStack.addFragment(FragmentsEnum.DOWNLOAD_FRAGMENT, "Folder", null)
        foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        FragmentsStack.addFragment(FragmentsEnum.EMPTY_FOLDER_FRAGMENT, "Folder1", foldersAddData)
        //		FragmentsStack.addFragment(HomeActivity.FragmentsEnum.CREATE_FILE_FRAGMENT, "Create file", foldersAddData);
        FragmentsStack.addFragment(FragmentsEnum.FILE_FRAGMENT, "image", foldersAddData)
        assertEquals("wrong FragmentsStack size", 6, FragmentsStack.getStackSize())
        val actualStackFragmentsAfterAdd = FragmentsStack.getFragmentsList()
//        printCollection("after fragments added", actualStackFragmentsAfterAdd)
        Log.v(TAG, " - actualStackFragmentsAfterAdd: ${printCollection("after fragments added", actualStackFragmentsAfterAdd)}")

        val actionCancelled = false
        val actualFragmentsStackResponse = FragmentsStack.removeTopFragment("testRemoveTopFragment_topFragmentFolder_03", actionCancelled)

        val expectedStackFragments = arrayOf(
                FragmentsEnum.ACTIVITY_LOG_FRAGMENT,
                FragmentsEnum.DOWNLOAD_FRAGMENT,
                FragmentsEnum.FOLDER_FRAGMENT,
                FragmentsEnum.DOWNLOAD_FRAGMENT,
                FragmentsEnum.FOLDER_FRAGMENT)
        Log.v(TAG, " - expectedStackFragments: ${printCollection("expected after removeTopFragment", expectedStackFragments)}")
        val actualStackFragments = FragmentsStack.getFragmentsList()
        Log.v(TAG, " - actualStackFragments: ${printCollection("actual after removeTopFragment", actualStackFragments)}")
        val match = verifyFoldersStack(expectedStackFragments, actualStackFragments)

        Assert.assertTrue("wrong fragments on stack", match)
        assertEquals("wrong FragmentsStack size", 5, FragmentsStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.FOLDER_FRAGMENT, FragmentsStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 1, FoldersData.getCurrFolderLevel())
        val expectedFragmentsStackResponse = FragmentsStackResponse(
                false,
                FragmentsEnum.FOLDER_FRAGMENT,
                "Folder",
                true,
                true,
                true)
        val errMsg = validateFragmentsStackResponse(expectedFragmentsStackResponse, actualFragmentsStackResponse)
        Assert.assertEquals("wrong fragmentsStackResponse", null, errMsg)
    }

    /* Creating text note in the empty folder - after CREATE_FILE_FRAGMENT set, FILE_FRAGMENT is set and through Navigation Door the Activity log is shown.
	 * In 'file name and password' dialog clicked on the Save button.
	 *
	 * add ACTIVITY_LOG_FRAGMENT, DOWNLOAD_FRAGMENT, FOLDER_FRAGMENT, CREATE_FILE_FRAGMENT, FILE_FRAGMENT, ACTIVITY_LOG_FRAGMENT
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_05_new_text_file_opened_and_switched_to_activity_log() {
        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        FragmentsStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        FragmentsStack.addFragment(FragmentsEnum.DOWNLOAD_FRAGMENT, "Folder", null)
        val foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        FragmentsStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder", foldersAddData)
        //		FragmentsStack.addFragment(HomeActivity.FragmentsEnum.CREATE_FILE_FRAGMENT, "Create file", foldersAddData);
        FragmentsStack.addFragment(FragmentsEnum.FILE_FRAGMENT, "text", foldersAddData)
        FragmentsStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        assertEquals("wrong FragmentsStack size", 5, FragmentsStack.getStackSize())

        val actionCancelled = false
        val actualFragmentsStackResponse = FragmentsStack.removeTopFragment("testRemoveTopFragment_topFragmentFolder_03", actionCancelled)

        val expectedStackFragments = arrayOf(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, FragmentsEnum.DOWNLOAD_FRAGMENT, FragmentsEnum.FOLDER_FRAGMENT,
                //				FragmentsEnum.CREATE_FILE_FRAGMENT,
                FragmentsEnum.FILE_FRAGMENT)
        val actualStackFragments = FragmentsStack.getFragmentsList()
        val match = verifyFoldersStack(expectedStackFragments, actualStackFragments)

        Assert.assertTrue("wrong fragments on stack", match)
        assertEquals("wrong FragmentsStack size", 4, FragmentsStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.FILE_FRAGMENT, FragmentsStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 0, FoldersData.getCurrFolderLevel())
        val expectedFragmentsStackResponse = FragmentsStackResponse(false, FragmentsEnum.FILE_FRAGMENT, "text", false, false, true)
        val errMsg = validateFragmentsStackResponse(expectedFragmentsStackResponse, actualFragmentsStackResponse)
        Assert.assertEquals("wrong fragmentsStackResponse", null, errMsg)
    }

    /* Creating text note in the empty folder - after CREATE_FILE_FRAGMENT set, IMAGE_VIEW_FRAGMENT is set and through Navigation Door the Activity log is shown.
	 * In 'file name and password' dialog clicked on the Save button.
	 *
	 * add ACTIVITY_LOG_FRAGMENT, DOWNLOAD_FRAGMENT, FOLDER_FRAGMENT, CREATE_FILE_FRAGMENT, IMAGE_VIEW_FRAGMENT, ACTIVITY_LOG_FRAGMENT
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_06_photo_file_opened_and_switched_to_activity_log() {
        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        FragmentsStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        FragmentsStack.addFragment(FragmentsEnum.DOWNLOAD_FRAGMENT, "Folder", null)
        val foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        FragmentsStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder", foldersAddData)
        //		FragmentsStack.addFragment(HomeActivity.FragmentsEnum.CREATE_FILE_FRAGMENT, "Create file", foldersAddData);
        FragmentsStack.addFragment(FragmentsEnum.IMAGE_VIEW_FRAGMENT, "text", foldersAddData)
        FragmentsStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        assertEquals("wrong FragmentsStack size", 5, FragmentsStack.getStackSize())

        val actionCancelled = false
        val actualFragmentsStackResponse = FragmentsStack.removeTopFragment("testRemoveTopFragment_topFragmentFolder_03", actionCancelled)

        val expectedStackFragments = arrayOf(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, FragmentsEnum.DOWNLOAD_FRAGMENT, FragmentsEnum.FOLDER_FRAGMENT,
                //				FragmentsEnum.CREATE_FILE_FRAGMENT,
                FragmentsEnum.IMAGE_VIEW_FRAGMENT)
        val actualStackFragments = FragmentsStack.getFragmentsList()
        val match = verifyFoldersStack(expectedStackFragments, actualStackFragments)

        Assert.assertTrue("wrong fragments on stack", match)
        assertEquals("wrong FragmentsStack size", 4, FragmentsStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.IMAGE_VIEW_FRAGMENT, FragmentsStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 0, FoldersData.getCurrFolderLevel())
        val expectedFragmentsStackResponse = FragmentsStackResponse(false, FragmentsEnum.IMAGE_VIEW_FRAGMENT, "text", false, false, true)
        val errMsg = validateFragmentsStackResponse(expectedFragmentsStackResponse, actualFragmentsStackResponse)
        Assert.assertEquals("wrong fragmentsStackResponse", null, errMsg)
    }

    /*
	 * add ACTIVITY_LOG_FRAGMENT, DOWNLOAD_FRAGMENT, FOLDER_FRAGMENT, LEGAL_NOTICES
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_07_legal_notices_top_folder_below() {
        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        FragmentsStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        FragmentsStack.addFragment(FragmentsEnum.DOWNLOAD_FRAGMENT, "Folder", null)
        val foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        FragmentsStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder", foldersAddData)
        FragmentsStack.addFragment(FragmentsEnum.LEGAL_NOTICES, "Activity log", null)
        assertEquals("wrong FragmentsStack size", 4, FragmentsStack.getStackSize())

        val actionCancelled = false
        val actualFragmentsStackResponse = FragmentsStack.removeTopFragment("testRemoveTopFragment_topFragmentFolder_03", actionCancelled)

        val expectedStackFragments = arrayOf(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, FragmentsEnum.DOWNLOAD_FRAGMENT, FragmentsEnum.FOLDER_FRAGMENT)
        val actualStackFragments = FragmentsStack.getFragmentsList()
        val match = verifyFoldersStack(expectedStackFragments, actualStackFragments)

        Assert.assertTrue("wrong fragments on stack", match)
        assertEquals("wrong FragmentsStack size", 3, FragmentsStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.FOLDER_FRAGMENT, FragmentsStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 0, FoldersData.getCurrFolderLevel())
        val expectedFragmentsStackResponse = FragmentsStackResponse(false, FragmentsEnum.FOLDER_FRAGMENT, "A", false, false, true)
        val errMsg = validateFragmentsStackResponse(expectedFragmentsStackResponse, actualFragmentsStackResponse)
        Assert.assertEquals("wrong fragmentsStackResponse", null, errMsg)
    }

    /*
	 * add ACTIVITY_LOG_FRAGMENT, DOWNLOAD_FRAGMENT, EMPTY_FOLDER_FRAGMENT, LEGAL_NOTICES
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_07_legal_notices_top_empty_folder_below() {
        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        FragmentsStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        FragmentsStack.addFragment(FragmentsEnum.DOWNLOAD_FRAGMENT, "Folder", null)
        var foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        FragmentsStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder", foldersAddData)
        FragmentsStack.addFragment(FragmentsEnum.DOWNLOAD_FRAGMENT, "Folder", null)
        foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        FragmentsStack.addFragment(FragmentsEnum.EMPTY_FOLDER_FRAGMENT, "Folder", foldersAddData)
        FragmentsStack.addFragment(FragmentsEnum.LEGAL_NOTICES, "Activity log", null)
        assertEquals("wrong FragmentsStack size", 6, FragmentsStack.getStackSize())

        val actionCancelled = false
        val actualFragmentsStackResponse = FragmentsStack.removeTopFragment("testRemoveTopFragment_topFragmentFolder_03", actionCancelled)

        val expectedStackFragments = arrayOf(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, FragmentsEnum.DOWNLOAD_FRAGMENT, FragmentsEnum.FOLDER_FRAGMENT, FragmentsEnum.DOWNLOAD_FRAGMENT, FragmentsEnum.EMPTY_FOLDER_FRAGMENT)
        val actualStackFragments = FragmentsStack.getFragmentsList()
        val match = verifyFoldersStack(expectedStackFragments, actualStackFragments)

        Assert.assertTrue("wrong fragments on stack", match)
        assertEquals("wrong FragmentsStack size", 5, FragmentsStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.EMPTY_FOLDER_FRAGMENT, FragmentsStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 1, FoldersData.getCurrFolderLevel())
        val expectedFragmentsStackResponse = FragmentsStackResponse(false, FragmentsEnum.EMPTY_FOLDER_FRAGMENT, "A", false, false, true)
        val errMsg = validateFragmentsStackResponse(expectedFragmentsStackResponse, actualFragmentsStackResponse)
        Assert.assertEquals("wrong fragmentsStackResponse", null, errMsg)
    }

    /*
	 * add FOLDER_FRAGMENT, FILE_FRAGMENT, LEGAL_NOTICES
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_07_legal_notices_top_text_note_below() {
        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        FragmentsStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        FragmentsStack.addFragment(FragmentsEnum.DOWNLOAD_FRAGMENT, "Folder", null)
        val foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        FragmentsStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder", foldersAddData)
        //		FragmentsStack.addFragment(HomeActivity.FragmentsEnum.CREATE_FILE_FRAGMENT, "Create file", foldersAddData);
        FragmentsStack.addFragment(FragmentsEnum.FILE_FRAGMENT, "text", foldersAddData)
        FragmentsStack.addFragment(FragmentsEnum.LEGAL_NOTICES, "Activity log", null)
        assertEquals("wrong FragmentsStack size", 5, FragmentsStack.getStackSize())

        val actionCancelled = false
        val actualFragmentsStackResponse = FragmentsStack.removeTopFragment("testRemoveTopFragment_topFragmentFolder_03", actionCancelled)

        val expectedStackFragments = arrayOf(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, FragmentsEnum.DOWNLOAD_FRAGMENT, FragmentsEnum.FOLDER_FRAGMENT,
                //				FragmentsEnum.CREATE_FILE_FRAGMENT,
                FragmentsEnum.FILE_FRAGMENT)
        val actualStackFragments = FragmentsStack.getFragmentsList()
        val match = verifyFoldersStack(expectedStackFragments, actualStackFragments)

        Assert.assertTrue("wrong fragments on stack", match)
        assertEquals("wrong FragmentsStack size", 4, FragmentsStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.FILE_FRAGMENT, FragmentsStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 0, FoldersData.getCurrFolderLevel())
        val expectedFragmentsStackResponse = FragmentsStackResponse(false, FragmentsEnum.FILE_FRAGMENT, "text", false, false, true)
        val errMsg = validateFragmentsStackResponse(expectedFragmentsStackResponse, actualFragmentsStackResponse)
        Assert.assertEquals("wrong fragmentsStackResponse", null, errMsg)
    }

    /*
	 * add ACTIVITY_LOG_FRAGMENT, DOWNLOAD_FRAGMENT, FOLDER_FRAGMENT, LEGAL_NOTICES
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_07_legal_notices_top_image_note_below() {
        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        FragmentsStack.addFragment(HomeActivity.FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        FragmentsStack.addFragment(HomeActivity.FragmentsEnum.DOWNLOAD_FRAGMENT, "Folder", null)
        val foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        FragmentsStack.addFragment(HomeActivity.FragmentsEnum.FOLDER_FRAGMENT, "Folder", foldersAddData)
        //		FragmentsStack.addFragment(HomeActivity.FragmentsEnum.CREATE_FILE_FRAGMENT, "Create file", foldersAddData);
        FragmentsStack.addFragment(HomeActivity.FragmentsEnum.IMAGE_VIEW_FRAGMENT, "text", foldersAddData)
        FragmentsStack.addFragment(HomeActivity.FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        assertEquals("wrong FragmentsStack size", 5, FragmentsStack.getStackSize())

        val actionCancelled = false
        val actualFragmentsStackResponse = FragmentsStack.removeTopFragment("testRemoveTopFragment_topFragmentFolder_03", actionCancelled)

        val expectedStackFragments = arrayOf(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, FragmentsEnum.DOWNLOAD_FRAGMENT, FragmentsEnum.FOLDER_FRAGMENT,
                //				FragmentsEnum.CREATE_FILE_FRAGMENT,
                FragmentsEnum.IMAGE_VIEW_FRAGMENT)
        val actualStackFragments = FragmentsStack.getFragmentsList()
        val match = verifyFoldersStack(expectedStackFragments, actualStackFragments)

        Assert.assertTrue("wrong fragments on stack", match)
        assertEquals("wrong FragmentsStack size", 4, FragmentsStack.getStackSize())
        assertEquals("wrong currFragment", HomeActivity.FragmentsEnum.IMAGE_VIEW_FRAGMENT, FragmentsStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 0, FoldersData.getCurrFolderLevel())
        val expectedFragmentsStackResponse = FragmentsStackResponse(false, HomeActivity.FragmentsEnum.IMAGE_VIEW_FRAGMENT, "text", false, false, true)
        val errMsg = validateFragmentsStackResponse(expectedFragmentsStackResponse, actualFragmentsStackResponse)
        Assert.assertEquals("wrong fragmentsStackResponse", null, errMsg)
    }

    /*
	 * add ACTIVITY_LOG_FRAGMENT, DOWNLOAD_FRAGMENT, FOLDER_FRAGMENT, FILE_DETAILS_FRAGMENT
	 * rename was clicked, rename dialog shown, entered new name and clicked OK
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_08_file_details_on_top() {
        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        FragmentsStack.addFragment(HomeActivity.FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        FragmentsStack.addFragment(HomeActivity.FragmentsEnum.DOWNLOAD_FRAGMENT, "Folder", null)
        val foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        FragmentsStack.addFragment(HomeActivity.FragmentsEnum.FOLDER_FRAGMENT, "Folder", foldersAddData)
        FragmentsStack.addFragment(HomeActivity.FragmentsEnum.FILE_DETAILS_FRAGMENT, "File details", null)
        assertEquals("wrong FragmentsStack size", 4, FragmentsStack.getStackSize())

        val actionCancelled = false
        val actualFragmentsStackResponse = FragmentsStack.removeTopFragment("testRemoveTopFragment_topFragmentFolder_03", actionCancelled)

        val expectedStackFragments = arrayOf(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, FragmentsEnum.DOWNLOAD_FRAGMENT, FragmentsEnum.FOLDER_FRAGMENT)
        val actualStackFragments = FragmentsStack.getFragmentsList()
        val match = verifyFoldersStack(expectedStackFragments, actualStackFragments)

        Assert.assertTrue("wrong fragments on stack", match)
        assertEquals("wrong FragmentsStack size", 3, FragmentsStack.getStackSize())
        assertEquals("wrong currFragment", HomeActivity.FragmentsEnum.FOLDER_FRAGMENT, FragmentsStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 0, FoldersData.getCurrFolderLevel())
        val expectedFragmentsStackResponse = FragmentsStackResponse(false, HomeActivity.FragmentsEnum.FOLDER_FRAGMENT, "A", false, false, true)
        val errMsg = validateFragmentsStackResponse(expectedFragmentsStackResponse, actualFragmentsStackResponse)
        Assert.assertEquals("wrong fragmentsStackResponse", null, errMsg)
    }

    // helper functions

    private fun getFoldersAddData(
            fileParentFolderDriveId: DriveId,
            folderLevel: Int,
            newFolderTitle: String = "A"): FolderData {
//        val newFolderTitle: String
        val newFolderDriveId: DriveId
        val isFolder: Boolean?
        val newFolderData: Boolean
        val mimeType: String
        val createDate: Date
        val updateDate: Date
        val trashedFilesCnt = 0

//        newFolderTitle = "A"
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

    private fun verifyFoldersStack(expexted: Array<FragmentsEnum>, actual: Array<FragmentsEnum>): Boolean {
        var verificationSuccessful = true
        val testListLgth = expexted.size
        val stackListLgth = actual.size
        if (testListLgth != stackListLgth) {
            verificationSuccessful = false
            Log.i(TAG, "@#verifyFoldersStack - wrong length")
        } else {
            var i = 0
            val cnt = if (testListLgth > stackListLgth) testListLgth else stackListLgth
            while (i < cnt) {
                if (expexted[i] !== actual[i]) {
                    Log.i(TAG, "@#verifyFoldersStack - elements in pos " + i + " doesn't match - expected/actual" + expexted[i] + "/" + actual[i])
                    verificationSuccessful = false
                    break
                }
                i++
            }
        }
        if (!verificationSuccessful) {
            Log.i(TAG, "@#verifyFoldersStack - expected: " + getFragmentsString(expexted))
            Log.i(TAG, "@#verifyFoldersStack - actual:   " + getFragmentsString(actual))
        }
        return verificationSuccessful
    }

    private fun getFragmentsString(array: Array<FragmentsEnum>): String {
        val sb = StringBuilder()
        for (fragmentsEnum in array) {
            sb.append(fragmentsEnum.toString() + ", ")
        }
        return sb.toString()
    }

    private fun printCollection(msg: String, coll: Array<HomeActivity.FragmentsEnum>) {
        Log.i(TAG, "\nprintCollection $msg")
        coll.forEach { Log.i(TAG, it.toString()) }
        Log.i(TAG, "\nend")
    }
}


//    @Test
//    fun initialize() {
//    }
//
//    @Test
//    fun replaceCurrFragment() {
//    }
//
//    @Test
//    fun addFragment() {
//    }
//
//    @Test
//    fun removeTopFragment() {
//    }