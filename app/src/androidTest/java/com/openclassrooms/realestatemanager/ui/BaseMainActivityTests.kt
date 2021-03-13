package com.openclassrooms.realestatemanager.ui

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.api.property.FakePropertyApiService
import com.openclassrooms.realestatemanager.repository.property.FakePropertyRepository
import com.openclassrooms.realestatemanager.ui.BaseMainActivityTests.ScreenSize.*
import com.openclassrooms.realestatemanager.util.Constants.TIMEOUT_INTERNET_CONNECTION
import com.openclassrooms.realestatemanager.util.Utils.isInternetAvailable
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import junit.framework.TestCase
import org.hamcrest.Description
import org.hamcrest.Matcher
import java.util.concurrent.TimeUnit

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
        private var compositeDisposable: CompositeDisposable = CompositeDisposable()

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

        fun switchAllNetworks(enabled: Boolean) : Completable {
            return Completable.create { emitter ->
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                        allNetworksEnableLollipopMinSdkVersion(enabled = enabled)
                    }
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP -> {
                        allNetworksEnableKitKatMaxSdkVersion(enabled = enabled)
                    }
                }
                emitter.onComplete()
            }
        }

        fun waitInternetStateChange(isInternetAvailable: Boolean) : Completable {
            return Completable.create { emitter ->
                compositeDisposable.add(Single.fromCallable { isInternetAvailable() }
                        .subscribeOn(Schedulers.io())
                        .repeat()
                        .skipWhile { it != isInternetAvailable }
                        .take(1)
                        .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
                        .subscribe {
                            emitter.onComplete()
                            compositeDisposable.clear()
                        })
            }
        }

        @SdkSuppress(minSdkVersion = Build.VERSION_CODES.LOLLIPOP)
        private fun allNetworksEnableLollipopMinSdkVersion(enabled: Boolean) {
            when(enabled) {
                true -> {
                    switchWifiLollipopMinSdkVersion(true)
                    switchMobileDataLollipopMinSdkVersion(true)
                }

                false -> {
                    switchWifiLollipopMinSdkVersion(false)
                    switchMobileDataLollipopMinSdkVersion(false)
                }
            }
        }

        @SdkSuppress(minSdkVersion = Build.VERSION_CODES.LOLLIPOP)
        fun switchWifiLollipopMinSdkVersion(enabled: Boolean) {
            val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
            when(enabled) {
                true -> {
                    uiAutomation.executeShellCommand("svc wifi enable")
                }

                false -> {
                    uiAutomation.executeShellCommand("svc wifi disable")
                }
            }
        }

        @SdkSuppress(minSdkVersion = Build.VERSION_CODES.LOLLIPOP)
        fun switchMobileDataLollipopMinSdkVersion(enabled: Boolean) {
            val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
            when(enabled) {
                true -> {
                    uiAutomation.executeShellCommand("svc data enable")
                }

                false -> {
                    uiAutomation.executeShellCommand("svc data disable")
                }
            }
        }

        @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.KITKAT_WATCH)
        private fun allNetworksEnableKitKatMaxSdkVersion(enabled: Boolean) {
            when(enabled) {
                true -> {
                    switchAllNetworksDataKitKatMaxSdkVersion(true)
                }

                false -> {
                    switchAllNetworksDataKitKatMaxSdkVersion(false)
                }
            }
        }

        @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.KITKAT_WATCH)
        fun switchAllNetworksDataKitKatMaxSdkVersion(enabled: Boolean) {
            try {
                val connectivityManager = mainActivity.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val connectivityManagerClass = Class.forName(connectivityManager.javaClass.name)
                val iConnectivityManagerField = connectivityManagerClass.getDeclaredField("mService")
                iConnectivityManagerField.isAccessible = true
                val iConnectivityManager = iConnectivityManagerField[connectivityManager]
                val iConnectivityManagerClass = Class.forName(iConnectivityManager.javaClass.name)
                val setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", java.lang.Boolean.TYPE)
                setMobileDataEnabledMethod.isAccessible = true
                setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    enum class ScreenSize {
        SMARTPHONE, PHABLET, TABLET, UNDEFINED
    }

    abstract fun injectTest(application: TestBaseApplication)
}