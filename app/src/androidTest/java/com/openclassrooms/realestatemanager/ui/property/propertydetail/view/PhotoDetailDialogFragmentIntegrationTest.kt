package com.openclassrooms.realestatemanager.ui.property.propertydetail.view

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.data.repository.DefaultPropertyRepository
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.models.PhotoType
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.models.storageLocalDatabase
import com.openclassrooms.realestatemanager.ui.BaseFragmentTests
import com.openclassrooms.realestatemanager.ui.property.BaseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment
import com.openclassrooms.realestatemanager.ui.property.propertydetail.PhotoDetailAdapter
import com.openclassrooms.realestatemanager.ui.property.propertydetail.PropertyDetailFragment
import com.openclassrooms.realestatemanager.util.BitmapUtil.sameAs
import com.openclassrooms.realestatemanager.util.Constants.FROM
import com.openclassrooms.realestatemanager.util.Constants.PROPERTY_ID
import com.openclassrooms.realestatemanager.util.OrientationChangeAction.Companion.orientationLandscape
import com.openclassrooms.realestatemanager.util.OrientationChangeAction.Companion.orientationPortrait
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.util.*

@RunWith(AndroidJUnit4::class)
@MediumTest
class PhotoDetailDialogFragmentIntegrationTest : BaseFragmentTests() {

    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var propertyDetailFragment: PropertyDetailFragment
    private lateinit var bundle: Bundle

    @Before
    public override fun setUp() {
        super.setUp()
        configure_fake_repository()
        injectTest(testApplication)

        (propertiesRepository as DefaultPropertyRepository).cachedProperties.clear()
        fakeProperties = propertiesRepository.findAllProperties().blockingFirst()
        itemPosition = (fakeProperties.indices).random()

        BrowseFragment.WHEN_NORMAL_MODE_IS_DETAIL_FRAGMENT_SELECTED = false

        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        bundle = bundleOf(FROM to MapFragment::class.java.name,
            PROPERTY_ID to fakeProperties[itemPosition].id)

        fakeProperties[itemPosition].photos.forEach { photo ->
            val photoFile = File(photo.storageLocalDatabase(testApplication.applicationContext.cacheDir,true))

            if(!photoFile.exists()) {
                val defaultImage = testApplication.resources.getDrawable(R.drawable.default_image, null)

                val outputStream = FileOutputStream(photoFile, true)

                (defaultImage as BitmapDrawable).bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            }
        }
    }

    @After
    public override fun tearDown() {
        fakeProperties[itemPosition].photos.forEach { photo ->
            val photoFile = File(photo.storageLocalDatabase(testApplication.applicationContext.cacheDir,true))
            if(photoFile.exists()) { photoFile.delete() }
        }
        if(BaseFragment.properties.value != null) { BaseFragment.properties.value!!.clear() }
        (propertiesRepository as DefaultPropertyRepository).cachedProperties.clear()
        super.tearDown()
    }

    @Test
    fun given_dialog_when_click_on_item_in_photo_recycler_view_then_alert_dialog_shown() {
        // Given Detail fragment and When fragment is launched
        launchFragmentInContainer(fragmentArgs = bundle, R.style.AppTheme, Lifecycle.State.RESUMED) {
            PropertyDetailFragment(propertiesViewModelFactory/*, requestManager*/)
        }.onFragment {
            propertyDetailFragment = it
        }

        val photoDetailPosition = fakeProperties[itemPosition].photos.indices.random()

        with(onView(withId(R.id.photos_recycler_view))) {
            perform(scrollToPosition<PhotoDetailAdapter.PhotoViewHolder>(photoDetailPosition))
            perform(actionOnItemAtPosition<PhotoDetailAdapter.PhotoViewHolder>(photoDetailPosition, click()))
        }

        onView(withId(R.id.photo_detail_dialog_fragment))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun given_detail_and_photo_detail_dialog_shown_when_rotate_then_dialog_shown_again() {
        // Given Detail fragment and When fragment is launched
        launchFragmentInContainer(fragmentArgs = bundle, R.style.AppTheme, Lifecycle.State.RESUMED) {
            PropertyDetailFragment(propertiesViewModelFactory/*, requestManager*/)
        }.onFragment {
            propertyDetailFragment = it
            mainActivity = it.requireActivity()
        }

        val photoDetailPosition = fakeProperties[itemPosition].photos.indices.random()

        with(onView(withId(R.id.photos_recycler_view))) {
            perform(scrollToPosition<PhotoDetailAdapter.PhotoViewHolder>(photoDetailPosition))
            perform(actionOnItemAtPosition<PhotoDetailAdapter.PhotoViewHolder>(photoDetailPosition, click()))
        }

        onView(withId(R.id.photo_detail_dialog_fragment))
            .inRoot(isDialog())
            .check(matches(isDisplayed()));

        // Then Update fragment rotate
        val orientation = mainActivity.applicationContext.resources.configuration.orientation
        if(orientation == Configuration.ORIENTATION_PORTRAIT) {
            onView(isRoot()).perform(orientationLandscape(mainActivity))
        }
        if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
            onView(isRoot()).perform(orientationPortrait(mainActivity))
        }

        onView(withId(R.id.photo_detail_dialog_fragment)).check(matches(isDisplayed()))
    }

