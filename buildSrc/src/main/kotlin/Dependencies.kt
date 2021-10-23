import org.gradle.api.artifacts.dsl.DependencyHandler

object Dependencies {

    private val multiDex by lazy { "com.android.support:multidex:${Versions.multi_dex}" }

    private val kotlinStandardLibrary by lazy { "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}" }
    private val kotlinReflect by lazy { "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}" }

    private val appCompat by lazy { "androidx.appcompat:appcompat:${Versions.appcompat}" }
    private val material by lazy { "com.google.android.material:material:${Versions.material}" }

    private val constraintLayout by lazy { "androidx.constraintlayout:constraintlayout:${Versions.constraint_layout}" }

    private val legacySupport by lazy { "androidx.legacy:legacy-support-v4:${Versions.legacy_support_v4}" }

    private val junit by lazy { "junit:junit:${Versions.junit}" }
    private val extJunit by lazy { "androidx.test.ext:junit:${Versions.ext_junit}" }

    private val mockitoKotlin by lazy { "com.nhaarman:mockito-kotlin:${Versions.mockito_kotlin}" }

    private val mockitoAndroid by lazy { "org.mockito:mockito-android:${Versions.mockito_android}" }

    private val testingFragment by lazy { "androidx.fragment:fragment-testing:${Versions.fragment_testing}" }

    private val espressoCore by lazy { "androidx.test.espresso:espresso-core:${Versions.androidx_espresso_core}" }
    val espressoContrib by lazy { "androidx.test.espresso:espresso-contrib:${Versions.androidx_espresso_core}" }
    private val espressoIdlingResource by lazy { "androidx.test.espresso:espresso-idling-resource:${Versions.androidx_espresso_idling_resource}" }

//    // Espresso
//    def androidx_espresso_core = '3.4.0'
//    androidTestImplementation "androidx.test.espresso:espresso-core:$androidx_espresso_core"
//    androidTestImplementation ("androidx.test.espresso:espresso-contrib:$androidx_espresso_core") {
//        exclude module: "protobuf-lite"
//        exclude group: 'org.checkerframework', module: 'checker'
//    }
//

    private val uiAutomator by lazy { "androidx.test.uiautomator:uiautomator:${Versions.androidx_ui_automator}" }

    private val testRunner by lazy { "androidx.test:runner:${Versions.androidx_test_runner}" }

    private val testRules by lazy { "androidx.test:rules:${Versions.androidx_test_rules}" }

    private val testCore by lazy { "androidx.test:core-ktx:${Versions.androidx_test_core}" }

    private val archTesting by lazy { "androidx.arch.core:core-testing:${Versions.arch_testing}" }

    private val robolectric by lazy { "org.robolectric:robolectric:${Versions.robolectric}" }

    val testOrchestrator by lazy { "androidx.test:orchestrator:${Versions.androidx_test_orchestrator}" }

    private val truth by lazy { "androidx.test.ext:truth:${Versions.truth}" }

    private val navigationFragment by lazy { "androidx.navigation:navigation-fragment-ktx:${Versions.navigation_component}" }
    private val navigationUi by lazy { "androidx.navigation:navigation-ui-ktx:${Versions.navigation_component}" }

    private val fragment by lazy { "androidx.fragment:fragment-ktx:${Versions.androidx_fragment}" }

    private val activity by lazy { "androidx.activity:activity-ktx:${Versions.androidx_activity}" }
    private val dagger by lazy { "com.google.dagger:dagger:${Versions.dagger}" }
    private val daggerCompiler by lazy { "com.google.dagger:dagger-compiler:${Versions.dagger}" }

    val firebaseBom by lazy { "com.google.firebase:firebase-bom:${Versions.firebase_bom}" }
    private val firebaseAnalytics by lazy { "com.google.firebase:firebase-analytics-ktx" }
    private val firebaseFirestore by lazy { "com.google.firebase:firebase-firestore-ktx" }
    private val grpcOkhttp by lazy { "io.grpc:grpc-okhttp:${Versions.grpc_okhttp}" }
    private val gson by lazy { "com.google.code.gson:gson:${Versions.gson}" }

    private val firebaseStorage by lazy { "com.google.firebase:firebase-storage-ktx" }
    private val firebaseUiStorage by lazy { "com.firebaseui:firebase-ui-storage:${Versions.firebase_ui_storage}" }

