
-keepattributes Exceptions, Signature, InnerClasses, LineNumberTable

-dontshrink #不压缩输入的类文件
-dontoptimize #不优化输入的类文件
-keepattributes #*Annotation* 保留Annotation

# 不提示引用错误
-dontwarn android.**

-keep class android.** { *;}
-keep class *.** { *;}
-keep class org.jeson.reinforce.shell.$$$.processor.ApplicationDefaultProcessor { }
-keep class org.jeson.reinforce.shell.$$$.processor.ApplicationMergeProcessor { }

############################################### Android开发中一些需要保留的公共部分############################################### 保留我们使用的四大组件，自定义的Application等等这些类不被混淆# 因为这些子类都有可能被外部调用
-keep public class * extends android.**
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Appliction
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class com.android.vending.licensing.ILicensingService
# 保留support下的所有类及其内部类
-keep class android.support.** {*;}
# 保留继承的
-keep public class * extends android.support.v4.**
-keep public class * extends android.support.v7.**
-keep public class * extends android.support.annotation.**
# 保留R下面的资源
#-keep class **.R$* {*;}
# 保留本地native方法不被混淆
-keepclasseswithmembernames class * {    native <methods>;}
# 保留在Activity中的方法参数是view的方法，
# 这样以来我们在layout中写的onClick就不会被影响-keepclassmembers class * extends android.app.Activity{    public void *(android.view.View);}
# 保留枚举类不被混淆
-keepclassmembers enum * {    public static **[] values();    public static ** valueOf(java.lang.String);}
# 保留我们自定义控件（继承自View）不被混淆
-keep public class * extends android.view.View{    *** get*();    void set*(***);    public <init>(android.content.Context);    public <init>(android.content.Context, android.util.AttributeSet);    public <init>(android.content.Context, android.util.AttributeSet, int);}
# 保留Parcelable序列化类不被混淆
# -keep class * implements android.os.Parcelable {    public static final android.os.Parcelable$Creator *;}
# 保留Serializable序列化的类不被混淆
# -keepclassmembers class * implements java.io.Serializable {    static final long serialVersionUID;    private static final java.io.ObjectStreamField[] serialPersistentFields;    !static !transient <fields>;    !private <fields>;    !private <methods>;    private void writeObject(java.io.ObjectOutputStream);    private void readObject(java.io.ObjectInputStream);    java.lang.Object writeReplace();    java.lang.Object readResolve();}
# 对于带有回调函数的onXXEvent、**On*Listener的，不能被混淆
-keepclassmembers class * {    void *(**On*Event);    void *(**On*Listener);}
