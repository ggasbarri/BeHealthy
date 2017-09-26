package com.behealthy.gincos.adapters;

import android.animation.AnimatorInflater;
import android.content.Context;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import cdflynn.android.library.checkview.CheckView;
import com.behealthy.gincos.R;
import com.behealthy.gincos.fragments.DailyRegistryFragment;
import com.behealthy.gincos.utils.MyTextView;
import com.behealthy.gincos.utils.Task;
import java.util.ArrayList;

/**
 * Used by {@link DailyRegistryFragment}, populates the RV so the users can score their tasks from this day.
 */
public class DailyRecyclerAdapter extends RecyclerView.Adapter<DailyRecyclerAdapter.DailyViewHolder>{

    private ArrayList<Task> activeTasks;
    private Context context;
    private String currentDate;
    private DailyRegistryFragment fragment;

    public DailyRecyclerAdapter(Context context, ArrayList<Task> activeTasks, String currentDate, DailyRegistryFragment fragment){
        this.currentDate = currentDate;
        this.activeTasks = activeTasks;
        this.context = context;
        this.fragment = fragment;
    }

    @Override
    public DailyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.daily_reg_task_item,parent,false);
        return new DailyViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(final DailyViewHolder holder, int position) {

        final int itemPosition = position;
        Task task = activeTasks.get(position);
        final String id = task.getId();
        holder.setID(id);
        holder.taskTV.setText(task.getTitle());
        holder.taskIcon.setImageResource(task.getIconResID());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.greenButton.setStateListAnimator(AnimatorInflater.loadStateListAnimator(context,R.animator.button_raise));
            holder.yellowButton.setStateListAnimator(AnimatorInflater.loadStateListAnimator(context,R.animator.button_raise));
            holder.redButton.setStateListAnimator(AnimatorInflater.loadStateListAnimator(context,R.animator.button_raise));
        }
        View.OnClickListener completeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isLastItem = fragment.subtractRemainingTasks();
                holder.qualifyDay(v.getId(),id,itemPosition, isLastItem);
            }
        };

        if(task.getLastRegistryDate() == null || task.getLastRegistryDate().equals(currentDate)){
            boolean isLastItem = fragment.subtractRemainingTasks();
            if(!isLastItem){
                holder.fadeItem();
            }
        }else{
            holder.taskLayout.setVisibility(View.VISIBLE);
            holder.greenButton.setOnClickListener(completeClickListener);
            holder.yellowButton.setOnClickListener(completeClickListener);
            holder.redButton.setOnClickListener(completeClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return activeTasks.size();
    }

    public void setActiveTasks(ArrayList<Task> activeTasks){
        this.activeTasks = activeTasks;
    }

    class DailyViewHolder extends RecyclerView.ViewHolder{

        //Views
        MyTextView taskTV;
        ImageView taskIcon;
        CardView greenButton;
        CardView yellowButton;
        CardView redButton;
        ConstraintLayout taskLayout;
        CheckView checkView;

        String id;

        DailyViewHolder(View itemView) {
            super(itemView);

            //Assign views
            taskTV = itemView.findViewById(R.id.daily_task_tv);
            taskIcon = itemView.findViewById(R.id.daily_task_iv);
            greenButton = itemView.findViewById(R.id.green_button);
            yellowButton = itemView.findViewById(R.id.yellow_button);
            redButton = itemView.findViewById(R.id.red_button);
            taskLayout = itemView.findViewById(R.id.daily_task_item_layout);
            checkView = itemView.findViewById(R.id.daily_task_item_check);
            taskLayout.setVisibility(View.INVISIBLE);
        }

        void fadeItem(){
            AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
            anim.setDuration(400);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    taskLayout.setVisibility(View.GONE);
                    checkView.check();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            taskLayout.startAnimation(anim);
        }

        void qualifyDay(int buttonId, String id, int position, boolean isLastItem){
            switch (buttonId){
                case R.id.green_button:
                    DailyRegistryFragment.fetchTaskScore(id, DailyRegistryFragment.SCORE_GREEN, position);
                    break;
                case R.id.yellow_button:
                    DailyRegistryFragment.fetchTaskScore(id, DailyRegistryFragment.SCORE_YELLOW, position);
                    break;
                case R.id.red_button:
                    DailyRegistryFragment.fetchTaskScore(id, DailyRegistryFragment.SCORE_RED, position);
                    break;
                default:
                    break;
            }
            //If there are other tasks remaining, fade just this item
            if (!isLastItem){
                fadeItem();
            }
        }

        void setID(String id){
            this.id = id;
        }

    }
}