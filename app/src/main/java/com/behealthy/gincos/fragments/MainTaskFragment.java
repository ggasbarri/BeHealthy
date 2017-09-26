package com.behealthy.gincos.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.behealthy.gincos.R;
import com.behealthy.gincos.utils.MyTextView;
import com.behealthy.gincos.utils.Task;

/**
 * Used inside {@link com.behealthy.gincos.adapters.MainTaskPagerAdapter}, each shows an Active Task in {@link MainFragment}.
 */
public class MainTaskFragment extends Fragment {

    Task task;
    String taskName;
    String taskHint;
    int taskIconResID;
    Context context;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_task_pager,container,false);

        if(task!=null){
            //Assign views
            MyTextView taskNameView = rootView.findViewById(R.id.main_task_name);
            ImageView taskIconView = rootView.findViewById(R.id.main_task_icon);
            MyTextView taskHintView = rootView.findViewById(R.id.main_task_hint);
            CardView taskCardView = rootView.findViewById(R.id.pager_task_card);

            //Populate vies with task info
            taskNameView.setText(taskName);
            taskHintView.setText(taskHint);
            taskHintView.setTextColor(ContextCompat.getColor(getContext(),R.color.greenDark));
            taskIconView.setImageResource(taskIconResID);

            ViewCompat.setTransitionName(taskCardView,getString(R.string.active_task_image_transition));

            taskCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainFragment)getParentFragment()).openTaskDetail();
                }
            });
        }
        return rootView;
    }

    public MainTaskFragment(){}

    public void setTask(Task task){
        this.task=task;
        taskName = context.getString(task.getTitle());
        taskHint = task.getCurrentHint();
        taskIconResID = task.getIconResID();
    }

    public void setContext(Context context){
        this.context = context;
    }
}
