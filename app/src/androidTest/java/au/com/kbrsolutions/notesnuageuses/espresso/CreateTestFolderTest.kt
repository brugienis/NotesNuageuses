package au.com.kbrsolutions.notesnuageuses.espresso

import androidx.test.InstrumentationRegistry.getInstrumentation
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.R.id.*
import au.com.kbrsolutions.notesnuageuses.espresso.helpers.*
import au.com.kbrsolutions.notesnuageuses.features.espresso.ActiveFlagsController
import au.com.kbrsolutions.notesnuageuses.features.main.HomeActivity
import com.azimolabs.conditionwatcher.ConditionWatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@LargeTest
@RunWith(AndroidJUnit4::class)
class CreateTestFolderTest {

     /*check package androidx.test.espresso.matcher - HasSiblingMatcher
     (hasSibling(ViewMatchers.withText("some text"))), etc.
     public final class ViewMatchers.

     package org.hamcrest
     public class Matchers*/

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(HomeActivity::class.java)

    /*
        It looks like the app starts as soon as the
            var mActivityTestRule = ActivityTestRule(HomeActivity::class.java)
        is executed. That makes the first test's results unpredictable - sometimes it succeeds,
        other times it fails.

        Before this test starts,the 'device.pressHome()' clicks on the 'home' button'. And then the
        app is started. It makes testing predictable.
     */
    @Before
    fun launchActivity() {
        val device = UiDevice.getInstance(getInstrumentation())
        device.pressHome()

        // Bring up the default launcher by searching for a UI component that matches the content
        // description for the launcher button.
        val allAppsButton = device.findObject(UiSelector().description("NotesNuageuses"))

        // Perform a click on the button to load the app launcher.
        allAppsButton.clickAndWaitForNewWindow()
        ActiveFlagsController.isEspressoTestRunning = true
    }

    @Test
    fun createNewFolderInRootFolder() {
        val resources = mActivityTestRule.activity.applicationContext.resources

        val date: LocalDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val formattedTimeNow = date.format(formatter)
        val testFolderName = "Espresso test $formattedTimeNow"

        ConditionWatcher.setTimeoutLimit(15 * 1000)

        ConditionWatcher.waitForCondition(WaitForFolderIsActiveInstruction())

        validateActionbarTitle(APP_ROOT_FOLDER)

        menuCreateFile.performMenuItemClick(resources.getString(R.string.menu_create_file))

        createDialog_FileName.performTypeText(testFolderName)

        delay(2000)

        createDialog_CreateFolder.performClick()

        validateActionbarTitle(testFolderName)

        /*                               Folder was created                                       */

        delay(2000)

        testItemAgainstAdapterData(testFolderName, true)

        delay(2000)

        showFileDetailsViewForTestFolderName(R.id.folderFragmentLayoutId, testFolderName)

        /* Row with the folderName just created is now selected */

        delay(3000)

        fileDetailRootView.checkIsDisplayed()

        /*                            File Info screen shows                                      */

        delay(3000)

        /* Trash test folder */

        fileDetail_TrashOrDelete.performClick()

        validateActionbarTitle(APP_ROOT_FOLDER)

        /* We are back to the app root folder layout - the trashed folder should not be visible */

        delay(3000)

        val showTrashedFilesMenuItem = resources.getString(R.string.menu_show_trashed_files)
        val parenStartPos = showTrashedFilesMenuItem.indexOf('(')

        menuShowTrashed.performMenuItemClick(showTrashedFilesMenuItem.substring(0, parenStartPos))

        /* We are back to the folder layout - the trashed folder should be visible */

        delay(2000)

        showFileDetailsViewForTestFolderName(R.id.folderFragmentLayoutId, testFolderName)

        /* Row with the folderName is selected */


        fileDetailRootView.checkIsDisplayed()

        /* File Detail screen shows */

        /* Delete test folder */

        fileDetail_TrashOrDelete.performClick()

        /* The test folder should not be in the adapter */

        testItemAgainstAdapterData(testFolderName, false)

        delay(2000)

        validateActionbarTitle(APP_ROOT_FOLDER)

        delay(2000)

        ActiveFlagsController.performEndOfTestValidations("createNewFolderInRootFolder")

    }

    /*                                  End of test code                                          */

    private val mDoNotSleep: Boolean = true
    private fun delay(msec: Int) {
        if (mDoNotSleep) {
            return
        }
        try {
            //            Log.v(TAG, "delay - sleep start");
            Thread.sleep(msec.toLong())
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    companion object {

//        private val TAG =  CreateTestFolderTest::class.simpleName
        private const val APP_ROOT_FOLDER =  "App folder"

    }
}