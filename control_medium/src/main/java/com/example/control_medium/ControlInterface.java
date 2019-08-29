package com.example.control_medium;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

public interface ControlInterface {
    void onCreate(Bundle saveInstance);

    void attachContext(FragmentActivity activity);

    void onStart();

    void onResume();

    void onRestart();

    void onDestroy();

    void onStop();

    void onPause();
}
