package com.autodesk.shejijia.enterprise.nodeprocess.plan;

import com.autodesk.shejijia.shared.components.common.entity.microbean.Task;
import com.autodesk.shejijia.shared.framework.BasePresenter;
import com.autodesk.shejijia.shared.framework.BaseView;

import java.util.List;

/**
 * Created by wenhulin on 11/2/16.
 */

public interface EditPlanContract {
    interface View extends BaseView {
        void showTasks(List<Task> tasks);
        void bindPresenter(Presenter presenter);
    }

    interface Presenter extends BasePresenter {
        void bindView(View view);
        /**
         * fetch exist plan
         */
        void fetchPlan();

        /**
         * update edit plan data, the data will save in memory or local
         */
        void updatePlan();

        /**
         * commit edited plan
         */
        void commitPlan();

        void updateEditState(EditState newState);
        EditState getEditState();
    }
}
