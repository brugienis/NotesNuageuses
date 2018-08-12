package au.com.kbrsolutions.notesnuageuses.features.core

import au.com.kbrsolutions.notesnuageuses.features.main.HomeActivity
import junit.framework.Assert
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test

class FragmentsStackTest {

    internal var fragmentStack: FragmentsStack? = null
    internal var foldersData: FoldersData? = null

    private val TAG = "FragmentsStackTest"

    @Before
    fun setUp() {
        foldersData = FoldersData
        fragmentStack = FragmentsStack
        foldersData!!.init()
        fragmentStack!!.init(true)
    }

    @After
    fun tearDown() {
        foldersData = null
        fragmentStack = null
    }

    @Test
    fun init() {
        Assert.assertTrue("fragmentStack can't be null", fragmentStack != null)
    }

    @Test
    fun addFragment() {
        val foldersAddData: FolderData? = null
        fragmentStack!!.addFragment(HomeActivity.FragmentsEnum.ACTIVITY_LOG_FRAGMENT, "Activity log", foldersAddData)

        assertEquals("wrong fragmentStack size", 1, fragmentStack?.getStackSize())
        assertEquals("wrong currFragment", HomeActivity.FragmentsEnum.ACTIVITY_LOG_FRAGMENT, fragmentStack?.getCurrFragment())
        assertEquals("wrong currFolderLevel", -1, foldersData?.getCurrFolderLevel())
    }
}