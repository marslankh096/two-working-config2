# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

-keep class com.google.android.gms.internal.consent_sdk.** { <fields>; }
-keepattributes *Annotation*
-keepattributes Signature

#
## pangle
#-keep class com.bytedance.sdk.** { *; }
#-keep class com.pgl.sys.ces.* {*;}
#
##vungle
#-keep class com.vungle.** {*;}
#-keep interface com.vungle.** {*;}
#
## Applovin
#-keep class com.applovin.** { *; }
#-keep interface com.applovin.** { *; }
#
## Mintegral
#-keep class com.mbridge.msdk.** {*;}
#
## facebook
#-keep class com.facebook.* {*;}
#-keep interface com.facebook.* {*;}
#

# AdMob Ads
-dontwarn com.google.android.gms.**
-keep class com.google.android.gms.** { *; }
-keep class com.google.firebase.** { *; }


## Chartboost Mediation SDK
#-keep class com.chartboost.heliumsdk.** { *; }
#
## Chartboost Mediation Adapters
#-keep class com.chartboost.mediation.** { *; }


-keep class com.hm.admanagerx.AdConfig { *; }
-keep class com.hm.admanagerx.RemoteAdConfig { *; }