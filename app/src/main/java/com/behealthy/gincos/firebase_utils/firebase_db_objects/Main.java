package com.behealthy.gincos.firebase_utils.firebase_db_objects;

/**
 * Util class used to obtain user's data located in the "main" child from Firebase Realtime Database.
 */
public class Main {

    private Last7Days last7Days;
    private long xp;
    private String lastTimeOnline;
    public Main(){}

    public Main(Last7Days last7Days, long xp, String lastTimeOnline) {
        this.last7Days = last7Days;
        this.xp = xp;
        this.lastTimeOnline = lastTimeOnline;
    }

    public Last7Days getLast7Days() {
        return last7Days;
    }

    public void setLast7Days(Last7Days last7Days) {
        this.last7Days = last7Days;
    }

    public long getXp() {
        return xp;
    }

    public void setXp(long xp) {
        this.xp = xp;
    }

    public String getLastTimeOnline() {
        return lastTimeOnline;
    }

    public void setLastTimeOnline(String lastTimeOnline) {
        this.lastTimeOnline = lastTimeOnline;
    }

}
