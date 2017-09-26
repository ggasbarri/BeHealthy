package com.behealthy.gincos.firebase_utils.firebase_db_objects;

/**
 * Util class used to obtain an Achievement from Firebase Realtime Database.
 */
public class Achievement {

    private String description;
    private String timestamp;

    public Achievement(){}

    public Achievement(String description, String timestamp) {
        this.description = description;
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
