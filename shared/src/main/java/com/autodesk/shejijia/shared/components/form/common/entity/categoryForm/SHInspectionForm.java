package com.autodesk.shejijia.shared.components.form.common.entity.categoryForm;

import com.autodesk.shejijia.shared.components.form.common.constant.SHFormConstant;
import com.autodesk.shejijia.shared.components.form.common.entity.CategoryFormPresenter;
import com.autodesk.shejijia.shared.components.form.common.entity.SHForm;

import java.util.HashMap;

/**
 * Created by t_panya on 16/11/13.
 */

public class SHInspectionForm extends SHForm implements CategoryFormPresenter {
    public SHInspectionForm(HashMap map) {
        super(map);
    }

    @Override
    public String formType() {
        return null;
    }

    @Override
    public String formCategory() {
        return SHFormConstant.SHFormCategory.INSPECTION;
    }

}
