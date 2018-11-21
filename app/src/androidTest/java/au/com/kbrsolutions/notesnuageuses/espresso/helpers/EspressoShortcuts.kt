package au.com.kbrsolutions.notesnuageuses.espresso.helpers

import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers

    fun ViewInteraction.performClick() = perform(ViewActions.click())

    fun Int.matchView(): ViewInteraction = Espresso.onView(ViewMatchers.withId(this))

    fun Int.performClick() = matchView().performClick()