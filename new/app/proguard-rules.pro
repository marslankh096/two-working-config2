# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }

-dontwarn com.squareup.okhttp.**
-dontwarn okio.**
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

-dontwarn okhttp3.**

-keep class com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.** { *; }
-keep class com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.widgets.** { *; }
-keep class com.ahmadullahpk.alldocumentreader.xs.** { *; }


# For Google Play Services
-keep public class com.google.android.gms.ads.**{
   public *;
}

# For old ads classes
-keep public class com.google.ads.**{
   public *;
}

# For mediation
-keepattributes *Annotation*

# Other required classes for Google Play Services
# Read more at http://developer.android.com/google/play-services/setup.html
-keep class * extends java.util.ListResourceBundle {
   protected Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
   public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
   @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
   public static final ** CREATOR;
}
-keep class com.google.android.material.** { *; }
-keep class com.google.android.material.R$drawable { *; }

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**

-keep class com.google.gson.stream.** { *; }

# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken
##---------------End: proguard configuration for Gson  ----------

##---------------Begin: proguard configuration for Retrofit  ----------
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Keep Retrofit-related classes and methods
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keepattributes Signature

# If you're using Gson for JSON parsing, keep Gson-related classes
-keep class com.google.gson.** { *; }

# If you're using other converter factories, keep their classes
-keep class com.squareup.** { *; }

# If you're using RxJava with Retrofit, keep RxJava-related classes
-keep class io.reactivex.** { *; }

# Add any other rules specific to your project
##---------------End: proguard configuration for Retrofit  ----------

-keep class com.facebook.** {*;}
-keep interface com.facebook.** {*;}
-keep class kotlin.jvm.internal.** { *; }
##liftoff
# GMA Vungle Mediation Adapter
#-keep public class com.google.ads.mediation.vungle.** { *; }
#-keep public class com.vungle.publisher.** { *; }

# Additional Rules (Optional)
#-keep class com.vungle.publisher.VunglePub { *; }
#-keep class com.vungle.publisher.VunglePubInterface { *; }

# GMA Applovin Mediation Adapter
#-keep public class com.google.ads.mediation.applovin.** { *; }
#-keep class com.applovin.sdk.** { *; } # Preserve Applovin classes

# Optional (GMA internal classes)
#-keep class com.google.ads.mediation.MediationAdapter { *; }
#-keep public class com.google.ads.mediation.** { *; }


-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
   public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
   @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
   public static final ** CREATOR;
}

-dontwarn android.media.LoudnessCodecController$OnLoudnessCodecUpdateListener
-dontwarn android.media.LoudnessCodecController