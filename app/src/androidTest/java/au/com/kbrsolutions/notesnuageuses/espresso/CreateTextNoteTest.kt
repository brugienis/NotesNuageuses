package au.com.kbrsolutions.notesnuageuses.espresso

import android.util.Log
import androidx.test.InstrumentationRegistry.getInstrumentation
import androidx.test.espresso.Espresso
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import au.com.kbrsolutions.notesnuageuses.R
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
class CreateTextNoteTest {

    val date: LocalDateTime = LocalDateTime.now()
    //        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")
    val formattedTimeNow = date.format(formatter)
    val testFolderName = "Test $formattedTimeNow"

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

        ConditionWatcher.setTimeoutLimit(15 * 1000)

        ConditionWatcher.waitForCondition(WaitForFolderIsActiveInstruction())

        val resources = mActivityTestRule.activity.applicationContext.resources

        val testFolderHandler = TestFolderHandler()

        testFolderHandler.createTestFolder(testFolderName, APP_ROOT_FOLDER, resources)

        /*                      Root folder files list shows                                      */

        showTestFolderFiles(R.id.folderFragmentLayoutId, testFolderName)

        Log.v("CreateTextNoteTest", """createNewFolderInRootFolder -
        after click on the test folder
        """)

        /* Row with the folderName just created is now selected */

        delay(3000)

        Log.v("CreateTextNoteTest", """createNewFolderInRootFolder -
        before  R.id.menuCreateFile.performMenuItemClick()
        """)
        delay(5000)

        R.id.menuCreateFile.performMenuItemClick(resources.getString(R.string.menu_create_file))

        val date: LocalDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val formattedTimeNow = date.format(formatter)
        val testTextNoteName = "Text note $formattedTimeNow"

        R.id.createDialog_FileName.performTypeText(testTextNoteName)
        R.id.createDialog_CreateTextNote.performClick()

        // fixLater: Jan 27, 2019 - type note text
        val testTextNoteContent = "Text note content $formattedTimeNow"
        R.id.fileFragmentTextId.performTypeText(testTextNoteContent)
        Log.v("CreateTextNoteTest", """createNewFolderInRootFolder -
        before  R.id.menuSaveOpenedFile.performClick()
        """)
        delay(5000)

        R.id.menuSaveOpenedFile.performClick()

        Log.v("CreateTextNoteTest", """createNewFolderInRootFolder -
        after   R.id.menuSaveOpenedFile.performClick()
        """)
        delay(5000)

        Espresso.pressBack()
        delay(5000)

        testFolderHandler.deleteTestFolder(testFolderName, APP_ROOT_FOLDER, resources)

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