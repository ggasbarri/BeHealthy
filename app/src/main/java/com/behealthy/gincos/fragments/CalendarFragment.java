package com.behealthy.gincos.fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ProgressBar;
import com.behealthy.gincos.R;
import com.behealthy.gincos.activities.MainActivity;
import com.behealthy.gincos.firebase_utils.DatabaseContract;
import com.behealthy.gincos.firebase_utils.firebase_db_objects.Achievement;
import com.behealthy.gincos.utils.CalendarAchievementDecorator;
import com.behealthy.gincos.utils.MyTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

/**
 * Shows a Calendar with user's achievements.
 */
public class CalendarFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    //Views
    MaterialCalendarView calendarView;
    CardView cardView;
    MyTextView descriptionTv;
    MyTextView dateTv;
    ProgressBar progressBar;

    ArrayList<Achievement> achievements = new ArrayList<>();

    public boolean isCardviewShown;

    //Firebase variables
    private DatabaseReference achievementsReference;
    private DatabaseReference firstTimeOnlineReference;

    static HashSet<CalendarDay> set;
    CalendarDay currentDay;

    String dateFormatPattern;

    public CalendarFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_calendar, container, false);

        //Get date format from preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        dateFormatPattern = prefs.getString(getString(R.string.settings_date_format_key),getString(R.string.settings_date_option_latin));

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        //Database references setup
        FirebaseDatabase mFirebaseDatabase = ((MainActivity) getActivity()).mFirebaseDatabase;
        assert user != null;
        achievementsReference = mFirebaseDatabase.getReference().child(user.getUid()).child(DatabaseContract.ACHIEVEMENTS);
        firstTimeOnlineReference = mFirebaseDatabase.getReference().child(user.getUid()).child(DatabaseContract.FIRST_TIME_ONLINE);

        isCardviewShown = false;

        //Assign views
        calendarView = rootView.findViewById(R.id.materialCalendarView);
        cardView = rootView.findViewById(R.id.calendar_description_cv);
        descriptionTv = rootView.findViewById(R.id.calendar_achievement_desc);
        dateTv = rootView.findViewById(R.id.calendar_achivement_date);
        progressBar = rootView.findViewById(R.id.progress_bar_calendar);

        set = new HashSet<>();

        setupFirebaseListeners();
        
        return rootView;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        MainActivity.currentFragment = MainActivity.FRAGMENT_CALENDAR;
        ((MainActivity)getActivity()).navView.setCheckedItem(R.id.drawer_calendar);
    }
    
    private void animateCardView(CalendarDay calendarDay, final String description, final String date){

        if(calendarDay!=currentDay) {
            if (!isCardviewShown) {
                AnimationSet animationSet = new AnimationSet(true);
                TranslateAnimation translateFromBottom = new TranslateAnimation(0f, 0f, 50f, 0f);
                translateFromBottom.setDuration(400);
                AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
                alphaAnimation.setDuration(200);

                animationSet.addAnimation(alphaAnimation);
                animationSet.addAnimation(translateFromBottom);
                animationSet.setDuration(500);

                animationSet.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        dateTv.setText(date);
                        descriptionTv.setText(description);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        cardView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                cardView.startAnimation(animationSet);

                isCardviewShown = true;
            } else {
                AlphaAnimation anim1 = new AlphaAnimation(1f, 0f);
                anim1.setDuration(200);
                anim1.setRepeatCount(1);
                anim1.setRepeatMode(Animation.REVERSE);
                anim1.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        dateTv.setText(date);
                        descriptionTv.setText(description);
                    }
                });
                cardView.startAnimation(anim1);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.settings_date_format_key))){
            dateFormatPattern = sharedPreferences.getString(getString(R.string.settings_date_format_key),getString(R.string.settings_date_option_latin));
        }
    }

    private void setupFirebaseListeners(){
        achievementsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Achievement achievement = dataSnapshot.getValue(Achievement.class);
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = new Date();
                if(achievement!=null) {
                    try {
                        date = format.parse(achievement.getTimestamp());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    CalendarDay calendarDay = CalendarDay.from(date);
                    set.add(calendarDay);
                    achievements.add(achievement);

                    calendarView.removeDecorators();
                    calendarView.addDecorator(new CalendarAchievementDecorator(ContextCompat.getColor(getContext(),R.color.blueDark),set));
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                Achievement achievement = dataSnapshot.getValue(Achievement.class);
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = new Date();
                if(achievement!=null) {
                    try {
                        date = format.parse(achievement.getTimestamp());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    CalendarDay calendarDay = CalendarDay.from(date);
                    set.add(calendarDay);
                    achievements.add(achievement);

                    calendarView.removeDecorators();
                    calendarView.addDecorator(new CalendarAchievementDecorator(ContextCompat.getColor(getContext(),R.color.greenPrimary),set));
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        firstTimeOnlineReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null){
                    String firstTimeString = dataSnapshot.getValue(String.class);
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    Date firstTimeDate = null;
                    try {
                        firstTimeDate = format.parse(firstTimeString);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if(firstTimeDate==null){
                        firstTimeDate = new Date();
                    }
                    setupCalendar(firstTimeDate);

                    //Show calendar
                    progressBar.setVisibility(View.INVISIBLE);
                    calendarView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {

                if(set.contains(date)){
                    SimpleDateFormat originalFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    SimpleDateFormat userFormat = new SimpleDateFormat(dateFormatPattern,Locale.getDefault());
                    for(Achievement achievement : achievements){
                        if(achievement.getTimestamp().equals(originalFormat.format(date.getDate()))){
                            String finalDateOutput = "";
                            try{
                                finalDateOutput = userFormat.format(originalFormat.parse(achievement.getTimestamp()));
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            animateCardView(date,achievement.getDescription(),finalDateOutput);
                            currentDay = date;
                        }
                    }

                }

            }
        });
    }

    private void setupCalendar(Date firstTimeDate){
        //Current Date
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        calendarView.setShowOtherDates(MaterialCalendarView.SHOW_OUT_OF_RANGE);
        calendarView.state().edit()
                .setMinimumDate(CalendarDay.from(firstTimeDate))
                .setMaximumDate(CalendarDay.from(calendar))
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();
    }
}
