package com.behealthy.gincos.firebase_utils.firebase_db_objects;

/**
 * Util class used to obtain scores from previous 7 days from Firebase Realtime Database.
 */
public class Last7Days {

    public Last7Days(long day1, long day2, long day3, long day4, long day5, long day6, long day7) {
        this.day1 = day1;
        this.day2 = day2;
        this.day3 = day3;
        this.day4 = day4;
        this.day5 = day5;
        this.day6 = day6;
        this.day7 = day7;
    }

    public long getDay1() {
        return day1;
    }

    public void setDay1(long day1) {
        this.day1 = day1;
    }

    public long getDay2() {
        return day2;
    }

    public void setDay2(long day2) {
        this.day2 = day2;
    }

    public long getDay3() {
        return day3;
    }

    public void setDay3(long day3) {
        this.day3 = day3;
    }

    public long getDay4() {
        return day4;
    }

    public void setDay4(long day4) {
        this.day4 = day4;
    }

    public long getDay5() {
        return day5;
    }

    public void setDay5(long day5) {
        this.day5 = day5;
    }

    public long getDay6() {
        return day6;
    }

    public void setDay6(long day6) {
        this.day6 = day6;
    }

    public long getDay7() {
        return day7;
    }

    public void setDay7(long day7) {
        this.day7 = day7;
    }

    private long day1 = 0; //yesterday
    private long day2 = 0;
    private long day3 = 0;
    private long day4 = 0;
    private long day5 = 0;
    private long day6 = 0;
    private long day7 = 0; //a week ago

    public Last7Days(){}

}
