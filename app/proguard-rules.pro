-keep class kotlin.collections.** { *; }
-keep class kotlin.** { *; }
-dontwarn kotlin.**

# WorkManager (usado por Glance): se instancia por reflexión
-keep class androidx.work.InputMerger { *; }
-keep class * extends androidx.work.InputMerger {
    <init>();
}
-keep class * extends androidx.work.ListenableWorker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.Worker { *; }
-dontwarn androidx.work.**
