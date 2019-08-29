package com.example.hotdemo.test;

import android.content.Context;
import android.widget.Toast;

public class DexFixTest {
    public void testFix(Context context) {
        int num1 = 100;
        int num2 = 0;

        Toast.makeText(context, "Test=" + (num1 / num2), Toast.LENGTH_LONG).show();
    }

/*    public void testFix(Context context) {
        int num1 = 100;
        int num2 = 10;

        Toast.makeText(context, "Test=" + (num1 / num2), Toast.LENGTH_LONG).show();
    }*/
}
