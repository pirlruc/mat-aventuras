# GDScript calls these methods by the names in host.gd (UsedByGodot).
-keepclassmembers class pt.mataventuras.app.engine.godot.MatAventurasGodotPlugin {
    public java.lang.String getPluginName();
    public java.lang.String mascotCode();
    public java.lang.String childName();
    public boolean youngBand();
    public java.lang.String livesLine(int, int);
    public void prizeReady();
    public java.lang.String rewardScene();
    public void completeReward(boolean);
    public int prizeLimitSeconds();
    public boolean introHolding();
}

# libgodot_android.so looks up Godot, the fragment, and the plugin base by name.
-keep class org.godotengine.godot.Godot { *; }
-keep class org.godotengine.godot.GodotFragment { *; }
-keep class org.godotengine.godot.GodotHost { *; }
-keep class org.godotengine.godot.GodotLib { *; }
-keep class org.godotengine.godot.plugin.GodotPlugin { *; }
-keep class org.godotengine.godot.plugin.UsedByGodot { *; }
-keep class org.godotengine.godot.plugin.SignalInfo { *; }
