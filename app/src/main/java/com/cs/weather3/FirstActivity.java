package com.cs.weather3;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

public class FirstActivity extends AppCompatActivity implements AutoPermissionsListener {
    private final String TAG = "FirstActi-";

    boolean boolPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        new FirstThread(this).start();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
    }
    @Override
    public void onDenied(int i, String[] strings) {
        Log.e(TAG+"권한", "거절");
        boolPermission = true;
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("boolPermission", boolPermission);
        startActivity(intent);
        finish();
    }
    @Override
    public void onGranted(int i, String[] strings) {
        Log.e(TAG+"권한", "거절");
        boolPermission = true;
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("boolPermission", boolPermission);
        startActivity(intent);
        finish();
    }


    class FirstThread extends Thread{
        Activity activity;

        public FirstThread(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void run() {
            if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                //권한
                AutoPermissions.Companion.loadSelectedPermissions(activity, 101,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
            }
            else{
                Log.e(TAG+"권한", "이미 얻음");
                boolPermission = true;
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent(activity, MainActivity.class);
                intent.putExtra("boolPermission", boolPermission);
                activity.startActivity(intent);
                activity.finish();
            }
        }
    }

}



