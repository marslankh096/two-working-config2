plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.hm.admanagerx"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        targetSdk = 35
//        versionCode = 1
//        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )

            resValue("string", "in_app_key", "lifetime")


            //new ads plan ids
            resValue ("string", "ad_app_id", "ca-app-pub-8620219021552434~9473669450")

            resValue("string", "app_open_ad", "ca-app-pub-8620219021552434/9685025154")
            resValue("string", "inter_ad_splash_first_open", "ca-app-pub-8620219021552434/1835327437")
            resValue("string", "inter_ad_splash_second_open", "ca-app-pub-8620219021552434/6602161275")
            resValue("string", "inter_ad_language", "ca-app-pub-8620219021552434/1835327437")
            resValue("string", "inter_ad_voice_result_finding", "ca-app-pub-8620219021552434/6374930567")
            resValue("string", "inter_ad_translate_done", "ca-app-pub-8620219021552434/2879943657")
            resValue("string", "inter_ad_conversation", "ca-app-pub-8620219021552434/3558600503")
            resValue("string", "inter_ad_camera_translation", "ca-app-pub-8620219021552434/7134883430")
            resValue("string", "native_ad_language", "ca-app-pub-8620219021552434/6136662389")
            resValue("string", "banner_ad_main", "ca-app-pub-8620219021552434/6276263185")
            resValue("string", "native_ad_main", "ca-app-pub-8620219021552434/8640408930")
            resValue("string", "native_ad_settings", "ca-app-pub-8620219021552434/8160190494")
            resValue("string", "native_ad_on_boarding", "ca-app-pub-8620219021552434/3099435503")
            resValue("string", "native_ad_exit_dialog", "ca-app-pub-8620219021552434/5937351832")
            resValue("string", "native_ad_history", "ca-app-pub-8620219021552434/6859551267")
            resValue("string", "native_ad_favourite", "ca-app-pub-8620219021552434/3056037625")
            resValue("string", "native_ad_language_selection", "ca-app-pub-8620219021552434/5091174022")
            resValue("string", "native_ad_translation", "ca-app-pub-8620219021552434/9110310089")
            resValue("string", "banner_ad_voice_result", "ca-app-pub-8620219021552434/5111355329")
            resValue("string", "banner_ad_camera", "ca-app-pub-8620219021552434/7120277856")
        }
        debug {

            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            resValue("string", "in_app_key", "android.test.purchased")


            //new ads plan ids
            resValue ("string", "ad_app_id", "ca-app-pub-3940256099942544~3347511713")

            resValue("string", "app_open_ad", "ca-app-pub-3940256099942544/9257395921")
            resValue("string", "inter_ad_splash_first_open", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "inter_ad_splash_second_open", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "inter_ad_language", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "inter_ad_voice_result_finding", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "inter_ad_translate_done", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "inter_ad_conversation", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "inter_ad_camera_translation", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "native_ad_language", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "banner_ad_main", "ca-app-pub-3940256099942544/6300978111")
            resValue("string", "native_ad_main", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "native_ad_settings", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "native_ad_on_boarding", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "native_ad_exit_dialog", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "native_ad_history", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "native_ad_favourite", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "native_ad_language_selection", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "native_ad_translation", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "banner_ad_voice_result", "ca-app-pub-3940256099942544/6300978111")
            resValue("string", "banner_ad_camera", "ca-app-pub-3940256099942544/6300978111")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true

    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")


    api(platform("com.google.firebase:firebase-bom:33.1.2"))
    api("com.google.firebase:firebase-config")
    api("com.google.firebase:firebase-analytics")


    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-ktx:1.9.0")
    annotationProcessor("androidx.lifecycle:lifecycle-compiler:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.2")

    implementation("com.google.code.gson:gson:2.10.1")

    api("com.facebook.shimmer:shimmer:0.5.0")

    api("com.google.android.gms:play-services-ads:24.2.0")

//    api ("com.google.ads.mediation:facebook:6.18.0.0")
//    api ("com.google.ads.mediation:vungle:7.4.2.0")
//    api ("com.google.ads.mediation:mintegral:16.9.41.0")
//    api ("com.google.ads.mediation:pangle:6.5.0.3.0")
//    api ("com.google.ads.mediation:applovin:13.1.0.0")
//    api("com.google.ads.mediation:inmobi:10.8.0.0")
//    api("com.google.ads.mediation:ironsource:8.7.0.0")
//    api("com.unity3d.ads:unity-ads:4.13.1")
//    api("com.google.ads.mediation:unity:4.13.1.0")
//
//    api ("com.facebook.infer.annotation:infer-annotation:0.18.0")


}