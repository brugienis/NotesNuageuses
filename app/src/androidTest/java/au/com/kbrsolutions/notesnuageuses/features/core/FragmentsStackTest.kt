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

    private var testFragmentStack: FragmentsStack? = null
    private var testFoldersData: FoldersData? = null

    private val TAG = FragmentsStackTest::class.java.simpleName

    @Before
    fun setUp() {
        testFragmentStack = FragmentsStack
        testFoldersData = FoldersData
        testFragmentStack!!.init(true)
        testFoldersData!!.init()
    }

    @After
    fun tearDown() {
        fun tearDown() {
            testFoldersData = null
            testFragmentStack = null
        }
    }

    @Test
    fun testInit() {
        Assert.assertTrue("testFragmentStack can't be null", testFragmentStack != null)
    }

    fun getEsentials(): Pair<FragmentsStack, FoldersData> {
        val fragmentStack: FragmentsStack = testFragmentStack ?:
        throw RuntimeException("testFragmentStack cant be null")

        val lfoldersData: FoldersData = testFoldersData ?:
        throw RuntimeException("testFoldersData cant be null")
        return fragmentStack to lfoldersData
    }

    @Test
    fun testAddFragment_notFolder() {
        val (fragmentStack, foldersData) = getEsentials()

        val foldersAddData: FolderData? = null
        fragmentStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", foldersAddData)

        assertEquals("wrong testFragmentStack size", 1, fragmentStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.ACTIVITY_LOG_FRAGMENT, fragmentStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", -1, foldersData.getCurrFolderLevel())
    }

    @Test
    fun testAddFragment_folder() {
        val (fragmentStack, foldersData) = getEsentials()

        var folderLevel = -1
        val fileParentFolderDriveId = addTopFolderDetails()

        val foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        fragmentStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder", foldersAddData)

        assertEquals("wrong testFragmentStack size", 1, fragmentStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.FOLDER_FRAGMENT, fragmentStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 0, foldersData.getCurrFolderLevel())
    }

    @Test
    fun testRemoveTopFragment_just_one_top_IsFolder() {
        val (fragmentStack, foldersData) = getEsentials()

        assertEquals("wrong currFolderLevel", -1, foldersData.getCurrFolderLevel())
        testAddFragment_folder()
        assertEquals("wrong currFolderLevel", 0, foldersData.getCurrFolderLevel())

        val fragmentsStackResponse = fragmentStack.removeTopFragment(
                "testRemoveTopFragment_fromEmptyStack", false)

        assertEquals("wrong testFragmentStack size", 0, fragmentStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.NONE, fragmentStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", -1, foldersData.getCurrFolderLevel())
        assertEquals("wrong fragmentsStackResponse", true, fragmentsStackResponse.finishRequired)
    }

    @Test
    fun testRemoveTopFragment_fromEmptyStack() {
        val (fragmentStack, foldersData) = getEsentials()

        val fragmentsStackResponse = fragmentStack.removeTopFragment(
                "testRemoveTopFragment_fromEmptyStack", false)
        assertEquals("wrong fragmentsStackResponse", true, fragmentsStackResponse.finishRequired)
    }

    /*
	 * ACTIVITY_LOG_FRAGMENT
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_00() {
        val (fragmentStack, foldersData) = getEsentials()

        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        val foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        fragmentStack.addFragment(
                FragmentsEnum.ACTIVITY_LOG_FRAGMENT,
                "Activity log", foldersAddData)
        assertEquals(
                "wrong testFragmentStack size", 1, fragmentStack.getStackSize())

        val actualFragmentsStackResponse = fragmentStack.removeTopFragment(
                "testRemoveTopFragment_topFragmentFolder_00", false)

//        Log.v(TAG, "testRemoveTopFragment_topFragmentFolder_00 - actualFragmentsStackResponse: " +
//                actualFragmentsStackResponse)

        assertNotNull("actualFragmentsStackResponse cannot be null",
                actualFragmentsStackResponse)
        assertEquals("wrong testFragmentStack size", 0,
                fragmentStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.NONE ,
                fragmentStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", -1,
                foldersData.getCurrFolderLevel())
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
	 *		ACTIVITY_LOG_FRAGMENT, RETRIEVE_FOLDER_PROGRESS_FRAGMENT, FOLDER_FRAGMENT
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_01() {
        val (fragmentStack, foldersData) = getEsentials()

        Log.v(TAG, " - testRemoveTopFragment_topFragmentFolder_01 start");
        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        var foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        fragmentStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log",
                foldersAddData)
        fragmentStack.addFragment(FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT,
                "Folder", null)
        foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        fragmentStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder",
                foldersAddData)
        assertEquals("wrong testFragmentStack size", 3,
                fragmentStack.getStackSize())

        val actualFragmentsStackResponse = fragmentStack.removeTopFragment(
                "testRemoveTopFragment_topFragmentFolder_01", false)

        Log.v(TAG, "testRemoveTopFragment_topFragmentFolder_00 - actualFragmentsStackResponse: " +
                actualFragmentsStackResponse)

        assertEquals(
                "wrong testFragmentStack size", 0, fragmentStack.getStackSize())
        assertEquals(
                "wrong currFragment",
                FragmentsEnum.NONE,
                fragmentStack.getCurrFragment())
        assertEquals(
                "wrong currFolderLevel", -1, foldersData.getCurrFolderLevel())
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
	 * 		ACTIVITY_LOG_FRAGMENT, RETRIEVE_FOLDER_PROGRESS_FRAGMENT, FOLDER_FRAGMENT, RETRIEVE_FOLDER_PROGRESS_FRAGMENT, FOLDER_FRAGMENT
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_02() {
        val (fragmentStack, foldersData) = getEsentials()
        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        fragmentStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log",
                null)
        fragmentStack.addFragment(FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT,
                "Folder",
                null)
        var foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        fragmentStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder",
                foldersAddData)
        fragmentStack.addFragment(FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT,
                "Folder",
                null)
        foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        fragmentStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder1",
                foldersAddData)
        assertEquals("wrong fragmentStack size", 5, fragmentStack.getStackSize())
        assertEquals("wrong currFolderLevel", 1, foldersData.getCurrFolderLevel())

        val actualFragmentsStackResponse = fragmentStack.removeTopFragment(
                "testRemoveTopFragment_topFragmentFolder_02",
                false)

        assertEquals("wrong fragmentStack size", 3, fragmentStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.FOLDER_FRAGMENT,
                fragmentStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 0, foldersData.getCurrFolderLevel())
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

    /* Creating text note in non empty folder - after CREATE_FILE_FRAGMENT set, TEXT_VIEW_FRAGMENT is set.
	 * Either user clicked on the Back button or in 'file name and password' dialog clicked on the Cancel button.
	 *
	 *		ACTIVITY_LOG_FRAGMENT, RETRIEVE_FOLDER_PROGRESS_FRAGMENT, FOLDER_FRAGMENT, RETRIEVE_FOLDER_PROGRESS_FRAGMENT, FOLDER_FRAGMENT, CREATE_FILE_FRAGMENT, TEXT_VIEW_FRAGMENT
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_04_create_text_file_in_non_empty_folder_Cancel_clicked() {
        val (fragmentStack, foldersData) = getEsentials()
        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        fragmentStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        fragmentStack.addFragment(FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT, "progress folder", null)
        var foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        fragmentStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder", foldersAddData)
        fragmentStack.addFragment(FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT, "progress folder", null)
        foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        fragmentStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder1", foldersAddData)
        //		fragmentStack.addFragment(HomeActivity.FragmentsEnum.CREATE_FILE_FRAGMENT, "Create file", foldersAddData);
        fragmentStack.addFragment(FragmentsEnum.TEXT_VIEW_FRAGMENT, "image", foldersAddData)
        assertEquals("wrong fragmentStack size", 6, fragmentStack.getStackSize())

        val actionCancelled = true
        Log.i(TAG, "@#testRemoveTopFragment_topFragmentFolder_03 - before removeTopFragment")
        val actualFragmentsStackResponse = fragmentStack.removeTopFragment("testRemoveTopFragment_topFragmentFolder_03", actionCancelled)
        Log.i(TAG, "@#testRemoveTopFragment_topFragmentFolder_03 - after  removeTopFragment")

        assertEquals("wrong fragmentStack size", 5, fragmentStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.FOLDER_FRAGMENT, fragmentStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 1, foldersData.getCurrFolderLevel())
        val expectedFragmentsStackResponse = FragmentsStackResponse(false, FragmentsEnum.FOLDER_FRAGMENT, "A", true, true, true)
        val errMsg = validateFragmentsStackResponse(expectedFragmentsStackResponse, actualFragmentsStackResponse)
        Assert.assertEquals("wrong fragmentsStackResponse", null, errMsg)
    }

    /* Creating text note in non empty folder - after CREATE_FILE_FRAGMENT set, TEXT_VIEW_FRAGMENT is set.
	 * In 'file name and password' dialog clicked on the Save button.
	 *
	 *		ACTIVITY_LOG_FRAGMENT, RETRIEVE_FOLDER_PROGRESS_FRAGMENT, FOLDER_FRAGMENT, RETRIEVE_FOLDER_PROGRESS_FRAGMENT, FOLDER_FRAGMENT, CREATE_FILE_FRAGMENT, TEXT_VIEW_FRAGMENT
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_04_create_text_file_in_non_empty_folder_Save_clicked() {
        val (fragmentStack, foldersData) = getEsentials()
        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        fragmentStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        fragmentStack.addFragment(FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT, "progress folder", null)
        var foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        fragmentStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder", foldersAddData)
        fragmentStack.addFragment(FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT, "progress folder", null)
        foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        fragmentStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder1", foldersAddData)
        //		fragmentStack.addFragment(HomeActivity.FragmentsEnum.CREATE_FILE_FRAGMENT, "Create file", foldersAddData);
        fragmentStack.addFragment(FragmentsEnum.TEXT_VIEW_FRAGMENT, "image", foldersAddData)
        assertEquals("wrong fragmentStack size", 6, fragmentStack.getStackSize())

        val actionCancelled = false
        Log.i(TAG, "@#testRemoveTopFragment_topFragmentFolder_03 - before removeTopFragment")
        val actualFragmentsStackResponse = fragmentStack.removeTopFragment("testRemoveTopFragment_topFragmentFolder_03", actionCancelled)
        Log.i(TAG, "@#testRemoveTopFragment_topFragmentFolder_03 - after  removeTopFragment")

        assertEquals("wrong fragmentStack size", 5, fragmentStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.FOLDER_FRAGMENT, fragmentStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 1, foldersData.getCurrFolderLevel())
        val expectedFragmentsStackResponse = FragmentsStackResponse(false, FragmentsEnum.FOLDER_FRAGMENT, "A", true, true, true)
        val errMsg = validateFragmentsStackResponse(expectedFragmentsStackResponse, actualFragmentsStackResponse)
        Assert.assertEquals("wrong fragmentsStackResponse", null, errMsg)
    }

    /*Creating text note in the empty folder - after CREATE_FILE_FRAGMENT set, TEXT_VIEW_FRAGMENT is set.
	 * Either user clicked on the Back button or in 'file name and password' dialog clicked on the Cancel button.
	 *
	 *		ACTIVITY_LOG_FRAGMENT, RETRIEVE_FOLDER_PROGRESS_FRAGMENT, FOLDER_FRAGMENT, RETRIEVE_FOLDER_PROGRESS_FRAGMENT, EMPTY_FOLDER_FRAGMENT, CREATE_FILE_FRAGMENT, TEXT_VIEW_FRAGMENT
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_04_create_text_file_in_empty_folder_Cancel_clicked() {
        val (fragmentStack, foldersData) = getEsentials()
        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        fragmentStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        fragmentStack.addFragment(FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT, "Folder", null)
        var foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        fragmentStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder", foldersAddData)
        fragmentStack.addFragment(FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT, "Folder", null)
        foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        fragmentStack.addFragment(FragmentsEnum.EMPTY_FOLDER_FRAGMENT, "Folder1", foldersAddData)
        //		fragmentStack.addFragment(HomeActivity.FragmentsEnum.CREATE_FILE_FRAGMENT, "Create file", foldersAddData);
        fragmentStack.addFragment(FragmentsEnum.TEXT_VIEW_FRAGMENT, "image", foldersAddData)
        assertEquals("wrong fragmentStack size", 6, fragmentStack.getStackSize())

        val actionCancelled = true
        val actualFragmentsStackResponse = fragmentStack.removeTopFragment("testRemoveTopFragment_topFragmentFolder_03", actionCancelled)

        assertEquals("wrong fragmentStack size", 5, fragmentStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.EMPTY_FOLDER_FRAGMENT, fragmentStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 1, foldersData.getCurrFolderLevel())
        val expectedFragmentsStackResponse = FragmentsStackResponse(false, FragmentsEnum.EMPTY_FOLDER_FRAGMENT, "A", true, true, true)
        val errMsg = validateFragmentsStackResponse(expectedFragmentsStackResponse, actualFragmentsStackResponse)
        Assert.assertEquals("wrong fragmentsStackResponse", null, errMsg)
    }

    // [FOLDER_FRAGMENT, RETRIEVE_FOLDER_PROGRESS_FRAGMENT, FOLDER_FRAGMENT, IMAGE_VIEW_FRAGMENT]

    /*
	 * Either user clicked on the Cancel or Back button in Image view screen.
	 *
	 *		ACTIVITY_LOG_FRAGMENT, RETRIEVE_FOLDER_PROGRESS_FRAGMENT, FOLDER_FRAGMENT, IMAGE_VIEW_FRAGMENT
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_04_picture_view_in_non_empty_folder_Cancel_clicked() {
        val (fragmentStack, foldersData) = getEsentials()
        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        fragmentStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        fragmentStack.addFragment(FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT, "Folder", null)
        val foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        fragmentStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder", foldersAddData)
        fragmentStack.addFragment(FragmentsEnum.IMAGE_VIEW_FRAGMENT, "image", foldersAddData)
        assertEquals("wrong fragmentStack size", 4, fragmentStack.getStackSize())

        val actionCancelled = true
        val actualFragmentsStackResponse = fragmentStack.removeTopFragment("testRemoveTopFragment_topFragmentFolder_03", actionCancelled)

        assertEquals("wrong fragmentStack size", 3, fragmentStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.FOLDER_FRAGMENT, fragmentStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 0, foldersData.getCurrFolderLevel())
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

    /* Creating text note in the empty folder - after CREATE_FILE_FRAGMENT set, TEXT_VIEW_FRAGMENT is set.
	 * In 'file name and password' dialog clicked on the Save button.
	 *
	 * add ACTIVITY_LOG_FRAGMENT, RETRIEVE_FOLDER_PROGRESS_FRAGMENT, FOLDER_FRAGMENT, RETRIEVE_FOLDER_PROGRESS_FRAGMENT, EMPTY_FOLDER_FRAGMENT, CREATE_FILE_FRAGMENT, TEXT_VIEW_FRAGMENT
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_04_create_text_file_in_empty_folder_Save_clicked() {
        val (fragmentStack, foldersData) = getEsentials()
        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        fragmentStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        fragmentStack.addFragment(FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT, "Folder", null)
        var foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        fragmentStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder", foldersAddData)
        fragmentStack.addFragment(FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT, "Folder", null)
        foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        fragmentStack.addFragment(FragmentsEnum.EMPTY_FOLDER_FRAGMENT, "Folder1", foldersAddData)
        //		fragmentStack.addFragment(HomeActivity.FragmentsEnum.CREATE_FILE_FRAGMENT, "Create file", foldersAddData);
        fragmentStack.addFragment(FragmentsEnum.TEXT_VIEW_FRAGMENT, "image", foldersAddData)
        assertEquals("wrong fragmentStack size", 6, fragmentStack.getStackSize())
        val actualStackFragmentsAfterAdd = fragmentStack.getFragmentsList()
//        printCollection("after fragments added", actualStackFragmentsAfterAdd)
        Log.v(TAG, " - actualStackFragmentsAfterAdd: ${printCollection("after fragments added", actualStackFragmentsAfterAdd)}")

        val actionCancelled = false
        val actualFragmentsStackResponse = fragmentStack.removeTopFragment("testRemoveTopFragment_topFragmentFolder_03", actionCancelled)

        val expectedStackFragments = arrayOf(
                FragmentsEnum.ACTIVITY_LOG_FRAGMENT,
                FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT,
                FragmentsEnum.FOLDER_FRAGMENT,
                FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT,
                FragmentsEnum.FOLDER_FRAGMENT)
        Log.v(TAG, " - expectedStackFragments: ${printCollection("expected after removeTopFragment", expectedStackFragments)}")
        val actualStackFragments = fragmentStack.getFragmentsList()
        Log.v(TAG, " - actualStackFragments: ${printCollection("actual after removeTopFragment", actualStackFragments)}")
        val match = verifyFoldersStack(expectedStackFragments, actualStackFragments)

        Assert.assertTrue("wrong fragments on stack", match)
        assertEquals("wrong fragmentStack size", 5, fragmentStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.FOLDER_FRAGMENT, fragmentStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 1, foldersData.getCurrFolderLevel())
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

    /* Creating text note in the empty folder - after CREATE_FILE_FRAGMENT set, TEXT_VIEW_FRAGMENT is set and through Navigation Door the Activity log is shown.
	 * In 'file name and password' dialog clicked on the Save button.
	 *
	 * add ACTIVITY_LOG_FRAGMENT, RETRIEVE_FOLDER_PROGRESS_FRAGMENT, FOLDER_FRAGMENT, CREATE_FILE_FRAGMENT, TEXT_VIEW_FRAGMENT, ACTIVITY_LOG_FRAGMENT
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_05_new_text_file_opened_and_switched_to_activity_log() {
        val (fragmentStack, foldersData) = getEsentials()
        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        fragmentStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        fragmentStack.addFragment(FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT, "Folder", null)
        val foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        fragmentStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder", foldersAddData)
        //		fragmentStack.addFragment(HomeActivity.FragmentsEnum.CREATE_FILE_FRAGMENT, "Create file", foldersAddData);
        fragmentStack.addFragment(FragmentsEnum.TEXT_VIEW_FRAGMENT, "text", foldersAddData)
        fragmentStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        assertEquals("wrong fragmentStack size", 5, fragmentStack.getStackSize())

        val actionCancelled = false
        val actualFragmentsStackResponse = fragmentStack.removeTopFragment("testRemoveTopFragment_topFragmentFolder_03", actionCancelled)

        val expectedStackFragments = arrayOf(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT, FragmentsEnum.FOLDER_FRAGMENT,
                //				FragmentsEnum.CREATE_FILE_FRAGMENT,
                FragmentsEnum.TEXT_VIEW_FRAGMENT)
        val actualStackFragments = fragmentStack.getFragmentsList()
        val match = verifyFoldersStack(expectedStackFragments, actualStackFragments)

        Assert.assertTrue("wrong fragments on stack", match)
        assertEquals("wrong fragmentStack size", 4, fragmentStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.TEXT_VIEW_FRAGMENT, fragmentStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 0, foldersData.getCurrFolderLevel())
        val expectedFragmentsStackResponse = FragmentsStackResponse(false, FragmentsEnum.TEXT_VIEW_FRAGMENT, "text", false, false, true)
        val errMsg = validateFragmentsStackResponse(expectedFragmentsStackResponse, actualFragmentsStackResponse)
        Assert.assertEquals("wrong fragmentsStackResponse", null, errMsg)
    }

    /* Creating text note in the empty folder - after CREATE_FILE_FRAGMENT set, IMAGE_VIEW_FRAGMENT is set and through Navigation Door the Activity log is shown.
	 * In 'file name and password' dialog clicked on the Save button.
	 *
	 * add ACTIVITY_LOG_FRAGMENT, RETRIEVE_FOLDER_PROGRESS_FRAGMENT, FOLDER_FRAGMENT, CREATE_FILE_FRAGMENT, IMAGE_VIEW_FRAGMENT, ACTIVITY_LOG_FRAGMENT
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_06_photo_file_opened_and_switched_to_activity_log() {
        val (fragmentStack, foldersData) = getEsentials()
        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        fragmentStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        fragmentStack.addFragment(FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT, "Folder", null)
        val foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        fragmentStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder", foldersAddData)
        //		fragmentStack.addFragment(HomeActivity.FragmentsEnum.CREATE_FILE_FRAGMENT, "Create file", foldersAddData);
        fragmentStack.addFragment(FragmentsEnum.IMAGE_VIEW_FRAGMENT, "text", foldersAddData)
        fragmentStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        assertEquals("wrong fragmentStack size", 5, fragmentStack.getStackSize())

        val actionCancelled = false
        val actualFragmentsStackResponse = fragmentStack.removeTopFragment("testRemoveTopFragment_topFragmentFolder_03", actionCancelled)

        val expectedStackFragments = arrayOf(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT, FragmentsEnum.FOLDER_FRAGMENT,
                //				FragmentsEnum.CREATE_FILE_FRAGMENT,
                FragmentsEnum.IMAGE_VIEW_FRAGMENT)
        val actualStackFragments = fragmentStack.getFragmentsList()
        val match = verifyFoldersStack(expectedStackFragments, actualStackFragments)

        Assert.assertTrue("wrong fragments on stack", match)
        assertEquals("wrong fragmentStack size", 4, fragmentStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.IMAGE_VIEW_FRAGMENT, fragmentStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 0, foldersData.getCurrFolderLevel())
        val expectedFragmentsStackResponse = FragmentsStackResponse(false, FragmentsEnum.IMAGE_VIEW_FRAGMENT, "text", false, false, true)
        val errMsg = validateFragmentsStackResponse(expectedFragmentsStackResponse, actualFragmentsStackResponse)
        Assert.assertEquals("wrong fragmentsStackResponse", null, errMsg)
    }

    /*
	 * add ACTIVITY_LOG_FRAGMENT, RETRIEVE_FOLDER_PROGRESS_FRAGMENT, FOLDER_FRAGMENT, LEGAL_NOTICES
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_07_legal_notices_top_folder_below() {
        val (fragmentStack, foldersData) = getEsentials()
        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        fragmentStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        fragmentStack.addFragment(FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT, "Folder", null)
        val foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        fragmentStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder", foldersAddData)
        fragmentStack.addFragment(FragmentsEnum.LEGAL_NOTICES, "Activity log", null)
        assertEquals("wrong fragmentStack size", 4, fragmentStack.getStackSize())

        val actionCancelled = false
        val actualFragmentsStackResponse = fragmentStack.removeTopFragment("testRemoveTopFragment_topFragmentFolder_03", actionCancelled)

        val expectedStackFragments = arrayOf(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT, FragmentsEnum.FOLDER_FRAGMENT)
        val actualStackFragments = fragmentStack.getFragmentsList()
        val match = verifyFoldersStack(expectedStackFragments, actualStackFragments)

        Assert.assertTrue("wrong fragments on stack", match)
        assertEquals("wrong fragmentStack size", 3, fragmentStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.FOLDER_FRAGMENT, fragmentStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 0, foldersData.getCurrFolderLevel())
        val expectedFragmentsStackResponse = FragmentsStackResponse(false, FragmentsEnum.FOLDER_FRAGMENT, "A", false, false, true)
        val errMsg = validateFragmentsStackResponse(expectedFragmentsStackResponse, actualFragmentsStackResponse)
        Assert.assertEquals("wrong fragmentsStackResponse", null, errMsg)
    }

    /*
	 * add ACTIVITY_LOG_FRAGMENT, RETRIEVE_FOLDER_PROGRESS_FRAGMENT, EMPTY_FOLDER_FRAGMENT, LEGAL_NOTICES
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_07_legal_notices_top_empty_folder_below() {
        val (fragmentStack, foldersData) = getEsentials()
        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        fragmentStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        fragmentStack.addFragment(FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT, "Folder", null)
        var foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        fragmentStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder", foldersAddData)
        fragmentStack.addFragment(FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT, "Folder", null)
        foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        fragmentStack.addFragment(FragmentsEnum.EMPTY_FOLDER_FRAGMENT, "Folder", foldersAddData)
        fragmentStack.addFragment(FragmentsEnum.LEGAL_NOTICES, "Activity log", null)
        assertEquals("wrong fragmentStack size", 6, fragmentStack.getStackSize())

        val actionCancelled = false
        val actualFragmentsStackResponse = fragmentStack.removeTopFragment("testRemoveTopFragment_topFragmentFolder_03", actionCancelled)

        val expectedStackFragments = arrayOf(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT, FragmentsEnum.FOLDER_FRAGMENT, FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT, FragmentsEnum.EMPTY_FOLDER_FRAGMENT)
        val actualStackFragments = fragmentStack.getFragmentsList()
        val match = verifyFoldersStack(expectedStackFragments, actualStackFragments)

        Assert.assertTrue("wrong fragments on stack", match)
        assertEquals("wrong fragmentStack size", 5, fragmentStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.EMPTY_FOLDER_FRAGMENT, fragmentStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 1, foldersData.getCurrFolderLevel())
        val expectedFragmentsStackResponse = FragmentsStackResponse(false, FragmentsEnum.EMPTY_FOLDER_FRAGMENT, "A", false, false, true)
        val errMsg = validateFragmentsStackResponse(expectedFragmentsStackResponse, actualFragmentsStackResponse)
        Assert.assertEquals("wrong fragmentsStackResponse", null, errMsg)
    }

    /*
	 * add FOLDER_FRAGMENT, TEXT_VIEW_FRAGMENT, LEGAL_NOTICES
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_07_legal_notices_top_text_note_below() {
        val (fragmentStack, foldersData) = getEsentials()
        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        fragmentStack.addFragment(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        fragmentStack.addFragment(FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT, "Folder", null)
        val foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        fragmentStack.addFragment(FragmentsEnum.FOLDER_FRAGMENT, "Folder", foldersAddData)
        //		fragmentStack.addFragment(HomeActivity.FragmentsEnum.CREATE_FILE_FRAGMENT, "Create file", foldersAddData);
        fragmentStack.addFragment(FragmentsEnum.TEXT_VIEW_FRAGMENT, "text", foldersAddData)
        fragmentStack.addFragment(FragmentsEnum.LEGAL_NOTICES, "Activity log", null)
        assertEquals("wrong fragmentStack size", 5, fragmentStack.getStackSize())

        val actionCancelled = false
        val actualFragmentsStackResponse = fragmentStack.removeTopFragment("testRemoveTopFragment_topFragmentFolder_03", actionCancelled)

        val expectedStackFragments = arrayOf(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT, FragmentsEnum.FOLDER_FRAGMENT,
                //				FragmentsEnum.CREATE_FILE_FRAGMENT,
                FragmentsEnum.TEXT_VIEW_FRAGMENT)
        val actualStackFragments = fragmentStack.getFragmentsList()
        val match = verifyFoldersStack(expectedStackFragments, actualStackFragments)

        Assert.assertTrue("wrong fragments on stack", match)
        assertEquals("wrong fragmentStack size", 4, fragmentStack.getStackSize())
        assertEquals("wrong currFragment", FragmentsEnum.TEXT_VIEW_FRAGMENT, fragmentStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 0, foldersData.getCurrFolderLevel())
        val expectedFragmentsStackResponse = FragmentsStackResponse(false, FragmentsEnum.TEXT_VIEW_FRAGMENT, "text", false, false, true)
        val errMsg = validateFragmentsStackResponse(expectedFragmentsStackResponse, actualFragmentsStackResponse)
        Assert.assertEquals("wrong fragmentsStackResponse", null, errMsg)
    }

    /*
	 * add ACTIVITY_LOG_FRAGMENT, RETRIEVE_FOLDER_PROGRESS_FRAGMENT, FOLDER_FRAGMENT, LEGAL_NOTICES
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_07_legal_notices_top_image_note_below() {
        val (fragmentStack, foldersData) = getEsentials()
        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        fragmentStack.addFragment(HomeActivity.FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        fragmentStack.addFragment(HomeActivity.FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT, "Folder", null)
        val foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        fragmentStack.addFragment(HomeActivity.FragmentsEnum.FOLDER_FRAGMENT, "Folder", foldersAddData)
        //		fragmentStack.addFragment(HomeActivity.FragmentsEnum.CREATE_FILE_FRAGMENT, "Create file", foldersAddData);
        fragmentStack.addFragment(HomeActivity.FragmentsEnum.IMAGE_VIEW_FRAGMENT, "text", foldersAddData)
        fragmentStack.addFragment(HomeActivity.FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        assertEquals("wrong fragmentStack size", 5, fragmentStack.getStackSize())

        val actionCancelled = false
        val actualFragmentsStackResponse = fragmentStack.removeTopFragment("testRemoveTopFragment_topFragmentFolder_03", actionCancelled)

        val expectedStackFragments = arrayOf(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT, FragmentsEnum.FOLDER_FRAGMENT,
                //				FragmentsEnum.CREATE_FILE_FRAGMENT,
                FragmentsEnum.IMAGE_VIEW_FRAGMENT)
        val actualStackFragments = fragmentStack.getFragmentsList()
        val match = verifyFoldersStack(expectedStackFragments, actualStackFragments)

        Assert.assertTrue("wrong fragments on stack", match)
        assertEquals("wrong fragmentStack size", 4, fragmentStack.getStackSize())
        assertEquals("wrong currFragment", HomeActivity.FragmentsEnum.IMAGE_VIEW_FRAGMENT, fragmentStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 0, foldersData.getCurrFolderLevel())
        val expectedFragmentsStackResponse = FragmentsStackResponse(false, HomeActivity.FragmentsEnum.IMAGE_VIEW_FRAGMENT, "text", false, false, true)
        val errMsg = validateFragmentsStackResponse(expectedFragmentsStackResponse, actualFragmentsStackResponse)
        Assert.assertEquals("wrong fragmentsStackResponse", null, errMsg)
    }

    /*
	 * add ACTIVITY_LOG_FRAGMENT, RETRIEVE_FOLDER_PROGRESS_FRAGMENT, FOLDER_FRAGMENT, FILE_DETAILS_FRAGMENT
	 * rename was clicked, rename dialog shown, entered new name and clicked OK
	 */
    @Test
    fun testRemoveTopFragment_topFragmentFolder_08_file_details_on_top() {
        val (fragmentStack, foldersData) = getEsentials()
        var folderLevel = -1

        val fileParentFolderDriveId = addTopFolderDetails()
        fragmentStack.addFragment(HomeActivity.FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", null)
        fragmentStack.addFragment(HomeActivity.FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT, "Folder", null)
        val foldersAddData = getFoldersAddData(fileParentFolderDriveId, folderLevel++)
        fragmentStack.addFragment(HomeActivity.FragmentsEnum.FOLDER_FRAGMENT, "Folder", foldersAddData)
        fragmentStack.addFragment(HomeActivity.FragmentsEnum.FILE_DETAILS_FRAGMENT, "File details", null)
        assertEquals("wrong fragmentStack size", 4, fragmentStack.getStackSize())

        val actionCancelled = false
        val actualFragmentsStackResponse = fragmentStack.removeTopFragment("testRemoveTopFragment_topFragmentFolder_03", actionCancelled)

        val expectedStackFragments = arrayOf(FragmentsEnum.ACTIVITY_LOG_FRAGMENT, FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT, FragmentsEnum.FOLDER_FRAGMENT)
        val actualStackFragments = fragmentStack.getFragmentsList()
        val match = verifyFoldersStack(expectedStackFragments, actualStackFragments)

        Assert.assertTrue("wrong fragments on stack", match)
        assertEquals("wrong fragmentStack size", 3, fragmentStack.getStackSize())
        assertEquals("wrong currFragment", HomeActivity.FragmentsEnum.FOLDER_FRAGMENT, fragmentStack.getCurrFragment())
        assertEquals("wrong currFolderLevel", 0, foldersData.getCurrFolderLevel())
        val expectedFragmentsStackResponse = FragmentsStackResponse(false, HomeActivity.FragmentsEnum.FOLDER_FRAGMENT, "A", false, false, true)
        val errMsg = validateFragmentsStackResponse(expectedFragmentsStackResponse, actualFragmentsStackResponse)
        Assert.assertEquals("wrong fragmentsStackResponse", null, errMsg)
    }

    // helper functions

    private fun getFoldersAddData(fileParentFolderDriveId: DriveId, folderLevel: Int): FolderData {
        val newFolderTitle: String
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
//    fun init() {
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