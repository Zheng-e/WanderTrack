plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.mappractice"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mappractice"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    sourceSets {
        getByName("main"){
            jniLibs.srcDirs("libs")
        }
    }

    buildToolsVersion = "34.0.0"


}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(files("libs\\BaiduLBS_Android.jar"))
    implementation(files("libs\\mysql-connector-java-5.0.4-bin.jar"))
    implementation("net.zetetic:android-database-sqlcipher:4.5.4@aar")
    implementation("androidx.sqlite:sqlite:2.1.0")
    implementation("androidx.sqlite:sqlite-framework:2.1.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}