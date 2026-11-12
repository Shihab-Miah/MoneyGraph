# Room database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class com.miah_shihab.moneygraph.data.** { *; }
-keep class com.miah_shihab.moneygraph.domain.model.** { *; }

# Google APIs
-keep class com.google.api.client.** { *; }
-keep class com.google.api.services.drive.** { *; }
-dontwarn com.google.api.client.**
-dontwarn org.apache.http.**
-dontwarn com.google.common.**
