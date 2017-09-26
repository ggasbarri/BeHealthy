package com.behealthy.gincos.fragments;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import com.behealthy.gincos.R;
import com.behealthy.gincos.activities.MainActivity;
import com.behealthy.gincos.adapters.MainTaskPagerAdapter;
import com.behealthy.gincos.firebase_utils.DatabaseContract;
import com.behealthy.gincos.firebase_utils.firebase_db_objects.ActiveTask;
import com.behealthy.gincos.firebase_utils.firebase_db_objects.Last7Days;
import com.behealthy.gincos.firebase_utils.firebase_db_objects.Main;
import com.behealthy.gincos.utils.AxisFormatter;
import com.behealthy.gincos.utils.DailyButtonTransition;
import com.behealthy.gincos.utils.MyTextView;
import com.behealthy.gincos.utils.Task;
import com.behealthy.gincos.utils.Tasks.WaterFighter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Home Fragment, shows currently active tasks, a graph with user's recent progression and connects to {@link DailyRegistryFragment}
 */
public class MainFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static ArrayList<Task> currentTasks;
    String dateFormatPattern;

    //Firebase variables
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mainDatabaseReference;
    private DatabaseReference activeTasksReference;
    private DatabaseReference xpReference;
    private DatabaseReference achievementReference;
    private DatabaseReference masterReference;
    private DatabaseReference firstTimeOnlineReference;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    protected long XP;
    protected Last7Days last7Days;
    protected String lastTimeOnline;
    public String firstTime;

    private MainTaskPagerAdapter adapter;
    private Date currentDate;

    Context context;

    //Views
    MyTextView levelNumberTV;
    ProgressBar levelProgressBar;
    LineChart graph;
    ViewPager mTaskViewPager;
    TabLayout tabLayout;
    FrameLayout dailyRegistryButton;
    View buttonLabelView;
    FrameLayout dailyLayout;
    ProgressBar progressBar;
    RelativeLayout viewPagerLayout;


    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main,container,false);

        //Get preferences values
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        dateFormatPattern = prefs.getString(getString(R.string.settings_date_format_key),getString(R.string.settings_date_option_latin));

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        currentDate = c.getTime();

        currentTasks = new ArrayList<>();

        //Get database from parent Activity
        mFirebaseDatabase = ((MainActivity)getActivity()).mFirebaseDatabase;

        //Setup Auth user and listener
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser newUser = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    user = newUser;
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        //Assign views
        graph = rootView.findViewById(R.id.main_progress_graph);
        mTaskViewPager = rootView.findViewById(R.id.main_pager);
        tabLayout = rootView.findViewById(R.id.main_tabDots);
        levelNumberTV = rootView.findViewById(R.id.main_level_number);
        levelProgressBar = rootView.findViewById(R.id.main_level_progress);
        dailyRegistryButton = rootView.findViewById(R.id.daily_registry_button);
        dailyLayout = rootView.findViewById(R.id.daily_button_layout);
        buttonLabelView = rootView.findViewById(R.id.view2);
        progressBar = rootView.findViewById(R.id.main_progress_bar);
        viewPagerLayout = rootView.findViewById(R.id.main_pager_layout);

        //Set graph empty info
        graph.setNoDataText(context.getString(R.string.main_connecting));
        graph.setNoDataTextColor(ContextCompat.getColor(context,R.color.greenDark));

        //Set Compat Elevations
        ViewCompat.setElevation(dailyLayout,40);
        ViewCompat.setElevation(dailyRegistryButton,40);
        ViewCompat.setElevation(buttonLabelView, 40);

        //Compat transition name for Shared Element Transition Animation
        ViewCompat.setTransitionName(dailyLayout,getString(R.string.daily_button_transition));

        dailyRegistryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Fragment dailyFragment = DailyRegistryFragment.newInstance();

                mTaskViewPager.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);

                //Shared element fragment transition
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Fade fade1 = new Fade();
                    fade1.setDuration(100);
                    Fade fade2 = new Fade();
                    fade2.setDuration(500);
                    Slide slide = new Slide();
                    slide.setDuration(400);
                    slide.setInterpolator(new DecelerateInterpolator());
                    dailyFragment.setSharedElementEnterTransition(new DailyButtonTransition(DailyButtonTransition.MODE_ENTER));
                    dailyFragment.setEnterTransition(slide);
                    setExitTransition(fade1);
                    setReenterTransition(fade2);
                    dailyFragment.setSharedElementReturnTransition(new DailyButtonTransition(DailyButtonTransition.MODE_EXIT));
                }

                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .addSharedElement(dailyLayout,getString(R.string.daily_button_transition))
                        .replace(R.id.content_frame, dailyFragment)
                        .addToBackStack(null)
                        .commit();

            }
        });

        if (user != null){
            setupData();
        }else{
            Log.e("MainFragment","No user");
        }

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;

    }

    @Override
    public void onResume() {
        super.onResume();

        MainActivity.setDefaultActivityTitle((AppCompatActivity)getActivity());

        //If the user had pressed "Logout", translate selected drawer item now to "Home"
        ((MainActivity)getActivity()).navView.getMenu().getItem(3).setChecked(false);
        ((MainActivity)getActivity()).navView.setCheckedItem(R.id.drawer_home);
        ((MainActivity)getActivity()).navView.getMenu().getItem(0).setChecked(true);

        //Checked last time user had logged in
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy",Locale.getDefault());
        String currentDateString = dateFormat.format(currentDate);
        long daysBetween = calculateDaysBetween(lastTimeOnline, currentDateString);
        if(daysBetween!=0 && daysBetween!=-1) {
            updateLast7Days();
        }

        mAuth.addAuthStateListener(mAuthListener);
        
        MainActivity.currentFragment = MainActivity.FRAGMENT_MAIN;
        
        setContext(getContext());
    }

    private void setupData(){
        //Assign database references
        masterReference = mFirebaseDatabase.getReference().child(user.getUid());
        mainDatabaseReference = masterReference.child(DatabaseContract.MAIN);
        activeTasksReference = masterReference.child(DatabaseContract.ACTIVE_TASKS);
        xpReference = masterReference.child(DatabaseContract.XP);
        achievementReference = masterReference.child(DatabaseContract.ACHIEVEMENTS);
        firstTimeOnlineReference = masterReference.child(DatabaseContract.FIRST_TIME_ONLINE);

        firstTimeOnlineReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                firstTime = dataSnapshot.getValue(String.class);
                if(firstTime==null || firstTime.equals("")){
                    // First Login ever
                    setInitialDbData();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Toast.makeText(getContext(),getString(R.string.network_error),Toast.LENGTH_LONG).show();
            }
        });

        mainDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null){
                    // First Login ever
                }else {
                    Log.d("MainFragment","Reading data");
                    Main mainData = dataSnapshot.getValue(Main.class);
                    if (mainData != null) {
                        last7Days = mainData.getLast7Days();
                        String newLastTimeOnline = mainData.getLastTimeOnline();
                        if(newLastTimeOnline!=null && !newLastTimeOnline.equals(lastTimeOnline)) {
                            lastTimeOnline = newLastTimeOnline;
                            updateLast7Days();
                        }
                    }
                    if(last7Days!=null) {
                        updateGraph();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        activeTasksReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String id = dataSnapshot.getKey();
                ActiveTask data = dataSnapshot.getValue(ActiveTask.class);
                Task task = null;
                if (data != null) {
                    task = Task.getTaskById(id,data.getStep());
                }
                boolean newTask = false;
                for(Task mTask : currentTasks){
                    if (task != null && !mTask.getId().equals(task.getId())) {
                        newTask = true;
                        break;
                    }
                }
                if (currentTasks.size()==0){
                    newTask = true;
                }
                //If it is a new task, add it to the Task Pager Adapter
                if(newTask) {
                    currentTasks.add(task);
                    if(adapter!=null) {
                        adapter.notifyDataSetChanged();
                    }
                    //TODO: Alert New Task on the rest of cases
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String id = dataSnapshot.getKey();
                ActiveTask data = dataSnapshot.getValue(ActiveTask.class);
                Task task = null;
                if (data != null) {
                    task = Task.getTaskById(id,data.getStep());
                }
                currentTasks.remove(task);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        xpReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null) {
                    XP = (long) dataSnapshot.getValue();
                    populateViews();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void populateViews(){

        progressBar.setVisibility(View.GONE);
        mTaskViewPager.setVisibility(View.VISIBLE);

        tabLayout.setupWithViewPager(mTaskViewPager, true);

        levelNumberTV.setText(String.valueOf(calculateLevel(XP)));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            levelProgressBar.setProgress(((int)XP%1000)/10,true);
        }else {
            levelProgressBar.setProgress(((int)XP%1000)/10);
        }

        adapter = new MainTaskPagerAdapter(context,getChildFragmentManager(),currentTasks);
        mTaskViewPager.setAdapter(adapter);

        if(last7Days!=null) {
            updateGraph();
        }

    }

    public void updateGraph(){

        List<Entry> entries = new ArrayList<>();
        Last7Days newDays = AxisFormatter.reverseLast7Days(last7Days);
        int i = 0;
        for(long dataEntry : get7Days(newDays)){
            entries.add(new Entry((float)i,(float)dataEntry));
            i++;
        }
        LineDataSet dataSet = new LineDataSet(entries,context.getString(R.string.main_scores));
        dataSet.setColor(ContextCompat.getColor(context,R.color.greenDark));
        dataSet.setValueTextSize(14f);
        dataSet.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return String.valueOf((int) value);
            }
        });
        dataSet.setCircleColor(ContextCompat.getColor(context,R.color.greenPrimary));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(10f);
        dataSet.setDrawValues(false);
        dataSet.setHighlightEnabled(true);
        LineData lineData = new LineData(dataSet);
        graph.setData(lineData);
        Description description = new Description();
        description.setEnabled(false);
        description.setText("");
        graph.setDescription(description);
        graph.setNoDataText(context.getString(R.string.main_connecting));
        graph.setNoDataTextColor(ContextCompat.getColor(context,R.color.greenDark));
        graph.setDrawGridBackground(false);
        graph.setDrawBorders(false);
        YAxis leftAxis = graph.getAxisLeft();
        leftAxis.setEnabled(false);
        leftAxis.setAxisMinimum(0);
        YAxis rightAxis = graph.getAxisRight();
        rightAxis.setEnabled(false);
        XAxis xAxis = graph.getXAxis();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        ArrayList<Date> dateArrayList = new ArrayList<>();
        for(int count = 0;count<7;count++){
            Date date = c.getTime();
            dateArrayList.add(date);
            c.setTimeInMillis(c.getTimeInMillis() - ((long) 1000 * 60 * 60 * 24 ));
        }
        Collections.reverse(dateArrayList);
        xAxis.setValueFormatter(new AxisFormatter(context,dateArrayList,dateFormatPattern));
        xAxis.setTextSize(10f);
        graph.setTouchEnabled(false);
        graph.setDragEnabled(false);
        graph.setScaleEnabled(false);
        graph.setScaleXEnabled(false);
        graph.setScaleYEnabled(false);
        graph.setPinchZoom(false);
        graph.setDoubleTapToZoomEnabled(false);
        Legend legend = graph.getLegend();
        legend.setEnabled(false);
        graph.invalidate();

    }

    public static int calculateLevel(long xp){
        if(xp < 1000){
            return 0;
        }else{
            return ((int)Math.floor(((double)xp)/(double)1000));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    public ArrayList<Long> get7Days(Last7Days days){
        ArrayList<Long> list = new ArrayList<>();
        list.add(days.getDay1());
        list.add(days.getDay2());
        list.add(days.getDay3());
        list.add(days.getDay4());
        list.add(days.getDay5());
        list.add(days.getDay6());
        list.add(days.getDay7());
        return list;
    }

    public void updateLast7Days(){

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy",Locale.getDefault());
        String currentDateString = dateFormat.format(currentDate);
        long daysBetween = calculateDaysBetween(lastTimeOnline, currentDateString);
        boolean isToday = false;
        if(daysBetween!=-1){
            Last7Days newLast7Days = last7Days;
            switch ((int)daysBetween){
                case 6:
                    newLast7Days = new Last7Days(0,0,0,0,0,0,last7Days.getDay1());
                    break;
                case 5:
                    newLast7Days = new Last7Days(0,0,0,0,0,last7Days.getDay1(),last7Days.getDay2());
                    break;
                case 4:
                    newLast7Days = new Last7Days(0,0,0,0,last7Days.getDay1(),last7Days.getDay2(),last7Days.getDay3());
                    break;
                case 3:
                    newLast7Days = new Last7Days(0,0,0,last7Days.getDay1(),last7Days.getDay2(),last7Days.getDay3(),last7Days.getDay4());
                    break;
                case 2:
                    newLast7Days = new Last7Days(0,0,last7Days.getDay1(),last7Days.getDay2(),last7Days.getDay3(),last7Days.getDay4(),last7Days.getDay5());
                    break;
                case 1:
                    newLast7Days = new Last7Days(0,last7Days.getDay1(),last7Days.getDay2(),last7Days.getDay3(),last7Days.getDay4(),last7Days.getDay5(),last7Days.getDay6());
                    break;
                case 0:
                    isToday = true;
                    break;
                default:
                    newLast7Days = new Last7Days(0,0,0,0,0,0,0);
                    break;
            }
            //If it is not the same day, update DB values
            if (!isToday) {
                mainDatabaseReference.child(DatabaseContract.LAST_7_DAYS).setValue(newLast7Days);
                mainDatabaseReference.child(DatabaseContract.LAST_TIME_ONLINE).setValue(currentDateString);
            }

        }else{
            Log.e("MainFragment","Stored date was not ok");
        }

    }

    private long calculateDaysBetween(String firstDay, String secondDay){

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy",Locale.getDefault());
        if(firstDay == null || firstDay.equals("")){
            return 0;
        }
        try {
            Date date1 = dateFormat.parse(firstDay);
            Date date2 = dateFormat.parse(secondDay);
            return TimeUnit.DAYS.convert(date2.getTime()-date1.getTime(),TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return -1;
        }
    }

    private void setInitialDbData(){

        Log.d("MainFragment","Adding data");
        mainDatabaseReference.child(DatabaseContract.LAST_7_DAYS).setValue(new Last7Days(0,0,0,0,0,0,0));
        xpReference.setValue(0);
        DatabaseReference waterTaskReference = activeTasksReference.child(WaterFighter.ID);
        waterTaskReference.child(DatabaseContract.STEP).setValue(0);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(currentDate);

        mainDatabaseReference.child(DatabaseContract.LAST_TIME_ONLINE).setValue(formattedDate);
        waterTaskReference.child(DatabaseContract.START_DATE).setValue(dateFormat.format(currentDate));
        waterTaskReference.child(DatabaseContract.LAST_REGISTRY_DATE).setValue("");

        DatabaseReference newAchievementReference = achievementReference.push();
        newAchievementReference.child(DatabaseContract.DESCRIPTION).setValue(getString(R.string.achievement_started));
        newAchievementReference.child(DatabaseContract.TIMESTAMP).setValue(formattedDate);

        firstTimeOnlineReference.setValue(formattedDate);

        alertNewTask(Task.getTaskById(WaterFighter.ID,0));

    }

    public void alertNewTask(Task task){

        //Create dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.new_task_dialog_layout, null);
        dialogBuilder.setView(dialogView);

        //Populate dialog
        MyTextView taskNameTv = dialogView.findViewById(R.id.dialog_task_name);
        taskNameTv.setText(context.getString(task.getTitle()));
        ImageView taskImage = dialogView.findViewById(R.id.dialog_task_image);
        taskImage.setImageResource(task.getIconResID());

        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.show();

        FrameLayout dialogDismiss = dialogView.findViewById(R.id.dialog_dismiss);
        dialogDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

    }

    public void openTaskDetail(){
        Task clickedTask = adapter.getTask(mTaskViewPager.getCurrentItem());

        Fragment taskDetailFragment = new TaskDetailFragment();
        ((TaskDetailFragment)taskDetailFragment).setDisplayedTask(clickedTask);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            Explode explode = new Explode();
            explode.setDuration(300);
            Fade fade = new Fade();
            fade.setDuration(400);
            taskDetailFragment.setEnterTransition(fade);
            setExitTransition(explode);
            setReenterTransition(explode);
        }

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, taskDetailFragment)
                .addToBackStack(null)
                .commit();

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.settings_date_format_key))){
            dateFormatPattern = sharedPreferences.getString(getString(R.string.settings_date_format_key),getString(R.string.settings_date_option_latin));
        }
    }
}
