package com.zaze.hotfix;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.main_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadBugClass bugClass = new LoadBugClass();
                Log.i(Tag.tag, "测试调用方法 : " + bugClass.getBugString());
                Toast.makeText(MainActivity.this, "测试调用方法 : " + bugClass.getBugString(), Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.main_fix_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File dexPath = new File(getDir("dex", Context.MODE_PRIVATE), "path_dex.jar");
                Log.i(Tag.tag, "dexPath : " + dexPath.getAbsolutePath());
                // 准备补丁
                prepareDex(MainActivity.this, dexPath, "path_dex.jar");
                HotFix.patch(MainActivity.this, dexPath.getAbsolutePath(), "com.zaze.hotfix.BugClass");
            }
        });
    }

    public boolean prepareDex(Context context, File dexInternalStoragePath, String dex_file) {
        BufferedInputStream bis = null;
        OutputStream dexWriter = null;
        try {
            bis = new BufferedInputStream(context.getAssets().open(dex_file));
            dexWriter = new BufferedOutputStream(new FileOutputStream(dexInternalStoragePath));
            int BUF_SIZE = 2048;
            byte[] buf = new byte[BUF_SIZE];
            int len;
            while ((len = bis.read(buf, 0, BUF_SIZE)) > 0) {
                dexWriter.write(buf, 0, len);
            }
            dexWriter.close();
            bis.close();
            return true;
        } catch (IOException e) {
            if (dexWriter != null) {
                try {
                    dexWriter.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            return false;
        }
    }
}
