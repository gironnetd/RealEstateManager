package com.openclassrooms.realestatemanager.ui.property.update.view.update

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.app.ActivityOptionsCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle.State.RESUMED
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE
import androidx.test.espresso.matcher.ViewMatchers.Visibility.INVISIBLE
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.R.style.AppTheme
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.models.PhotoType
import com.openclassrooms.realestatemanager.models.PhotoType.*
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.models.storageLocalDatabase
import com.openclassrooms.realestatemanager.ui.BaseFragmentTests
import com.openclassrooms.realestatemanager.ui.property.BaseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.propertydetail.PhotoDetailAdapter
import com.openclassrooms.realestatemanager.ui.property.update.PhotoUpdateAdapter
import com.openclassrooms.realestatemanager.ui.property.update.PropertyUpdateFragment
import com.openclassrooms.realestatemanager.util.BitmapUtil.sameAs
import com.openclassrooms.realestatemanager.util.Constants
import com.openclassrooms.realestatemanager.util.OrientationChangeAction
import com.openclassrooms.realestatemanager.util.ToastMatcher
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import java.io.File
import java.io.FileOutputStream


@RunWith(AndroidJUnit4::class)
@MediumTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class PhotoUpdateDialogFragmentIntegrationTest  : BaseFragmentTests() {

    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var propertyUpdateFragment: PropertyUpdateFragment
    private lateinit var bundle: Bundle

    @Before
    public override fun setUp() {
        super.setUp()
        configure_fake_repository()
        injectTest(testApplication)

        fakeProperties = propertiesRepository.findAllProperties().blockingFirst().right!!
        fakeProperties.forEach { property ->
            property.photos = property.photos.toSet().toMutableList()
        }
        itemPosition = (fakeProperties.indices).random()

        BrowseFragment.WHEN_NORMAL_MODE_IS_DETAIL_FRAGMENT_SELECTED = false

        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        bundle = bundleOf(Constants.FROM to "", Constants.PROPERTY_ID to fakeProperties[itemPosition].id)

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
        BaseFragment.properties.value!!.clear()
        super.tearDown()
    }

    @Test
    fun given_update_photo_dialog_when_click_on_item_in_photo_recycler_view_then_alert_dialog_shown() {
        // Given Update fragment
        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyUpdateFragment(propertiesViewModelFactory, /*requestManager,*/ null)
        }.onFragment {
            propertyUpdateFragment = it
        }

        val photoUpdatePosition = fakeProperties[itemPosition].photos.indices.random()

        with(onView(withId(R.id.photos_recycler_view))) {
            perform(scrollToPosition<PhotoDetailAdapter.PhotoViewHolder>(photoUpdatePosition))
            perform(actionOnItemAtPosition<PhotoDetailAdapter.PhotoViewHolder>(photoUpdatePosition, click()))
        }

        onView(withId(R.id.update_photo_dialog_fragment)).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    @Test
    fun given_update_photo_dialog_when_update_dialog_shown_then_photo_detail_displayed() {
        // Given Update fragment
        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyUpdateFragment(propertiesViewModelFactory,/*requestManager,*/ null)
        }.onFragment {
            propertyUpdateFragment = it
        }

        val photoUpdatePosition = fakeProperties[itemPosition].photos.indices.random()

        with(onView(withId(R.id.photos_recycler_view))) {
            perform(scrollToPosition<PhotoUpdateAdapter.PhotoViewHolder>(photoUpdatePosition))
            perform(actionOnItemAtPosition<PhotoDetailAdapter.PhotoViewHolder>(photoUpdatePosition, click()))
        }

        when(fakeProperties[itemPosition].photos[photoUpdatePosition].type) {
            LOUNGE -> { onView(withId(R.id.radio_button_lounge)).check(matches(isChecked())) }
            FACADE -> { onView(withId(R.id.radio_button_facade)).check(matches(isChecked())) }
            KITCHEN -> { onView(withId(R.id.radio_button_kitchen)).check(matches(isChecked())) }
            BEDROOM -> { onView(withId(R.id.radio_button_bedroom)).check(matches(isChecked())) }
            BATHROOM -> { onView(withId(R.id.radio_button_bathroom)).check(matches(isChecked())) }
        }

        onView(withId(R.id.description_edit_text))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .check(matches(withText(fakeProperties[itemPosition].photos[photoUpdatePosition].description)))

        val viewHolder = propertyUpdateFragment.binding.photosRecyclerView
                .findViewHolderForAdapterPosition(photoUpdatePosition) as PhotoUpdateAdapter.PhotoViewHolder

        val viewHolderPhotoBitmap = (viewHolder.photo.drawable as BitmapDrawable).bitmap

        val updateDialogPhotoBitmap = (propertyUpdateFragment.updatePhotoAlertDialog.binding.
        photoImageview.drawable as BitmapDrawable).bitmap

        assertThat(sameAs(viewHolderPhotoBitmap, updateDialogPhotoBitmap)).isTrue()
    }



    @Test
    fun given_update_photo_dialog_when_update_photo_then_changes_occurs_in_photo_recycler_view() {
        // Given Update fragment

        val expectedResult = BitmapFactory.decodeResource(testApplication.resources, R.drawable.default_image )
        // Create the test ActivityResultRegistry
        val testRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                dispatchResult(requestCode, expectedResult)
            }
        }

        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyUpdateFragment(propertiesViewModelFactory,/*requestManager,*/ testRegistry)
        }.onFragment {
            propertyUpdateFragment = it
        }

        val photoUpdatePosition = fakeProperties[itemPosition].photos
            .indexOf(fakeProperties[itemPosition].photos.first { photo -> !photo.mainPhoto })

        with(onView(withId(R.id.photos_recycler_view))) {
            perform(scrollToPosition<PhotoUpdateAdapter.PhotoViewHolder>(photoUpdatePosition))
            perform(actionOnItemAtPosition<PhotoDetailAdapter.PhotoViewHolder>(photoUpdatePosition, click()))
        }

        onView(withId(R.id.take_photo)).perform(click())

        val differentType = PhotoType.values().first {
                type -> type != propertyUpdateFragment.updatePhotoAlertDialog.photo?.type && type != MAIN }

        when(differentType) {
            LOUNGE -> { onView(withId(R.id.radio_button_lounge)).perform(click()) }
            FACADE -> { onView(withId(R.id.radio_button_facade)).perform(click()) }
            KITCHEN -> { onView(withId(R.id.radio_button_kitchen)).perform(click()) }
            BEDROOM -> { onView(withId(R.id.radio_button_bedroom)).perform(click()) }
            BATHROOM -> { onView(withId(R.id.radio_button_bathroom)).perform(click()) }
            else -> {}
        }

        onView(withText(R.string.update_photo_detail)).perform(click())

        val viewHolder = propertyUpdateFragment.binding.photosRecyclerView
            .findViewHolderForAdapterPosition(photoUpdatePosition) as PhotoUpdateAdapter.PhotoViewHolder

        val viewHolderPhotoBitmap = (viewHolder.photo.drawable as BitmapDrawable).bitmap

        val updateDialogPhotoBitmap = (propertyUpdateFragment.updatePhotoAlertDialog.binding.
        photoImageview.drawable as BitmapDrawable).bitmap

        assertThat(sameAs(viewHolderPhotoBitmap, updateDialogPhotoBitmap)).isTrue()

        assertThat(viewHolder.type.text)
            .isEqualTo(
                testApplication.resources.getString(differentType.type).uppercase()
            )
    }

    @Test
    fun given_update_photo_dialog_when_rotate_then_alert_dialog_shown_again() {
        // Given Update fragment
        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyUpdateFragment(propertiesViewModelFactory, /*requestManager,*/ null)
        }.onFragment {
            mainActivity = it.requireActivity()
        }

        val photoUpdatePosition = fakeProperties[itemPosition].photos.indices.random()

        with(onView(withId(R.id.photos_recycler_view))) {
            perform(scrollToPosition<PhotoUpdateAdapter.PhotoViewHolder>(photoUpdatePosition))
            perform(actionOnItemAtPosition<PhotoDetailAdapter.PhotoViewHolder>(photoUpdatePosition, click()))
        }

        // Then Update fragment rotate
        val orientation = mainActivity.applicationContext.resources.configuration.orientation
        if(orientation == Configuration.ORIENTATION_PORTRAIT) {
            onView(isRoot())
                .perform(OrientationChangeAction.orientationLandscape(mainActivity))
        }
        if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
            onView(isRoot())
                .perform(OrientationChangeAction.orientationPortrait(mainActivity))
        }
        onView(withId(R.id.update_photo_dialog_fragment)).check(matches(isDisplayed()))
    }

    @Test
    fun given_update_photo_dialog_and_values_updated_when_rotate_then_same_values_displayed() {
        // Given Update fragment and Create an expected result Bitmap
        val expectedResult = BitmapFactory.decodeResource(testApplication.resources, R.drawable.default_image )
        // Create the test ActivityResultRegistry
        val testRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                dispatchResult(requestCode, expectedResult)
            }
        }

        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyUpdateFragment(propertiesViewModelFactory, /*requestManager,*/ testRegistry)
        }.onFragment {
            mainActivity = it.requireActivity()
            propertyUpdateFragment = it
        }

        val photoUpdatePosition = fakeProperties[itemPosition].photos.indices.random()

        with(onView(withId(R.id.photos_recycler_view))) {
            perform(scrollToPosition<PhotoUpdateAdapter.PhotoViewHolder>(photoUpdatePosition))
            perform(actionOnItemAtPosition<PhotoDetailAdapter.PhotoViewHolder>(photoUpdatePosition, click()))
        }

        onView(withId(R.id.radio_button_kitchen)).perform(click())
        onView(allOf(withId(R.id.description_edit_text), isDisplayed())).perform(replaceText(
            DESCRIPTION_TEXT))

        onView(withId(R.id.take_photo)).perform(click())

        // Then Update fragment rotate
        val orientation = mainActivity.applicationContext.resources.configuration.orientation
        if(orientation == Configuration.ORIENTATION_PORTRAIT) {
            onView(isRoot())
                .perform(OrientationChangeAction.orientationLandscape(mainActivity))
        }
        if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
            onView(isRoot())
                .perform(OrientationChangeAction.orientationPortrait(mainActivity))
        }

        onView(withId(R.id.radio_button_kitchen))
            .check(matches(isChecked()))

        onView(allOf(withId(R.id.description_edit_text), isDisplayed()))
            .check(matches(withText(DESCRIPTION_TEXT)))

        val bitmap = (propertyUpdateFragment.updatePhotoAlertDialog.binding.photoImageview.drawable as BitmapDrawable).bitmap

        assertThat(bitmap).isNotNull()
        assertThat(bitmap).isInstanceOf(expectedResult::class.java)
        assertThat(sameAs(bitmap, expectedResult)).isTrue()
    }

    @Test
    fun given_update_photo_dialog_when_select_image_from_gallery_then_return_result_with_success() {
        // Given Update fragment and Create an expected result Uri
        val expectedResult = File.createTempFile("test_",".jpg").apply {
            createNewFile()
        }.toUri()

        // Create the test ActivityResultRegistry
        val testRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                dispatchResult(requestCode, expectedResult)
            }
        }

        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyUpdateFragment(propertiesViewModelFactory, /*requestManager,*/ testRegistry)
        }.onFragment {
            propertyUpdateFragment = it
        }

        val photoUpdatePosition = fakeProperties[itemPosition].photos.indices.random()

        with(onView(withId(R.id.photos_recycler_view))) {
            perform(scrollToPosition<PhotoUpdateAdapter.PhotoViewHolder>(photoUpdatePosition))
            perform(actionOnItemAtPosition<PhotoDetailAdapter.PhotoViewHolder>(photoUpdatePosition, click()))
        }

        onView(withId(R.id.select_photo_from_gallery)).perform(click())
        assertThat(propertyUpdateFragment.updatePhotoAlertDialog.latestTmpUri).isSameInstanceAs(expectedResult)
    }

    @Test
    fun given_update_photo_dialog_when_take_a_photo_then_return_result_with_success() {
        // Given Update fragment and Create an expected result Bitmap
        val expectedResult = BitmapFactory.decodeResource(testApplication.resources, R.drawable.default_image )
        // Create the test ActivityResultRegistry
        val testRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                dispatchResult(requestCode, expectedResult)
            }
        }

        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyUpdateFragment(propertiesViewModelFactory, /*requestManager,*/ testRegistry)
        }.onFragment {
            propertyUpdateFragment = it
        }

        val photoUpdatePosition = fakeProperties[itemPosition].photos.indices.random()

        with(onView(withId(R.id.photos_recycler_view))) {
            perform(scrollToPosition<PhotoUpdateAdapter.PhotoViewHolder>(photoUpdatePosition))
            perform(actionOnItemAtPosition<PhotoDetailAdapter.PhotoViewHolder>(photoUpdatePosition, click()))
        }

        onView(withId(R.id.take_photo)).perform(click())

        val file = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, propertyUpdateFragment.updatePhotoAlertDialog.latestTmpUri!!.lastPathSegment!!).absolutePath
        try {
            val bitmap = BitmapFactory.decodeFile(file)
            assertThat(bitmap).isNotNull()
            assertThat(bitmap).isInstanceOf(expectedResult::class.java)
            assertThat(bitmap.sameAs(expectedResult)).isTrue()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Test
    fun given_update_photo_dialog_when_take_a_photo_then_tmp_file_created() {
        // Given Update fragment and Create an expected result Bitmap
        val expectedResult = BitmapFactory.decodeResource(testApplication.resources, R.drawable.default_image )

        // Create the test ActivityResultRegistry
        val testRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                dispatchResult(requestCode, expectedResult)
            }
        }

        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyUpdateFragment(propertiesViewModelFactory, /*requestManager,*/ testRegistry)
        }.onFragment {
            propertyUpdateFragment = it
        }

        val photoUpdatePosition = fakeProperties[itemPosition].photos.indices.random()

        with(onView(withId(R.id.photos_recycler_view))) {
            perform(scrollToPosition<PhotoUpdateAdapter.PhotoViewHolder>(photoUpdatePosition))
            perform(actionOnItemAtPosition<PhotoDetailAdapter.PhotoViewHolder>(photoUpdatePosition, click()))
        }

        onView(withId(R.id.take_photo)).perform(click())

        assertThat(propertyUpdateFragment.updatePhotoAlertDialog.tmpFile).isNotNull()

        val storedFile = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, propertyUpdateFragment.updatePhotoAlertDialog.latestTmpUri!!.lastPathSegment!!)

        assertThat(storedFile).isNotNull()
        assertThat(storedFile).isEqualTo(propertyUpdateFragment.updatePhotoAlertDialog.tmpFile)
    }

    @Test
    fun given_update_photo_dialog_when_select_image_from_gallery_then_tmp_file_created() {
        // Given Update fragment and Create an expected result Uri
        val expectedResult = File.createTempFile("test_",".jpg").apply {
            createNewFile()
        }.toUri()

        // Create the test ActivityResultRegistry
        val testRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                dispatchResult(requestCode, expectedResult)
            }
        }

        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyUpdateFragment(propertiesViewModelFactory, /*requestManager,*/ testRegistry)
        }.onFragment {
            propertyUpdateFragment = it
        }

        val photoUpdatePosition = fakeProperties[itemPosition].photos.indices.random()

        with(onView(withId(R.id.photos_recycler_view))) {
            perform(scrollToPosition<PhotoUpdateAdapter.PhotoViewHolder>(photoUpdatePosition))
            perform(actionOnItemAtPosition<PhotoDetailAdapter.PhotoViewHolder>(photoUpdatePosition, click()))
        }

        onView(withId(R.id.select_photo_from_gallery)).perform(click())

        assertThat(propertyUpdateFragment.updatePhotoAlertDialog.tmpFile).isNotNull()

        val storedFile = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, propertyUpdateFragment.updatePhotoAlertDialog.latestTmpUri!!.lastPathSegment!!)

        assertThat(storedFile).isNotNull()
        assertThat(storedFile).isEqualTo(propertyUpdateFragment.updatePhotoAlertDialog.tmpFile)
    }

    @Test
    fun given_update_photo_dialog_and_tmp_file_created_when_click_on_add_photo_button_then_tmp_file_deleted() {
        // Given Update fragment and Create an expected result Bitmap
        val expectedResult = BitmapFactory.decodeResource(testApplication.resources, R.drawable.default_image )

        // Create the test ActivityResultRegistry
        val testRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                dispatchResult(requestCode, expectedResult)
            }
        }

        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyUpdateFragment(propertiesViewModelFactory, /*requestManager,*/ testRegistry)
        }.onFragment {
            propertyUpdateFragment = it
        }

        val photoUpdatePosition = fakeProperties[itemPosition].photos.indices.random()

        with(onView(withId(R.id.photos_recycler_view))) {
            perform(scrollToPosition<PhotoUpdateAdapter.PhotoViewHolder>(photoUpdatePosition))
            perform(actionOnItemAtPosition<PhotoDetailAdapter.PhotoViewHolder>(photoUpdatePosition, click()))
        }

        onView(withId(R.id.take_photo)).perform(click())

        val file = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, propertyUpdateFragment.updatePhotoAlertDialog.latestTmpUri!!.lastPathSegment!!)
        assertThat(file.exists()).isTrue()

        onView(withText(R.string.update_photo_detail)).perform(click())
        assertThat(file.exists()).isFalse()
    }

    @Test
    fun given_update_photo_dialog_and_photo_selected_when_click_on_add_photo_button_then_tmp_file_saved_with_other_photos() {
        // Given Update fragment and Create an expected result Bitmap
        val expectedResult = BitmapFactory.decodeResource(testApplication.resources, R.drawable.default_image )

        // Create the test ActivityResultRegistry
        val testRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                dispatchResult(requestCode, expectedResult)
            }
        }

        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyUpdateFragment(propertiesViewModelFactory, /*requestManager,*/ testRegistry)
        }.onFragment {
            propertyUpdateFragment = it
        }

        val photoUpdatePosition = fakeProperties[itemPosition].photos.indices.random()

        with(onView(withId(R.id.photos_recycler_view))) {
            perform(scrollToPosition<PhotoUpdateAdapter.PhotoViewHolder>(photoUpdatePosition))
            perform(actionOnItemAtPosition<PhotoDetailAdapter.PhotoViewHolder>(photoUpdatePosition, click()))
        }

        onView(withId(R.id.take_photo)).perform(click())

        val tmpFile = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, propertyUpdateFragment.updatePhotoAlertDialog.latestTmpUri!!.lastPathSegment!!)
        val tmpFileAsBitmap = BitmapFactory.decodeFile(tmpFile.toString())

        onView(withText(R.string.update_photo_detail)).perform(click())

        val localFile = File(propertyUpdateFragment.updatePhotoAlertDialog.photo!!
            .storageLocalDatabase(testApplication.applicationContext.cacheDir, true))

        assertThat(localFile).isNotNull()
        assertThat(localFile.exists()).isTrue()

        val localFileAsBitmap = BitmapFactory.decodeFile(localFile.toString())

        assertThat(sameAs(localFileAsBitmap, tmpFileAsBitmap)).isTrue()
    }

    @Test
    fun given_update_photo_dialog_when_photo_displayed_then_delete_icon_shown_or_not() {
        // Given Update fragment and Create an expected result Bitmap
        val expectedResult = BitmapFactory.decodeResource(testApplication.resources, R.drawable.default_image )

        // Create the test ActivityResultRegistry
        val testRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                dispatchResult(requestCode, expectedResult)
            }
        }

        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyUpdateFragment(propertiesViewModelFactory, /*requestManager,*/ testRegistry)
        }.onFragment {
            propertyUpdateFragment = it
        }

        val photoUpdatePosition = fakeProperties[itemPosition].photos.indices.random()

        with(onView(withId(R.id.photos_recycler_view))) {
            perform(scrollToPosition<PhotoUpdateAdapter.PhotoViewHolder>(photoUpdatePosition))
            perform(actionOnItemAtPosition<PhotoDetailAdapter.PhotoViewHolder>(photoUpdatePosition, click()))
        }

        onView(withId(R.id.take_photo)).perform(click())
        onView(withId(R.id.delete_photo)).perform(click())

        onView(withId(R.id.photo_imageview)).check(matches(withEffectiveVisibility(INVISIBLE)))
        onView(withId(R.id.delete_photo)).check(matches(withEffectiveVisibility(GONE)))

        onView(withId(R.id.take_photo)).perform(click())

        onView(withId(R.id.delete_photo)).check(matches(isDisplayed()))
        onView(withId(R.id.photo_imageview)).check(matches(isDisplayed()))
    }


    @Test
    fun given_update_photo_dialog_when_click_on_delete_photo_button_then_photo_deleted_in_photo_recycler_view() {
        // Given Update fragment and Create an expected result Bitmap
        val expectedResult = BitmapFactory.decodeResource(testApplication.resources, R.drawable.default_image )

        // Create the test ActivityResultRegistry
        val testRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                dispatchResult(requestCode, expectedResult)
            }
        }

        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyUpdateFragment(propertiesViewModelFactory, /*requestManager,*/ testRegistry)
        }.onFragment {
            propertyUpdateFragment = it
        }

        val photoUpdatePosition = fakeProperties[itemPosition].photos.indexOf(
            fakeProperties[itemPosition].photos.first { photo -> !photo.mainPhoto }
        )

        val photoUpdate = fakeProperties[itemPosition].photos[photoUpdatePosition]

        with(onView(withId(R.id.photos_recycler_view))) {
            perform(scrollToPosition<PhotoUpdateAdapter.PhotoViewHolder>(photoUpdatePosition))
            perform(actionOnItemAtPosition<PhotoDetailAdapter.PhotoViewHolder>(photoUpdatePosition, click()))
        }

        onView(withText(R.string.delete_photo)).perform(click())

        assertThat(propertyUpdateFragment.property.photos.contains(photoUpdate)).isFalse()

        val photoAdapter: PhotoUpdateAdapter = propertyUpdateFragment.binding.photosRecyclerView.adapter as PhotoUpdateAdapter
        assertThat(photoAdapter.differ.currentList.contains(photoUpdate)).isFalse()
    }

    @Test
    fun given_update_photo_dialog_when_photo_main_one_and_click_on_delete_photo_button_then_toast_shown_and_photo_not_deleted_in_photo_recycler_view() {
        // Given Update fragment and Create an expected result Bitmap
        val expectedResult = BitmapFactory.decodeResource(testApplication.resources, R.drawable.default_image )

        // Create the test ActivityResultRegistry
        val testRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                dispatchResult(requestCode, expectedResult)
            }
        }

        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyUpdateFragment(propertiesViewModelFactory, /*requestManager,*/ testRegistry)
        }.onFragment {
            propertyUpdateFragment = it
            mainActivity = it.requireActivity()
        }

        val photoUpdatePosition = fakeProperties[itemPosition].photos.indexOf(
            fakeProperties[itemPosition].photos.first { photo -> photo.mainPhoto }
        )

        val photoUpdate = fakeProperties[itemPosition].photos[photoUpdatePosition]

        with(onView(withId(R.id.photos_recycler_view))) {
            perform(scrollToPosition<PhotoUpdateAdapter.PhotoViewHolder>(photoUpdatePosition))
            perform(actionOnItemAtPosition<PhotoDetailAdapter.PhotoViewHolder>(photoUpdatePosition, click()))
        }

        onView(withText(R.string.delete_photo)).perform(click())

        onView(withText(R.string.cannot_delete_photo))
            .inRoot(ToastMatcher().apply {
                ViewAssertions.matches(isDisplayed())
            })

        assertThat(propertyUpdateFragment.property.photos.contains(photoUpdate)).isTrue()

        val photoAdapter: PhotoUpdateAdapter = propertyUpdateFragment.binding.photosRecyclerView.adapter as PhotoUpdateAdapter
        assertThat(photoAdapter.differ.currentList.contains(photoUpdate)).isTrue()
    }

    @Test
    fun given_update_photo_dialog_when_click_on_delete_photo_icon_then_photo_deleted() {
        // Given Update fragment and Create an expected result Bitmap
        val expectedResult = BitmapFactory.decodeResource(testApplication.resources, R.drawable.default_image )

        // Create the test ActivityResultRegistry
        val testRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                dispatchResult(requestCode, expectedResult)
            }
        }

        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyUpdateFragment(propertiesViewModelFactory, /*requestManager,*/ testRegistry)
        }.onFragment {
            propertyUpdateFragment = it
        }

        val photoUpdatePosition = fakeProperties[itemPosition].photos.indices.random()

        with(onView(withId(R.id.photos_recycler_view))) {
            perform(scrollToPosition<PhotoUpdateAdapter.PhotoViewHolder>(photoUpdatePosition))
            perform(actionOnItemAtPosition<PhotoDetailAdapter.PhotoViewHolder>(photoUpdatePosition, click()))
        }

        if(propertyUpdateFragment.updatePhotoAlertDialog.binding.photoImageview.drawable == null) {
            onView(withId(R.id.take_photo)).perform(click())
        }

        onView(withId(R.id.delete_photo)).perform(click())

        assertThat(propertyUpdateFragment.updatePhotoAlertDialog.binding.photoImageview.drawable).isNull()
    }

    @Test
    fun given_update_photo_dialog_and_no_photo_when_update_photo_then_photo_at_position_in_recycler_view_empty() {
        // Given Update fragment and Create an expected result Bitmap
        val expectedResult = BitmapFactory.decodeResource(testApplication.resources, R.drawable.default_image )

        // Create the test ActivityResultRegistry
        val testRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                dispatchResult(requestCode, expectedResult)
            }
        }

        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyUpdateFragment(propertiesViewModelFactory, /*requestManager,*/ testRegistry)
        }.onFragment {
            propertyUpdateFragment = it
        }

        val photoUpdatePosition = fakeProperties[itemPosition].photos.indices.random()

        with(onView(withId(R.id.photos_recycler_view))) {
            perform(scrollToPosition<PhotoUpdateAdapter.PhotoViewHolder>(photoUpdatePosition))
            perform(actionOnItemAtPosition<PhotoDetailAdapter.PhotoViewHolder>(photoUpdatePosition, click()))
        }

        onView(withId(R.id.take_photo)).perform(click())

        onView(withId(R.id.delete_photo)).check(matches(isDisplayed()))
        onView(withId(R.id.delete_photo)).perform(click())

        onView(withText(R.string.update_photo_detail)).perform(click())

        val viewHolder =
            propertyUpdateFragment.binding.photosRecyclerView
                .findViewHolderForAdapterPosition(photoUpdatePosition) as PhotoUpdateAdapter.PhotoViewHolder

        assertThat(viewHolder.photo.drawable).isNull()
    }

    @Test
    fun given_update_photo_dialog_when_click_on_item_which_is_main_photo_then_main_photo_is_checked_and_non_clickable() {
        // Given Update fragment
        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyUpdateFragment(propertiesViewModelFactory, /*requestManager,*/ null)
        }.onFragment {
            propertyUpdateFragment = it
        }

        val photoUpdatePosition: Int = fakeProperties[itemPosition].photos
            .indexOf(fakeProperties[itemPosition].photos.single { photo -> photo.mainPhoto })

        with(onView(withId(R.id.photos_recycler_view))) {
            perform(scrollToPosition<PhotoUpdateAdapter.PhotoViewHolder>(photoUpdatePosition))
            perform(actionOnItemAtPosition<PhotoDetailAdapter.PhotoViewHolder>(photoUpdatePosition, click()))
        }

        onView(withId(R.id.is_main_photo)).check(matches(isChecked()))
        onView(withId(R.id.is_main_photo)).check(matches(not(isClickable())))
    }

    @Test
    fun given_update_photo_dialog_when_click_on_main_photo_then_main_photo_change() {
        // Given Update fragment
        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyUpdateFragment(propertiesViewModelFactory, /*requestManager,*/ null)
        }.onFragment {
            propertyUpdateFragment = it
        }

        val mainPhotoPosition: Int = fakeProperties[itemPosition].photos
            .indexOf(fakeProperties[itemPosition].photos.single { photo -> photo.mainPhoto })

        val firstMainPhotoViewHolder = propertyUpdateFragment.binding.photosRecyclerView
            .findViewHolderForAdapterPosition(mainPhotoPosition) as PhotoUpdateAdapter.PhotoViewHolder

        assertThat(firstMainPhotoViewHolder.type.text).isEqualTo(testApplication.resources.getString(MAIN.type).uppercase())
        assertThat(firstMainPhotoViewHolder.type.text).isNotEqualTo(testApplication.resources.getString(
            fakeProperties[itemPosition].photos[mainPhotoPosition].type.type
        ).uppercase())

        val otherPhotoPosition: Int = fakeProperties[itemPosition].photos
            .indexOf(fakeProperties[itemPosition].photos.first { photo -> !photo.mainPhoto })

        val newMainPhotoViewHolder = propertyUpdateFragment.binding.photosRecyclerView
            .findViewHolderForAdapterPosition(otherPhotoPosition) as PhotoUpdateAdapter.PhotoViewHolder

        assertThat(newMainPhotoViewHolder.type.text).isNotEqualTo(testApplication.resources.getString(MAIN.type).uppercase())
        assertThat(newMainPhotoViewHolder.type.text).isEqualTo(testApplication.resources.getString(
            fakeProperties[itemPosition].photos[otherPhotoPosition].type.type
        ).uppercase())

        with(onView(withId(R.id.photos_recycler_view))) {
            perform(scrollToPosition<PhotoUpdateAdapter.PhotoViewHolder>(mainPhotoPosition))
            perform(actionOnItemAtPosition<PhotoDetailAdapter.PhotoViewHolder>(mainPhotoPosition, click()))
        }

        onView(withId(R.id.is_main_photo)).check(matches(isChecked()))
        onView(withId(R.id.is_main_photo)).check(matches(not(isClickable())))
        onView(withText(R.string.cancel)).perform(click())

        with(onView(withId(R.id.photos_recycler_view))) {
            perform(scrollToPosition<PhotoUpdateAdapter.PhotoViewHolder>(otherPhotoPosition))
            perform(actionOnItemAtPosition<PhotoDetailAdapter.PhotoViewHolder>(otherPhotoPosition, click()))
        }

        onView(withId(R.id.is_main_photo)).check(matches(isNotChecked()))
        onView(withId(R.id.is_main_photo)).check(matches(isClickable()))
        onView(withId(R.id.is_main_photo)).inRoot(isDialog()).perform(object: ViewAction {
            override fun getConstraints(): Matcher<View> { return isEnabled() }
            override fun getDescription(): String { return "" }
            override fun perform(uiController: UiController?, view: View?) {
                view?.performClick()
            }
        })

        onView(withText(R.string.update_photo_detail)).perform(click())

        assertThat(firstMainPhotoViewHolder.type.text).isNotEqualTo(testApplication.resources.getString(MAIN.type).uppercase())
        assertThat(firstMainPhotoViewHolder.type.text).isEqualTo(testApplication.resources.getString(
            fakeProperties[itemPosition].photos[mainPhotoPosition].type.type
        ).uppercase())


        assertThat(newMainPhotoViewHolder.type.text).isEqualTo(testApplication.resources.getString(MAIN.type).uppercase())
        assertThat(newMainPhotoViewHolder.type.text).isNotEqualTo(testApplication.resources.getString(
            fakeProperties[itemPosition].photos[otherPhotoPosition].type.type
        ).uppercase())
    }

    @Test
    fun given_update_photo_dialog_when_main_photo_change_then_change_occurs_in_property() {
        // Given Update fragment
        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyUpdateFragment(propertiesViewModelFactory, /*requestManager,*/ null)
        }.onFragment {
            propertyUpdateFragment = it
        }

        val mainPhotoPosition: Int = fakeProperties[itemPosition].photos
            .indexOf(fakeProperties[itemPosition].photos.single { photo -> photo.mainPhoto })

        val newPhotoPosition: Int = fakeProperties[itemPosition].photos
            .indexOf(fakeProperties[itemPosition].photos.first { photo -> !photo.mainPhoto })

        assertThat(fakeProperties[itemPosition].photos[mainPhotoPosition].mainPhoto).isTrue()
        assertThat(fakeProperties[itemPosition].photos[newPhotoPosition].mainPhoto).isFalse()

        with(onView(withId(R.id.photos_recycler_view))) {
            perform(scrollToPosition<PhotoUpdateAdapter.PhotoViewHolder>(newPhotoPosition))
            perform(actionOnItemAtPosition<PhotoDetailAdapter.PhotoViewHolder>(newPhotoPosition, click()))
        }

        onView(withId(R.id.is_main_photo)).check(matches(isNotChecked()))
        onView(withId(R.id.is_main_photo)).check(matches(isClickable()))
        onView(withId(R.id.is_main_photo)).inRoot(isDialog()).perform(object: ViewAction {
            override fun getConstraints(): Matcher<View> { return isEnabled() }
            override fun getDescription(): String { return "" }
            override fun perform(uiController: UiController?, view: View?) {
                view?.performClick()
            }
        })

        onView(withText(R.string.update_photo_detail)).perform(click())

        assertThat(fakeProperties[itemPosition].photos[mainPhotoPosition].mainPhoto).isFalse()
        assertThat(fakeProperties[itemPosition].photos[newPhotoPosition].mainPhoto).isTrue()
    }

    companion object {
        const val DESCRIPTION_TEXT = "Hello the world !!!!!"
    }

    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
            .inject(this)
    }
}