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
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-optimizationpasses 5
-dontwarn javax.lang.model.**
-dontwarn sun.misc.**
-dontwarn org.xmlpull.v1.**
-dontwarn org.kxml2.io.**
-dontwarn android.content.res.**
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn org.jetbrains.kotlin.**
-dontwarn com.google.gson.**
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keep,allowobfuscation,allowshrinking class indi.dmzz_yyhyy.lightnovelreader.data.json.** { *; }
-keep,allowobfuscation,allowshrinking class indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.** { *; }
-keepclassmembers class indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.** { *; }
-keepnames class indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.** { *; }
-keep,allowobfuscation,allowshrinking class indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.json.** { *; }
-keepclassmembers class indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.json.** { *; }
-keepnames class indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.json.** { *; }
-keep,allowobfuscation,allowshrinking class indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.exploration.** { *; }
-keepclassmembers class indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.exploration.** { *; }
-keepnames class indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.exploration.** { *; }
-keepclassmembernames class indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.** { *; }
-keep,allowobfuscation,allowshrinking class indi.dmzz_yyhyy.lightnovelreader.data.update.** { *; }
-keepnames class indi.dmzz_yyhyy.lightnovelreader.data.update.** { *; }
-keepclassmembernames class indi.dmzz_yyhyy.lightnovelreader.data.update.** { *; }

-keepattributes Signature, *Annotation*, InnerClasses
-keep public class * implements java.lang.reflect.Type
-keepclassmembers,allowobfuscation,allowoptimization class <1> {
  <init>();
}
-keep class org.xmlpull.** { *; }
-keepclassmembers class org.xmlpull.** { *; }
-dontnote kotlinx.serialization.AnnotationsKt
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class indi.dmzz_yyhyy.lightnovelreader.**$$serializer { *; }
-keepclassmembers class indi.dmzz_yyhyy.lightnovelreader.** {
    *** Companion;
}
-keep class indi.dmzz_yyhyy.lightnovelreader.data.** { *; }
-keep class indi.dmzz_yyhyy.lightnovelreader.utils.** { *; }
-keep class indi.dmzz_yyhyy.lightnovelreader.R$* { *; }
-keep class io.nightfish.** { *; }
-keepclasseswithmembers class indi.dmzz_yyhyy.lightnovelreader.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class indi.dmzz_yyhyy.lightnovelreader.defaultplugin.** { *; }

-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

-dontwarn org.dom4j.**
-keep class org.dom4j.**{*;}
-keep interface org.dom4j.** { *; }

#CxHttp
-keep class * extends cxhttp.response.CxHttpResult{*;}
-keep class cxhttp.converter.*{*;}

-dontwarn com.fasterxml.jackson.annotation.JsonInclude$Include
-dontwarn com.fasterxml.jackson.core.json.JsonWriteFeature
-dontwarn com.fasterxml.jackson.core.type.TypeReference
-dontwarn com.fasterxml.jackson.databind.DeserializationFeature
-dontwarn com.fasterxml.jackson.databind.Module
-dontwarn com.fasterxml.jackson.databind.ObjectMapper
-dontwarn com.fasterxml.jackson.databind.SerializationFeature
-dontwarn com.fasterxml.jackson.databind.cfg.MapperBuilder
-dontwarn com.fasterxml.jackson.databind.json.JsonMapper$Builder
-dontwarn com.fasterxml.jackson.databind.json.JsonMapper
-dontwarn com.fasterxml.jackson.module.kotlin.KotlinModule$Builder
-dontwarn com.fasterxml.jackson.module.kotlin.KotlinModule