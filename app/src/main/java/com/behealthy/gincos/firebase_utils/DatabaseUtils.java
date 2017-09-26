package com.behealthy.gincos.firebase_utils;

import com.google.firebase.database.FirebaseDatabase;

/**
 * This class is used to retrieve an Instance of the Firebase Realtime Database, securing uniqueness on the instance returned.
 */
public class DatabaseUtils {

    private static FirebaseDatabase mDatabase;

    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }

}
