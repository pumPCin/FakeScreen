package me.neversleep.plusplus;

import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class HookImpl {
     public static final String TAG = "neversleep";

     public static void main(final ClassLoader classLoader) {
          try {
               final XSharedPreferences xSharedPreferences = new XSharedPreferences(BuildConfig.APPLICATION_ID, "x_conf");
               xSharedPreferences.makeWorldReadable();
               xSharedPreferences.reload();
               XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.server.policy.PhoneWindowManager", classLoader), "powerPress", new XC_MethodHook() { // from class: me.neversleep.plusplus.HookImpl.1
                    int mode = 0;

                    protected void beforeHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) throws Throwable {
                         super.beforeHookedMethod(methodHookParam);
                         try {
                              XUtils.xLog("neversleep", "beforeH00kedMethod: start");
                              xSharedPreferences.reload();
                              int i = 0;
                              if (!xSharedPreferences.getBoolean("power", false)) {
                                   Log.e("neversleep", "beforeH00kedMethod: power is false");
                                   return;
                              }
                              XUtils.xLog("neversleep", "beforeH00kedMethod: power is true");
                              Class<?> cls = Class.forName("android.view.SurfaceControl", false, classLoader);
                              IBinder iBinder = getDisplayBinder(classLoader);
                              if (iBinder != null) {
                                   XposedHelpers.callStaticMethod(cls, "setDisplayPowerMode", iBinder, this.mode);
                                   if (this.mode == 0) {
                                        i = 2;
                                   }
                                   this.mode = i;
                              }
                              methodHookParam.setResult(null);
                              XUtils.xLog("neversleep", "replace success");
                         } catch (Throwable th) {
                              XUtils.xLog("neversleep", "beforeH00kedMethod: error:", th);
                         }
                    }
               });
               XUtils.xLog("neversleep", "main: H00k success");
          } catch (Throwable th) {
               th.printStackTrace();
               XUtils.xLog("neversleep", "main: error:" + th.getMessage(), th);
          }
     }

     static IBinder getDisplayBinder(ClassLoader classLoader){
          try {
               Class<?> clsSurfaceControl = XposedHelpers.findClass("android.view.SurfaceControl", classLoader);
               if(Build.VERSION.SDK_INT<Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
                    return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ?
                            (IBinder) XposedHelpers.callStaticMethod(clsSurfaceControl, "getBuiltInDisplay", 0) :
                            (IBinder) XposedHelpers.callStaticMethod(clsSurfaceControl, "getInternalDisplayToken");
               }else {
                    Class<?> clsDisplayControl = XposedHelpers.findClass("com.android.server.display.DisplayControl", classLoader);
                    long[] ids= (long[]) XposedHelpers.callStaticMethod(clsDisplayControl,"getPhysicalDisplayIds");
                    if(ids==null||ids.length==0){
                         return null;
                    }
                    return (IBinder) XposedHelpers.callStaticMethod(clsDisplayControl,"getPhysicalDisplayToken",ids[0]);
               }

          }catch (Throwable t){
               XUtils.xLog(TAG,"getDisplayBinder",t);
          }
          return null;
     }

}
