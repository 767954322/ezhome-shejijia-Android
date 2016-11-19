package com.autodesk.shejijia.shared.components.nodeprocess.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.autodesk.shejijia.shared.R;
import com.autodesk.shejijia.shared.components.common.appglobal.ConstructionConstants;
import com.autodesk.shejijia.shared.components.common.entity.microbean.Task;
import com.autodesk.shejijia.shared.components.common.uielements.ConProgressDialog;
import com.autodesk.shejijia.shared.components.common.uielements.calanderview.CalendarDay;
import com.autodesk.shejijia.shared.components.common.uielements.calanderview.MaterialCalendarView;
import com.autodesk.shejijia.shared.components.common.utility.DateUtil;
import com.autodesk.shejijia.shared.components.common.utility.ToastUtils;
import com.autodesk.shejijia.shared.components.nodeprocess.contract.EditPlanContract;
import com.autodesk.shejijia.shared.components.nodeprocess.contract.EditPlanContract.TaskNodePresenter;
import com.autodesk.shejijia.shared.components.nodeprocess.presenter.EditTaskNodePresenter;
import com.autodesk.shejijia.shared.components.nodeprocess.ui.adapter.EditTaskNodeAdapter;
import com.autodesk.shejijia.shared.components.nodeprocess.ui.widgets.AddTaskDialogFragment;
import com.autodesk.shejijia.shared.components.nodeprocess.ui.widgets.calendar.ActiveMileStoneDecorator;
import com.autodesk.shejijia.shared.components.nodeprocess.ui.widgets.calendar.DateSelectorDecorator;
import com.autodesk.shejijia.shared.components.nodeprocess.ui.widgets.calendar.MileStoneDayFormatter;
import com.autodesk.shejijia.shared.components.nodeprocess.ui.widgets.calendar.MileStoneNodeDecorator;
import com.autodesk.shejijia.shared.framework.fragment.BaseFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;



/**
 * @author wenhulin
 * @since 11/3/16
 */

public class EditTaskNodeFragment extends BaseFragment implements EditPlanContract.TaskNodeView {
    private final static String FRAGMENT_TAG_ADD_TASK = "add_task";
    private final static int REQUEST_CODE_ADD_TASK = 99;

    private TaskNodePresenter mPresenter;

    private RecyclerView mRecyclerView;
    private EditTaskNodeAdapter mAdapter;

    private BottomSheetDialog mBottomSheetDialog;
    private MaterialCalendarView mCalendarView;

    private ConProgressDialog mProgressDialog;