    @Test
    fun given_detail_dialog_shown_when_rotate_then_then_same_values_displayed() {
        // Given Detail fragment and When fragment is launched
        launchFragmentInContainer(fragmentArgs = bundle, R.style.AppTheme, Lifecycle.State.RESUMED) {
            PropertyDetailFragment(propertiesViewModelFactory)
        }.onFragment {
            propertyDetailFragment = it
            mainActivity = it.requireActivity()
        }

        val photoDetailPosition = fakeProperties[itemPosition].photos.indices.random()

        with(onView(withId(R.id.photos_recycler_view))) {
            perform(scrollToPosition<PhotoDetailAdapter.PhotoViewHolder>(photoDetailPosition))
            perform(actionOnItemAtPosition<PhotoDetailAdapter.PhotoViewHolder>(photoDetailPosition, click()))
        }
        onView(withId(R.id.photo_detail_dialog_fragment))
            .inRoot(isDialog()).check(matches(isDisplayed()));

        // Then Update fragment rotate
        val orientation = mainActivity.applicationContext.resources.configuration.orientation
        if(orientation == Configuration.ORIENTATION_PORTRAIT) {
            onView(isRoot()).perform(orientationLandscape(mainActivity))
        }
        if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
            onView(isRoot()).perform(orientationPortrait(mainActivity))
        }
        onView(withId(R.id.photo_detail_dialog_fragment))
            .check(matches(isDisplayed()))

        onView(allOf(withId(R.id.label_photo_image), isDisplayed()))
            .check(matches(withText(
                testApplication.resources.getString(
                    fakeProperties[itemPosition].photos[photoDetailPosition].type.type
                ).replaceFirstChar {
                    if (it.isLowerCase())
                        it.titlecase(Locale.getDefault())
                    else
                        it.toString()
                }
            )))

        onView(allOf(withId(R.id.description), isDisplayed()))
            .check(matches(withText(fakeProperties[itemPosition].photos[photoDetailPosition].description)))
    }

    @Test
    fun given_dialog_when_detail_dialog_shown_then_photo_detail_displayed() {
        // Given Detail fragment and When fragment is launched
        launchFragmentInContainer(fragmentArgs = bundle, R.style.AppTheme, Lifecycle.State.RESUMED) {
            PropertyDetailFragment(propertiesViewModelFactory/*, requestManager*/)
        }.onFragment {
            propertyDetailFragment = it
        }

        val photoDetailPosition = fakeProperties[itemPosition].photos.indices.random()

        with(onView(withId(R.id.photos_recycler_view))) {
            perform(scrollToPosition<PhotoDetailAdapter.PhotoViewHolder>(photoDetailPosition))
            perform(actionOnItemAtPosition<PhotoDetailAdapter.PhotoViewHolder>(photoDetailPosition, click()))
        }

        var photoTypeResId: Int = -1
        when(fakeProperties[itemPosition].photos[photoDetailPosition].type) {
            PhotoType.LOUNGE -> { photoTypeResId = PhotoType.LOUNGE.type }
            PhotoType.FACADE -> { photoTypeResId = PhotoType.FACADE.type }
            PhotoType.KITCHEN -> { photoTypeResId = PhotoType.KITCHEN.type }
            PhotoType.BEDROOM -> { photoTypeResId = PhotoType.BEDROOM.type }
            PhotoType.BATHROOM -> { photoTypeResId = PhotoType.BATHROOM.type }
            else -> {}
        }

        onView(withId(R.id.label_photo_image))
            .check(
                matches(
                    withText(
                        testApplication.resources.getString(photoTypeResId)
                            .replaceFirstChar {
                                if (it.isLowerCase())
                                    it.titlecase(Locale.getDefault())
                                else
                                    it.toString()
                            }
                    )
                )
            )

        onView(withId(R.id.description))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .check(matches(withText(fakeProperties[itemPosition].photos[photoDetailPosition].description)))

        val viewHolder = propertyDetailFragment.binding.photosRecyclerView
            .findViewHolderForAdapterPosition(photoDetailPosition) as PhotoDetailAdapter.PhotoViewHolder

        val viewHolderPhotoBitmap = (viewHolder.photo.drawable as BitmapDrawable).bitmap

        val updateDialogPhotoBitmap = (propertyDetailFragment.detailPhotoAlertDialog.binding.
        photoImageView.drawable as BitmapDrawable).bitmap

        assertThat(sameAs(viewHolderPhotoBitmap, updateDialogPhotoBitmap)).isTrue()
    }

    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
            .inject(this)
    }
}