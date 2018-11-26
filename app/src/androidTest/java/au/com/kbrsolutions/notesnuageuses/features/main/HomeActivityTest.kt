package au.com.kbrsolutions.notesnuageuses.features.main


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import au.com.kbrsolutions.notesnuageuses.R
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class HomeActivityTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(HomeActivity::class.java)

    @Before
    fun launchActivity() {
//        ActiveFlagsController.setEspressoTestRunning()
//        ActivityScenario.launch(HomeActivity::class.java)
    }

    @Test
    fun homeActivityTest() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(7000)

        val actionMenuItemView = onView(
                allOf(withId(R.id.menuCreateFile), withContentDescription("Create"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.action_bar),
                                        2),
                                0),
                        isDisplayed()))
        actionMenuItemView.perform(click())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(7000)

        val appCompatEditText = onView(
                allOf(withId(R.id.createDialog_FileName),
                        childAtPosition(
                                allOf(withId(R.id.locationDialogRootViewId),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                2),
                        isDisplayed()))
        appCompatEditText.perform(replaceText("Melbourne"), closeSoftKeyboard())

        val appCompatTextView = onView(
                allOf(withId(R.id.createDialog_CreateFolder), withText("Folder"),
                        childAtPosition(
                                allOf(withId(R.id.locationDialogRootViewId),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                4),
                        isDisplayed()))
        appCompatTextView.perform(click())

        val textView = onView(
                allOf(withId(R.id.fileNameId), withText("Melbourne"),
                        childAtPosition(
                                allOf(withId(R.id.folderFragmentLayoutId),
                                        childAtPosition(
                                                withId(android.R.id.list),
                                                0)),
                                1),
                        isDisplayed()))
        textView.check(matches(withText("Melbourne")))
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
}
