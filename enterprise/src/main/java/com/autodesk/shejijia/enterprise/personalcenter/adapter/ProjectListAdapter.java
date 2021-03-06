package com.autodesk.shejijia.enterprise.personalcenter.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.autodesk.shejijia.enterprise.R;
import com.autodesk.shejijia.shared.components.common.entity.ProjectInfo;
import com.autodesk.shejijia.shared.components.common.appglobal.ConstructionConstants;
import com.autodesk.shejijia.shared.components.common.utility.ScreenUtil;
import com.autodesk.shejijia.shared.components.nodeprocess.ui.activity.ProjectDetailsActivity;
import com.autodesk.shejijia.shared.components.common.utility.LogUtils;

import java.util.List;

/**
 * Created by t_xuz on 10/13/16.
 * 我页--项目列表adapter
 */
public class ProjectListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ProjectInfo> projectLists;
    private int resId;
    private Context mContext;

    public ProjectListAdapter(List<ProjectInfo> projectLists, int resId, Context mContext) {
        this.resId = resId;
        this.mContext = mContext;
        this.projectLists = projectLists;
    }

    public void setProjectLists(List<ProjectInfo> projectLists) {
        this.projectLists = projectLists;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(resId, parent, false);
        return new ProjectListVH(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        ProjectListVH projectListVH = (ProjectListVH) holder;

        initView(projectListVH, position);

        initEvents(projectListVH, position);

    }

    @Override
    public int getItemCount() {
        return projectLists.size();
    }

    private void initView(ProjectListVH projectVh, int position) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            projectVh.mStarLabel.setZ(ScreenUtil.dip2px(6));
            projectVh.mCardView.setClipToOutline(false);
        }

        if (!TextUtils.isEmpty(projectLists.get(position).getName())) {
            projectVh.mProjectName.setText(projectLists.get(position).getName());
        }

        if (projectLists.get(position).getPlan().getMilestone() != null) {
            if (!TextUtils.isEmpty(projectLists.get(position).getPlan().getMilestone().getMilestoneName())) {
                projectVh.mProjectStatus.setText(projectLists.get(position).getPlan().getMilestone().getMilestoneName());
            }
        }
    }

    private void initEvents(ProjectListVH projectVh, final int position) {
        projectVh.mProjectDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long projectId = projectLists.get(position).getProjectId();
                Intent intent = new Intent(mContext, ProjectDetailsActivity.class);
                intent.putExtra(ConstructionConstants.BUNDLE_KEY_PROJECT_ID, projectId);
                mContext.startActivity(intent);
            }
        });
    }

    private static final class ProjectListVH extends RecyclerView.ViewHolder {
        private CardView mCardView;
        private Button mStarLabel; //星标按钮
        private TextView mProjectName; //项目名 project/name
        private TextView mProjectStatus; //项目状态 plan/status
        private LinearLayout mProjectDetails;

        ProjectListVH(View itemView) {
            super(itemView);
            mCardView = (CardView) itemView.findViewById(R.id.cdv_task_list);
            mProjectName = (TextView) itemView.findViewById(R.id.tv_project_name);
            mProjectStatus = (TextView) itemView.findViewById(R.id.tv_project_status);
            mStarLabel = (Button) itemView.findViewById(R.id.btn_star_label);
            mProjectDetails = (LinearLayout) itemView.findViewById(R.id.lv_project_details);
        }
    }
}
