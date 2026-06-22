# ArcOra Production ProGuard Rules

# Keep generic signatures — critical for Retrofit + Gson List<T> deserialization
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Retrofit — keep interface signatures so getGenericReturnType() works
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keep class com.arcora.data.api.ArcOraApi { *; }
-keep class com.arcora.data.api.ArcOraApi$* { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson — must NOT allowshrinking on TypeToken
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep class com.google.gson.** { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.arcora.data.api.** { *; }
-keep class com.arcora.domain.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# Google Play Services Auth
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.common.** { *; }
-dontwarn com.google.android.gms.**

# AndroidX Credentials
-keep class androidx.credentials.** { *; }
-keep class androidx.credentials.playservices.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Biometric
-keep class androidx.biometric.** { *; }

# Coil
-keep class coil.** { *; }
-dontwarn coil.**

# ZXing QR
-keep class com.google.zxing.** { *; }

# Socket.IO
-keep class io.socket.** { *; }
-dontwarn io.socket.**
