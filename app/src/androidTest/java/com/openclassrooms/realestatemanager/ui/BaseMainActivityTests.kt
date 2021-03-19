package com.openclassrooms.realestatemanager.ui

import android.app.Activity
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.api.property.FakePropertyApiService
import com.openclassrooms.realestatemanager.repository.property.FakePropertyRepository
import com.openclassrooms.realestatemanager.ui.BaseMainActivityTests.ScreenSize.*
import junit.framework.TestCase
import org.hamcrest.Description
import org.hamcrest.Matcher

abstract class BaseMainActivityTests: TestCase() {

    fun configureFakeApiService(
            propertiesDataSource: String? = null,
            networkDelay: Long? = null,
            application: TestBaseApplication,
    ): FakePropertyApiService {
        val apiService = (application.browseComponent()).apiService
        propertiesDataSource?.let { apiService.propertiesJsonFileName = it }
        networkDelay?.let { apiService.networkDelay = it }
        return apiService
    }

    fun configureFakeRepository(
            apiService: FakePropertyApiService,
            application: TestBaseApplication,
    ): FakePropertyRepository {
        val propertyRepository = (application.browseComponent())
                .propertyRepository
        propertyRepository.apiService = apiService
        return propertyRepository
    }

    fun <T : Activity> ActivityScenario<T>.getToolbarNavigationContentDescription()
            : String {
        var description = ""
        onActivity {
            description =
                    it.findViewById<Toolbar>(R.id.tool_bar).navigationContentDescription as String
        }
        return description
    }

    fun withBottomNavItemCheckedStatus(isChecked: Boolean): Matcher<View?> {
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

    companion object {
        lateinit var mainActivity : FragmentActivity

        fun screenSize() : ScreenSize {
            val smallestScreen = mainActivity.applicationContext.resources.configuration.smallestScreenWidthDp
            return when {
                smallestScreen >= 720 -> {
                    TABLET
                }
                smallestScreen >= 600 -> {
                    PHABLET
                }
                smallestScreen < 600 -> {
                    SMARTPHONE
                }
                else -> UNDEFINED
            }
        }
    }

    enum class ScreenSize {
        SMARTPHONE, PHABLET, TABLET, UNDEFINED
    }

    abstract fun injectTest(application: TestBaseApplication)
}