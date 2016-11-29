package com.autodesk.shejijia.shared.components.nodeprocess.presenter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import com.autodesk.shejijia.shared.components.common.appglobal.ConstructionConstants;
import com.autodesk.shejijia.shared.components.common.entity.ProjectInfo;
import com.autodesk.shejijia.shared.components.common.entity.ProjectList;
import com.autodesk.shejijia.shared.components.common.entity.ResponseError;
import com.autodesk.shejijia.shared.components.common.entity.microbean.Like;
import com.autodesk.shejijia.shared.components.common.entity.microbean.Task;
import com.autodesk.shejijia.shared.components.common.listener.ResponseCallback;
import com.autodesk.shejijia.shared.components.common.utility.LogUtils;
import com.autodesk.shejijia.shared.components.common.utility.LoginUtils;
import com.autodesk.shejijia.shared.components.nodeprocess.contract.ProjectListContract;
import com.autodesk.shejijia.shared.components.nodeprocess.data.ProjectRepository;
import com.autodesk.shejijia.shared.components.nodeprocess.ui.activity.ProjectDetailsActivity;
import com.autodesk.shejijia.shared.components.nodeprocess.ui.fragment.TaskDetailsFragment;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by t_xuz on 10/11/16.
 * 主页 项目(任务)列表页对应的presenter的实现类-->对应 TaskListFragment
 */
public class ProjectListPresenter implements ProjectListContract.Presenter {

    private static final int PAGE_LIMIT = 10;
    private Context mContext;
    private ProjectListContract.View mProjectListView;
    private ProjectRepository mProjectRepository;
    private FragmentManager fragmentManager;
    private int mOffset = 1;//why 0 and 1 is the same data
    private String mSelectedDate;
    private String mFilterLike; //null or true or false
    private String mFilterStatus;

    public ProjectListPresenter(Context context, FragmentManager fragmentManager, ProjectListContract.View projectListsView) {
        this.mContext = context;
        this.fragmentManager = fragmentManager;
        this.mProjectListView = projectListsView;
        mProjectRepository = ProjectRepository.getInstance();
    }

    @Override
    public void initFilterRequestParams(@Nullable String date, @Nullable String filterLike, @Nullable String filterStatus) {
        this.mSelectedDate = date;
        this.mFilterLike = filterLike;
        this.mFilterStatus = filterStatus;
    }

    @Override
    public void onFilterDateChange(String newDate) {
        this.mSelectedDate = newDate;
        refreshProjectList();
    }

    @Override
    public void onFilterStatusChange(String newStatus) {
        this.mFilterStatus = newStatus;
        refreshProjectList();
    }

    @Override
    public void onFilterLikeChange(String newLike) {
        this.mFilterLike = newLike;
//        refreshProjectList();
    }

    @Override
    public String getScreenPopupState() {
        return mFilterLike;
    }

    @Override
    public void refreshProjectList() {
        mOffset = 1;
        loadProjectList(mOffset);
    }

    @Override
    public void loadMoreProjectList() {
        // TODO: 11/24/16 offset得有个界限 
        mOffset++;
        loadProjectList(mOffset);
    }

    private void loadProjectList(int offset) {
        Bundle requestParamsBundle = new Bundle();
        if (mSelectedDate != null) {
            requestParamsBundle.putString("findDate", mSelectedDate);
        }
        if (mFilterStatus != null) {
            requestParamsBundle.putString("status", mFilterStatus);
        }
        if (mFilterLike != null) {
            requestParamsBundle.putString("like", mFilterLike);
        }
        requestParamsBundle.putInt("limit", PAGE_LIMIT);
        requestParamsBundle.putInt("offset", offset);
        loadProjectListData(requestParamsBundle);
    }

    private void loadProjectListData(Bundle requestParams) {

        mProjectRepository.getProjectList(requestParams, ConstructionConstants.REQUEST_TAG_LOAD_PROJECTS, new ResponseCallback<ProjectList, ResponseError>() {
            @Override
            public void onSuccess(ProjectList taskList) {
                mProjectListView.hideLoading();
                if (taskList.getOffset() == 1) {
                    mProjectListView.refreshProjectListView(taskList.getData());
                } else {
                    mProjectListView.addMoreProjectListView(taskList.getData());
                }
            }

            @Override
            public void onError(ResponseError error) {
                mProjectListView.hideLoading();
                mProjectListView.showNetError(error);
            }
        });
    }


    @Override
    public void navigateToProjectDetail(List<ProjectInfo> projectList, int position) {
        long projectId = projectList.get(position).getProjectId();
        String projectName = projectList.get(position).getName();
        Intent intent = new Intent(mContext, ProjectDetailsActivity.class);
        intent.putExtra(ConstructionConstants.BUNDLE_KEY_PROJECT_ID, projectId);
        intent.putExtra(ConstructionConstants.BUNDLE_KEY_PROJECT_NAME, projectName);
        mContext.startActivity(intent);
    }

    @Override
    public void navigateToTaskDetail(ProjectInfo projectInfo, Task task) {
        TaskDetailsFragment taskDetailsFragment = TaskDetailsFragment.newInstance(projectInfo, task);
        taskDetailsFragment.show(fragmentManager, "task_details");
    }

    @Override
    public void updateProjectLikesState(List<ProjectInfo> projectList, final boolean like, int position) {
        //init requestParams
        Bundle requestParamsBundle = new Bundle();
        requestParamsBundle.putLong("pid", projectList.get(position).getProjectId());
        LogUtils.e("projectId", projectList.get(position).getProjectId() + "");
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("like", like);
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateProjectLikes(requestParamsBundle, requestJson, position);
    }

    private void updateProjectLikes(Bundle requestParamsBundle, JSONObject requestJson, final int position) {
        mProjectRepository.updateProjectLikes(requestParamsBundle, ConstructionConstants.REQUEST_TAG_STAR_PROJECTS, requestJson, new ResponseCallback<Like, ResponseError>() {
            @Override
            public void onSuccess(Like data) {
                LogUtils.e("like", data.getLike() + "---" + data.getUid());
                mProjectListView.hideLoading();
                mProjectListView.refreshLikesButton(data, position);
            }

            @Override
            public void onError(ResponseError error) {
                mProjectListView.hideLoading();
                mProjectListView.showNetError(error);
            }
        });
    }


}