    private Menu mMenu;
    private ActionMode mActionMode;

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.edit_task_node_cab, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int i = item.getItemId();
            if (i == R.id.menu_delete_task) {
                List<Task> selectedTasks = mAdapter.getSelectedTasks();
                mPresenter.deleteTasks(selectedTasks);
                mode.finish();
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            mAdapter.setIsActionMode(false);
        }
    };

    public EditPlanContract.TaskNodePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_edit_tasknode;
    }

    @Override
    protected void initView() {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.edit_plan_title_second_step);
        }
        setHasOptionsMenu(true);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new EditTaskNodeAdapter(getActivity(), new EditTaskNodeAdapter.ItemClickListener() {
            @Override
            public void onTaskClick(Task task) {
                mPresenter.editTaskNode(task);
            }

            @Override
            public boolean onTaskLongClick(Task task) {
                if (mActionMode != null) {
                    return false;
                }

                // Start the CAB using the ActionMode.Callback defined above
                mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
                mAdapter.setIsActionMode(true);
                return true;
            }

            @Override
            public void onSelectedTaskCountChanged(int count) {
                mActionMode.setTitle(String.format(getString(R.string.edit_plan_cab_title_delete_task), count));
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        initBottomSheetDialog();
    }

    @Override
    protected void initData() {
        mPresenter = new EditTaskNodePresenter(getActivity().getIntent().getStringExtra(ConstructionConstants.BundleKey.PROJECT_ID));
        mPresenter.bindView(this);
        mPresenter.fetchPlan();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.edit_task_node, menu);
        mMenu = menu;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        showAddIcon(mPresenter.shouldShowAddIcon());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menu_add_task) {
            mPresenter.startAddTask();
            return true;
        } else if (i == R.id.menu_filter_task_all) {
            mPresenter.onFilterTypeChange(TaskNodePresenter.TaskFilterType.ALL_TASKS);
            return true;
        } else if (i == R.id.menu_filter_task_construction) {
            mPresenter.onFilterTypeChange(TaskNodePresenter.TaskFilterType.CONSTRUCTION_TASKS);
            return true;
        } else if (i == R.id.menu_filter_task_material) {
            mPresenter.onFilterTypeChange(TaskNodePresenter.TaskFilterType.MATERIAL_TASKS);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_ADD_TASK:
                if (resultCode ==  Activity.RESULT_OK) {
                    Task task = (Task) data.getSerializableExtra("task");
                    mPresenter.addTask(task);
                    ToastUtils.showShort(getActivity(), task.getName());
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void showTasks(List<Task> tasks) {
        mAdapter.setData(tasks);
    }

    @Override
    public void onCommitSuccess() {
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    @Override
    public void onCommitError(String error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.alert_dialog__default_title);
        builder.setMessage(error);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
            }
        });
        builder.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPresenter.commitPlan();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void updateFilterIcon(int icon) {
        if (mMenu != null) {
            MenuItem item = mMenu.findItem(R.id.menu_filter);
            if (item != null) {
                item.setIcon(icon);
            }
        }
    }

    @Override
    public void showAddIcon(boolean show) {
        if (mMenu != null) {
            MenuItem item = mMenu.findItem(R.id.menu_add_task);
            if (item != null) {
                item.setVisible(show);
            }
        }
    }

    @Override
    public void showAddTaskDialog(ArrayList<Task> deletedTasks) {
        AddTaskDialogFragment addTaskDialogFragment = AddTaskDialogFragment.newInstance(deletedTasks);
        addTaskDialogFragment.setTargetFragment(this, REQUEST_CODE_ADD_TASK);
        addTaskDialogFragment.show(getChildFragmentManager(), FRAGMENT_TAG_ADD_TASK);
    }

    @Override
    public void scrollToTask(Task task) {
        int position = mAdapter.getItemPosition(task);
        if (position != -1) {
            mRecyclerView.scrollToPosition(position);
        }
    }

    @Override
    public boolean scrollToPosition(int position) {
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        int firstPosition = linearLayoutManager.findFirstVisibleItemPosition();
        int lastPosition = linearLayoutManager.findLastVisibleItemPosition();
        if (position < 0) {
            position = 0;
        }

        if (position >= firstPosition && position < lastPosition) {
            return false;
        } else {
            linearLayoutManager.scrollToPositionWithOffset(position, 0);
            return true;
        }
     }

    @Override
    public void showNetError(String msg) {

    }

    @Override
    public void showError(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.alert_dialog__default_title);
        builder.setMessage(msg);
        builder.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void showLoading() {
    }

    @Override
    public void hideLoading() {
    }

    @Override
    public void showPickDayDialog(List<Task> milestones, Task task) {
        @SuppressLint("InflateParams")
        View view  = LayoutInflater.from(getActivity()).inflate(R.layout.layout_edit_task_date_dialog, null);
        mBottomSheetDialog.setContentView(view);
        mCalendarView = (MaterialCalendarView) view.findViewById(R.id.calendarView);

        setupBottomSheetHeader(view, task);
        setupCalendarView(milestones, task);

        View parent = (View) view.getParent();
        BottomSheetBehavior behavior = BottomSheetBehavior.from(parent);
        behavior.setHideable(false);

        mBottomSheetDialog.show();
    }

    @Override
    public void showUpLoading() {
        if (mProgressDialog == null) {
            mProgressDialog = new ConProgressDialog(getActivity());
            mProgressDialog.setMessage(getString(R.string.autonym_uploading));
        }

        mProgressDialog.show();
    }

    @Override
    public void hideUpLoading() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }
    }

    private void initBottomSheetDialog() {
        mBottomSheetDialog = new BottomSheetDialog(getActivity(),
                R.style.BottomSheetDialogTheme_Calendar);
    }

    private void hideBottomSheetDialog() {
        if (mBottomSheetDialog != null && mBottomSheetDialog.isShowing()) {
            mBottomSheetDialog.cancel();
        }
    }

    private void setupBottomSheetHeader(View parentView, Task task) {
        TextView taskNameView = (TextView) parentView.findViewById(R.id.tv_task_name);
        taskNameView.setText(task.getName());

        TextView actionBtn = (TextView) parentView.findViewById(R.id.tv_action);
        actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideBottomSheetDialog();
                List<CalendarDay> selectedDays = mCalendarView.getSelectedDates();
                List<Date> selectedDates = new ArrayList<>();
                for (CalendarDay day: selectedDays) {
                    selectedDates.add(day.getDate());
                }
                mPresenter.updateTask(selectedDates);
            }
        });
    }

    private void setupCalendarView(List<Task> milstoneTasks, Task task) {
        mCalendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_RANGE);

        // Set formatter
        MileStoneDayFormatter mileStoneDayFormatter = new MileStoneDayFormatter();
        mileStoneDayFormatter.setData(milstoneTasks);
        mCalendarView.setDayFormatter(mileStoneDayFormatter);

        // Set decorators
        mCalendarView.removeDecorators();
        MileStoneNodeDecorator mileStoneDecorator = new MileStoneNodeDecorator(getActivity());
        DateSelectorDecorator selectorDecorator = new DateSelectorDecorator(getActivity());
        ActiveMileStoneDecorator activeMileStoneDecorator = new ActiveMileStoneDecorator();
        activeMileStoneDecorator.setActiveTask(milstoneTasks, task);
        mileStoneDecorator.setData(milstoneTasks);
        mCalendarView.addDecorators(selectorDecorator,
                activeMileStoneDecorator,
                mileStoneDecorator);

        // set date limit
        Date startDate = DateUtil.iso8601ToDate(milstoneTasks.get(0).getPlanningTime().getStart());
        Date endDate = DateUtil.iso8601ToDate(milstoneTasks.get(milstoneTasks.size() - 1).getPlanningTime().getCompletion());

        int limitMonthOffset = 6;
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        // TODO optimize date format fail case
        calendar.setTime(startDate == null ? today : startDate);
        calendar.add(Calendar.MONTH, -limitMonthOffset);
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);
        Date minDate = calendar.getTime();

        // TODO optimize date format fail case
        calendar.setTime(endDate == null ? today : endDate);
        calendar.add(Calendar.MONTH, limitMonthOffset);
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date maxDate = calendar.getTime();

        mCalendarView.state()
                .edit()
                .setMinimumDate(minDate)
                .setMaximumDate(maxDate)
                .commit();

        Date taskStartDay = DateUtil.iso8601ToDate(task.getPlanningTime().getStart());
        Date taskEndDay = DateUtil.iso8601ToDate(task.getPlanningTime().getCompletion());
        mCalendarView.setCurrentDate(taskStartDay == null ? today : taskStartDay);

        // set selected days
        if (DateUtil.isSameDay(taskStartDay, taskEndDay)) {
            mCalendarView.setSelectedDate(taskStartDay);
        } else if (taskStartDay != null){
            mCalendarView.setSelectedRange(taskStartDay, taskEndDay);
        }
    }

}
