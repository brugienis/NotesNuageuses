package au.com.kbrsolutions.notesnuageuses.espresso.helpers

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.test.InstrumentationRegistry
import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import au.com.kbrsolutions.notesnuageuses.R
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher

fun ViewInteraction.performClick(): ViewInteraction = perform(ViewActions.click())

fun Int.matchView(): ViewInteraction = Espresso.onView(ViewMatchers.withId(this))

fun Int.performClick(): ViewInteraction = matchView().performClick()

//-------------------------------------------------------------------------------

fun ViewInteraction.checkIsDisplayed(): ViewInteraction =
        check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

fun Int.checkIsDisplayed(): ViewInteraction = matchView().checkIsDisplayed()

//-------------------------------------------------------------------------------

fun validateActionbarTitle(expectedTitle: String) {
    Log.v("CreateTestFolderTest", """validateActionbarTitle - start
            |expectedTitle: ${expectedTitle}
            |""".trimMargin())
    val activityTitleTextView0 = Espresso.onView(
            Matchers.allOf(
                    childTextViewAtPosition(
                            ViewMatchers.withId(R.id.toolbar),
                            expectedTitle,
                            0),
                    ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    Log.v("CreateTestFolderTest", """validateActionbarTitle - end
            |expectedTitle: ${expectedTitle}
            |""".trimMargin())
    activityTitleTextView0.check(ViewAssertions.matches(ViewMatchers.withText(expectedTitle)))

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

//-------------------------------------------------------------------------------


fun childTextViewAtPosition(
        parentMatcher: Matcher<View>,
        expectedText: String,
        position: Int): Matcher<View> {
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
            /*if (lastExpectedString != expectedText) {
                lastExpectedString = expectedText
                rowFound = false
            }*/
            if (view !is TextView) {
                return false
            }
            val parent = view.parent

            /*if (parent is ViewGroup && parent.id == R.id.toolbar) {
                for (pos in 0.. parent.childCount) {
                    Log.v("CreateTestFolderTest", """childTextViewAtPosition.matchesSafely -
                            |parent:   $parent
                            |pos:      $pos
                            |child:    ${parent.getChildAt(pos)}
                            |""".trimMargin())
                }
            }*/
            if (parent is ViewGroup &&
                    parentMatcher.matches(parent) &&
                    view == parent.getChildAt(position) &&
                    view.text.toString() == expectedText) {

                /*val child = parent.getChildAt(position) as TextView
                Log.v("CreateTestFolderTest", """childTextViewAtPosition.matchesSafely - match found
                            |parent:        $parent
                            |parentMatcher: $parentMatcher
                            |position:      $position
                            |child's:       ${child.text}; id: ${child.id}
                            |view's text:   ${view.text};  id: ${view.id}
                            |""".trimMargin())*/

                return true
            }
            return false

        }
    }
}

//-------------------------------------------------------------------------------

var isMenuItemDisplayed = true
fun Int.performMenuItemClick(menuItemText: String) {
    Espresso.onView(ViewMatchers.withId(this))
            .withFailureHandler { error, viewMatcher -> isMenuItemDisplayed = false }
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .perform(ViewActions.click())

    /* If the 'sMenuItemDisplayed' was not found in the ActionBar, try to find it in the      */
    /* overflow view, and clicked on it.                                                      */
    if (!isMenuItemDisplayed) {
        val overflowViewId = R.id.title
        try {
            Espresso.openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext())
        } catch (e: Exception) {
            //This is normal. Maybe we don't have overflow menu.
        }

        Espresso.onView(
                Matchers.allOf(
                        ViewMatchers.withId(overflowViewId),
                        withTextStartWithString(menuItemText)
                ))
                .perform(ViewActions.click())
    }
}

// fixLater: Nov 15, 2018 - correct descriptions below
fun withTextStartWithString(itemTextMatcher: String): Matcher<Any> {
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

//-------------------------------------------------------------------------------

fun showFileDetailsViewForTestFolderName(id:Int, testFolderName: String) {
    Espresso.onView(
            infoImageOnRowWithFileName(
                    ViewMatchers.withId(id),
                    testFolderName))
            .perform(ViewActions.click())
}

/*
    matchesSafely(...) will return true on a first list view row, containing 'infoImageId' view
    and a 'fileNameId' with text equal to 'fileName'.
 */
fun infoImageOnRowWithFileName(
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
                Log.v("CreateTestFolderTest", """matchesSafely - found view with text
                        |contentText: ${contentText} """.trimMargin())
                return true
            }
            return false
        }
    }
}
