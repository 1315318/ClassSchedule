# 极简课表 - ProGuard 混淆规则
# Room
-keep class com.example.classschedule.data.model.** { *; }
# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.example.classschedule.utils.BackupData { *; }
