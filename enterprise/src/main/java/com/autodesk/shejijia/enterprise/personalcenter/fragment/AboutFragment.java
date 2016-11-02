package com.autodesk.shejijia.enterprise.personalcenter.fragment;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.autodesk.shejijia.enterprise.R;
import com.autodesk.shejijia.shared.framework.fragment.BaseConstructionFragment;

/**
 * Created by t_xuz on 9/2/16.
 * 我页--关于app页面
 */
@SuppressWarnings("ALL")
public class AboutFragment extends BaseConstructionFragment {
    private ImageButton mBackBtn;
    @SuppressWarnings("FieldCanBeLocal")
    private TextView mTopBarTitle;

    public static AboutFragment newInstance(){
        return new AboutFragment();
    }
    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_personal_center_about;
    }

    @Override
    protected void initView() {
        mBackBtn = (ImageButton)rootView.findViewById(R.id.imgBtn_back);
        mTopBarTitle = (TextView)rootView.findViewById(R.id.tv_personal_title);
        mTopBarTitle.setText(mContext.getString(R.string.personal_center_more_about));
    }

    @Override
    protected void initData() {}

    @Override
    protected void initListener() {
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContext.getSupportFragmentManager().popBackStack();
            }
        });
    }

}
