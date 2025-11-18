# Add project specific ProGuard rules here.
-keep class com.monitor.smsnetwork.** { *; }
-keepclassmembers class ** {
    @androidx.room.* <methods>;
}
