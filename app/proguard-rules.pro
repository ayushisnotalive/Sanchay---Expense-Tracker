# Sanchay ProGuard & R8 Optimization Rules for Google Play Store Production

# Kotlin Coroutines & Flow
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}

# Android Architecture Components & Room Database
-keep class androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.paging.**
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep class * extends androidx.room.migration.Migration
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public abstract *;
}

# Retrofit 2 & OkHttp 3 & Okio
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Gson Serialization / Deserialization
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Sanchay Financial Domain Models & DTOs
-keep class com.camuel.sanchay.Expense { *; }
-keep class com.camuel.sanchay.Category { *; }
-keep class com.camuel.sanchay.Wallet { *; }
-keep class com.camuel.sanchay.Subscription { *; }
-keep class com.camuel.sanchay.SavingsGoal { *; }
-keep class com.camuel.sanchay.Debt { *; }
-keep class com.camuel.sanchay.Investment { *; }
-keep class com.camuel.sanchay.CategoryBudget { *; }
-keep class com.camuel.sanchay.services.** { *; }
-keep class com.camuel.sanchay.utils.** { *; }

# WorkManager
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-keep class * extends androidx.work.WorkerParameters { *; }

# Jetpack Compose Runtime & Stable Models
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.animation.** { *; }
-keep class androidx.compose.ui.** { *; }

# Google ML Kit Text Recognition & Play Services Location
-dontwarn com.google.mlkit.vision.**
-keep class com.google.mlkit.vision.** { *; }
-dontwarn com.google.android.gms.vision.**
-keep class com.google.android.gms.vision.** { *; }
-dontwarn com.google.android.gms.location.**
-keep class com.google.android.gms.location.** { *; }
-dontwarn com.google.android.gms.tasks.**
-keep class com.google.android.gms.tasks.** { *; }

# Retain line numbers for Play Console crash symbolication
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Apache POI & XMLBeans & Saxon (Excel Export)
-dontwarn java.awt.**
-dontwarn javax.xml.stream.**
-dontwarn net.sf.saxon.**
-dontwarn org.osgi.framework.**
-dontwarn org.apache.poi.**
-dontwarn org.apache.xmlbeans.**
-dontwarn org.apache.logging.log4j.**
-keep class org.apache.poi.** { *; }
-keep class org.apache.xmlbeans.** { *; }

# OSMDroid Maps
-dontwarn org.osmdroid.**
-keep class org.osmdroid.** { *; }