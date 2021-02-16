package com.openclassrooms.realestatemanager.repository

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.ui.BaseMainActivityTests
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.util.Constants.TIMEOUT_INTERNET_CONNECTION
import com.openclassrooms.realestatemanager.util.ConstantsTest
import com.openclassrooms.realestatemanager.util.NetworkConnectionLiveData
import com.openclassrooms.realestatemanager.util.Utils.isInternetAvailable
import io.reactivex.Completable
import io.reactivex.Completable.concatArray
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@MediumTest
class ConnectivityManagerTest : BaseMainActivityTests() {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Inject
    lateinit var networkConnectionLiveData: NetworkConnectionLiveData

    private lateinit var compositeDisposable: CompositeDisposable

    private lateinit var activityScenario: ActivityScenario<MainActivity>
    private lateinit var app : TestBaseApplication

    @Before
    public override fun setUp() {
        super.setUp()
        app = InstrumentationRegistry.getInstrumentation()
                .targetContext.applicationContext as TestBaseApplication
        compositeDisposable = CompositeDisposable()

        val apiService = configureFakeApiService(
                propertiesDataSource = ConstantsTest.EMPTY_LIST, // empty list of data
                networkDelay = 0L,
                application = app
        )

        configureFakeRepository(apiService, app)

        injectTest(app)

        activityScenario = ActivityScenario.launch(MainActivity::class.java)
                .onActivity { activity ->
                    mainActivity = activity
                    if(!networkConnectionLiveData.value!!) {
                        concatArray(switchNetworks(true),
                                waitInternetStateChange(true))
                                .blockingAwait()
                    }
                }
    }

    @After
    public override fun tearDown() {
        if(!networkConnectionLiveData.value!!) {
            concatArray(switchNetworks(true),
                        waitInternetStateChange(true))
                    .blockingAwait().let {
                        if(networkConnectionLiveData.hasObservers()) {
                            networkConnectionLiveData.removeObservers(mainActivity)
                        }
                        super.tearDown()
                    }
        } else {
            super.tearDown()
        }
    }

    @Test
    fun verify_when_connection_is_available() {
        networkConnectionLiveData.observe(mainActivity, {})

        concatArray(switchNetworks(false),
                waitInternetStateChange(false))
                .blockingAwait()
                .let { assertThat(networkConnectionLiveData.value).isFalse() }

        concatArray(switchNetworks(true),
                waitInternetStateChange(true))
                .blockingAwait()
                .let { assertThat(networkConnectionLiveData.value).isTrue() }

    }

    @Test
    fun verify_when_connection_is_unavailable() {
        networkConnectionLiveData.observe(mainActivity, {})

        concatArray(switchNetworks(false),
                    waitInternetStateChange(false))
                .blockingAwait()
                .let { assertThat(networkConnectionLiveData.value).isFalse() }
    }

    @Test
    fun verify_when_connection_is_switching() {
        networkConnectionLiveData.observe(mainActivity, {})

        concatArray(switchNetworks(false),
                    waitInternetStateChange(false))
                .blockingAwait()
                .let { assertThat(networkConnectionLiveData.value).isFalse() }

        concatArray(switchNetworks(true),
                    waitInternetStateChange(true))
                .blockingAwait()
                .let { assertThat(networkConnectionLiveData.value).isTrue() }

        concatArray(switchNetworks(false),
                    waitInternetStateChange(false))
                .blockingAwait()
                .let { assertThat(networkConnectionLiveData.value).isFalse() }
    }

    private fun switchNetworks(enabled: Boolean) : Completable {
        return Completable.create { emitter ->
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                    networkEnableLollipopMinSdkVersion(enabled = enabled)
                }
                Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP -> {
                    networkEnableKitKatMaxSdkVersion(enabled = enabled)
                }
            }
            emitter.onComplete()
        }
    }

    private fun waitInternetStateChange(isInternetAvailable: Boolean) : Completable {
        return Completable.create { emitter ->
            compositeDisposable.add(Single
                    .fromCallable { isInternetAvailable() }
                    .subscribeOn(Schedulers.io())
                    .repeat()
                    .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
                    .skipWhile {
                        it != isInternetAvailable
                    }
                    .subscribe {
                        emitter.onComplete()
                        compositeDisposable.clear()
                    }
            )
        }
    }

    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.LOLLIPOP)
    private fun networkEnableLollipopMinSdkVersion(enabled: Boolean) {
        val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
        when(enabled) {
            true -> {
                uiAutomation.executeShellCommand("svc wifi enable")
                uiAutomation.executeShellCommand("svc data enable")
            }

            false -> {
                uiAutomation.executeShellCommand("svc wifi disable")
                uiAutomation.executeShellCommand("svc data disable")
            }
        }
    }

    @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.KITKAT_WATCH)
    private fun networkEnableKitKatMaxSdkVersion(enabled: Boolean) {
        try {
            val connectivityManager = app.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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

    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
                .inject(this)
    }
}