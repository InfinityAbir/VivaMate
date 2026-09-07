# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Moshi & Retrofit serialization models
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Data Models
-keep class com.example.data.model.** { *; }

