package com.openclassrooms.realestatemanager.ui.property.edit.dialog.photo.add

import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.view.View
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.app.ActivityOptionsCompat
import androidx.core.net.toUri
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.data.repository.DefaultPropertyRepository
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.models.property.PhotoType
import com.openclassrooms.realestatemanager.models.property.storageLocalDatabase
import com.openclassrooms.realestatemanager.ui.BaseFragmentTests
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.edit.update.PhotoUpdateAdapter.PhotoViewHolder
import com.openclassrooms.realestatemanager.ui.property.edit.update.PropertyUpdateFragment
import com.openclassrooms.realestatemanager.ui.property.propertydetail.PhotoDetailAdapter
import com.openclassrooms.realestatemanager.ui.property.shared.BaseFragment
import com.openclassrooms.realestatemanager.util.BitmapUtil.sameAs
import com.openclassrooms.realestatemanager.util.OrientationChangeAction.Companion.orientationLandscape
import com.openclassrooms.realestatemanager.util.OrientationChangeAction.Companion.orientationPortrait
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
@MediumTest
class AddPhotoDialogFragmentIntegrationTest : BaseFragmentTests() {

    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var propertyUpdateFragment: PropertyUpdateFragment

    @Before
    public override fun setUp() {
        super.setUp()
        configure_fake_repository()
        injectTest(testApplication)

        (propertiesRepository as DefaultPropertyRepository).cachedProperties.clear()
        fakeProperties = propertiesRepository.findAllProperties().blockingFirst()
        fakeProperties.forEach { property ->
            property.photos = property.photos.toSet().toMutableList()
        }
        itemPosition = (fakeProperties.indices).random()

        BrowseFragment.WHEN_NORMAL_MODE_IS_DETAIL_FRAGMENT_SELECTED = false
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
    fun given_add_photo_dialog_when_click_on_add_a_photo_icon_then_alert_dialog_shown() {
        // Given Update fragment
        BaseFragment.properties.value = fakeProperties.toMutableList()
        val scenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        onView(allOf(withId(R.id.add_a_photo), isDisplayed())).perform(click())
        onView(withId(R.id.edit_photo_dialog_fragment)).check(matches(isDisplayed()))
    }

    @Test
    fun given_add_photo_dialog_when_rotate_then_alert_dialog_shown_again() {
        // Given Update fragment
        BaseFragment.properties.value = fakeProperties.toMutableList()
        val scenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        scenario.onActivity {
            propertyUpdateFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyUpdateFragment
        }

        onView(allOf(withId(R.id.add_a_photo), isDisplayed())).perform(click())

        // Then Update fragment rotate
        val orientation = mainActivity.applicationContext.resources.configuration.orientation
        if(orientation == Configuration.ORIENTATION_PORTRAIT) {
            onView(isRoot()).perform(orientationLandscape(mainActivity))
        }
        if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
            onView(isRoot()).perform(orientationPortrait(mainActivity))
        }
        onView(withId(R.id.edit_photo_dialog_fragment)).check(matches(isDisplayed()))
    }

    @Test
    fun given_add_photo_dialog_and_values_completed_when_rotate_then_same_values_displayed() {
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

        BaseFragment.properties.value = fakeProperties.toMutableList()
        val scenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        scenario.onActivity {
            propertyUpdateFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyUpdateFragment
            propertyUpdateFragment.registry = testRegistry
        }

        onView(allOf(withId(R.id.add_a_photo), isDisplayed())).perform(click())

        onView(withId(R.id.radio_button_kitchen)).perform(click())
        onView(allOf(withId(R.id.description_edit_text), isDisplayed()))
            .perform(replaceText(DESCRIPTION_TEXT))

        onView(withId(R.id.take_photo)).perform(click())

        // Then Update fragment rotate
        val orientation = mainActivity.applicationContext.resources.configuration.orientation
        if(orientation == Configuration.ORIENTATION_PORTRAIT) {
            onView(isRoot()).perform(orientationLandscape(mainActivity))
        }
        if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
            onView(isRoot()).perform(orientationPortrait(mainActivity))
        }

        onView(withId(R.id.radio_button_kitchen)).check(matches(isChecked()))

        onView(allOf(withId(R.id.description_edit_text), isDisplayed()))
            .check(matches(withText(DESCRIPTION_TEXT)))

