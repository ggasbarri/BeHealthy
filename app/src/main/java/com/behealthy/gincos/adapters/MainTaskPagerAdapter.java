package com.behealthy.gincos.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.behealthy.gincos.fragments.MainTaskFragment;
import com.behealthy.gincos.utils.Task;
import java.util.ArrayList;

/**
 * Contained in {@link com.behealthy.gincos.fragments.MainFragment}, used by {@link MainTaskFragment}, populates each fragment to show an active task each.
 */
public class MainTaskPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<Task> currentTasks;
    private Context context;

    public MainTaskPagerAdapter(Context context,FragmentManager fm, ArrayList<Task> currentTasks) {
        super(fm);
        this.currentTasks=currentTasks;
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        Task task = currentTasks.get(position);
        Fragment fragment = new MainTaskFragment();

        //Set context and task for the item
        ((MainTaskFragment)fragment).setContext(context);
        ((MainTaskFragment)fragment).setTask(task);

        return fragment;
    }

    @Override
    public int getCount() {
        return currentTasks.size();
    }

    public Task getTask(int position){
        return currentTasks.get(position);
    }


    public void setContext(Context context) {
        this.context = context;
    }
}
