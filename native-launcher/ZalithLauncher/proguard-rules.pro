-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn com.github.luben.zstd.**
-dontwarn java.lang.management.**
-dontwarn io.ktor.util.debug.**
-dontwarn com.google.auto.value.**
-dontwarn com.google.crypto.tink.**
-dontwarn com.microsoft.device.display.**
-dontwarn edu.umd.cs.findbugs.annotations.**

# Room
-keepclassmembers class * {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}

# Launcher
-keep class org.lwjgl.glfw.CallbackBridge {
    *;
}
-keep class com.oracle.dalvik.VMLauncher {
    *;
}