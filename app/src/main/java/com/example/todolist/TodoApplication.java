package com.example.todolist;

import android.app.Application;
import android.util.Log;

import com.example.todolist.data.firebase.FirebaseConfig;
import com.google.firebase.FirebaseApp;

public class TodoApplication extends Application {
    private static final String TAG = "TodoApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        Log.d(TAG, "Firebase initialized successfully");

        // Initialize Firebase configuration
        FirebaseConfig.initialize();
        Log.d(TAG, "FirebaseConfig initialized successfully");
    }
}