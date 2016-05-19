package com.photoselector.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.photoselector.R;
import com.photoselector.model.PhotoModel;
import com.polites.GestureImageView;

/**
 * @author tream(cntream@gmail.com)
 * @version V1.0
 * @ClassName: PhotoPreview
 * @Description: 照片预览控件
 * @Date 2016/4/19 0019 17:43
 */
public class PhotoPreview extends FrameLayout implements OnClickListener {

    private ProgressBar pbLoading;
    private GestureImageView ivContent;
    private PhotoModel photoModel;
    private OnClickListener l;

    public PhotoPreview(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.view_photopreview, this, true);

        pbLoading = (ProgressBar) findViewById(R.id.pb_loading_vpp);
        ivContent = (GestureImageView) findViewById(R.id.iv_content_vpp);
        ivContent.setOnClickListener(this);
    }

    public PhotoPreview(Context context, AttributeSet attrs, int defStyle) {
        this(context);
    }

    public PhotoPreview(Context context, AttributeSet attrs) {
        this(context);
    }

    //设置当前view的图像
    public void setBitMap(Bitmap bitMap) {
        if (bitMap != null) {
            ivContent.setImageBitmap(bitMap);
            pbLoading.setVisibility(GONE);
        }
    }

    public PhotoModel getPhotoModel() {
        return photoModel;
    }

    public void setPhotoModel(PhotoModel photoModel) {
        this.photoModel = photoModel;
    }

//    public void loadImageBitmap(PhotoModel photoModel) {
//        pbLoading.setVisibility(View.VISIBLE);
//        this.photoModel = photoModel;
//        Log.w("loadImageBitmap:", photoModel.getOriginalPath());
////		loadImageBitmap("file://" + photoModel.getOriginalPath());
//        //获取屏幕分辨率,设置为加载大图片的大小
//        Point point = new Point();
//        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
//        wm.getDefaultDisplay().getSize(point);
//        NativeImageLoader.getInstance().loadImageBitmap(photoModel.getOriginalPath(), point, false, nativeImageCallBack);
//    }
//
//    //加载图片的回调方法
//    private NativeImageLoader.NativeImageCallBack nativeImageCallBack = new NativeImageLoader.NativeImageCallBack() {
//        @Override
//        public void onImageLoad(Bitmap bitmap, String path) {
//            if (path.equals(photoModel.getOriginalPath())) {
//                ivContent.setImageBitmap(bitmap);
//                pbLoading.setVisibility(View.GONE);
//            }
//        }
//    };

//    private void loadImageBitmap(String path) {
//		ImageLoader.getInstance().loadImageBitmap(path, new SimpleImageLoadingListener() {
//			@Override
//			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//				ivContent.setImageBitmap(loadedImage);
//				pbLoading.setVisibility(View.GONE);
//			}
//
//			@Override
//			public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
//				ivContent.setImageDrawable(getResources().getDrawable(R.drawable.ic_picture_loadfailed));
//				pbLoading.setVisibility(View.GONE);
//			}
//		});
//    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        this.l = l;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_content_vpp && l != null)
            l.onClick(ivContent);
    }
}
