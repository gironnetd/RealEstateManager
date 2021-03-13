package com.openclassrooms.realestatemanager.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
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
import com.openclassrooms.realestatemanager.util.schedulers.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Completable.concatArray
import org.junit.*
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import timber.log.Timber
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@MediumTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ConnectivityManagerTest : BaseMainActivityTests() {

    @get:Rule var rule: TestRule = InstantTaskExecutorRule()

    lateinit var networkConnectionLiveData: NetworkConnectionLiveData

    private lateinit var activityScenario: ActivityScenario<MainActivity>
    private lateinit var app : TestBaseApplication

    @Before
    public override fun setUp() {
        super.setUp()
        app = InstrumentationRegistry.getInstrumentation()
                .targetContext.applicationContext as TestBaseApplication

        val apiService = configureFakeApiService(
                propertiesDataSource = ConstantsTest.EMPTY_LIST, // empty list of data
                networkDelay = 0L,
                application = app
        )

        configureFakeRepository(apiService, app)
        injectTest(app)

        networkConnectionLiveData = NetworkConnectionLiveData(app.applicationContext)

        activityScenario = ActivityScenario.launch(MainActivity::class.java)
                .onActivity { activity ->
                    mainActivity = activity
                    Completable.fromCallable { if(!isInternetAvailable()) {
                        concatArray(switchAllNetworks(true),
                                    waitInternetStateChange(true))
                                .blockingAwait()
                    }
                        networkConnectionLiveData.observe(mainActivity,{})
                    }.subscribeOn(SchedulerProvider.io()).blockingAwait()
                }
    }

    @After
    public override fun tearDown() {
        if(networkConnectionLiveData.value != true) {
            concatArray(switchAllNetworks(true),
                        waitInternetStateChange(true))
                    .doOnComplete {
                        if(networkConnectionLiveData.hasObservers()) {
                            networkConnectionLiveData.removeObservers(mainActivity)
                        }
                        super.tearDown()
                    }.blockingAwait()
        } else {
            super.tearDown()
        }
    }

    @Test
    @Suppress("UnstableApiUsage")
    fun verify_when_connection_is_available() {
        Timber.tag(TAG).i("/** verify_when_connection_is_available **/")

        concatArray(switchAllNetworks(false), waitInternetStateChange(false))
                .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
                .blockingAwait()
                .let {
                    Completable.fromAction { networkConnectionLiveDataValue(false) }
                            .delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 2), TimeUnit.MILLISECONDS)
                            .blockingAwait()
                }

        concatArray(switchAllNetworks(true), waitInternetStateChange(true))
                .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
                .blockingAwait()
                .let {
                    Completable.fromAction { networkConnectionLiveDataValue(true) }
                            .delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 2), TimeUnit.MILLISECONDS)
                            .blockingAwait()
                }
    }

    @Test
    @Suppress("UnstableApiUsage")
    fun verify_when_connection_is_unavailable() {
        Timber.tag(TAG).i("/** verify_when_connection_is_unavailable **/")

        concatArray(switchAllNetworks(false), waitInternetStateChange(false))
                .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
                .blockingAwait()
                .let {
                    Completable.fromAction { networkConnectionLiveDataValue(false) }
                            .delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 2), TimeUnit.MILLISECONDS)
                            .blockingAwait()
                }
    }

    @Test
    @Suppress("UnstableApiUsage")
    fun verify_when_connection_is_switching() {
        Timber.tag(TAG).i("/** verify_when_connection_is_switching **/")

        concatArray(switchAllNetworks(false), waitInternetStateChange(false))
                .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
                .blockingAwait()
                .let {
                    Completable.fromAction { networkConnectionLiveDataValue(false) }
                            .delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 2), TimeUnit.MILLISECONDS)
                            .blockingAwait()
                }

        concatArray(switchAllNetworks(true), waitInternetStateChange(true))
                .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
                .blockingAwait()
                .let {
                    Completable.fromAction { networkConnectionLiveDataValue(true) }
                            .delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 2), TimeUnit.MILLISECONDS)
                            .blockingAwait()
                }

        concatArray(switchAllNetworks(false), waitInternetStateChange(false))
                .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
                .blockingAwait()
                .let {
                    Completable.fromAction { networkConnectionLiveDataValue(false) }
                            .delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 2), TimeUnit.MILLISECONDS)
                            .blockingAwait()
                }
    }

    private fun networkConnectionLiveDataValue(enabled: Boolean) {
                when(enabled) {
                    true -> {
                        assertThat(networkConnectionLiveData.value).isTrue()
                        Timber.tag(TAG).i("networkConnectionLiveData value is expected to be true" +
                                " and is : ${networkConnectionLiveData.value}")
                    }

                    false -> {
                        assertThat(networkConnectionLiveData.value).isFalse()
                        Timber.tag(TAG).i("networkConnectionLiveData value is expected to be false" +
                                " and is : ${networkConnectionLiveData.value}")
                    }
                }
    }


    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
                .inject(this)
    }

    companion object {
        private val TAG = ConnectivityManagerTest::class.simpleName
    }
}