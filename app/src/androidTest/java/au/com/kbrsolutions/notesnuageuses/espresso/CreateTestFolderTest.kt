package au.com.kbrsolutions.notesnuageuses.espresso

import android.util.Log.v
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.test.InstrumentationRegistry
import androidx.test.InstrumentationRegistry.getInstrumentation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.FailureHandler
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.R.id.createDialogFolderId
import au.com.kbrsolutions.notesnuageuses.espresso.helpers.WaitForFolderIsActiveInstruction
import au.com.kbrsolutions.notesnuageuses.espresso.helpers.performClick
import au.com.kbrsolutions.notesnuageuses.features.espresso.ActiveFlagsController
import au.com.kbrsolutions.notesnuageuses.features.main.HomeActivity
import au.com.kbrsolutions.notesnuageuses.features.main.adapters.FolderItem
import com.azimolabs.conditionwatcher.ConditionWatcher
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@LargeTest
@RunWith(AndroidJUnit4::class)
class CreateTestFolderTest {

//    init {
//        ActiveFlagsController.setEspressoTestRunning()
//    }

    // check package androidx.test.espresso.matcher
    // public final class ViewMatchers.

    // package org.hamcrest
    // public class Matchers

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(HomeActivity::class.java)

    /*
        It looks like the app starts as soon
        as the var mActivityTestRule = ActivityTestRule(HomeActivity::class.java) is executed. That
        makes the first test's results unpredictable - sometimes it will succeeds, other times it fails.

        Before this test starts,the 'device.pressHome()' clicks on the 'home' button'. And then the
        app is started.
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
        v("CreateTestFolderTest", """XXX-launchActivity - start""")
        ActiveFlagsController.isEspressoTestRunning = true
        v("CreateTestFolderTest", """XXX-launchActivity - end""")
    }

    @Test
    fun createNewFolderInRootFolder() {
        v("CreateTestFolderTest", """XXX-createNewFolderInRootFolder - start""")
        val resources = mActivityTestRule.activity.applicationContext.resources

        val date: LocalDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val formattedTimeNow = date.format(formatter)
        val testFolderName = "Espresso test $formattedTimeNow"
        v("CreateTestFolderTest", """createNewFolderInRootFolder -
            |testFolderName: ${testFolderName} """.trimMargin())

        ConditionWatcher.setTimeoutLimit(15 * 1000)

        ConditionWatcher.waitForCondition(WaitForFolderIsActiveInstruction())

        validateActionbarTitle("App folder")

        clickMenuItem(
                R.id.menuCreateFile,
                resources.getString(R.string.menu_create_file)
        )

        val createDialogFileNameTestView = onView(
                Matchers.allOf(
                        withId(R.id.createDialogFileNameId),
                        isDisplayed()
                        ))

        createDialogFileNameTestView.perform(ViewActions.typeText(testFolderName))

        delay(2000)

        createDialogFolderId.performClick()

        /*val createDialogFolderButtonView = onView(
                Matchers.allOf(
                        withId(R.id.createDialogFolderId),
                        isDisplayed()
                        ))

        createDialogFolderButtonView.perform(ViewActions.click())*/

        validateActionbarTitle(testFolderName)

        /*                               Folder was created                                       */

        delay(2000)

        testItemAgainstAdapterData(testFolderName, true)

        delay(2000)

        val fileInfoIconImage = onView(
                infoImageOnRowWithFileName(
                        ViewMatchers.withId(R.id.folderFragmentLayoutId),
                        testFolderName)
        )

        fileInfoIconImage.perform(ViewActions.click())
        v("CreateTestFolderTest", """createNewFolderInRootFolder
            | after - fileInfoIconImage.perform(ViewActions.click())
            | """.trimMargin())
        /* Row with the folderName just created is now selected */

        delay(3000)

        onView(
                Matchers.allOf(
                        withId(R.id.fileDetailRootViewId),
//                        hasSibling(ViewMatchers.withText("item: 0")),
                        isDisplayed()
                ))
                .check(matches(isDisplayed()))

        /*                            File Info screen shows                                      */

        delay(3000)

        v("CreateTestFolderTest", """createNewFolderInRootFolder -
            |before click on the fileDetailTrashDeleteLayoutId """.trimMargin())
        /* Trash created folder */
        onView(
                Matchers.allOf(
                        withId(R.id.fileDetailTrashDeleteLayoutId),
                        isDisplayed()
                ))
                .check(matches(isDisplayed()))
                .perform(click())

        /* We are back to the folder layout - the trashed folder should not be visible */

        delay(3000)

        val showTrashedFilesMenuItem = resources.getString(R.string.menu_show_trashed_files)
        val parenStartPos = showTrashedFilesMenuItem.indexOf('(')
        clickMenuItem(
                R.id.menuShowTrashed,
                showTrashedFilesMenuItem.substring(0, parenStartPos)
        )

