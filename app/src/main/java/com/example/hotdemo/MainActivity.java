package com.example.hotdemo;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.view.View;

import com.example.hotdemo.databinding.ActivityMainBinding;
import com.example.hotdemo.test.DexFixTest;

import java.io.File;
import java.util.HashSet;

public class MainActivity extends FragmentActivity implements View.OnClickListener {
    protected ActivityMainBinding mBinding;
    protected DexFixTest dexFixTest;
    private static HashSet<File> loadedDex;
    private static final String TAG = "ZLRab";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        dexFixTest = new DexFixTest();
        loadedDex = new HashSet<>();
    }

    @Override
    protected void onResume() {

        super.onResume();
        mBinding.btnBug.setOnClickListener(this);
        mBinding.btnFix.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_bug:
                dexFixTest.testFix(this);
                break;
            case R.id.btn_fix:

                break;
            default:
                break;
        }
    }


}