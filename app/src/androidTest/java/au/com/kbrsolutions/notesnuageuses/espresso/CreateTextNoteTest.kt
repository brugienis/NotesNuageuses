package au.com.kbrsolutions.notesnuageuses.espresso

import androidx.test.InstrumentationRegistry.getInstrumentation
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
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
import org.hamcrest.CoreMatchers.allOf
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

        clickOnTheListViewRowMatchingFileName(R.id.folderFragmentLayoutId, testFolderName)

        /* List view of the test folder shows - it is en empty folder */

        delay(1000)

        R.id.menuCreateFile.performMenuItemClick(resources.getString(R.string.menu_create_file))

        val date: LocalDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val formattedTimeNow = date.format(formatter)
        val testTextNoteName = "Text note $formattedTimeNow"
        val testTextNoteNameWithExtension = "$testTextNoteName.pntxt"

        R.id.createDialog_FileName.performTypeText(testTextNoteName)
        R.id.createDialog_CreateTextNote.performClick()

        /* Empty text note shows - it waits for some text to by typed                             */

        val testTextNoteContent = "Text note content $formattedTimeNow"
        R.id.fileFragmentTextId.performTypeText(testTextNoteContent)
        delay(2000)

        R.id.menuSaveOpenedFile.performClick()
        delay(2000)

        /* Text note is saved                                                                     */

        // Show text note content
        clickOnTheListViewRowMatchingFileName(R.id.folderFragmentLayoutId, testTextNoteNameWithExtension)
        delay(5000)

        // Verify the note content

        /* When verifying note content, I use originally withText(testTextNoteContent) and match
        failed. When I used debugger, I found that the content was 'testTextNoteContent\n'. Looks
         like the 'performTypeText()' added the new line. That explains why the
         withTextStartWithString(testTextNoteContent) worked fine */
        val testTextNoteContentExpected = "$testTextNoteContent\n"
        onView(
                allOf(
                        withId(R.id.fileFragmentTextId),
//                        withTextStartWithString(testTextNoteContent)
                         withText(testTextNoteContentExpected) // for some reason this is not working
                ))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        // Go back to test folder
        Espresso.pressBack()
        delay(5000)

        // Go back to the 'current' folder
        Espresso.pressBack()
        delay(5000)

        testFolderHandler.deleteTestFolder(testFolderName, APP_ROOT_FOLDER, resources)

        ActiveFlagsController.performEndOfTestValidations("createNewFolderInRootFolder")

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

    companion object {

//        private val TAG =  CreateTestFolderTest::class.simpleName
        private const val APP_ROOT_FOLDER =  "App folder"

    }
}