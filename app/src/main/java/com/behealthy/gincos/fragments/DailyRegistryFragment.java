package com.behealthy.gincos.fragments;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import cdflynn.android.library.checkview.CheckView;
import com.behealthy.gincos.R;
import com.behealthy.gincos.adapters.DailyRecyclerAdapter;
import com.behealthy.gincos.firebase_utils.DatabaseContract;
import com.behealthy.gincos.firebase_utils.DatabaseUtils;
import com.behealthy.gincos.firebase_utils.firebase_db_objects.ActiveTask;
import com.behealthy.gincos.firebase_utils.firebase_db_objects.Last7Days;
import com.behealthy.gincos.firebase_utils.firebase_db_objects.Main;
import com.behealthy.gincos.utils.MyTextView;
import com.behealthy.gincos.utils.Task;
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
import java.util.Date;
import java.util.Locale;

/**
 * Allows users to set today's scores to each of their tasks.
 */
public class DailyRegistryFragment extends Fragment {

    //Views
    MyTextView dailyRegistryButton;
    View buttonLabelView;
    FrameLayout dailyLayout;
    MyTextView dailyDoneTV;
    RecyclerView tasksRV;
    CheckView checkView;

    static int remainingTasks = 0;
    protected static Last7Days last7Days = new Last7Days(0,0,0,0,0,0,0);
    public static ArrayList<Task> currentTasks;
    protected static long XP = 0;
    private static int currentDayScore = 0;
    private static Date currentDate;
    DailyRecyclerAdapter adapter;

    //Firebase variables
    private static DatabaseReference mainDatabaseReference;
    private static DatabaseReference activeTasksReference;
    private static DatabaseReference xpReference;

    //Score buttons constants
    public static final int SCORE_GREEN = 3;
    public static final int SCORE_YELLOW = 1;
    public static final int SCORE_RED = 0;

    public DailyRegistryFragment() {
        // Required empty public constructor
    }

    public static DailyRegistryFragment newInstance() {
        DailyRegistryFragment fragment = new DailyRegistryFragment();
        //Add any arguments if needed
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_daily_registry, container, false);

        //Assign views
        dailyRegistryButton = rootView.findViewById(R.id.daily_registry_button);
        dailyLayout = rootView.findViewById(R.id.daily_button_layout);
        buttonLabelView = rootView.findViewById(R.id.view2);
        tasksRV = rootView.findViewById(R.id.daily_tasks_rv);
        checkView = rootView.findViewById(R.id.daily_check_view);
        dailyDoneTV = rootView.findViewById(R.id.daily_done_tv);

        currentTasks = new ArrayList<>();

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        currentDate = c.getTime();

        ViewCompat.setTransitionName(dailyLayout,getString(R.string.daily_button_transition));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide slide = new Slide();
            slide.setDuration(400);
            slide.setInterpolator(new AccelerateInterpolator());
            setExitTransition(slide);
        }

        //Get user, database, and DB references
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        FirebaseDatabase mFirebaseDatabase = DatabaseUtils.getDatabase();
        mainDatabaseReference = mFirebaseDatabase.getReference().child(user.getUid()).child(DatabaseContract.MAIN);
        activeTasksReference = mFirebaseDatabase.getReference().child(user.getUid()).child(DatabaseContract.ACTIVE_TASKS);
        xpReference = mFirebaseDatabase.getReference().child(user.getUid()).child(DatabaseContract.XP);

        LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        tasksRV.setLayoutManager(lm);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy",Locale.getDefault());
        adapter = new DailyRecyclerAdapter(getActivity(),currentTasks, dateFormat.format(currentDate), this);
        tasksRV.setAdapter(adapter);

        setupData();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateRemainingTasks();
    }

    private void setupData(){
        mainDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    Main mainData = dataSnapshot.getValue(Main.class);
                    if (mainData != null) {
                        last7Days = mainData.getLast7Days();
                        currentDayScore = (int)last7Days.getDay1();
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
                    if(data.getLastRegistryDate()!=null && !data.getLastRegistryDate().equals("") && task != null){
                        task.setLastRegistryDate(data.getLastRegistryDate());
                    }
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
                if(newTask) {
                    currentTasks.add(task);
                }
                updateRemainingTasks();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                String id = dataSnapshot.getKey();
                ActiveTask data = dataSnapshot.getValue(ActiveTask.class);
                if (data != null) {
                    for (Task pseudoTask : currentTasks){
                        String pseudoID = pseudoTask.getId();
                        if(pseudoID.equals(id)){
                            currentTasks.get(currentTasks.indexOf(pseudoTask)).setLastRegistryDate(data.getLastRegistryDate());
                            adapter.setActiveTasks(currentTasks);
                        }
                    }
                }

                adapter.notifyDataSetChanged();

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
                updateRemainingTasks();
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
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void fetchTaskScore(String id, int score, int position){

        Task task = currentTasks.get(position);

        int currentStep = task.getCurrentHintPosition();

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        task.setLastRegistryDate(dateFormat.format(currentDate));

        //Update this task's last registry date
        activeTasksReference.child(id).child(DatabaseContract.LAST_REGISTRY_DATE).setValue(task.getLastRegistryDate());

        //Set task as finished if steps are over, else update step if score is positive
        if(currentStep+1>=task.getHints().length){
            //TODO: Set task as finished
        }else if(score>0) {
            activeTasksReference.child(id).child(DatabaseContract.STEP).setValue(currentStep + 1);
        }
        currentDayScore += score;
        last7Days.setDay1(currentDayScore);

        //Update today's score in the graph
        mainDatabaseReference.child(DatabaseContract.LAST_7_DAYS).setValue(last7Days);

        //Update XP
        XP += task.getXpObtained()[currentStep];
        xpReference.setValue(XP);

    }

    public void updateRemainingTasks(){
        remainingTasks = currentTasks.size();
    }

    public boolean subtractRemainingTasks(){
        remainingTasks -= 1;
        if(remainingTasks==0){
            AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
            anim.setDuration(500);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    AlphaAnimation anim2 = new AlphaAnimation(0.0f,1.0f);
                    anim2.setDuration(300);
                    anim2.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            dailyDoneTV.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    tasksRV.setVisibility(View.GONE);
                    dailyDoneTV.startAnimation(anim2);
                    checkView.check();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            tasksRV.startAnimation(anim);
            return true;
        }
        else{
            return false;
        }
    }
}
