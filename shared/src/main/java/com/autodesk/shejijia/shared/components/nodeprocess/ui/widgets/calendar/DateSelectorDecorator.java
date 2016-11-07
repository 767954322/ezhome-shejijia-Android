package com.autodesk.shejijia.shared.components.nodeprocess.ui.widgets.calendar;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.autodesk.shejijia.shared.R;
import com.autodesk.shejijia.shared.components.common.uielements.calanderview.CalendarDay;
import com.autodesk.shejijia.shared.components.common.uielements.calanderview.DayViewDecorator;
import com.autodesk.shejijia.shared.components.common.uielements.calanderview.DayViewFacade;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by wenhulin on 10/20/16.
 */

public class DateSelectorDecorator implements DayViewDecorator {
    private final Drawable drawable;

    @SuppressWarnings("deprecation")
    public DateSelectorDecorator(Activity context) {
        drawable = context.getResources().getDrawable(R.drawable.calander_default_selector);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return true;
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setSelectionDrawable(drawable);
    }
}