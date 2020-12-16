package com.openclassrooms.realestatemanager.view

import android.app.Activity
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.openclassrooms.realestatemanager.R
import junit.framework.TestCase
import org.hamcrest.Description
import org.hamcrest.Matcher

abstract class BaseMainActivityTests: TestCase() {

    fun <T : Activity> ActivityScenario<T>.getToolbarNavigationContentDescription()
            : String {
        var description = ""
        onActivity {
            description =
                    it.findViewById<Toolbar>(R.id.tool_bar).navigationContentDescription as String
        }
        return description
    }

    fun withBottomNavItemCheckedStatus(isChecked: Boolean): Matcher<View?>? {
        return object : BoundedMatcher<View?, BottomNavigationItemView>(BottomNavigationItemView::class.java) {
            var triedMatching = false
            override fun describeTo(description: Description) {
                if (triedMatching) {
                    description.appendText("with BottomNavigationItem check status: $isChecked")
                }
            }

            override fun matchesSafely(item: BottomNavigationItemView): Boolean {
                triedMatching = true
                return item.itemData.isChecked == isChecked
            }
        }
    }

    fun waitFor(delay: Long): ViewAction {
        return object : ViewAction {
            override fun perform(uiController: UiController?, view: View?) {
                uiController?.loopMainThreadForAtLeast(delay)
            }

            override fun getConstraints(): Matcher<View> {
                return ViewMatchers.isRoot()
            }

            override fun getDescription(): String {
                return "wait for " + delay + "milliseconds"
            }
        }
    }
}