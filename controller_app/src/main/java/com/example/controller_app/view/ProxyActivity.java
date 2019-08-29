package com.example.controller_app.view;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import com.example.control_medium.ControlInterface;
import com.example.controller_app.util.ControlManagment;

public class ProxyActivity extends FragmentActivity {

    protected ControlInterface controlInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String className = getIntent().getStringExtra("className");

        try {
            Class<?> clazz = ControlManagment.getInstance().getControlledDexClassLoader().loadClass(className);

            Object classObject = clazz.newInstance();
            if (classObject instanceof ControlInterface) {
                controlInterface = (ControlInterface) classObject;
                controlInterface.attachContext(this);

                if (savedInstanceState == null) savedInstanceState = new Bundle();
                savedInstanceState.putString("zlrab", "test");
                controlInterface.onCreate(savedInstanceState);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Resources getResources() {
        return ControlManagment.getInstance().getControlledResources();
    }

    @Override
    public void startActivity(Intent intent) {
        Intent newIntent = new Intent(this, ProxyActivity.class);
        newIntent.putExtra("className", intent.getComponent().getClassName());
        super.startActivity(newIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        controlInterface.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        controlInterface.onRestart();
    }

    @Override
    protected void onStart() {
        super.onStart();
        controlInterface.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        controlInterface.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        controlInterface.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        controlInterface.onPause();
    }
}
