package com.autodesk.shejijia.shared.components.form.data.source;

import android.support.annotation.NonNull;

import com.autodesk.shejijia.shared.components.form.common.entity.ContainedForm;
import com.autodesk.shejijia.shared.components.common.listener.LoadDataCallback;

import java.util.List;

/**
 * Created by t_panya on 16/10/20.
 * 表单获取data source,包括本地和网络
 */

public interface FormDataSource {

    void getRemoteFormItemDetails(@NonNull LoadDataCallback<List> callBack, String[] fIds);

    void updateRemoteFormItems(@NonNull LoadDataCallback callBack, String projectId, String taskId, List<ContainedForm> forms);

}
