# Godot JNI and the plugin host. R8 must not rename these (ANDROID-BUILD-003).
-keep class org.godotengine.** { *; }
-keep class pt.mataventuras.app.engine.** { *; }
-keep class pt.mataventuras.plugin.** { *; }
-keepclassmembers class * {
    @org.godotengine.godot.plugin.UsedByGodot <methods>;
}
