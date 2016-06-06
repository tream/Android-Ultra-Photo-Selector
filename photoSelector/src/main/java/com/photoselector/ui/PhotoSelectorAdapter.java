package com.photoselector.ui;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.photoselector.R;
import com.photoselector.model.PhotoModel;
import com.photoselector.ui.PhotoItem.OnItemClickListener;

import java.util.ArrayList;

/**
 * @author tream(cntream@gmail.com)
 * @version V1.0
 * @ClassName: PhotoSelectorAdapter
 * @Description: 图片选择适配器
 * @Date 2016/4/26 0026 11:56
 */
public class PhotoSelectorAdapter extends MBaseAdapter<PhotoModel> {

    private int itemWidth;//单个item的宽度
    private int horizentalNum = 3;//单行显示数量,默认3
    private PhotoItem.OnPhotoItemCheckedListener OnPhotoItemCheckedListener;
    private AbsListView.LayoutParams itemLayoutParams;
    private OnItemClickListener onItemClickListener;
    private OnClickListener cameraListener;//不为null时显示拍照选项,为null时不显示

    private PhotoSelectorAdapter(Context context, ArrayList<PhotoModel> models) {
        super(context, models);
    }

    public PhotoSelectorAdapter(Context context, ArrayList<PhotoModel> models, int screenWidth, PhotoItem.OnPhotoItemCheckedListener onPhotoItemCheckedListener, OnItemClickListener OnItemClickListener,
                                OnClickListener cameraListener) {
        this(context, models);
        setItemWidth(horizentalNum, screenWidth);
        this.OnPhotoItemCheckedListener = onPhotoItemCheckedListener;
        this.onItemClickListener = OnItemClickListener;
        this.cameraListener = cameraListener;
    }

    public void setCameraListener(OnClickListener cameraListener) {
        this.cameraListener = cameraListener;
    }

    /**
     * 设置每一个Item的宽高
     */
    public void setItemWidth(int horizentalNum, int screenWidth) {
        int horizentalSpace = context.getResources().getDimensionPixelSize(R.dimen.sticky_item_horizontalSpacing);
        this.itemWidth = (screenWidth - (horizentalSpace * (horizentalNum - 1))) / horizentalNum;
        this.itemLayoutParams = new AbsListView.LayoutParams(itemWidth, itemWidth);
    }

    @Override
    public int getCount() {
        if (cameraListener != null) {
            return models.size() + 1;
        } else {
            return models.size();
        }
    }

    @Override
    public Object getItem(int position) {
        if (null != cameraListener) {
            if (position == 0) {
                return null;
            }
            return models.get(position - 1);
        } else {
            return models.get(position);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null != cameraListener) {//显示拍照选项
            if (position == 0) {
                convertView = View.inflate(context, R.layout.view_camera, null);
                convertView.setOnClickListener(cameraListener);//设置拍照回调
                convertView.setLayoutParams(itemLayoutParams);
                return convertView;
            }
            position--;//修正position对应items中的位置
        }
        PhotoItem item = null;
        if (convertView == null || !(convertView instanceof PhotoItem)) {
            item = new PhotoItem(context, OnPhotoItemCheckedListener);
            convertView = item;
        } else {
            item = (PhotoItem) convertView;
        }
        item.setLayoutParams(itemLayoutParams);
        PhotoModel photoModel = models.get(position);
        item.setPhoto(photoModel);
        item.setOnClickListener(onItemClickListener, position);
        return convertView;
    }
}
