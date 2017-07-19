package com.updater.eccm.apps.updatertestapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.updater.eccm.appupdater.AppUpdater;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new AppUpdater(MainActivity.this).sendNetworkUpdateAppRequest("https://www.caliente.mx/android/calienteSports_new.json");
    }
}
