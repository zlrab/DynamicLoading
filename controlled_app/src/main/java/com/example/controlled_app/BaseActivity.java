package com.example.controlled_app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.fragment.app.FragmentActivity;

import com.example.control_medium.ControlInterface;

public class BaseActivity extends FragmentActivity implements ControlInterface {

    protected FragmentActivity mContext;

    @Override
    public void onCreate(Bundle saveInstance) {

    }

    @Override
    public void setContentView(int layoutResID) {
        mContext.setContentView(layoutResID);
    }

    @Override
    public void setContentView(View view) {
        mContext.setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        mContext.setContentView(view, params);
    }

    @Override
    public LayoutInflater getLayoutInflater() {
        return mContext.getLayoutInflater();
    }

    @Override
    public Window getWindow() {
        return mContext.getWindow();
    }


    @Override
    public View findViewById(int id) {
        return mContext.findViewById(id);
    }

    @Override
    public void attachContext(FragmentActivity activity) {
        this.mContext = activity;
    }
    @Override
    public ClassLoader getClassLoader() {
        return mContext.getClassLoader();
    }
    @Override
    public Resources getResources() {
        return mContext.getResources();
    }
    @Override
    public WindowManager getWindowManager() {
        return mContext.getWindowManager();
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        return mContext.getApplicationInfo();
    }

    @Override
    public void startActivity(Intent intent) {
        mContext.startActivity(intent);
    }



    @Override
    public void finish() {
        mContext.finish();
    }

    @Override
    public void onBackPressed() {
        mContext.onBackPressed();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mContext.onTouchEvent(event);
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onRestart() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onPause() {

    }
}
