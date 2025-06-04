package com.example.todolist.data.firebase;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class FirebaseConfig {
    private static final String TAG = "FirebaseConfig";
    private static boolean isInitialized = false;

    public static void initialize() {
        if (!isInitialized) {
            // Enable Firestore offline persistence
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                    .build();

            db.setFirestoreSettings(settings);

            isInitialized = true;
        }
    }

    /**
     * Get configured Firestore instance
     */
    public static FirebaseFirestore getFirestore() {
        initialize();
        return FirebaseFirestore.getInstance();
    }
}