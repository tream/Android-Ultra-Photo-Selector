package com.photoselector.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.photoselector.R;
import com.photoselector.model.AlbumModel;
import com.photoselector.util.NativeImageLoader;

/**
 * @author tream(cntream@gmail.com)
 * @version V1.0
 * @ClassName: AlbumItem
 * @Description: 相册条目
 * @Date 2016/4/26 0026 13:38
 */
public class AlbumItem extends LinearLayout {

    private ImageView ivAlbum, ivIndex;
    private TextView tvName, tvCount;

    public AlbumItem(Context context) {
        this(context, null);
    }

    public AlbumItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.layout_album, this, true);

        ivAlbum = (ImageView) findViewById(R.id.iv_album_la);
        ivIndex = (ImageView) findViewById(R.id.iv_index_la);
        tvName = (TextView) findViewById(R.id.tv_name_la);
        tvCount = (TextView) findViewById(R.id.tv_count_la);
    }

    public AlbumItem(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    /**
     * 加载相册首页图片
     */
    public void setAlbumImage(String path) {
//		ImageLoader.getInstance().displayImage("file://" + path, ivAlbum);
        NativeImageLoader.getInstance().loadNativeImage(path, new Point(200, 200), true, new NativeImageLoader.NativeImageCallBack() {
            @Override
            public void onImageLoader(Bitmap bitmap, String path) {
                ivAlbum.setImageBitmap(bitmap);
            }
        });
    }

    /**
     * 更新相册内容
     */
    public void update(AlbumModel album) {
        setAlbumImage(album.getRecent());
        setName(album.getName());
        setCount(album.getCount());
        isCheck(album.isCheck());
    }

    //设置相册名称
    public void setName(CharSequence title) {
        tvName.setText(title);
    }

    //设置相册内条目数量
    public void setCount(int count) {
        tvCount.setHint(count + "张");
    }

    //设置是否选取
    public void isCheck(boolean isCheck) {
        if (isCheck)
            ivIndex.setVisibility(View.VISIBLE);
        else
            ivIndex.setVisibility(View.GONE);
    }

}
