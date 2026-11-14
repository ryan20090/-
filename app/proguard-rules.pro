# Add project specific ProGuard rules here.
-keep class com.example.model.** { *; }
-keep class com.example.data.remote.** { *; }
-keep class com.example.data.local.** { *; }
-keepnames class * extends androidx.room.RoomDatabase
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
