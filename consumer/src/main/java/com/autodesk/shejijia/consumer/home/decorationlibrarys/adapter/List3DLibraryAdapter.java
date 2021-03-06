package com.autodesk.shejijia.consumer.home.decorationlibrarys.adapter;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.autodesk.shejijia.consumer.R;
import com.autodesk.shejijia.consumer.home.decorationlibrarys.entity.Case3DDetailImageListBean;

import java.util.List;

public class List3DLibraryAdapter extends BaseAdapter {

    private List<Case3DDetailImageListBean> mImageLists;
    private Context mContext;
    private LayoutInflater inflater;

    public List3DLibraryAdapter(Context context, List<Case3DDetailImageListBean> mImageLists) {
        mContext = context;
        this.mImageLists = mImageLists;
        inflater = LayoutInflater.from(mContext);
    }

    public int getCount() {
        return mImageLists.size();
    }

    public Object getItem(int position) {
        return mImageLists.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        MyViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.activity_case_library_detail_3d_item, null);
            viewHolder = new MyViewHolder();
            viewHolder.mTypeName = (TextView) convertView.findViewById(R.id.tv_type_name);
            viewHolder.mTypeImagePic = (ImageView) convertView.findViewById(R.id.img_type_icon);
            viewHolder.mTypeDetailsList = (RecyclerView) convertView.findViewById(R.id.rly_type_details);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (MyViewHolder) convertView.getTag();
        }

        if (mImageLists.get(position).getType().equalsIgnoreCase("0")) {
            viewHolder.mTypeName.setText("渲染图");
            viewHolder.mTypeImagePic.setImageResource(R.drawable.xuanran_icon);
        }
//        else if (mImageLists.get(position).getType().equalsIgnoreCase("4")) {
//            viewHolder.mTypeName.setText("漫游图");
//            viewHolder.mTypeImagePic.setImageResource(R.drawable.manyou_icon);
//        }
        else if (mImageLists.get(position).getType().equalsIgnoreCase("10")) {
            viewHolder.mTypeName.setText("户型图");
            viewHolder.mTypeImagePic.setImageResource(R.drawable.huxing_icon);
        }

        // set image list to recyclerView
        set3DDetailsImageList(viewHolder, position);

        return convertView;
    }

    private void set3DDetailsImageList(MyViewHolder myViewHolder, int position) {


        //set data to recyclerView
        if (mImageLists.get(position).getImageList() !=null && mImageLists.get(position).getImageList().size()>0){
            String type = mImageLists.get(position).getType();
            if (type.equals("4")){
//                LinearLayoutManager linearLayoutManager=new LinearLayoutManager(mContext){
//                    @Override
//                    public boolean canScrollHorizontally() {
//                        return true;
//                    }
//
//                    @Override
//                    public boolean canScrollVertically() {
//                        return false;
//                    }
//                };
//                linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//                myViewHolder.mTypeDetailsList.setLayoutManager(linearLayoutManager);
//                myViewHolder.mTypeDetailsList.setAdapter(new List3DLibraryDetailsAdapter(type,mImageLists.get(position).getImageList(),R.layout.dynamic_add_3d_view,mContext));

            }else {
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext){
                    @Override
                    public boolean canScrollVertically() {
                        return false;
                    }
                };
                linearLayoutManager.setAutoMeasureEnabled(true);
                myViewHolder.mTypeDetailsList.setHasFixedSize(true);
                myViewHolder.mTypeDetailsList.setItemAnimator(new DefaultItemAnimator());
                myViewHolder.mTypeDetailsList.setLayoutManager(linearLayoutManager);
                myViewHolder.mTypeDetailsList.setAdapter(new List3DLibraryDetailsAdapter(type,mImageLists.get(position).getImageList(),R.layout.dynamic_add_3d_view,mContext));

            }
        }
    }

    static class MyViewHolder {
        private ImageView mTypeImagePic; //类型 Icon
        private TextView mTypeName; //类型名
        private RecyclerView mTypeDetailsList; //不同类型对应的图片列表
    }
}
