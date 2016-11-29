package com.autodesk.shejijia.shared.components.nodeprocess.utility;

import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.autodesk.shejijia.shared.R;
import com.autodesk.shejijia.shared.components.common.appglobal.ConstructionConstants;
import com.autodesk.shejijia.shared.components.common.entity.microbean.Task;
import com.autodesk.shejijia.shared.components.common.entity.microbean.Time;

/**
 * Class description
 *
 * @author wenhulin
 * @since 16/11/29
 */

public class TaskUtils {

    public static int getDisplayStatus(String status) {
        int displayStringId;
        switch (status.toLowerCase()) {
            case ConstructionConstants.TaskStatus.OPEN:
                displayStringId = R.string.task_open;
                break;
            case ConstructionConstants.TaskStatus.RESERVED:
                displayStringId = R.string.task_reserved;
                break;
            case ConstructionConstants.TaskStatus.RESERVING:
                displayStringId = R.string.task_reserving;
                break;
            case ConstructionConstants.TaskStatus.INPROGRESS:
                displayStringId = R.string.task_inProgress;
                break;
            case ConstructionConstants.TaskStatus.DELAYED:
                displayStringId = R.string.task_delayed;
                break;
            case ConstructionConstants.TaskStatus.QUALIFIED:
                displayStringId = R.string.task_qualified;
               break;
            case ConstructionConstants.TaskStatus.UNQUALIFIED:
                displayStringId = R.string.task_unqualified;
                break;
            case ConstructionConstants.TaskStatus.RESOLVED:
                displayStringId = R.string.task_resolved;
                break;
            case ConstructionConstants.TaskStatus.REJECTED:
                displayStringId = R.string.task_rejected;
                break;
            case ConstructionConstants.TaskStatus.REINSPECTION:
                displayStringId = R.string.task_reinspection;
                break;
            case ConstructionConstants.TaskStatus.RECTIFICATION:
                displayStringId = R.string.task_rectification;
                break;
            case ConstructionConstants.TaskStatus.REINSPECTING:
                displayStringId = R.string.task_reinspecting;
                break;
            case ConstructionConstants.TaskStatus.REINSPECTION_AND_RECTIFICATION:
                displayStringId = R.string.task_reinspectionand_rectification;
                break;
            case ConstructionConstants.TaskStatus.REINSPECT_RESERVING:
                displayStringId = R.string.task_reinspect_treserving;
                break;
            case ConstructionConstants.TaskStatus.REINSPECT_RESERVED:
                displayStringId = R.string.task_reinspect_reserved;
                break;
            case ConstructionConstants.TaskStatus.REINSPECT_INPROGRESS:
                displayStringId = R.string.task_reinspect_inprogress;
                break;
            case ConstructionConstants.TaskStatus.REINSPECT_DELAY:
                displayStringId = R.string.task_reinspect_delay;
                break;
            case ConstructionConstants.TaskStatus.DELETED:
                displayStringId = R.string.task_deleted;
                break;
            default:
                displayStringId = R.string.task_status_unknow;
                break;
        }

        return displayStringId;
    }

    public static int getStatusLevel(String status) {
        int level;
        switch (status.toLowerCase()) {
            case ConstructionConstants.TaskStatus.RESERVING:
            case ConstructionConstants.TaskStatus.INPROGRESS:
                level = 1;
                break;
            case ConstructionConstants.TaskStatus.DELAYED:
                level = 2;
                break;
            case ConstructionConstants.TaskStatus.UNQUALIFIED:
                level = 3;
                break;
            default:
                level = 0;
                break;
        }

        return level;
    }

    public static Time getTaskTime(Task task) {
        String category = task.getCategory();
        if (TextUtils.isEmpty(task.getCategory())) {
            return null;
        }

        Time time = null;
        switch (category) {
            case ConstructionConstants.TaskCategory.CONSTRUCTION:
                time = task.getPlanningTime();
                break;
            default:
                if (!TextUtils.isEmpty(task.getStatus())) {
                    String status = task.getStatus();
                    if (status.equalsIgnoreCase(ConstructionConstants.TaskStatus.OPEN) ||
                            status.equalsIgnoreCase(ConstructionConstants.TaskStatus.RESERVING)) {
                        if (task.getPlanningTime() != null) {
                            time = task.getPlanningTime();
                        }
                    } else {
                        time = task.getReserveTime();
                    }
                }
                break;
        }

        return time;
    }
}
