package com.behealthy.gincos.utils;

import com.behealthy.gincos.utils.Tasks.WaterFighter;

/**
 * Base constructor for each of the tasks.
 */
public class Task {

    private String id;                          //Firebase token
    private int status;
    private int title;
    private int description;
    private String lastRegistryDate;
    private String[] hints;
    private String currentHint;
    private int currentHintPosition;

    private int iconResID;
    private int secondaryDescription;         //Bayer reference
    private int curiousFact;                  // -> TasksActivity
    private long[] xpObtained;                //On Task Finished

    public static final int STATUS_ACTIVE = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_UPCOMING = 2;
    public static final int STATUS_DISMISSED = 3;

    public Task(String id, int status, int title, int description, String[] hints, int currentHintPosition, int iconResID, int secondaryDescription, int curiousFact, long[] xpObtained, String lastRegistryDate) {
        this.id = id;
        this.status = status;
        this.title = title;
        this.description = description;
        this.hints = hints;
        this.currentHintPosition = currentHintPosition;
        this.iconResID = iconResID;
        this.secondaryDescription = secondaryDescription;
        this.curiousFact = curiousFact;
        this.xpObtained = xpObtained;
        this.lastRegistryDate = lastRegistryDate;


        setCurrentHint(currentHintPosition);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public int getDescription() {
        return description;
    }

    public void setDescription(int description) {
        this.description = description;
    }

    public String[] getHints() {
        return hints;
    }

    public void setHints(String[] hints) {
        this.hints = hints;
    }

    public int getCurrentHintPosition() {
        return currentHintPosition;
    }

    public void setCurrentHintPosition(int currentHintPosition) {
        this.currentHintPosition = currentHintPosition;
    }

    public int getIconResID() {
        return iconResID;
    }

    public void setIcon(int iconResID) {
        this.iconResID = iconResID;
    }

    public int getSecondaryDescription() {
        return secondaryDescription;
    }

    public void setSecondaryDescription(int secondaryDescription) {
        this.secondaryDescription = secondaryDescription;
    }

    public int getCuriousFact() {
        return curiousFact;
    }

    public void setCuriousFact(int curiousFact) {
        this.curiousFact = curiousFact;
    }

    public long[] getXpObtained() {
        return xpObtained;
    }

    public void setXpObtained(long[] xpObtained) {
        this.xpObtained = xpObtained;
    }



    public void setCurrentHint(int currentHintPosition){
        this.currentHint = this.hints[currentHintPosition];
    }

    public String getCurrentHint(){
        return this.currentHint;
    }

    public static Task getTaskById(String id, int step){
        Task task;
        if (id.equals(WaterFighter.ID)){
            task = new WaterFighter(Task.STATUS_ACTIVE,step);
        }else {
            return null;
        }
        return task;
    }

    public String getLastRegistryDate() {
        return lastRegistryDate;
    }

    public void setLastRegistryDate(String lastRegistryDate) {
        this.lastRegistryDate = lastRegistryDate;
    }
}
