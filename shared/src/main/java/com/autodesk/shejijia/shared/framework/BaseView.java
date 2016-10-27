package com.autodesk.shejijia.shared.framework;

/**
 * Created by t_xuz on 10/10/16.
 *
 */
public interface BaseView {

    void showNetError(String msg);

    void showError(String msg);

    void showLoading();

    void hideLoading();

}
