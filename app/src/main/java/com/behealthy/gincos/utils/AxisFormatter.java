package com.behealthy.gincos.utils;

import android.content.Context;
import com.behealthy.gincos.R;
import com.behealthy.gincos.firebase_utils.firebase_db_objects.Last7Days;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AxisFormatter implements IAxisValueFormatter{

    ArrayList<String> mValues = new ArrayList<>();

    String pseudoDatePattern;

    public AxisFormatter(Context c, ArrayList<Date> list, String datePattern){

        //Be consistent with user's preferred date format.
        if(datePattern.equals(c.getString(R.string.settings_date_option_latin))){
            pseudoDatePattern = "dd/MM";
        }else if(datePattern.equals(c.getString(R.string.settings_date_option_american))||datePattern.equals(c.getString(R.string.settings_date_option_european))) {
            pseudoDatePattern = "MM/dd";
        } else {
            pseudoDatePattern = "dd/MM";
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(pseudoDatePattern, Locale.getDefault());
        for(Date date : list){
            mValues.add(dateFormat.format(date));
        }

    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return mValues.get((int)value);
    }

    public static Last7Days reverseLast7Days(Last7Days last7Days){
        Last7Days newDays = new Last7Days(0,0,0,0,0,0,0);
        newDays.setDay1(last7Days.getDay7());
        newDays.setDay2(last7Days.getDay6());
        newDays.setDay3(last7Days.getDay5());
        newDays.setDay4(last7Days.getDay4());
        newDays.setDay5(last7Days.getDay3());
        newDays.setDay6(last7Days.getDay2());
        newDays.setDay7(last7Days.getDay1());
        return newDays;

    }
}
