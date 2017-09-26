package com.behealthy.gincos.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.behealthy.gincos.R;
import com.behealthy.gincos.utils.MyTextView;
import com.behealthy.gincos.utils.Task;

/**
 * Shows details and descriptions from each task, accessed from {@link MainFragment}.
 */
public class TaskDetailFragment extends Fragment {

    Task displayedTask;

    public TaskDetailFragment() {
        // Required empty public constructor
    }

    public void setDisplayedTask(Task displayedTask){
        this.displayedTask = displayedTask;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_task_detail, container, false);

        ImageView taskImage = rootView.findViewById(R.id.detail_task_img);
        MyTextView taskNameTv = rootView.findViewById(R.id.detail_task_title);
        MyTextView taskDesc1Tv = rootView.findViewById(R.id.detail_task_desc_1);
        MyTextView taskDesc2Tv = rootView.findViewById(R.id.detail_task_desc_2);
        MyTextView taskCuriousFactTv = rootView.findViewById(R.id.detail_curious_fact);

        if(displayedTask!=null){

            Context c = getContext();

            taskImage.setImageResource(displayedTask.getIconResID());
            taskNameTv.setText(c.getString(displayedTask.getTitle()));
            taskDesc1Tv.setText(c.getString(displayedTask.getDescription()));
            taskDesc2Tv.setText(c.getString(displayedTask.getSecondaryDescription()));
            taskCuriousFactTv.setText(c.getString(displayedTask.getCuriousFact()));

        }

        return rootView;
    }

}
