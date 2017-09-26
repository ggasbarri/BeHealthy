package com.behealthy.gincos.firebase_utils.firebase_db_objects;

/**
 * Util class used to obtain an ActiveTask from Firebase Realtime Database.
 */
public class ActiveTask {

    private int step;
    private String startDate;
    private int id;
    private String lastRegistryDate;

    public ActiveTask(){}

    public ActiveTask(int step, String startDate, String lastRegistryDate) {
        this.step = step;
        this.startDate = startDate;
        this.lastRegistryDate = lastRegistryDate;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLastRegistryDate() {
        return lastRegistryDate;
    }

    public void setLastRegistryDate(String lastRegistryDate) {
        this.lastRegistryDate = lastRegistryDate;
    }
}
