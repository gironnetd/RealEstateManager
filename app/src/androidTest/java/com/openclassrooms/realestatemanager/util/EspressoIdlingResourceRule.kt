package com.codingwithmitch.espressodaggerexamples.util

import androidx.test.espresso.IdlingRegistry
import com.openclassrooms.realestatemanager.util.EspressoIdlingResource
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import timber.log.Timber

class EspressoIdlingResourceRule : TestWatcher() {

    private val CLASS_NAME = "EspressoIdlingResourceRule"

    private val idlingResource = EspressoIdlingResource.countingIdlingResource

    override fun finished(description: Description?) {
        Timber.d("FINISHED")
        IdlingRegistry.getInstance().unregister(idlingResource)
        super.finished(description)
    }

    override fun starting(description: Description?) {
        Timber.d("STARTING")
        IdlingRegistry.getInstance().register(idlingResource)
        super.starting(description)
    }
}