        val bitmap = (propertyUpdateFragment.addPhotoAlertDialog.binding.photoImageview.drawable as BitmapDrawable).bitmap

        assertThat(bitmap).isNotNull()
        assertThat(bitmap).isInstanceOf(expectedResult::class.java)
        assertThat(sameAs(bitmap, expectedResult)).isTrue()
    }

    @Test
    fun given_add_photo_dialog_when_select_image_from_gallery_then_return_result_with_success() {
        // Given Update fragment and Create an expected result Uri
        val tmpFile = File.createTempFile("test_",".jpg").apply {
            createNewFile()
        }
        val expectedResult = tmpFile.toUri()

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

        BaseFragment.properties.value = fakeProperties.toMutableList()
        val scenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        scenario.onActivity {
            propertyUpdateFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyUpdateFragment
            propertyUpdateFragment.registry = testRegistry
        }
        onView(allOf(withId(R.id.add_a_photo), isDisplayed())).perform(click())

        onView(withId(R.id.select_photo_from_gallery))
            .perform(click())
        assertThat(propertyUpdateFragment.addPhotoAlertDialog.latestTmpUri).isSameInstanceAs(expectedResult)
    }

    @Test
    fun given_add_photo_dialog_shown_when_take_a_photo_then_return_result_with_success() {
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

        BaseFragment.properties.value = fakeProperties.toMutableList()
        val scenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        scenario.onActivity {
            propertyUpdateFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyUpdateFragment
            propertyUpdateFragment.registry = testRegistry
        }
        onView(allOf(withId(R.id.add_a_photo), isDisplayed())).perform(click())

        onView(withId(R.id.take_photo)).perform(click())

        val file = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, propertyUpdateFragment.addPhotoAlertDialog.latestTmpUri!!.lastPathSegment!!).absolutePath
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
    fun given_add_photo_dialog_when_take_a_photo_then_tmp_file_created() {
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

        BaseFragment.properties.value = fakeProperties.toMutableList()
        val scenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        scenario.onActivity {
            propertyUpdateFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyUpdateFragment
            propertyUpdateFragment.registry = testRegistry
        }
        onView(allOf(withId(R.id.add_a_photo), isDisplayed())).perform(click())

        onView(withId(R.id.take_photo)).perform(click())

        assertThat(propertyUpdateFragment.addPhotoAlertDialog.tmpFile).isNotNull()

        val storedFile = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, propertyUpdateFragment.addPhotoAlertDialog.latestTmpUri!!.lastPathSegment!!)

        assertThat(storedFile).isNotNull()
        assertThat(storedFile).isEqualTo(propertyUpdateFragment.addPhotoAlertDialog.tmpFile)
    }

    @Test
    fun given_add_photo_dialog_when_select_image_from_gallery_then_tmp_file_created() {
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

        BaseFragment.properties.value = fakeProperties.toMutableList()
        val scenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        scenario.onActivity {
            propertyUpdateFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyUpdateFragment
            propertyUpdateFragment.registry = testRegistry
        }
        onView(allOf(withId(R.id.add_a_photo), isDisplayed())).perform(click())

        onView(withId(R.id.select_photo_from_gallery)).perform(click())

        assertThat(propertyUpdateFragment.addPhotoAlertDialog.tmpFile).isNotNull()

        val storedFile = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, propertyUpdateFragment.addPhotoAlertDialog.latestTmpUri!!.lastPathSegment!!)

        assertThat(storedFile).isNotNull()
        assertThat(storedFile).isEqualTo(propertyUpdateFragment.addPhotoAlertDialog.tmpFile)
    }

    @Test
    fun given_add_photo_dialog_and_tmp_file_created_when_click_on_add_photo_button_then_tmp_file_deleted() {
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

        BaseFragment.properties.value = fakeProperties.toMutableList()
        val scenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        scenario.onActivity {
            propertyUpdateFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyUpdateFragment
            propertyUpdateFragment.registry = testRegistry
        }
        onView(allOf(withId(R.id.add_a_photo), isDisplayed())).perform(click())

        onView(withId(R.id.take_photo)).perform(click())

        val file = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, propertyUpdateFragment.addPhotoAlertDialog.latestTmpUri!!.lastPathSegment!!)
        assertThat(file.exists()).isTrue()

        onView(withText(R.string.add_photo)).perform(click())
        assertThat(file.exists()).isFalse()
    }

    @Test
    fun given_add_photo_dialog_and_photo_selected_when_click_on_add_photo_button_then_tmp_file_saved_with_other_photos() {
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

        BaseFragment.properties.value = fakeProperties.toMutableList()
        val scenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        scenario.onActivity {
            propertyUpdateFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyUpdateFragment
            propertyUpdateFragment.registry = testRegistry
        }
        onView(allOf(withId(R.id.add_a_photo), isDisplayed())).perform(click())

        onView(withId(R.id.take_photo)).perform(click())

        val tmpFile = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, propertyUpdateFragment.addPhotoAlertDialog.latestTmpUri!!.lastPathSegment!!)
        val tmpFileAsBitmap = BitmapFactory.decodeFile(tmpFile.toString())

        onView(withText(R.string.add_photo)).perform(click())

        val localFile = File(propertyUpdateFragment.addPhotoAlertDialog.tmpPhoto
            .storageLocalDatabase(testApplication.applicationContext.cacheDir, true))

        assertThat(localFile).isNotNull()
        assertThat(localFile.exists()).isTrue()

        val localFileAsBitmap = BitmapFactory.decodeFile(localFile.toString())

        assertThat(sameAs(localFileAsBitmap, tmpFileAsBitmap)).isTrue()
    }

    @Test
    fun given_add_photo_dialog_and_photo_added_when_add_photo_button_then_photo_at_position_in_recycler_view_not_empty() {
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

        BaseFragment.properties.value = fakeProperties.toMutableList()
        val scenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        scenario.onActivity {
            propertyUpdateFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyUpdateFragment
            propertyUpdateFragment.registry = testRegistry
        }
        onView(allOf(withId(R.id.add_a_photo), isDisplayed())).perform(click())

        onView(withId(R.id.take_photo)).perform(click())

        onView(withText(R.string.add_photo)).perform(click())

        onView(allOf(withId(R.id.photos_recycler_view), withParent(allOf(withId(R.id.container), withEffectiveVisibility(VISIBLE))))).perform(
            scrollToPosition<PhotoViewHolder>(propertyUpdateFragment.binding.photosRecyclerView.adapter!!.itemCount - 1)
        )

        val viewHolder = propertyUpdateFragment.binding.photosRecyclerView
            .findViewHolderForAdapterPosition(
                propertyUpdateFragment.binding.photosRecyclerView.adapter!!.itemCount - 1
            ) as PhotoViewHolder

        val bitmap = (viewHolder.photo.drawable as BitmapDrawable).bitmap
        assertThat(bitmap).isNotNull()
    }

    @Test
    fun given_add_photo_dialog_when_photo_displayed_then_delete_icon_shown_or_not() {
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

        BaseFragment.properties.value = fakeProperties.toMutableList()
        val scenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        scenario.onActivity {
            propertyUpdateFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyUpdateFragment
            propertyUpdateFragment.registry = testRegistry
        }

        onView(allOf(withId(R.id.add_a_photo), isDisplayed())).perform(click())

        onView(withId(R.id.take_photo)).perform(click())
        onView(withId(R.id.delete_photo)).perform(click())

        onView(withId(R.id.photo_imageview)).check(matches(withEffectiveVisibility(Visibility.INVISIBLE)))
        onView(withId(R.id.delete_photo)).check(matches(withEffectiveVisibility(Visibility.GONE)))

        onView(withId(R.id.take_photo)).perform(click())

        onView(withId(R.id.delete_photo)).check(matches(isDisplayed()))
        onView(withId(R.id.photo_imageview)).check(matches(isDisplayed()))
    }

    @Test
    fun given_add_photo_dialog_when_click_on_delete_photo_then_photo_deleted() {
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

        BaseFragment.properties.value = fakeProperties.toMutableList()
        val scenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        scenario.onActivity {
            propertyUpdateFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyUpdateFragment
            propertyUpdateFragment.registry = testRegistry
        }
        onView(allOf(withId(R.id.add_a_photo), isDisplayed())).perform(click())

        if(propertyUpdateFragment.addPhotoAlertDialog.binding.photoImageview.drawable == null) {
            onView(withId(R.id.take_photo)).perform(click())
        }

        onView(withId(R.id.delete_photo)).perform(click())
        assertThat(propertyUpdateFragment.addPhotoAlertDialog.binding.photoImageview.drawable).isNull()
    }

    @Test
    fun given_add_photo_dialog_and_no_photo_when_add_photo_then_photo_at_position_in_recycler_view_empty() {
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

        BaseFragment.properties.value = fakeProperties.toMutableList()
        val scenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        scenario.onActivity {
            propertyUpdateFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyUpdateFragment
            propertyUpdateFragment.registry = testRegistry
        }
        onView(allOf(withId(R.id.add_a_photo), isDisplayed())).perform(click())

        onView(withId(R.id.take_photo)).perform(click())

        onView(withId(R.id.delete_photo)).check(matches(isDisplayed()))
        onView(withId(R.id.delete_photo)).perform(click())

        onView(withText(R.string.add_photo)).perform(click())

        onView(allOf(withId(R.id.photos_recycler_view), withParent(allOf(withId(R.id.container), withEffectiveVisibility(VISIBLE))))). perform(
            scrollToPosition<PhotoViewHolder>(propertyUpdateFragment.binding.photosRecyclerView.adapter!!.itemCount - 1)
        )

        val viewHolder = propertyUpdateFragment.binding.photosRecyclerView
            .findViewHolderForAdapterPosition(
                propertyUpdateFragment.binding.photosRecyclerView.adapter!!.itemCount - 1
            ) as PhotoViewHolder

        // The Default value is a VectorDrawable
        assertThat((viewHolder.photo.drawable)).isInstanceOf(VectorDrawable::class.java)
    }

    @Test
    fun given_add_photo_dialog_when_click_on_main_photo_then_main_photo_change() {
        // Given Update fragment
        BaseFragment.properties.value = fakeProperties.toMutableList()
        val scenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        scenario.onActivity {
            propertyUpdateFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyUpdateFragment
        }

        val mainPhotoPosition: Int = fakeProperties[itemPosition].photos
            .indexOf(fakeProperties[itemPosition].photos.single { photo -> photo.mainPhoto })

        val oldMainPhotoViewHolder = propertyUpdateFragment.binding.photosRecyclerView
            .findViewHolderForAdapterPosition(mainPhotoPosition) as PhotoViewHolder

        with(onView(allOf(withId(R.id.photos_recycler_view), withParent(allOf(withId(R.id.container), withEffectiveVisibility(VISIBLE)))))) {
            perform(scrollToPosition<PhotoViewHolder>(mainPhotoPosition))
            perform(actionOnItemAtPosition<PhotoDetailAdapter.PhotoViewHolder>(mainPhotoPosition, click()))
        }

        onView(withId(R.id.is_main_photo)).check(matches(isChecked()))
        onView(withId(R.id.is_main_photo)).check(matches(not(isClickable())))
        onView(withText(R.string.cancel)).perform(click())

        onView(allOf(withId(R.id.add_a_photo), isDisplayed())).perform(click())

        onView(withId(R.id.is_main_photo)).check(matches(isNotChecked()))
        onView(withId(R.id.is_main_photo)).check(matches(isClickable()))
        onView(withId(R.id.is_main_photo)).inRoot(RootMatchers.isDialog()).perform(object:
            ViewAction {
            override fun getConstraints(): Matcher<View> { return isEnabled() }
            override fun getDescription(): String { return "" }
            override fun perform(uiController: UiController?, view: View?) {
                view?.performClick()
            }
        })

        onView(withText(R.string.add_photo)).perform(click())

        assertThat(oldMainPhotoViewHolder.type.text.toString()).isNotEqualTo(testApplication.resources.getString(
            PhotoType.MAIN.type).uppercase())
        assertThat(oldMainPhotoViewHolder.type.text.toString()).isEqualTo(testApplication.resources.getString(
            fakeProperties[itemPosition].photos[mainPhotoPosition].type.type
        ).uppercase())

        with(onView(allOf(withId(R.id.photos_recycler_view), withParent(allOf(withId(R.id.container), withEffectiveVisibility(VISIBLE)))))) {
            perform(scrollToPosition<PhotoViewHolder>
                (propertyUpdateFragment.binding.photosRecyclerView.adapter!!.itemCount - 1))
        }

        val newMainPhotoViewHolder = propertyUpdateFragment.binding.photosRecyclerView
            .findViewHolderForAdapterPosition(propertyUpdateFragment.binding.photosRecyclerView.adapter!!.itemCount - 1) as PhotoViewHolder

        assertThat(newMainPhotoViewHolder.type.text.toString()).isEqualTo(testApplication.resources.getString(
            PhotoType.MAIN.type).uppercase())
    }

    @Test
    fun given_add_photo_dialog_when_main_photo_change_then_change_occurs_in_property() {
        // Given Update fragment
        BaseFragment.properties.value = fakeProperties.toMutableList()
        val scenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        scenario.onActivity {
            propertyUpdateFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyUpdateFragment
        }

        val mainPhotoPosition: Int = fakeProperties[itemPosition].photos
            .indexOf(fakeProperties[itemPosition].photos.single { photo -> photo.mainPhoto })

        assertThat(fakeProperties[itemPosition].photos[mainPhotoPosition].mainPhoto).isTrue()

        onView(allOf(withId(R.id.add_a_photo), isDisplayed())).perform(click())

        onView(withId(R.id.is_main_photo)).check(matches(isNotChecked()))
        onView(withId(R.id.is_main_photo)).check(matches(isClickable()))
        onView(withId(R.id.is_main_photo)).inRoot(RootMatchers.isDialog()).perform(object:
            ViewAction {
            override fun getConstraints(): Matcher<View> { return isEnabled() }
            override fun getDescription(): String { return "" }
            override fun perform(uiController: UiController?, view: View?) {
                view?.performClick()
            }
        })

        onView(withText(R.string.add_photo)).perform(click())

        with(onView(allOf(withId(R.id.photos_recycler_view), withParent(allOf(withId(R.id.container), withEffectiveVisibility(VISIBLE)))))) {
            perform(scrollToPosition<PhotoViewHolder>
                (propertyUpdateFragment.binding.photosRecyclerView.adapter!!.itemCount - 1))
        }

        assertThat(propertyUpdateFragment.newProperty.photos[mainPhotoPosition].mainPhoto).isFalse()
        assertThat(propertyUpdateFragment.newProperty.photos[propertyUpdateFragment.newProperty.photos.size - 1].mainPhoto).isTrue()
    }

    @Test
    fun given_add_photo_dialog_when_add_photo_then_changes_occurs_in_photo_recycler_view() {
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

        BaseFragment.properties.value = fakeProperties.toMutableList()
        val scenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        scenario.onActivity {
            propertyUpdateFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyUpdateFragment
            propertyUpdateFragment.registry = testRegistry
        }
        onView(allOf(withId(R.id.add_a_photo), isDisplayed())).perform(click())

        onView(withId(R.id.take_photo)).perform(click())

        val updateDialogPhotoBitmap = (propertyUpdateFragment.addPhotoAlertDialog.binding.
        photoImageview.drawable as BitmapDrawable).bitmap

        onView(withId(R.id.radio_button_lounge)).perform(click())

        onView(withText(R.string.add_photo)).perform(click())

       onView(allOf(withId(R.id.photos_recycler_view), withParent(allOf(withId(R.id.container), withEffectiveVisibility(VISIBLE)))))
           .perform(scrollToPosition<PhotoViewHolder>(
               propertyUpdateFragment.binding.photosRecyclerView.adapter!!.itemCount -1
           ))

        val viewHolder = propertyUpdateFragment.binding.photosRecyclerView
            .findViewHolderForAdapterPosition(propertyUpdateFragment.binding.photosRecyclerView.adapter!!.itemCount -1) as PhotoViewHolder

        val viewHolderPhotoBitmap = (viewHolder.photo.drawable as BitmapDrawable).bitmap
        assertThat(sameAs(viewHolderPhotoBitmap, updateDialogPhotoBitmap)).isTrue()

        assertThat(viewHolder.type.text.toString())
            .isEqualTo(testApplication.resources.getString(PhotoType.LOUNGE.type).uppercase())
    }

    override fun navigate_to_update_fragment() {
        navigate_to_detail_fragment()
        super.navigate_to_update_fragment()
    }

    companion object {
        const val DESCRIPTION_TEXT = "Hello the world !!!!!"
    }

    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
            .inject(this)
    }
}