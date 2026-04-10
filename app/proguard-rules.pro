# Add project specific ProGuard rules here.
-keep public class com.investledger.data.** { *; }
-keepclassmembers class com.investledger.data.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep class * extends androidx.room.Dao

# Kotlin
-keep class kotlin.** { *; }
-keepclassmembers class kotlin.** { *; }