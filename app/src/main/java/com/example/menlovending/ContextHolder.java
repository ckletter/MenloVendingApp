package com.example.menlovending;

import android.content.Context;

public class ContextHolder {
    private static Context context;

    public static void setContext(Context newContext) {
        context = newContext.getApplicationContext(); // Use application context to avoid memory leaks
    }

    public static Context getContext() {
        return context;
    }
}

