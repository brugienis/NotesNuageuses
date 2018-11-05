package au.com.kbrsolutions.notesnuageuses.espresso

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import au.com.kbrsolutions.notesnuageuses.R
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
        Log.v("CreateTestFolderTest", """launchActivity - start""")
        ActiveFlagsController.setEspressoTestRunning()
//        ActivityScenario.launch(HomeActivity::class.java)
        Log.v("CreateTestFolderTest", """launchActivity - end""")
    }

    @Test
    fun createNewFolderInRootFolder() {
//        ActiveFlagsController.setEspressoTestRunning()
        Log.v("CreateTestFolderTest", """createNewFolderInRootFolder - start""")

        ConditionWatcher.setTimeoutLimit(15 * 1000)

//        ConditionWatcher.waitForCondition(WaitForFolderIsActiveInstruction())

        validateActionbarTitle("App folder")

        val actionMenuItemView = onView(
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

        actionMenuItemView.perform(ViewActions.click())

        delay(3000)

//        ActiveFlagsController.performEndOfTestMethodValidation("createNewFolderInRootFolder")

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

        //        Log.v(TAG, "delay - sleep stop");
    }
}