package com.openclassrooms.realestatemanager.ui.suites

import com.openclassrooms.realestatemanager.data.cache.dao.PhotoDaoTest
import com.openclassrooms.realestatemanager.data.cache.dao.PropertyDaoTest
import com.openclassrooms.realestatemanager.data.cache.provider.AppContentProviderTest
import com.openclassrooms.realestatemanager.data.repository.ConnectivityManagerTest
import com.openclassrooms.realestatemanager.data.repository.CreatePropertyRepositoryTest
import com.openclassrooms.realestatemanager.data.repository.FindAllPropertyRepositoryTest
import com.openclassrooms.realestatemanager.data.repository.UpdatePropertyRepositoryTest
import com.openclassrooms.realestatemanager.ui.main.MainActivityTest
import com.openclassrooms.realestatemanager.ui.main.MainNavigationTest
import com.openclassrooms.realestatemanager.ui.main.MainRotationTest
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragmentTest
import com.openclassrooms.realestatemanager.ui.property.browse.list.BrowseListFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.browse.map.BrowseMapFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.edit.create.PropertyCreateFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.edit.dialog.photo.add.AddPhotoDialogFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.edit.dialog.photo.update.UpdatePhotoDialogFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.edit.update.PropertyUpdateFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.propertydetail.PropertyDetailFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.propertydetail.dialog.DetailPhotoDialogFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.search.PropertySearchFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.setting.PropertyCurrencySettingIntegrationTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
        BrowseFragmentTest::class,
        BrowseMapFragmentIntegrationTest::class,
        BrowseListFragmentIntegrationTest::class,
        ConnectivityManagerTest::class,
        MainActivityTest::class,
        MainNavigationTest::class,
        MainRotationTest::class,
        PropertyDetailFragmentIntegrationTest::class,
        DetailPhotoDialogFragmentIntegrationTest::class,
        PropertyUpdateFragmentIntegrationTest::class,
        AddPhotoDialogFragmentIntegrationTest::class,
        UpdatePhotoDialogFragmentIntegrationTest::class,
        PropertyCreateFragmentIntegrationTest::class,
        PropertySearchFragmentIntegrationTest::class,
        PropertyCurrencySettingIntegrationTest::class,
        PropertyDaoTest::class,
        PhotoDaoTest::class,
        AppContentProviderTest::class,
        FindAllPropertyRepositoryTest::class,
        UpdatePropertyRepositoryTest::class,
        CreatePropertyRepositoryTest::class
)
class RunAllTests