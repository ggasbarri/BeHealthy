package com.behealthy.gincos.firebase_utils;

/*
    Database JSON Structure:

root

    { user.getUid() }

        achievements

            { push ID }

                description:                String. Ex "Started being healthy!"
                timestamp:                  String Ex "19/08/2017"

        activeTasks

            { Task ID }

                lastRegistryDate:           String. Ex "19/08/2017"
                startDate:                  String. Ex "19/08/2017"
                step:                       Int. Ex 4

        main

            last7Days

                day1:                       Long. Yesterday score. Ex 7
                day2:                       Long. Ex 3
                day3:                       Long. Ex 3
                day4:                       Long. Ex 2
                day5:                       Long. Ex 0
                day6:                       Long. Ex 1
                day7:                       Long. Ex 3

            lastTimeOnline:                 String. Ex "19/08/2017"

        xp:                                 Long. Ex 3000

        firstTimeOnline:                    String. Ex "19/08/2017"

 */

/**
 * This class is used for database references.
 */
public class DatabaseContract {

    public static final String MAIN = "main";
    public static final String LAST_7_DAYS = "last7Days";
    public static final String LAST_TIME_ONLINE = "lastTimeOnline";

    public static final String ACHIEVEMENTS = "achievements";
    public static final String DESCRIPTION = "description";
    public static final String TIMESTAMP = "timestamp";

    public static final String XP = "xp";

    public static final String FIRST_TIME_ONLINE = "firstTimeOnline";

    public static final String ACTIVE_TASKS = "activeTasks";
    public static final String STEP = "step";
    public static final String START_DATE = "startDate";
    public static final String LAST_REGISTRY_DATE = "lastRegistryDate";

}