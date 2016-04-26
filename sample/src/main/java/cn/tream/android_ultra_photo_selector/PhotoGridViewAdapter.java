package cn.tream.android_ultra_photo_selector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.photoselector.model.PhotoModel;
import com.photoselector.util.NativeImageLoader;

import java.util.ArrayList;

/**
 * @author tream(cntream@gmail.com)
 * @version V1.0
 * @ClassName: PhotoGridViewAdapter
 * @Description: 本地选取图片包裹适配器
 * @Date 2016/4/25 0015 19:11
 */
public class PhotoGridViewAdapter extends BaseAdapter {

    private int itemWidth;
    private AbsListView.LayoutParams itemLayoutParams;
    private Context mContext;
    private ArrayList<PhotoModel> itemList = new ArrayList<>();

    public PhotoGridViewAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ImageView imageView;
        if (view == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(itemLayoutParams);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        } else {
            imageView = (ImageView) view;
        }
        final ImageView finalView = imageView;
        imageView.setImageResource(R.drawable.ic_picture_loading);
        NativeImageLoader.getInstance().loadNativeImage(itemList.get(i).getOriginalPath(), new Point(itemWidth, itemWidth), true, new NativeImageLoader.NativeImageCallBack() {
            @Override
            public void onImageLoader(Bitmap bitmap, String path) {
                if (path.equals(itemList.get(i).getOriginalPath())) {
                    finalView.setImageBitmap(bitmap);
                }
            }
        });
        return imageView;
    }

    /**
     * 通过横向排列栏数及允许屏幕宽度计算条目宽度
     *
     * @param horizentalNum 列数
     * @param screenWidth   总显示宽度
     */
    public void setItemWidth(int horizentalNum, int screenWidth) {
        int horizentalSpace = mContext.getResources().getDimensionPixelSize(com.photoselector.R.dimen.sticky_item_horizontalSpacing);
        this.itemWidth = (screenWidth - (horizentalSpace * (horizentalNum - 1))) / horizentalNum;
        this.itemLayoutParams = new AbsListView.LayoutParams(itemWidth, itemWidth);
    }

    public void setItemList(ArrayList<PhotoModel> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    public ArrayList<PhotoModel> getItemList() {
        return itemList;
    }
}
