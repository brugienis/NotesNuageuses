package au.com.kbrsolutions.notesnuageuses.espresso

import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import android.view.View
import android.view.ViewGroup
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.espresso.helpers.WaitForFolderIsActiveInstruction
import au.com.kbrsolutions.notesnuageuses.features.main.HomeActivity
import com.azimolabs.conditionwatcher.ConditionWatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class CreateTestFolderTest {


    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(HomeActivity::class.java)

    @Test
    fun createNewFolderInRootFolder() {
        Log.v("CreateTestFolderTest", """createNewFolderInRootFolder -
            | mActivityTestRule:          $mActivityTestRule
            | mActivityTestRule.activity: ${mActivityTestRule.activity}
            | """.trimMargin())

        ConditionWatcher.setTimeoutLimit(15 * 1000)

        ConditionWatcher.waitForCondition(
                WaitForFolderIsActiveInstruction(mActivityTestRule.activity))

        val actionMenuItemView = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(R.id.menuCreateFile), ViewMatchers.withContentDescription("Create"),
                        childAtPosition(
                                childAtPosition(
                                        ViewMatchers.withId(R.id.action_bar),
                                        2),
                                0),
                        ViewMatchers.isDisplayed()))
        actionMenuItemView.perform(ViewActions.click())

        delay(3000)

    }

    private fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {

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