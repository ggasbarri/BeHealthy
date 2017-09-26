package com.behealthy.gincos.utils.Tasks;

import com.behealthy.gincos.R;
import com.behealthy.gincos.utils.Task;

/**
 * First task, requires the user to drink a healthy amount of water daily. ID: 1.
 */
public class WaterFighter extends Task {

    public static final String ID = "1";

    public WaterFighter(int status, int step) {
        super(ID,
                status,
                R.string.water_title,
                R.string.water_description_1,
                new String[]{"5 Daily","5 Daily", "6 Daily", "6 Daily", "7 Daily", "7 Daily","8 Daily"},
                step,
                R.drawable.water_fighter_icon,
                R.string.water_description_2,
                R.string.water_curious_fact,
                new long[]{300,300,400,400,400,400,500},
                "");
    }

}
