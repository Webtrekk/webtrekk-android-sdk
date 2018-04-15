# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/user/Android/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# this congiguration should be considered when it would be time for resourse shrink.

-keep public class com.webtrekk.webtrekksdk.Webtrekk {
    public <methods>;
}
-keep public class com.webtrekk.webtrekksdk.WebtrekkApplication {
    public <methods>;
}
-keep public enum com.webtrekk.webtrekksdk.TrackingParameter$** {
    public *;
    **[] $VALUES;
}
-keep public class com.webtrekk.webtrekksdk.TrackingParameter {
    public <methods>;
}
-keep public class com.webtrekk.webtrekksdk.WebtrekkUserParameters {
    public <methods>;
}

-keep public class com.webtrekk.webtrekksdk.** extends android.content.BroadcastReceiver {
    public <methods>;
}

-keep public class com.webtrekk.webtrekksdk.Plugin {
    public <methods>;
}

-keep public class com.webtrekk.webtrekksdk.R$raw {
    public <fields>;
}

-keepattributes InnerClasses, EnclosingMethod
