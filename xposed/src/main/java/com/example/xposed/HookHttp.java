package com.season.xposed;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookHttp implements IXposedHookLoadPackage {

    boolean hooked = false;
    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        XposedBridge.log("开始--handleLoadPackage---");
        if (!lpparam.packageName.equals("com.jizhou.zhufudashi")) {
            XposedBridge.log("不是当前应用");
            return;
        }
        XposedBridge.log("开始--");




        // 不在Android应用默认的classes.dex文件中的类方法的Hook操作，例如:
        // 1.MultiDex情况下的，多dex文件中的类方法的Hook操作，例如:classes1.dex中的类方法
        // 2.主dex加载的jar(包含dex)情况下的，类方法的的Hook操作

        // Hook类方法ClassLoader#loadClass(String)
        XposedHelpers.findAndHookMethod(ClassLoader.class, "loadClass", String.class, new XC_MethodHook() {

            // 在类方法loadClass执行之后执行的代码
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                // 参数的检查
                if (param.hasThrowable()) {
                    return;
                }
                // 获取指定名称的类加载之后的Class<?>
                Class<?> clazz = (Class<?>) param.getResult();
                // 获取加载的指定类的名称
                String strClazz = clazz.getName();


                if (!hooked && strClazz.contains("Splash")) {
                    hooked = true;
                    XposedBridge.log("com.loopj.android.http.AsyncHttpClient : " + strClazz);

                    Class<?> cls = XposedHelpers.findClass("com.loopj.android.http.AsyncHttpClient", lpparam.classLoader);
                    XposedBridge.hookAllConstructors(cls,
                            new XC_MethodHook() {
                                protected void afterHookedMethod(XC_MethodHook.MethodHookParam mhparam) throws Throwable {

                                    XposedBridge.log("开始--afterHookedMethod");
                                    super.afterHookedMethod(mhparam);
                                    de.robv.android.xposed.XposedHelpers.callMethod(mhparam.thisObject, "setProxy",
                                            "192.168.0.102", 8888);
                                }
                            }
                    );

                    Class[] cArgs = {boolean.class, int.class, int.class};
                    Method m2 = cls.getDeclaredMethod("getDefaultSchemeRegistry", cArgs);
                    m2.setAccessible(true);
                    XposedBridge.hookMethod(m2, new XC_MethodHook() {
                        protected void beforeHookedMethod(XC_MethodHook.MethodHookParam mhparam) throws Throwable {
                            super.beforeHookedMethod(mhparam);
                            XposedBridge.log("开始--beforeHookedMethod");
                            mhparam.args[0] = true;
                        }
                    });
                }

                if (true){
                    return;
                }

                if (strClazz.contains("com.loopj.android.http.AsyncHttpClient")) {
                    XposedBridge.log("LoadClass : " + strClazz);
              //      Context context = (Context) param.args[0];
              //      ClassLoader classLoader =context.getClassLoader();

                    // 获取被Hook的目标类的名称
                    strClassName = strClazz;
                    //XposedBridge.log("HookedClass : "+strClazz);
                    // 获取到指定名称类声明的所有方法的信息
                    Method[] m = clazz.getDeclaredMethods();
                    // 打印获取到的所有的类方法的信息
                    for (int i = 0; i < m.length; i++) {

                        XposedBridge.log("Load : " + m[i]);

                    }

                    if (true){
                        return;
                    }
                    if (strClazz.contains("com.loopj.android.http.AsyncHttpClient")){
                        XposedBridge.log("com.loopj.android.http.AsyncHttpClient : " + strClazz);

                        Class<?> cls = XposedHelpers.findClass("com.loopj.android.http.AsyncHttpClient", lpparam.classLoader);
                        XposedBridge.hookAllConstructors(cls,
                                new XC_MethodHook() {
                                    protected void afterHookedMethod(XC_MethodHook.MethodHookParam mhparam) throws Throwable {

                                        XposedBridge.log("开始--afterHookedMethod");
                                        super.afterHookedMethod(mhparam);
                                        de.robv.android.xposed.XposedHelpers.callMethod(mhparam.thisObject, "setProxy",
                                                "192.168.0.102", 8888);
                                    }
                                }
                        );

                        Class[] cArgs = {boolean.class, int.class, int.class};
                        Method m2 = cls.getDeclaredMethod("getDefaultSchemeRegistry", cArgs);
                        m2.setAccessible(true);
                        XposedBridge.hookMethod(m2, new XC_MethodHook() {
                            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam mhparam) throws Throwable {
                                super.beforeHookedMethod(mhparam);
                                XposedBridge.log("开始--beforeHookedMethod");
                                mhparam.args[0] = true;
                            }
                        });
                        return;
                    }else{
                        return;
                    }
                }





//                  // 被Hook操作的目标类名称
//                  String strClazzName = "";
//                  // 被Hook操作的类方法的名称
//                  String strMethodName = "";

                // 所有的类都是通过loadClass方法加载的
                // 过滤掉Android系统的类以及一些常见的java类库
                if (false) {
                    // 或者只Hook加密算法类、网络数据传输类、按钮事件类等协议分析的重要类

                    // 同步处理一下
                    synchronized (this.getClass()) {

                        // 获取被Hook的目标类的名称
                        strClassName = strClazz;
                        //XposedBridge.log("HookedClass : "+strClazz);
                        // 获取到指定名称类声明的所有方法的信息
                        Method[] m = clazz.getDeclaredMethods();
                        // 打印获取到的所有的类方法的信息
                        for (int i = 0; i < m.length; i++) {

                            //XposedBridge.log("HOOKED CLASS-METHOD: "+strClazz+"-"+m[i].toString());
                            if (false && !Modifier.isAbstract(m[i].getModifiers())           // 过滤掉指定名称类中声明的抽象方法
                                    && !Modifier.isNative(m[i].getModifiers())     // 过滤掉指定名称类中声明的Native方法
                                    && !Modifier.isInterface(m[i].getModifiers())  // 过滤掉指定名称类中声明的接口方法
                            ) {

                                // 对指定名称类中声明的非抽象方法进行java Hook处理
                                XposedBridge.hookMethod(m[i], new XC_MethodHook() {

                                    // 被java Hook的类方法执行完毕之后，打印log日志
                                    @Override
                                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                                        // 打印被java Hook的类方法的名称和参数类型等信息
                                        XposedBridge.log("HOOKED METHOD: " + strClassName + "-" + param.method.toString());
                                    }
                                });
                            }
                        }
                    }

                }
            }
        });
    }
            String strClassName;


            // 获取指定名称的类声明的类成员变量、类方法、内部类的信息
            public void dumpClass(Class<?> actions) {

                XposedBridge.log("Dump class " + actions.getName());
                XposedBridge.log("Methods");

                // 获取到指定名称类声明的所有方法的信息
                Method[] m = actions.getDeclaredMethods();
                // 打印获取到的所有的类方法的信息
                for (int i = 0; i < m.length; i++) {

                    XposedBridge.log(m[i].toString());
                }

                XposedBridge.log("Fields");
                // 获取到指定名称类声明的所有变量的信息
                Field[] f = actions.getDeclaredFields();
                // 打印获取到的所有变量的信息
                for (int j = 0; j < f.length; j++) {

                    XposedBridge.log(f[j].toString());
                }

                XposedBridge.log("Classes");
                // 获取到指定名称类中声明的所有内部类的信息
                Class<?>[] c = actions.getDeclaredClasses();
                // 打印获取到的所有内部类的信息
                for (int k = 0; k < c.length; k++) {

                    XposedBridge.log(c[k].toString());
                }

            }
        }


