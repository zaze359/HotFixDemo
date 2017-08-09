package com.zaze.hotfix;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public final class HotFix {
    static String tag = Tag.tag;

    public static void patch(Context context, String patchDexFile, String patchClassName) {
        if (patchDexFile != null && new File(patchDexFile).exists()) {
            try {
                if (hasDexClassLoader()) {
                    injectAboveEqualApiLevel14(context, patchDexFile, patchClassName);
                } else {
//                    injectBelowApiLevel14(context, patchDexFile, patchClassName);
                    Log.i(tag, "弃！");
                }
            } catch (Throwable th) {
            }
        }
    }

    /**
     * @return 检查是否有 DexClassLoader这个类
     */
    private static boolean hasDexClassLoader() {
        Log.i(tag, "hasDexClassLoader");
        try {
            Class.forName("dalvik.system.BaseDexClassLoader");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static void injectAboveEqualApiLevel14(Context context, String patchDexFile, String patchClassName)
            throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Log.i(tag, "injectAboveEqualApiLevel14");
        Log.i(tag, "patchDexFile : " + patchDexFile);
        Log.i(tag, "patchClassName : " + patchClassName);
        PathClassLoader pathClassLoader = (PathClassLoader) context.getClassLoader();
        Object patchDexElements = getDexElements(getPathList(new DexClassLoader(patchDexFile, context.getDir("dex", 0).getAbsolutePath(), patchDexFile, context.getClassLoader())));
        Object combineDexElements = combineArray(getDexElements(getPathList(pathClassLoader)), patchDexElements);
        //  通过反射获取PathList(这是 dex 序列)
        Object pathList = getPathList(pathClassLoader);
        // 修改dexElements的值
        setField(pathList, pathList.getClass(), "dexElements", combineDexElements);
        //
        pathClassLoader.loadClass(patchClassName);
    }

//    @TargetApi(14)
//    private static void injectBelowApiLevel14(Context context, String patchDexFile, String patchClassName)
//            throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
//        Log.i(tag, "injectBelowApiLevel14");
//        Log.i(tag, "patchDexFile : " + patchDexFile);
//        Log.i(tag, "patchClassName : " + patchClassName);
//        PathClassLoader pathClassLoader = (PathClassLoader) context.getClassLoader();
//        DexClassLoader dexClassLoader = new DexClassLoader(patchDexFile, context.getDir("dex", 0).getAbsolutePath(), patchDexFile, context.getClassLoader());
//        dexClassLoader.loadClass(patchClassName);
//        setField(pathClassLoader, PathClassLoader.class, "mPaths",
//                appendArray(getField(pathClassLoader, PathClassLoader.class, "mPaths"), getField(dexClassLoader, DexClassLoader.class,
//                        "mRawDexPath")
//                ));
//        setField(pathClassLoader, PathClassLoader.class, "mFiles",
//                combineArray(getField(pathClassLoader, PathClassLoader.class, "mFiles"), getField(dexClassLoader, DexClassLoader.class,
//                        "mFiles")
//                ));
//        setField(pathClassLoader, PathClassLoader.class, "mZips",
//                combineArray(getField(pathClassLoader, PathClassLoader.class, "mZips"), getField(dexClassLoader, DexClassLoader.class,
//                        "mZips")));
//        setField(pathClassLoader, PathClassLoader.class, "mDexs",
//                combineArray(getField(pathClassLoader, PathClassLoader.class, "mDexs"), getField(dexClassLoader, DexClassLoader.class,
//                        "mDexs")));
//        pathClassLoader.loadClass(patchClassName);
//    }

    // --------------------------------------------------
    private static Object getPathList(Object obj) throws ClassNotFoundException, NoSuchFieldException,
            IllegalAccessException {
        return getField(obj, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    private static Object getDexElements(Object obj) throws NoSuchFieldException, IllegalAccessException {
        return getField(obj, obj.getClass(), "dexElements");
    }

    private static Object getField(Object obj, Class cls, String str) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = cls.getDeclaredField(str);
        declaredField.setAccessible(true);
        return declaredField.get(obj);
    }

    private static void setField(Object obj, Class cls, String str, Object obj2) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = cls.getDeclaredField(str);
        declaredField.setAccessible(true);
        declaredField.set(obj, obj2);
    }

    private static Object combineArray(Object baseDex, Object fixObj) {
        Class componentType = fixObj.getClass().getComponentType();
        int fixClassLength = Array.getLength(fixObj);
        int resultLength = Array.getLength(baseDex) + fixClassLength;
        Object newInstance = Array.newInstance(componentType, resultLength);
        for (int i = 0; i < resultLength; i++) {
            if (i < fixClassLength) {
                Array.set(newInstance, i, Array.get(fixObj, i));
            } else {
                Array.set(newInstance, i, Array.get(baseDex, i - fixClassLength));
            }
        }
        return newInstance;
    }

    private static Object appendArray(Object obj, Object obj2) {
        Class componentType = obj.getClass().getComponentType();
        int length = Array.getLength(obj);
        Object newInstance = Array.newInstance(componentType, length + 1);
        Array.set(newInstance, 0, obj2);
        for (int i = 1; i < length + 1; i++) {
            Array.set(newInstance, i, Array.get(obj, i - 1));
        }
        return newInstance;
    }
}