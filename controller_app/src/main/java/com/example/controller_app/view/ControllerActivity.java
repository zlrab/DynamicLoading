package com.example.controller_app.view;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import com.example.controller_app.R;
import com.example.controller_app.util.ControlManagment;

import java.io.File;

public class ControllerActivity extends FragmentActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        ControlManagment.getInstance().initLocalApkFile(Environment.getExternalStorageDirectory() + File.separator + "zlrab" + File.separator + "demo.apk", this);
        findViewById(R.id.btn_load_apk).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, ProxyActivity.class);
        intent.putExtra("className", ControlManagment.getInstance().getControlledPackageArchiveInfo().activities[0].name);
        startActivity(intent);
    }
}
