package com.autodesk.shejijia.shared.components.nodeprocess.ui.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.autodesk.shejijia.shared.R;
import com.autodesk.shejijia.shared.components.common.appglobal.ConstructionConstants;
import com.autodesk.shejijia.shared.components.common.entity.ProjectInfo;
import com.autodesk.shejijia.shared.components.common.entity.microbean.Member;
import com.autodesk.shejijia.shared.components.common.entity.microbean.Task;
import com.autodesk.shejijia.shared.components.common.entity.microbean.Time;
import com.autodesk.shejijia.shared.components.common.uielements.CircleImageView;
import com.autodesk.shejijia.shared.components.common.utility.ImageUtils;
import com.autodesk.shejijia.shared.components.common.utility.LogUtils;
import com.autodesk.shejijia.shared.components.common.utility.ScreenUtil;
import com.autodesk.shejijia.shared.components.common.utility.UserInfoUtils;
import com.autodesk.shejijia.shared.components.nodeprocess.utility.TaskHeadPicHelper;
import com.autodesk.shejijia.shared.components.nodeprocess.utility.TaskUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by t_xuz on 8/23/16.
 * 任务列表 － adapter
 */
public class TaskListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ProjectInfo mProject;
    private List<Task> taskLists;
    private int resId;
    private Context mContext;
    private ProjectListAdapter.ProjectListItemListener mTaskListItemListener;

    public TaskListAdapter(ProjectInfo projectInfo, int resId, Context mContext, ProjectListAdapter.ProjectListItemListener mTaskListItemListener) {
        this.mProject = projectInfo;
        this.taskLists = projectInfo.getPlan().getTasks();
        this.resId = resId;
        this.mContext = mContext;
        this.mTaskListItemListener = mTaskListItemListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(resId, parent, false);
        return new TaskListVH(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        TaskListVH taskListVH = (TaskListVH) holder;

        initView(taskListVH, position);

        initEvents(taskListVH, position);
    }

    @Override
    public int getItemCount() {
        return taskLists.size();
    }

    private void initView(TaskListVH taskListVH, int position) {
        // 当前任务节点名
        if (!TextUtils.isEmpty(taskLists.get(position).getName())) {
            taskListVH.mTaskName.setText(taskLists.get(position).getName());
        }

        // 当前任务节点的状态
        String status = taskLists.get(position).getStatus();
        if (!TextUtils.isEmpty(status)) {
            taskListVH.mTaskStatus.setText(TaskUtils.getDisplayStatus(status));
            taskListVH.mTaskStatus.setTextColor(TaskUtils.getStatusTextColor(mContext, status));
            taskListVH.mTaskStatus.getBackground().setLevel(TaskUtils.getStatusLevel(status));
        }

        // 当前任务节点头像
        String taskHeadStatus = TaskHeadPicHelper.getInstance().getActions(taskLists.get(position));
        switch (taskHeadStatus) {
            case TaskHeadPicHelper.SHOW_HEAD:
                taskListVH.mTaskDetails.setPadding(ScreenUtil.dip2px(12), ScreenUtil.dip2px(12), ScreenUtil.dip2px(12), ScreenUtil.dip2px(16));
                String avatarUrl = TaskUtils.getAvatarUrl(mContext, mProject.getMembers());
                if (!TextUtils.isEmpty(avatarUrl)) {
                    taskListVH.mTaskIcon.setVisibility(View.VISIBLE);
                    ImageUtils.loadUserAvatar(taskListVH.mTaskIcon, avatarUrl);
                }
                break;
            case TaskHeadPicHelper.SHOW_DEFAULT:
                taskListVH.mTaskIcon.setVisibility(View.GONE);
                taskListVH.mTaskDetails.setPadding(ScreenUtil.dip2px(16), ScreenUtil.dip2px(12), ScreenUtil.dip2px(12), ScreenUtil.dip2px(16));
                String category = taskLists.get(position).getCategory();
                if (!TextUtils.isEmpty(category)) {
                    Drawable drawable = null;
                    switch (category) {
                        case ConstructionConstants.TaskCategory.TIME_LINE:
                            drawable = ContextCompat.getDrawable(mContext, R.drawable.default_head);
                            break;
                        case ConstructionConstants.TaskCategory.INSPECTOR_INSPECTION://监理验收
                        case ConstructionConstants.TaskCategory.CLIENT_MANAGER_INSPECTION://客户经理验收
                            drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_task_checkaccept);
                            break;
                        case ConstructionConstants.TaskCategory.CONSTRUCTION:
                            drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_task_construction);
                            break;
                        case ConstructionConstants.TaskCategory.MATERIAL_MEASURING://主材测量
                        case ConstructionConstants.TaskCategory.MATERIAL_INSTALLATION://主材安装
                            drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_task_material);
                            break;
                        default:
                            break;

                    }
                    if (drawable != null) {
                        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                        taskListVH.mTaskName.setCompoundDrawables(drawable, null, null, null);
                    }
                }
                break;
        }
    }

    private void initEvents(TaskListVH taskListVH, final int position) {
        taskListVH.mTaskDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTaskListItemListener.onTaskClick(mProject, taskLists.get(position));
            }
        });
    }

    private static class TaskListVH extends RecyclerView.ViewHolder {
        private CircleImageView mTaskIcon;
        private TextView mTaskName;
        private TextView mTaskStatus;
        private LinearLayout mTaskDetails;

        TaskListVH(View itemView) {
            super(itemView);

            mTaskIcon = (CircleImageView) itemView.findViewById(R.id.img_task_icon);
            mTaskName = (TextView) itemView.findViewById(R.id.tv_task_name);
            mTaskStatus = (TextView) itemView.findViewById(R.id.tv_task_status);
            mTaskDetails = (LinearLayout) itemView.findViewById(R.id.ll_task_details);
        }
    }
}
