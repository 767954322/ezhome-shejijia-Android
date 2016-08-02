package com.autodesk.shejijia.consumer.home.decorationdesigners.entity;

/**
 * @author he.liu .
 * @version v1.0 .
 * @date 2016-8-2 .
 * @file ImagesBean.java .
 * @brief 案例库图片实体类 .
 */
public class ImagesBean {
    private Object file_id;
    private String file_name;
    private String file_url;
    private boolean is_primary;

    public Object getFile_id() {
        return file_id;
    }

    public void setFile_id(Object file_id) {
        this.file_id = file_id;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getFile_url() {
        return file_url;
    }

    public void setFile_url(String file_url) {
        this.file_url = file_url;
    }

    public boolean is_primary() {
        return is_primary;
    }

    public void setIs_primary(boolean is_primary) {
        this.is_primary = is_primary;
    }
}
