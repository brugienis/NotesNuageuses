package au.com.kbrsolutions.notesnuageuses.espresso.helpers

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.test.InstrumentationRegistry
import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.main.adapters.FolderItem
import org.hamcrest.*

/*
    check package androidx.test.espresso.matcher - HasSiblingMatcher
    (hasSibling(ViewMatchers.withText("some text"))), etc.
    public final class ViewMatchers.

    package org.hamcrest
    public class Matchers
*/

fun ViewInteraction.performClick(): ViewInteraction = perform(ViewActions.click())

fun Int.matchView(): ViewInteraction = Espresso.onView(ViewMatchers.withId(this))

fun Int.performClick(): ViewInteraction = matchView().performClick()

fun ViewInteraction.performTypeText(stringToBeTyped: String): ViewInteraction =
        perform(ViewActions.typeText(stringToBeTyped))

fun Int.performTypeText(stringToBeTyped: String): ViewInteraction =
        matchView().performTypeText(stringToBeTyped)

//-------------------------------------------------------------------------------

fun ViewInteraction.checkIsDisplayed(): ViewInteraction =
        check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

fun Int.checkIsDisplayed(): ViewInteraction = matchView().checkIsDisplayed()

//-------------------------------------------------------------------------------

fun validateActionbarTitle(expectedTitle: String) {
    val activityTitleTextView0 = Espresso.onView(
            Matchers.allOf(
                    childTextViewAtPosition(
                            ViewMatchers.withId(R.id.toolbar),
                            expectedTitle,
                            0),
                    ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    activityTitleTextView0.check(ViewAssertions.matches(ViewMatchers.withText(expectedTitle)))
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
    Log.v("EsspressoShortcuts <top>", """performMenuItemClick -
        |id:                  ${this}
        |menuItemText:        ${menuItemText}
        |isMenuItemDisplayed: $isMenuItemDisplayed
    """.trimMargin())
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
                return true
            }
            return false
        }
    }
}

//-------------------------------------------------------------------------------

fun clickOnTheListViewRowMatchingFileName(id:Int, testFolderName: String) {
    Espresso.onView(
            listRowWithFileName(
                    ViewMatchers.withId(id),
                    testFolderName))
            .perform(ViewActions.click())
}

/*
    matchesSafely(...) will return true on a first list view row, containing 'fileNameId' with
    text equal to 'fileName'.
 */
fun listRowWithFileName(
        parentMatcher: Matcher<View>,
        fileName: String): Matcher<View> {

    return object : TypeSafeMatcher<View>() {

        var rowFound = false
        var foundFirstInfoImageView: View? = null

        override fun describeTo(description: Description) {
            description.appendText("FileName with text: $fileName in parent ")
            parentMatcher.describeTo(description)
        }

        public override fun matchesSafely(view: View): Boolean {
            if (rowFound) return false
            if (view.id != R.id.fileNameId) return false
            val parent = view.parent
            if (parent == null || parent !is View) return false
            val fileNameView = parent.findViewById<View>(R.id.fileNameId)
            if (fileNameView == null || fileNameView !is TextView) return false
            val contentText = fileNameView.text ?: return false
            if (fileName == "Text note 2019-01-27 15:02.pntxt") {
                Log.v("<top>", """matchesSafely -
                |fileName:    ${fileName}
                |contentText: ${contentText}
                |
            """.trimMargin())
            }

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

//-------------------------------------------------------------------------------

/*
    Verify that adapter contains / doesn't contain 'item'
 */
fun testItemAgainstAdapterData(item: String, itemShouldBeInAdapter: Boolean) {
    if (itemShouldBeInAdapter) {
        Espresso.onView(ViewMatchers.withId(R.id.folderListView))
                .check(ViewAssertions.matches(withAdapterData(withFileName(
                        item))))
    } else {
        Espresso.onView(ViewMatchers.withId(R.id.folderListView))
                .check(ViewAssertions.matches(CoreMatchers.not(withAdapterData(withFileName(
                        item)))))
    }
}

/*
    Returns true if passed FolderItem contains 'filerName' equal to lookupFileName
 */
private fun withFileName(lookupFileName: String): Matcher<Any> {
    return object : TypeSafeMatcher<Any>() {

        override fun describeTo(description: Description) {
            description.appendText("with file name: $lookupFileName")
        }

        override fun matchesSafely(item: Any?): Boolean {
            if (item !is FolderItem) {
                return false
            }

            return lookupFileName == item.fileName
        }
    }
}

/*
    Returns true if adapter contains row that matched 'dataMatcher'
 */
private fun withAdapterData(dataMatcher: Matcher<Any>): Matcher<View> {
    return object : TypeSafeMatcher<View>() {

        override fun describeTo(description: Description) {
            description.appendText("adapter contains row: ")
            dataMatcher.describeTo(description)
        }

        public override fun matchesSafely(view: View) : Boolean {
            if (view !is AdapterView<*>) {
                return false
            }

            val adapter = view.adapter
            (0 until adapter.count).forEach { i ->
                if (dataMatcher.matches(adapter.getItem(i))) {
                    return true
                }
            }

            return false
        }
    }
}