        /* We are back to the folder layout - the trashed folder should be visible */

        delay(2000)

        onView(infoImageOnRowWithFileName(
                ViewMatchers.withId(R.id.folderFragmentLayoutId), testFolderName))
                .perform(ViewActions.click())

        /* Row with the folderName is selected */

        onView(
                Matchers.allOf(
                        withId(R.id.fileDetailRootViewId),
                        isDisplayed()
                ))
                .check(matches(isDisplayed()))

        /* File Info screen shows */

        /* Delete folder */
        onView(
                Matchers.allOf(
                        withId(R.id.fileDetailTrashDeleteLayoutId),
                        isDisplayed()
                ))
                .check(matches(isDisplayed()))
                .perform(click())

        /* The test folder should not be in the adapter */
        testItemAgainstAdapterData(testFolderName, false)


//        add test to very the folder is not in the list view

        delay(2000)

        validateActionbarTitle("App folder")

        delay(2000)

        ActiveFlagsController.performEndOfTestMethodValidation("createNewFolderInRootFolder")

    }

    /*                                  End of test code                                          */

    private fun validateActionbarTitle(expectedTitle: String) {
        v("CreateTestFolderTest", """validateActionbarTitle - start
            |expectedTitle: ${expectedTitle}
            |""".trimMargin())
        val activityTitleTextView0 = onView(
                Matchers.allOf(
                        childTextViewAtPosition(
                                withId(R.id.toolbar),
                                expectedTitle,
                                0),
                        isDisplayed()))
                .check(matches(isDisplayed()))
        v("CreateTestFolderTest", """validateActionbarTitle - end
            |expectedTitle: ${expectedTitle}
            |""".trimMargin())
        activityTitleTextView0.check(matches(withText(expectedTitle)))

        /*val activityTitleTextView = onView(
                Matchers.allOf(


                        childAtPosition(
                                Matchers.allOf(
//                                        withId(R.id.action_bar),
                                        withId(R.id.appbar),
                                        childAtPosition(
//                                                withId(R.id.action_bar_container),
                                                withId(R.id.toolbar),
                                                0)),
                                1),
                        isDisplayed())
        )
        activityTitleTextView.check(matches(withText(expectedTitle)))*/
    }

    private fun clickMenuItem(menuItemId: Int, menuItemText: String) {
        /* Find menuItemId and click on it, if it is displayed (is visible in the ActionBar). If it
        is not displayed, set the isMenuItemDisplayed - false.    */
        var isMenuItemDisplayed = true
        onView(withId(menuItemId))
                .withFailureHandler(object : FailureHandler {
                    override fun handle(error: Throwable, viewMatcher: Matcher<View>) {
                        isMenuItemDisplayed = false
                    }
                })
                .check(matches(isDisplayed()))
                .perform(click())


        /* If the 'sMenuItemDisplayed' was not found in the ActionBar, try to find it in the      */
        /* overflow view, and clicked on it.                                                      */
        if (!isMenuItemDisplayed) {
            val overflowViewId = R.id.title
            try {
                openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext())
            } catch (e: Exception) {
                //This is normal. Maybe we don't have overflow menu.
            }

            onView(
                    Matchers.allOf(
                            withId(overflowViewId),
                            withTextStartWithString(menuItemText)
                            ))
                    .perform(click())
        }
    }

    private fun testItemAgainstAdapterData(item: String, itemShouldBeInAdapter: Boolean) {
        if (itemShouldBeInAdapter) {
            onView(withId(R.id.folderListView))
                    .check(matches(withAdaptedData(withItemContent(
                            item))))
        } else {
            onView(withId(R.id.folderListView))
                    .check(matches(not(withAdaptedData(withItemContent(
                            item)))))
        }
    }

    // fixLater: Nov 15, 2018 - correct descriptions below
    private fun withTextStartWithString(itemTextMatcher: String): Matcher<Any> {
        return object : TypeSafeMatcher<Any>() {

            override fun describeTo(description: Description) {
                description.appendText("with text beginning with: $itemTextMatcher")
            }

            override fun matchesSafely(item: Any?): Boolean {
                if (item !is TextView) {
                    return false
                }

                return item.text.toString().startsWith(itemTextMatcher)
            }
        }
    }

    // fixLater: Nov 15, 2018 - correct descriptions below
    private fun withItemContent(itemTextMatcher: String): Matcher<Any> {
        return object : TypeSafeMatcher<Any>() {

            override fun describeTo(description: Description) {
                description.appendText("with class name: ")
            }

            override fun matchesSafely(item: Any?): Boolean {
                if (item !is String) {
                    return false
                }

                return itemTextMatcher == item
            }
        }
    }

    private fun withAdaptedData(dataMatcher: Matcher<Any>): Matcher<View> {
        return object : TypeSafeMatcher<View>() {

            override fun describeTo(description: Description) {
                description.appendText("with class name: ")
                dataMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View) : Boolean {
                if (view !is AdapterView<*>) {
                    return false
                }

                val adapter = view.adapter
                var cnt = 0
                var folderIitem: FolderItem?
                for (i in 0 until adapter.count) {
                    folderIitem = adapter.getItem(i) as FolderItem
                    cnt++
                    if (dataMatcher.matches(folderIitem.fileName)) {
                        return true
                    }
                }

                return false
            }
        }
    }

    /*
        matchesSafely(...) will return true on a first list view row, containing 'infoImageId' view
        and a 'fileNameId' with text equal to 'fileName'.
     */
    private fun infoImageOnRowWithFileName(
            parentMatcher: Matcher<View>,
            fileName: String): Matcher<View> {

        return object : TypeSafeMatcher<View>() {

            var rowFound = false
            var foundFirstInfoImageView: View? = null

            override fun describeTo(description: Description) {
                description.appendText("FileName with text $fileName in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                if (rowFound) return false
                if (view.id != R.id.infoImageId) return false
                val parent = view.parent
                if (parent == null || parent !is View) return false
                val fileNameView = parent.findViewById<View>(R.id.fileNameId)
                if (fileNameView == null || fileNameView !is TextView) return false
                val contentText = fileNameView.text ?: return false

                if (
                        !rowFound &&
                        parent is ViewGroup &&
                        parentMatcher.matches(parent) &&
                        contentText == fileName) {
                    foundFirstInfoImageView = view
                    rowFound = true
                    v("CreateTestFolderTest", """matchesSafely - found view with text
                        |contentText: ${contentText} """.trimMargin())
                    return true
                }
                return false
            }
        }
    }

    private fun childAtPosition(parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                v("CreateTestFolderTest", """childAtPosition.matchesSafely - start
                            |view: ${view}
                            |""".trimMargin())
                val parent = view.parent
                if (view is ViewGroup && view.id == R.id.appbar) {
                    val childCount = view.childCount
                    for (i in 0 until childCount) {
                        view.getChildAt(i)
                        v("CreateTestFolderTest", """childAtPosition.matchesSafely -
                            |i:    ${i}
                            |view: ${view}
                            |""".trimMargin())
                    }
                }
                if (parent is ViewGroup && view.id == R.id.toolbar) {
                    val childCount = parent.childCount
                    parent.getChildAt(position)
                    v("CreateTestFolderTest", """childAtPosition.matchesSafely -
                            |parent: ${parent}
                            |""".trimMargin())
                }
                return parent is ViewGroup &&
                        parentMatcher.matches(parent) &&
                        view == parent.getChildAt(position)
            }
        }
    }

    private fun childTextViewAtPosition(
            parentMatcher: Matcher<View>,
            expectedText: String,
            position: Int): Matcher<View> {
        var lastExpectedString = ""
        return object : TypeSafeMatcher<View>() {

            var rowFound = false
            override fun describeTo(description: Description) {
                description.appendText("""Child at position: $position
                    | with expected text: $expectedText in parent """.trimMargin())
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                /*v("CreateTestFolderTest", """matchesSafely -
                    |lastExpectedString: ${lastExpectedString}
                    |eExpectedString:    ${expectedText}
                    |""".trimMargin())*/
                if (lastExpectedString != expectedText) {
                    lastExpectedString = expectedText
                    rowFound = false
                }
                if (rowFound) return false
                if (view !is TextView) {
                    return false
                }
                val parent = view.parent

                /*if (parent is ViewGroup && view.id == R.id.toolbar) {
                    val childCount = parent.childCount
                    parent.getChildAt(position)
                    Log.v("CreateTestFolderTest", """childTextViewAtPosition.matchesSafely -
                            |parent:   ${parent}
                            |position: ${position}
                            |child:    ${parent.getChildAt(position)}
                            |""".trimMargin())
                }*/
                if (parent is ViewGroup &&
                        parentMatcher.matches(parent) &&
                        view == parent.getChildAt(position) &&
                        view.text.toString() == expectedText) {

                    val child = parent.getChildAt(position) as TextView
                    v("CreateTestFolderTest", """childTextViewAtPosition.matchesSafely -
                            |parent:        $parent
                            |parentMatcher: $parentMatcher
                            |position:      $position
                            |child's:       ${child.text}; id: ${child.id}
                            |view's text:   ${view.text};  id: ${view.id}
                            |""".trimMargin())

//                    rowFound = true
                    return true
                }
                return false

            }
        }
    }

    private val mDoNotSleep: Boolean = false
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

        private val TAG =  CreateTestFolderTest::class.simpleName
    }
}