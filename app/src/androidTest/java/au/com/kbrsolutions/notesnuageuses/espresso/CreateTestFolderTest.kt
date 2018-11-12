package au.com.kbrsolutions.notesnuageuses.espresso

//import androidx.test.ui.app.LongListMatchers.withItemContent
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.test.InstrumentationRegistry.getInstrumentation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.espresso.helpers.WaitForFolderIsActiveInstruction
import au.com.kbrsolutions.notesnuageuses.features.espresso.ActiveFlagsController
import au.com.kbrsolutions.notesnuageuses.features.main.HomeActivity
import au.com.kbrsolutions.notesnuageuses.features.main.adapters.FolderItem
import com.azimolabs.conditionwatcher.ConditionWatcher
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class CreateTestFolderTest {

//    init {
//        ActiveFlagsController.setEspressoTestRunning()
//    }

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(HomeActivity::class.java)

    @Before
    fun launchActivity() {
        val device = UiDevice.getInstance(getInstrumentation())
        device.pressHome()

// Bring up the default launcher by searching for a UI component that matches the content
// description for the launcher button.
        val allAppsButton = device.findObject(UiSelector().description("NotesNuageuses"))

// Perform a click on the button to load the app launcher.
        allAppsButton.clickAndWaitForNewWindow()
        Log.v("CreateTestFolderTest", """XXX-launchActivity - start""")
        ActiveFlagsController.isEspressoTestRunning = true
        Log.v("CreateTestFolderTest", """XXX-launchActivity - end""")
    }

    @Test
    fun createNewFolderInRootFolder() {
        Log.v("CreateTestFolderTest", """XXX-createNewFolderInRootFolder - start""")

        ConditionWatcher.setTimeoutLimit(15 * 1000)

        ConditionWatcher.waitForCondition(WaitForFolderIsActiveInstruction())

        validateActionbarTitle("App folder")

        val actionMenuItemCreateView = onView(
                Matchers.allOf(
                        ViewMatchers.withId(R.id.menuCreateFile),
                        ViewMatchers.withContentDescription("Create"),
                        childAtPosition(
                                childAtPosition(
                                        ViewMatchers.withId(R.id.action_bar),
                                        2),
//                                        0),
                                0),
                        ViewMatchers.isDisplayed()
                ))

        actionMenuItemCreateView.perform(ViewActions.click())

//        delay(3000)

        val createDialogFileNameTestView = onView(
                Matchers.allOf(
                        ViewMatchers.withId(R.id.createDialogFileNameId),
                        ViewMatchers.isDisplayed()
                        ))

        val folderName = "Espresso folder"
        createDialogFileNameTestView.perform(ViewActions.typeText(folderName))

        delay(2000)

        val createDialogFolderButtonView = onView(
                Matchers.allOf(
                        ViewMatchers.withId(R.id.createDialogFolderId),
                        ViewMatchers.isDisplayed()
                        ))

        createDialogFolderButtonView.perform(ViewActions.click())

        delay(2000)

        testItemAgainstAdapterData(folderName, true)

        delay(2000)

        val removeIconImage = onView(infoImageOnRowWithFileName(
                ViewMatchers.withId(R.id.folderFragmentLayoutId) ,folderName)
        )

        Log.v("CreateTestFolderTest", """createNewFolderInRootFolder -
            |removeIconImage: $removeIconImage """.trimMargin())

        removeIconImage.perform(ViewActions.click())

        delay(3000)

        ActiveFlagsController.performEndOfTestMethodValidation("createNewFolderInRootFolder")

    }

    private fun validateActionbarTitle(expectedTitle: String) {
        val activityTitleTextView = onView(
                allOf(
                        childAtPosition(
                                allOf(
                                        withId(R.id.action_bar),
                                        childAtPosition(
                                                withId(R.id.action_bar_container),
                                                0)),
                                0),
                        isDisplayed())
        )
        activityTitleTextView.check(matches(withText(expectedTitle)))
    }

    private fun testItemAgainstAdapterData(item: String, isInAdapter: Boolean) {
        if (isInAdapter) {
            onView(withId(R.id.folderListView))
                    .check(matches(withAdaptedData(withItemContent(
                            item))))
        } else {
            onView(withId(R.id.folderListView))
                    .check(matches(not(withAdaptedData(withItemContent(
                            item)))))
        }
    }

    private fun withItemContent(itemTextMatcher: String): Matcher<Any> {
        return object : TypeSafeMatcher<Any>() {

            override fun describeTo(description: Description) {
                description.appendText("with class name: ")
//                itemTextMatcher.describeTo(description)
            }

            override fun matchesSafely(item: Any?): Boolean {
                Log.v("CreateTestFolderTest.withItemContent", """matchesSafely -
                    |item: $itemTextMatcher
                    |item: $item
                    |""".trimMargin())
                if (item !is String) {
                    Log.v("CreateTestFolderTest.withItemContent", """matchesSafely -
                    |returns: false ; item is not String""".trimMargin())
                    return false
                }

                Log.v("CreateTestFolderTest.withItemContent", """matchesSafely -
                    |returns: ${itemTextMatcher == item} """.trimMargin())
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
                var folderIitem: FolderItem? = null
                for (i in 0 until adapter.count) {
                    folderIitem = adapter.getItem(i) as FolderItem
                    Log.v("CreateTestFolderTest.withAdaptedData", """matchesSafely -
                        |file name:    $cnt ${folderIitem.fileName}
                        |""".trimMargin())
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
                val contentText = fileNameView.text
                if (contentText == null) return false

                if (
                        !rowFound &&
                        parent is ViewGroup &&
                        parentMatcher.matches(parent) &&
                        contentText == fileName) {
                    foundFirstInfoImageView = view
                    rowFound = true
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
                val parent = view.parent
                return parent is ViewGroup &&
                        parentMatcher.matches(parent) &&
                        view == parent.getChildAt(position)
            }
        }
    }

    private val mDoNotSleep = false
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
}