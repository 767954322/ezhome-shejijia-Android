package com.autodesk.shejijia.shared.components.nodeprocess.presenter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.autodesk.shejijia.shared.components.common.appglobal.ConstructionConstants;
import com.autodesk.shejijia.shared.components.common.entity.ProjectInfo;
import com.autodesk.shejijia.shared.components.common.entity.ProjectList;
import com.autodesk.shejijia.shared.components.common.entity.ResponseError;
import com.autodesk.shejijia.shared.components.common.entity.microbean.Like;
import com.autodesk.shejijia.shared.components.common.entity.microbean.Task;
import com.autodesk.shejijia.shared.components.common.entity.microbean.UnreadMessageIssue;
import com.autodesk.shejijia.shared.components.common.listener.ResponseCallback;
import com.autodesk.shejijia.shared.components.common.utility.LogUtils;
import com.autodesk.shejijia.shared.components.nodeprocess.contract.ProjectListContract;
import com.autodesk.shejijia.shared.components.nodeprocess.data.ProjectRepository;
import com.autodesk.shejijia.shared.components.nodeprocess.ui.activity.ProjectDetailsActivity;
import com.autodesk.shejijia.shared.components.nodeprocess.ui.fragment.TaskDetailsFragment;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by t_xuz on 10/11/16.
 * 主页 项目(任务)列表页对应的presenter的实现类-->对应 TaskListFragment
 */
