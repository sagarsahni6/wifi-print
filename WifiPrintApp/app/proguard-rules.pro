
# Retrofit
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# Gson
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }

# Room
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# Models
-keep class com.wifiprint.app.data.models.** { *; }

# MLKit
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.** { *; }
