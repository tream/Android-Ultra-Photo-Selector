package com.photoselector.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.photoselector.R;
import com.photoselector.domain.PhotoSelectorDomain;
import com.photoselector.model.PhotoModel;
import com.photoselector.util.AnimationUtil;
import com.photoselector.util.NativeImageLoader;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * @author tream(cntream@gmail.com)
 * @version V1.0
 * @ClassName: PhotoPreviewActivity
 * @Description: 图片预览页面
 * @Date 2016/4/19 0019 17:53
 */
public class PhotoPreviewActivity extends Activity implements OnClickListener,
        CompoundButton.OnCheckedChangeListener, PhotoSelectorActivity.OnLocalPhotoListener {

    public static final String KEY_MAX = "key_max";//最大图片选择数量
    public static final String PHOTOS = "photos";//图片列表
    public static final String ALBUM = "album";//相册名称
    public static final String POSITION = "position";//当前position
    public static final String SELECTED = "selected";//已选择图片列表

    private ViewPager mViewPager;//ViewPager轮播对象
    private RelativeLayout layoutTop;//头布局
    protected RelativeLayout layoutBottom;//尾布局
    private ImageButton btnBack;
    private TextView tvPercent;//当前显示进度
    protected Button btnDone;
    protected CheckBox cbDone;//选择
    protected List<PhotoModel> photos;//当前界面轮播的图像列表

    protected ArrayList<PhotoModel> selected = new ArrayList<>();//当前选择的图片列表
    protected int MAX_IMAGE = 9;//最大图片选择数量
    protected int current;//当前位置
    protected Map<NativeImageLoader.NativeImageCallBack, PhotoPreview> photoPreviewMap = new Hashtable<>();//当前PhotoPreview对象集合
    protected NativeImageLoader nativeImageLoader;
    private PhotoSelectorDomain photoSelectorDomain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 设置页面主题类型
        setContentView(R.layout.activity_photopreview);
        layoutTop = (RelativeLayout) findViewById(R.id.layout_top_app);
        btnBack = (ImageButton) findViewById(R.id.btn_back_app);
        tvPercent = (TextView) findViewById(R.id.tv_percent_app);
        mViewPager = (ViewPager) findViewById(R.id.vp_base_app);
        layoutBottom = (RelativeLayout) findViewById(R.id.layout_bottom_app);
        btnDone = (Button) findViewById(R.id.btn_photo_preview_done);
        btnDone.setOnClickListener(this);
        cbDone = (CheckBox) findViewById(R.id.cb_photo_preview_select);
        cbDone.setOnCheckedChangeListener(this);

        btnBack.setOnClickListener(this);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                current = position;
                //更新当前位置刷新
                updatePercent();
            }
        });

        nativeImageLoader = NativeImageLoader.getInstance().setMaxTaskSize(1);//初始化ImageLoader

        overridePendingTransition(R.anim.activity_alpha_action_in, 0); // 加载进入动画

        photoSelectorDomain = new PhotoSelectorDomain(getApplicationContext());

        init(getIntent().getExtras());
    }

    /**
     * 展示指定图片列表
     *
     * @param photos   图片列表
     * @param position 当前显示位置
     * @return Bundle对象
     */
    public static Bundle getBundle(ArrayList<PhotoModel> photos, int position) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(PHOTOS, photos);
        bundle.putInt(POSITION, position);
        return bundle;
    }

    /**
     * 展示指定图片列表,并且可选择/取消选择
     *
     * @param photos   图片列表
     * @param position 当前显示位置
     * @param selected 已选择列表
     * @param key_max  选择允许最大值
     * @return Bundle对象
     */
    public static Bundle getBundle(ArrayList<PhotoModel> photos, int position, ArrayList<PhotoModel> selected, int key_max) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(PHOTOS, photos);
        bundle.putInt(POSITION, position);
        bundle.putSerializable(SELECTED, selected);
        bundle.putInt(KEY_MAX, key_max);
        return bundle;
    }

    /**
     * @param album    相册名称
     * @param position 当前显示位置
     * @return Bundle对象
     */
    public static Bundle getBundle(String album, int position) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ALBUM, album);
        bundle.putInt(POSITION, position);
        return bundle;
    }

    /**
     * @param album    相册名称
     * @param position 当前显示位置
     * @param selected 已选择列表
     * @param key_max  选择允许最大值
     * @return Bundle对象
     */
    public static Bundle getBundle(String album, int position, ArrayList<PhotoModel> selected, int key_max) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ALBUM, album);
        bundle.putInt(POSITION, position);
        bundle.putSerializable(SELECTED, selected);
        bundle.putInt(KEY_MAX, key_max);
        return bundle;
    }

    protected void init(Bundle extras) {
        if (extras == null)
            return;

        if (extras.containsKey(POSITION)) {
            this.current = extras.getInt(POSITION);
        }

        if (extras.containsKey(PHOTOS)) { // 是否包含图片列表
            photos = (List<PhotoModel>) extras.getSerializable(PHOTOS);
            bindData();
            updatePercent();//更新进度显示
        } else if (extras.containsKey(ALBUM)) { // 从相册中读取
            String albumName = extras.getString(ALBUM);
            if (!TextUtils.isEmpty(albumName) && albumName.equals(PhotoSelectorActivity.RECCENT_PHOTO)) {
                photoSelectorDomain.getReccent(this);
            } else {
                photoSelectorDomain.getAlbum(albumName, this);
            }
        }

        if (extras.containsKey(SELECTED) && extras.containsKey(KEY_MAX)) {
            selected = (ArrayList<PhotoModel>) extras.get(SELECTED);
            MAX_IMAGE = (int) extras.get(KEY_MAX);
            btnDone.setVisibility(View.VISIBLE);
            layoutBottom.setVisibility(View.VISIBLE);
            updateSelectedText();//更新完成按钮显示文本
        } else {
            btnDone.setVisibility(View.GONE);
            layoutBottom.setVisibility(View.GONE);
        }
    }

    //加载相册图片回调
    @Override
    public void onPhotoLoaded(List<PhotoModel> photos) {
        this.photos = photos;
        for (PhotoModel photoModel : selected) {
            int i = photos.indexOf(photoModel);
            if (i >= 0) photos.get(i).setChecked(true);
        }
        bindData(); // 绑定数据
        updatePercent();//更新进度显示
    }

    //更新显示位置
    protected void updatePercent() {
        tvPercent.setText((current + 1) + "/" + photos.size());//current为从0开始,所以此处加1
        cbDone.setChecked(photos.get(current).isChecked());//设置当前图片选择状态
    }

    /**
     * 设置图片适配器
     */
    protected void bindData() {
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(current);
    }

    //轮播图页面适配器
    private PagerAdapter mPagerAdapter = new PagerAdapter() {

        @Override
        public int getCount() {
            if (photos == null) {
                return 0;
            } else {
                return photos.size();
            }
        }

        @Override
        public View instantiateItem(final ViewGroup container, final int position) {
            PhotoPreview photoPreview = getPhotoPreview(photos.get(position));
            container.addView(photoPreview);
            photoPreview.setOnClickListener(photoItemClickListener);
            return photoPreview;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            PhotoPreview photoPreview = (PhotoPreview) object;
            for (NativeImageLoader.NativeImageCallBack callBack : photoPreviewMap.keySet()) {
                if (photoPreviewMap.get(callBack) == photoPreview) {
                    nativeImageCallBack = callBack;
                    break;
                }
            }
            container.removeView(photoPreview);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    };

    private NativeImageLoader.NativeImageCallBack nativeImageCallBack;//空闲的图片加载回调

    //添加viewPager复用机制,放置快速滑动时导致内存溢出(view)
    protected synchronized PhotoPreview getPhotoPreview(PhotoModel photoModel) {
        PhotoPreview photoPreview = new PhotoPreview(this);
        photoPreview.setPhotoModel(photoModel);
        NativeImageLoader.NativeImageCallBack callBack;
        if (nativeImageCallBack != null) {//复用旧的callback
            callBack = nativeImageCallBack;
            nativeImageCallBack = null;
        } else {
            callBack = new NativeImageLoader.NativeImageCallBack() {
                @Override
                public void onImageLoader(Bitmap bitmap, String path) {
                    if (photoPreviewMap.containsKey(this)) {
                        photoPreviewMap.get(this).setBitMap(bitmap);
                    }
                }
            };
        }
        photoPreviewMap.put(callBack, photoPreview);
        //获取屏幕分辨率,设置为加载大图片的大小
        Point point = new Point();
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getSize(point);
        //加载图片
        nativeImageLoader.loadNativeImage(photoModel.getOriginalPath(), point, false, callBack);
        return photoPreview;
    }

    protected boolean isHide;

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_back_app) {
            onBackPressed();
        } else if (v.getId() == R.id.btn_photo_preview_done) {
            ok();
        }
    }

    //点击完成传递已选择数据回原activity中
    private void ok() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("photos", selected);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (btnDone.getVisibility() == View.VISIBLE) {//说明需要返回结果
            setResult(RESULT_CANCELED);
            finish();
        } else {
            finish();
        }
    }

    /**
     * 单击图片时全屏看图(显示隐藏title栏)
     */
    private OnClickListener photoItemClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isHide) {
                new AnimationUtil(getApplicationContext(), R.anim.translate_up)
                        .setInterpolator(new LinearInterpolator()).setFillAfter(true).startAnimation(layoutTop);
                new AnimationUtil(getApplicationContext(), R.anim.translate_down)
                        .setInterpolator(new LinearInterpolator()).setFillAfter(true).startAnimation(layoutBottom);
                isHide = true;
            } else {
                new AnimationUtil(getApplicationContext(), R.anim.translate_down_current)
                        .setInterpolator(new LinearInterpolator()).setFillAfter(true).startAnimation(layoutTop);
                new AnimationUtil(getApplicationContext(), R.anim.translate_up_current)
                        .setInterpolator(new LinearInterpolator()).setFillAfter(true).startAnimation(layoutBottom);
                isHide = false;
            }
        }
    };

    //图片选择事件
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == cbDone) {
            updateSelected(isChecked);
        }
    }

    /**
     * 更新选择列表
     *
     * @param isChecked CheckBox选择状态
     */
    protected void updateSelected(boolean isChecked) {
        if (selected == null) {
            selected = new ArrayList<>();
        }
        PhotoModel photoModel = photos.get(current);
        if (isChecked) {//选取动作
            if (!selected.contains(photoModel)) {//选取列表不包含当前项时
                if (selected.size() < MAX_IMAGE) {
                    photoModel.setChecked(true);
                    selected.add(photoModel);
                } else {//超出选择范围
                    Toast.makeText(this, "超出选择范围!", Toast.LENGTH_SHORT).show();
                    cbDone.setChecked(false);//取消CheckBox选取
                }
            }
        } else {//取消选取动作
            selected.remove(photoModel);
            photoModel.setChecked(false);
        }
        updateSelectedText();
    }

    //更新完成按钮文字
    protected void updateSelectedText() {
        btnDone.setText(getString(R.string.done) + "(" + selected.size() + "/" + MAX_IMAGE + ")");
        if (selected.size() < 1) {
            btnDone.setText(getString(R.string.done));
        }
    }
}