public class ProjectListPresenter implements ProjectListContract.Presenter, ProjectRepository.ProjectDirtyListener,
        ProjectRepository.TaskDirtyListener {

    private static final int PAGE_LIMIT = 10;
    private Context mContext;
    private ProjectListContract.View mProjectListView;
    private ProjectRepository mProjectRepository;
    private FragmentManager fragmentManager;
    private int mOffset = 0;
    private String mSelectedDate;
    private String mFilterLike; //null or true or false
    private String mFilterStatus;
    private boolean mIsRefresh;
    private int mLoadedSize = 0;//记录服务端返回的数据个数
    private int mTotalSize;//服务器一共多少数据
    private List<ProjectInfo> mProjectInfos;
    private boolean mIsDirty;

    public ProjectListPresenter(Context context, FragmentManager fragmentManager, ProjectListContract.View projectListsView) {
        this.mContext = context;
        this.fragmentManager = fragmentManager;
        this.mProjectListView = projectListsView;
        mProjectRepository = ProjectRepository.getInstance();
    }

    @Override
    public void start() {
        mProjectRepository.addProjectDirtyListener(this);
        mProjectRepository.addTaskDirtyListener(this);
    }

    @Override
    public void stop() {
        mProjectRepository.removeProjectDirtyListener(this);
        mProjectRepository.removeTaskDirtyListener(this);
    }

    @Override
    public void checkDirty() {
        if (mIsDirty) {
//            refreshProjectList();
            mIsDirty = false;
        }
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
    }

    @Override
    public String getScreenPopupState() {
        return mFilterLike;
    }

    @Override
    public void refreshProjectList() {
        mOffset = 0;
        mLoadedSize = 0;
        this.mIsRefresh = true;
        loadProjectList(mOffset);
    }

    @Override
    public void loadMoreProjectList() {
        this.mIsRefresh = false;
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
                LogUtils.d("projectList", taskList + "");
                LogUtils.d("mLoadedSize", mLoadedSize + "");
                LogUtils.d("mOffset", mOffset + "");
                mTotalSize = taskList.getTotal();
                LogUtils.d("mTotalSize", mTotalSize + "");
                if (mIsRefresh) {
                    mProjectListView.refreshProjectListView(taskList.getData());
                    if (taskList.getData() != null && taskList.getData().size() > 0) {
                        mProjectInfos = taskList.getData();
                        mLoadedSize = taskList.getData().size();
                        mOffset = mLoadedSize;
                    } else {
                        mLoadedSize = 0;
                        mProjectInfos = new ArrayList<ProjectInfo>();
                    }
                } else {
                    mProjectListView.addMoreProjectListView(taskList.getData());
                    if (taskList.getData() != null && taskList.getData().size() > 0) {
                        mLoadedSize += taskList.getData().size();
                        mOffset = mLoadedSize;
                        mProjectInfos.addAll(taskList.getData());
                    }
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
        taskDetailsFragment.setTargetFragment((Fragment) mProjectListView, ConstructionConstants.REQUEST_CODE_SHOW_TASK_DETAILS);
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
                mProjectListView.refreshLikesButton(mFilterLike, data, position);
            }

            @Override
            public void onError(ResponseError error) {
                mProjectListView.hideLoading();
//                mProjectListView.showNetError(error);
            }
        });
    }

    @Override
    public void getUnReadMessageIssue() {
        mProjectRepository.getUnReadMessageAndIssue(null, ConstructionConstants.REQUEST_TAG_UNREAD_MESSAGEANDISSUE, new ResponseCallback<UnreadMessageIssue, ResponseError>() {
            @Override
            public void onSuccess(UnreadMessageIssue data) {

            }

            @Override
            public void onError(ResponseError error) {
                mProjectListView.showNetError(error);
            }
        });
    }

    @Override
    public void onProjectDirty(String projectId) {
        mIsDirty = true;
        LogUtils.i("Wenhui", "onProjectDirty = " + projectId);
//        loadUserTasksByProject(projectId);
    }

    private void loadUserTasksByProject(final String pid) {
//        mProjectListView.showLoading();
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
        requestParamsBundle.putInt("offset", (mOffset - 10 < 0 ? 0 : mOffset - 10));
        mProjectRepository.getProjectList(requestParamsBundle, ConstructionConstants.REQUEST_TAG_STAR_PROJECTS, new ResponseCallback<ProjectList, ResponseError>() {
            @Override
            public void onSuccess(ProjectList data) {
//                mProjectListView.showLoading();
                for (ProjectInfo projectInfo: data.getData()) {
                    if (pid.equalsIgnoreCase(String.valueOf(projectInfo.getProjectId()))) {
                        mProjectListView.updateItemData(0, projectInfo);
                    }
                }
            }

            @Override
            public void onError(ResponseError error) {
//                mProjectListView.hideLoading();
                mProjectListView.showError(error.getMessage());
            }
        });


//        mProjectRepository.getUserTasksByProject(pid, requestParamsBundle, "GET_USER_TASKS_BY_PROJECT", new ResponseCallback<ProjectInfo, ResponseError>() {
//            @Override
//            public void onSuccess(ProjectInfo data) {
//                LogUtils.i("Wenhui", "Got data");
//                mProjectListView.hideLoading();
//                for (int index = 0; index < mProjectInfos.size(); index++) {
//                    ProjectInfo projectInfo = mProjectInfos.get(index);
//                    if (projectInfo.getProjectId() == data.getProjectId()) {
//                        mProjectInfos.add(index, data);
//                        mProjectInfos.remove(index + 1);
//                        LogUtils.i("Wenhui", "update data");
//                        mProjectListView.updateItemData(index, projectInfo);
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onError(ResponseError error) {
//                mProjectListView.hideLoading();
//                mProjectListView.showError(error.getMessage());
//            }
//        });

    }

    @Override
    public void onTaskDirty(String projectId, String taskId) {
        LogUtils.i("Wenhui", "onTaskDirty = " + projectId);
        Bundle params = new Bundle();
        params.putString(ConstructionConstants.BUNDLE_KEY_PROJECT_ID, String.valueOf(projectId));
        params.putString(ConstructionConstants.BUNDLE_KEY_TASK_ID, String.valueOf(taskId));
        mProjectRepository.getTask(params, "GET_TASK", new ResponseCallback<Task, ResponseError>() {
            @Override
            public void onSuccess(Task data) {
                for (int index = 0; index < mProjectInfos.size(); index++) {
                    ProjectInfo projectInfo = mProjectInfos.get(index);
                    if (data.getProjectId().equalsIgnoreCase(String.valueOf(projectInfo.getProjectId()))) {
                        LogUtils.i("Wenhui", "update data");
                        List<Task> tasks = projectInfo.getPlan().getTasks();
                        for (int index2 = 0; index2 < tasks.size(); index2++) {
                            Task task = tasks.get(index2);
                            if (task.getTaskId().equalsIgnoreCase(data.getTaskId())) {
                                tasks.remove(task);
                                tasks.add(index2, data);
                                mProjectListView.updateItemData(index, projectInfo);
                                break;
                            }
                        }
                        break;
                    }
                }
            }

            @Override
            public void onError(ResponseError error) {
                mProjectListView.hideLoading();
                mProjectListView.showError(error.getMessage());
            }
        });
    }
}