    private val mockNeat by lazy { "net.andreinc:mockneat:${Versions.mock_neat}" }
    private val timber by lazy { "com.jakewharton.timber:timber:${Versions.timber}" }
    private val glide by lazy { "com.github.bumptech.glide:glide:${Versions.glide}" }
    private val glideCompiler by lazy { "com.github.bumptech.glide:compiler:${Versions.glide}" }

    private val rxKotlin by lazy { "io.reactivex.rxjava2:rxkotlin:${Versions.rx_kotlin}" }

    private val rxAndroid by lazy { "io.reactivex.rxjava2:rxandroid:${Versions.rx_android}" }

    private val room by lazy { "androidx.room:room-runtime:${Versions.room}" }
    private val roomCompiler by lazy { "androidx.room:room-compiler:${Versions.room}" }

    private val roomRxjava by lazy { "android.arch.persistence.room:rxjava2:${Versions.room_rxjava}" }

    private val googleMaps by lazy { "com.google.android.gms:play-services-maps:${Versions.google_maps}" }

    private val googlePlaces by lazy { "com.google.android.libraries.places:places:${Versions.google_places}" }

    private val volley by lazy { "com.android.volley:volley:${Versions.volley}" }

    private val googleMapsUtils by lazy { "com.google.maps.android:android-maps-utils:${Versions.google_maps_utils}" }

    private val leakCanary by lazy { "com.squareup.leakcanary:leakcanary-android:${Versions.leakcanary}" }

    val kaptAndroidTestLibraries = arrayListOf<String>().apply {
        add(daggerCompiler)
    }

    val kaptLibraries = arrayListOf<String>().apply {
        add(daggerCompiler)
        add(glideCompiler)
        add(roomCompiler)
    }

    val appLibraries = arrayListOf<String>().apply {
        add(multiDex)
        add(kotlinStandardLibrary)
        add(kotlinReflect)
        add(appCompat)
        add(material)
        add(constraintLayout)
        add(legacySupport)
        add(espressoIdlingResource)
        add(navigationFragment)
        add(navigationUi)
        add(fragment)
        add(activity)
        add(dagger)
        add(firebaseAnalytics)
        add(firebaseFirestore)
        add(grpcOkhttp)
        add(gson)
        add(firebaseStorage)
        add(firebaseUiStorage)
        add(mockNeat)
        add(timber)
        add(glide)
        add(rxKotlin)
        add(rxAndroid)
        add(room)
        add(roomRxjava)
        add(googleMaps)
        add(googlePlaces)
        add(volley)
        add(googleMapsUtils)
    }

    val androidTestLibraries = arrayListOf<String>().apply {
        add(extJunit)
        add(mockitoAndroid)
        add(espressoCore)
        add(espressoIdlingResource)
        add(uiAutomator)
        add(testRunner)
        add(testRules)
        add(testCore)
        add(archTesting)
        add(truth)
    }

    val testLibraries = arrayListOf<String>().apply {
        add(junit)
        add(extJunit)
        add(mockitoKotlin)
        add(testRunner)
        add(testRules)
        add(archTesting)
        add(robolectric)
        add(truth)
    }

    val debugLibraries = arrayListOf<String>().apply {
        add(testingFragment)
        add(leakCanary)
    }
}

fun DependencyHandler.kapt(list: List<String>) {
    list.forEach { dependency ->
        add("kapt", dependency)
    }
}

fun DependencyHandler.kaptAndroidTest(list: List<String>) {
    list.forEach { dependency ->
        add("kaptAndroidTest", dependency)
    }
}

fun DependencyHandler.implementation(list: List<String>) {
    list.forEach { dependency ->
        add("implementation", dependency)
    }
}

fun DependencyHandler.androidTestImplementation(list: List<String>) {
    list.forEach { dependency ->
        add("androidTestImplementation", dependency)
    }
}

fun DependencyHandler.testImplementation(list: List<String>) {
    list.forEach { dependency ->
        add("testImplementation", dependency)
    }
}

fun DependencyHandler.debugImplementation(list: List<String>) {
    list.forEach { dependency ->
        add("debugImplementation", dependency)
    }
}
