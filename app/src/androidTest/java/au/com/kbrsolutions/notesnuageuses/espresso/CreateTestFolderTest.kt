package au.com.kbrsolutions.notesnuageuses.espresso

import android.util.Log
import android.view.View
import android.view.ViewGroup
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
import com.azimolabs.conditionwatcher.ConditionWatcher
import org.hamcrest.CoreMatchers.allOf
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

        val actionCreateFolderView = onView(
                Matchers.allOf(
//                        ViewMatchers.withId(R.id.locationDialogRootViewId),
                        ViewMatchers.withId(R.id.createDialogFileNameId),
//                        childAtPosition( ViewMatchers.withId(R.id.createDialogFileNameId), 2),
                        ViewMatchers.isDisplayed()
                        ))

        Log.v("CreateTestFolderTest", """XXX-createNewFolderInRootFolder -
            |actionCreateFolderView: $actionCreateFolderView """.trimMargin())

        actionCreateFolderView.perform(ViewActions.typeText("Espresso folder"))

        delay(3000)

        ActiveFlagsController.performEndOfTestMethodValidation("createNewFolderInRootFolder")

    }

    private fun validateActionbarTitle(expectedTitle: String) {
        val activityTitleTextView = onView(
                allOf(
                        childAtPosition(
                                allOf(withId(R.id.action_bar),
                                        childAtPosition(
                                                withId(R.id.action_bar_container),
                                                0)),
                                0),
                        isDisplayed()))
        activityTitleTextView.check(matches(withText(expectedTitle)))
    }

    private fun childAtPosition(parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
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