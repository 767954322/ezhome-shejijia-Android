package com.autodesk.shejijia.enterprise.projectlists.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.autodesk.shejijia.enterprise.base.common.utils.LogUtils;
import com.autodesk.shejijia.enterprise.projectlists.entity.TaskListBean;
import com.autodesk.shejijia.enterprise.projectlists.viewholder.TaskListVH;

import java.util.List;

/**
 * Created by t_xuz on 8/23/16.
 *
 */
public class TaskDetailsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<TaskListBean.TaskList.Plan.Task> taskIdLists ;
    private int resId;

    public TaskDetailsListAdapter(List<TaskListBean.TaskList.Plan.Task> taskIdLists, int resId){
        this.taskIdLists = taskIdLists;
        this.resId = resId;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(resId,parent,false);
        return new TaskListVH(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        TaskListVH taskListVH = (TaskListVH)holder;
        LogUtils.e("taskId--name",taskIdLists.get(position).getName());
        if (!TextUtils.isEmpty(taskIdLists.get(position).getName())) {
            taskListVH.mTaskName.setText(taskIdLists.get(position).getName());
        }

        if (!TextUtils.isEmpty(taskIdLists.get(position).getStatus())) {
            String status = taskIdLists.get(position).getStatus();
            if (status.equalsIgnoreCase("open")) {
                taskListVH.mTaskStatus.setText("未开始");
            }else if (status.equalsIgnoreCase("reserving")){
                taskListVH.mTaskStatus.setText("待预约");
            }else if (status.equalsIgnoreCase("inProgress")){
                taskListVH.mTaskStatus.setText("进行中");
            }else if (status.equalsIgnoreCase("delayed")){
                taskListVH.mTaskStatus.setText("已延期");
            }else if (status.equalsIgnoreCase("qualified")){
                taskListVH.mTaskStatus.setText("合格");
            }else if (status.equalsIgnoreCase("unqualified")){
                taskListVH.mTaskStatus.setText("不合格");
            }else if (status.equalsIgnoreCase("resolved")){
                taskListVH.mTaskStatus.setText("验收拒绝");
            }else if (status.equalsIgnoreCase("reinspection")){
                taskListVH.mTaskStatus.setText("强制复验");
            }else if (status.equalsIgnoreCase("rectification")){
                taskListVH.mTaskStatus.setText("监督整改");
            }else if (status.equalsIgnoreCase("reinspecting")){
                taskListVH.mTaskStatus.setText("复验中");
            }else {
                taskListVH.mTaskStatus.setText(status);
            }
        }
    }

    @Override
    public int getItemCount() {
        return taskIdLists.size();
    }

}
