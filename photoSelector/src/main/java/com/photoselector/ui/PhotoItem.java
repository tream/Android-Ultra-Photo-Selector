package com.photoselector.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.photoselector.R;
import com.photoselector.model.PhotoModel;
import com.photoselector.util.NativeImageLoader;

/**
 * @author tream(cntream@gmail.com)
 * @version V1.0
 * @ClassName: PhotoItem
 * @Description: 列表中带选择图片条目
 * @Date 2016/4/19 0019 17:55
 */
public class PhotoItem extends LinearLayout implements OnCheckedChangeListener, View.OnClickListener,
        OnLongClickListener {

    private ImageView ivPhoto;
    private CheckBox cbPhoto;
    private OnPhotoItemCheckedListener listener;
    private PhotoModel photo;
    private OnItemClickListener l;
    private int position;
    private boolean isShowCheck;

    private PhotoItem(Context context) {
        super(context);
    }

    public PhotoItem(Context context, OnPhotoItemCheckedListener listener) {
        this(context);
        LayoutInflater.from(context).inflate(R.layout.layout_photoitem, this,
                true);
        this.listener = listener;

        setOnLongClickListener(this);

        ivPhoto = (ImageView) findViewById(R.id.iv_photo_lpsi);
        ivPhoto.setOnClickListener(this);
        cbPhoto = (CheckBox) findViewById(R.id.cb_photo_lpsi);

        cbPhoto.setOnCheckedChangeListener(this); // CheckBox选中状态改变监听器
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!isShowCheck) {//仅仅是更新界面,不是用户选取
            boolean checkSuccess = listener.onCheckedChanged(photo, buttonView, isChecked); // 调用主界面回调函数
            if (!checkSuccess) {
                cbPhoto.setChecked(false);
                return;
            }
        }
        // 让图片变暗或者变亮
        if (isChecked) {
            setDrawingable();
            ivPhoto.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        } else {
            ivPhoto.clearColorFilter();
        }
        photo.setChecked(isChecked);
    }


    /**
     * 设置路径下的图片对应的缩略图
     */
    public void setPhoto(PhotoModel photoModel) {
        this.photo = photoModel;
        setSelected(photoModel.isChecked());
        setImageDrawable();
    }

    //加载图片
    private void setImageDrawable() {
//        ImageLoader.getInstance().displayImage(
//                "file://" + photo.getOriginalPath(), ivPhoto);
        ivPhoto.setImageResource(R.drawable.ic_picture_loading);
        Point point = new Point(getLayoutParams().width, getLayoutParams().height);
        NativeImageLoader.getInstance().loadImage(photo.getOriginalPath(), point, true, ivPhoto);
//        NativeImageLoader.getInstance().loadImageBitmap(photo.getOriginalPath(), point, true, nativeImageCallBack);
    }

    //加载图片的回调方法,利于复用,避免oom
    private NativeImageLoader.NativeImageCallBack nativeImageCallBack = new NativeImageLoader.NativeImageCallBack() {
        @Override
        public void onImageLoad(Bitmap bitmap, String path) {
            if (path.equals(photo.getOriginalPath())) {
                if (bitmap != null) {
                    ivPhoto.setImageBitmap(bitmap);
                } else {
                    ivPhoto.setImageResource(R.drawable.ic_picture_loadfailed);
                }

            }
        }
    };

    private void setDrawingable() {
        ivPhoto.setDrawingCacheEnabled(true);
        ivPhoto.buildDrawingCache();
    }

    @Override
    public void setSelected(boolean selected) {
        if (photo == null) {
            return;
        }
        isShowCheck = true;
        cbPhoto.setChecked(selected);
        isShowCheck = false;
    }

    public void setOnClickListener(OnItemClickListener l, int position) {
        this.l = l;
        this.position = position;
    }

    /**
     * 图片Item选中事件监听器
     */
    public interface OnPhotoItemCheckedListener {
        boolean onCheckedChanged(PhotoModel photoModel, CompoundButton buttonView, boolean isChecked);
    }

    /**
     * 图片点击事件
     */
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    //图片长按事件
    @Override
    public boolean onLongClick(View v) {
        if (l != null)
            l.onItemClick(position);
        return true;
    }

    //图片点击事件
    @Override
    public void onClick(View v) {
        if (v == ivPhoto) {
            if (l != null)
                l.onItemClick(position);
        }
    }
}
