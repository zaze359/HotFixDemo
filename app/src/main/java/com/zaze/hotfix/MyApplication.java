package com.zaze.hotfix;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.io.File;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2017-08-01 - 18:58
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        File dexPath = new File(getDir("dex", Context.MODE_PRIVATE), "path_dex.jar");
        if (dexPath.exists()) {
            long time = System.currentTimeMillis();
            HotFix.patch(this, dexPath.getAbsolutePath(), "com.zaze.hotfix.BugClass");
            Log.i(Tag.tag, "spend : " + (System.currentTimeMillis() - time));
        }
    }
}
