package com.example.controller_app.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;


import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class ControlManagment {

    private DexClassLoader controlledDexClassLoader;

    private Resources controlledResources;

    private PackageInfo controlledPackageArchiveInfo;

    public void initLocalApkFile(@NotNull String apkAbsolutePath, Context context) {
        File file = new File(apkAbsolutePath);
        controlledDexClassLoader = new DexClassLoader(apkAbsolutePath, context.getDir("zlrab", Context.MODE_PRIVATE).getAbsolutePath(), null, context.getClassLoader());
        controlledPackageArchiveInfo = context.getPackageManager().getPackageArchiveInfo(apkAbsolutePath, PackageManager.GET_ACTIVITIES);

        AssetManager controlledAssetManager = null;

        try {
            controlledAssetManager = AssetManager.class.newInstance();
            Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);
            addAssetPath.invoke(controlledAssetManager, apkAbsolutePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        controlledResources = new Resources(controlledAssetManager, context.getResources().getDisplayMetrics(), context.getResources().getConfiguration());
    }

    public DexClassLoader getControlledDexClassLoader() {
        return controlledDexClassLoader;
    }

    public Resources getControlledResources() {
        return controlledResources;
    }

    public PackageInfo getControlledPackageArchiveInfo() {
        return controlledPackageArchiveInfo;
    }

    private ControlManagment() {

    }

    public static ControlManagment getInstance() {
        return Instance.sControlManagment;
    }


    private static class Instance {
        private static ControlManagment sControlManagment = new ControlManagment();
    }
}
