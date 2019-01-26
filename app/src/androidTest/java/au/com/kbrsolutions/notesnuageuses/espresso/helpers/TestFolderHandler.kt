package au.com.kbrsolutions.notesnuageuses.espresso.helpers

import android.content.res.Resources
import au.com.kbrsolutions.notesnuageuses.R

class TestFolderHandler {

//    private lateinit var testFolderName:String

    fun createTestFolder(testFolderName: String, currFolderName:String, resources: Resources) {
        /*val date: LocalDateTime = LocalDateTime.now()
//        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")
        val formattedTimeNow = date.format(formatter)
        testFolderName = "Test $formattedTimeNow"*/

        validateActionbarTitle(currFolderName)

        R.id.menuCreateFile.performMenuItemClick(resources.getString(R.string.menu_create_file))

        R.id.createDialog_FileName.performTypeText(testFolderName)

        delay(2000)

        R.id.createDialog_CreateFolder.performClick()

        validateActionbarTitle(testFolderName)

        /*                               Folder was created                                       */

        delay(2000)

        testItemAgainstAdapterData(testFolderName, true)

        delay(3000)
    }

    fun deleteTestFolder(testFolderName: String, currFolderName:String, resources: Resources) {

        showFileDetailsViewForTestFolderName(R.id.folderFragmentLayoutId, testFolderName)

        delay(3000)

        R.id.fileDetailRootView.checkIsDisplayed()

        /*               File details of the test folder screen shows                             */

        /* Trash test folder */

        R.id.fileDetail_TrashOrDelete.performClick()

        validateActionbarTitle(currFolderName)

        /* We are back to the 'currFolderName' folder layout - the trashed folder should not be   */
        /* visible                                                                                */

        delay(3000)

        val showTrashedFilesMenuItem = resources.getString(R.string.menu_show_trashed_files)
        val parenStartPos = showTrashedFilesMenuItem.indexOf('(')

        R.id.menuShowTrashed.performMenuItemClick(showTrashedFilesMenuItem.substring(0, parenStartPos))

        /* The trashed test folder is visible                                                     */

        delay(2000)

        showFileDetailsViewForTestFolderName(R.id.folderFragmentLayoutId, testFolderName)

        R.id.fileDetailRootView.checkIsDisplayed()

        /*               File details of the test folder screen shows                             */

        /* Delete test folder                                                                     */

        R.id.fileDetail_TrashOrDelete.performClick()

        /* The test folder should not be in the adapter */

        testItemAgainstAdapterData(testFolderName, false)

        delay(2000)

        validateActionbarTitle(currFolderName)

        delay(2000)

    }

    /*                                  End of test code                                          */

    private val mDoNotSleep: Boolean = true
    private fun delay(millis: Int) {
        if (mDoNotSleep) {
            return
        }
        try {
            //            Log.v(TAG, "delay - sleep start");
            Thread.sleep(millis.toLong())
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}