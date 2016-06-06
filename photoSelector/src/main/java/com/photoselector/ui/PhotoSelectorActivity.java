package com.photoselector.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.photoselector.R;
import com.photoselector.domain.PhotoSelectorDomain;
import com.photoselector.model.AlbumModel;
import com.photoselector.model.PhotoModel;
import com.photoselector.ui.PhotoItem.OnItemClickListener;
import com.photoselector.ui.PhotoItem.OnPhotoItemCheckedListener;
import com.photoselector.util.AnimationUtil;
import com.photoselector.util.CommonUtils;
import com.photoselector.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tream(cntream@gmail.com)
 * @version V1.0
 * @ClassName: PhotoSelectorActivity
 * @Description: 图片选择页面
 * @Date 2016/4/19 0019 17:54
 */
public class PhotoSelectorActivity extends Activity implements
        OnItemClickListener, OnPhotoItemCheckedListener, AdapterView.OnItemClickListener,
        OnClickListener {

    public static final String KEY_MAX = "key_max";
    public static final String SHOW_CAMERA = "show_camera";
    public static final int REQUEST_PREVIEW = 0;//预览请求码
    private static final int REQUEST_CAMERA = 1;//拍照请求码
    public static String RECCENT_PHOTO = null;//最近照片
    private int maxImage = 9;//最大选择图片数量,默认9
    private boolean showCamera = true;//是否显示相机，默认显示
    private GridView gvPhotos;
    private ListView lvAblum;
    private Button btnOk;
    private TextView tvAlbum, tvPreview, tvTitle;
    private PhotoSelectorDomain photoSelectorDomain;
    private PhotoSelectorAdapter photoAdapter;
    private AlbumAdapter albumAdapter;
    private RelativeLayout layoutAlbum;
    private ArrayList<PhotoModel> selected;
    private File mTmpFile;
    private Handler handler = new Handler();
    //相册显示回调
    private OnLocalAlbumListener albumListener = new OnLocalAlbumListener() {
        @Override
        public void onAlbumLoaded(List<AlbumModel> albums) {
            albumAdapter.update(albums);
        }
    };
    //图片显示回调
    private OnLocalPhotoListener photoListener = new OnLocalPhotoListener() {
        @Override
        public void onPhotoLoaded(List<PhotoModel> photos) {
            for (PhotoModel model : selected) {
                if (photos.contains(model)) {
                    model.setChecked(true);
                }
            }
            photoAdapter.update(photos);
            gvPhotos.smoothScrollToPosition(0); // 平滑滚动到第一个
            // reset(); //--keep selected photos
        }
    };

    public static Bundle getBundle(int maxImage, boolean showCamera) {
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_MAX, maxImage);
        bundle.putBoolean(SHOW_CAMERA, showCamera);
        return bundle;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RECCENT_PHOTO = getResources().getString(R.string.recent_photos);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 不显示title栏
        setContentView(R.layout.activity_photoselector);

        if (getIntent().getExtras() != null) {
            maxImage = getIntent().getIntExtra(KEY_MAX, 9);
            showCamera = getIntent().getBooleanExtra(SHOW_CAMERA, true);
        }

        //当最大图片数为0并且显示相机时,直接进入拍照,并且拍照结束直接返回
        if (maxImage == 0 && showCamera) {
            catchPicture();
        }

        //可选使用ImageLoader时需要初始化ImageLoader
        //创建默认的ImageLoader配置参数
//        ImageLoaderConfiguration configuration = ImageLoaderConfiguration.createDefault(this);
//        ImageLoader.getInstance().init(configuration);

        photoSelectorDomain = new PhotoSelectorDomain(getApplicationContext());

        selected = new ArrayList<PhotoModel>();

        tvTitle = (TextView) findViewById(R.id.tv_title_lh);
        gvPhotos = (GridView) findViewById(R.id.gv_photos_ar);
        lvAblum = (ListView) findViewById(R.id.lv_ablum_ar);
        btnOk = (Button) findViewById(R.id.btn_right_lh);
        tvAlbum = (TextView) findViewById(R.id.tv_album_ar);
        tvPreview = (TextView) findViewById(R.id.tv_preview_ar);
        layoutAlbum = (RelativeLayout) findViewById(R.id.layout_album_ar);

        btnOk.setOnClickListener(this);
        tvAlbum.setOnClickListener(this);
        tvPreview.setOnClickListener(this);

        photoAdapter = new PhotoSelectorAdapter(getApplicationContext(),
                new ArrayList<PhotoModel>(), CommonUtils.getWidthPixels(this),
                this, this, showCamera ? this : null);
        gvPhotos.setAdapter(photoAdapter);
        gvPhotos.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if ((right - left) != (oldRight - oldLeft)) {
                    if (photoAdapter != null) {
                        //重新设置gridView中ImageView大小
                        photoAdapter.setItemWidth(gvPhotos.getNumColumns(), v.getMeasuredWidth());
                        photoAdapter.notifyDataSetChanged();
                    }
                }
            }
        });


        albumAdapter = new AlbumAdapter(getApplicationContext(), new ArrayList<AlbumModel>());
        lvAblum.setAdapter(albumAdapter);
        lvAblum.setOnItemClickListener(this);

        findViewById(R.id.bv_back_lh).setOnClickListener(this); // 返回

        photoSelectorDomain.getReccent(photoListener); // 初始化为最近照片列表
        photoSelectorDomain.updateAlbum(albumListener); // 初始化更新相册列表
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_right_lh)
            ok(); // 确定
        else if (v.getId() == R.id.tv_album_ar)//切换相册
            album();
        else if (v.getId() == R.id.tv_preview_ar)//预览
            priview();
        else if (v.getId() == R.id.tv_camera_vc)//拍照
            catchPicture();
        else if (v.getId() == R.id.bv_back_lh)//返回
            onBackPressed();
    }

    /**
     * 拍照
     */
    private void catchPicture() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // 设置系统相机拍照后的输出路径
            // 创建临时文件
            try {
                mTmpFile = FileUtils.createTmpFile(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (mTmpFile != null && mTmpFile.exists()) {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTmpFile));
                startActivityForResult(cameraIntent, REQUEST_CAMERA);
            } else {
                Toast.makeText(this, "图片错误", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "没有系统相机", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            if (requestCode == REQUEST_CAMERA) {
                //删除缓存文件
                if (mTmpFile != null && mTmpFile.exists()) {
                    mTmpFile.delete();
                }
                //当最大为0且显示相机,即单拍照模式,直接返回
                if (maxImage <= 0 && showCamera) {
                    onBackPressed();
                }
            }
            return;
        }
        // 相机拍照完成后，返回图片路径
        if (requestCode == REQUEST_CAMERA) {
            if (mTmpFile != null && mTmpFile.exists()) {
                onCameraShot(mTmpFile);
                return;
            }
        }
        if (requestCode == REQUEST_PREVIEW) {//在大图页面点击完成,向上传递,返回需求页面
            selected = (ArrayList<PhotoModel>) data.getSerializableExtra("photos");
            ok();
        }
    }

    public void onCameraShot(File imageFile) {
        if (imageFile != null) {
            //采用发送广播并延迟加载的方式获取新拍照的图片
            // notify system
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));

            //当最大为0(单拍照模式)且显示相机或最大为1(单选模式下使用了相机),设置相机图像并返回
            if (maxImage <= 1 && showCamera) {
                selected.clear();
                PhotoModel photoModel = new PhotoModel();
                photoModel.setChecked(true);
                photoModel.setOriginalPath(imageFile.getAbsolutePath());
                selected.add(photoModel);
                ok();
            }
            //延迟加载,等待系统数据库添加图片后读取
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    photoSelectorDomain.getReccent(photoListener);
                }
            }, 500);

            //也可以不延迟直接先行添加到当前相册
//            photoAdapter.getItems().add(0, photoModel);
//            photoAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 确定
     */
    private void ok() {
        if (selected.isEmpty()) {
            setResult(RESULT_CANCELED);
        } else {
            Intent data = new Intent();
            Bundle bundle = new Bundle();
            bundle.putSerializable("photos", selected);
            data.putExtras(bundle);
            setResult(RESULT_OK, data);
        }
        finish();
    }

    /**
     * 预览
     */
    private void priview() {
        Bundle bundle = PhotoPreviewActivity.getBundle(selected, 0, selected, maxImage);
        Intent intent = new Intent(this, PhotoPreviewActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, REQUEST_PREVIEW);
    }

    //显示与隐藏选择相册popuwindow窗口
    private void album() {
        if (layoutAlbum.getVisibility() == View.GONE) {
            popAlbum();
        } else {
            hideAlbum();
        }
    }

    /**
     * 弹出选择相册的popuwindow窗口
     */
    private void popAlbum() {
        layoutAlbum.setVisibility(View.VISIBLE);
        new AnimationUtil(getApplicationContext(), R.anim.translate_up_current)
                .setLinearInterpolator().startAnimation(layoutAlbum);
    }

    /**
     * 隐藏选择相册窗口
     */
    private void hideAlbum() {
        new AnimationUtil(getApplicationContext(), R.anim.translate_down)
                .setLinearInterpolator().startAnimation(layoutAlbum);
        layoutAlbum.setVisibility(View.GONE);
    }

    /**
     * 重置选择
     */
    private void reset() {
        for (PhotoModel photoModel : selected) {
            photoModel.setChecked(false);
        }
        selected.clear();
        btnOk.setText(getString(R.string.done));
        tvPreview.setEnabled(false);
        photoAdapter.notifyDataSetChanged();
    }

    @Override
    /** 点击图片 */
    public void onItemClick(int position) {
        int currentPosition;
        if (tvAlbum.getText().toString().equals(RECCENT_PHOTO)) {
            if (showCamera) {
                currentPosition = position;
            } else {
                currentPosition = position - 1;
            }
        } else {
            currentPosition = position;
        }
        Bundle bundle = PhotoPreviewActivity.getBundle(tvAlbum.getText().toString(), currentPosition, selected, maxImage);
        Intent intent = new Intent(this, PhotoPreviewActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, REQUEST_PREVIEW);
    }

    /**
     * 勾选图片时
     *
     * @param photoModel 当前photoModel对象
     * @param buttonView 当前view对象
     * @param isChecked  选择状态
     * @return 判断是否允许选取的结果(超过最大选择数则选取失败返回false)
     */
    @Override
    public boolean onCheckedChanged(PhotoModel photoModel, CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if (!selected.contains(photoModel)) {
                if (selected.size() >= maxImage) {
                    Toast.makeText(this, String.format(getString(R.string.max_img_limit_reached),
                            maxImage), Toast.LENGTH_SHORT).show();
                    return false;//超出最大选取数
                } else {
                    selected.add(photoModel);
                }
            }
            tvPreview.setEnabled(true);
        } else {
            selected.remove(photoModel);
        }
        btnOk.setText(getString(R.string.done) + "(" + selected.size() + (maxImage > 0 ? "/" + maxImage : "") + ")");

        if (selected.isEmpty()) {
            tvPreview.setEnabled(false);
            tvPreview.setText(getString(R.string.preview));
            btnOk.setText(getString(R.string.done));
        }
        return true;//选取正常
    }

    @Override
    public void onBackPressed() {
        if (layoutAlbum.getVisibility() == View.VISIBLE) {
            hideAlbum();
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    /**
     * 点击相册条目时
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AlbumModel current = (AlbumModel) parent.getItemAtPosition(position);
        for (int i = 0; i < parent.getCount(); i++) {
            AlbumModel album = (AlbumModel) parent.getItemAtPosition(i);
            if (i == position)
                album.setCheck(true);
            else
                album.setCheck(false);
        }
        albumAdapter.notifyDataSetChanged();
        hideAlbum();
        tvAlbum.setText(current.getName());
        // tvTitle.setText(current.getName());

        // 最近照片
        if (current.getName().equals(RECCENT_PHOTO)) {
            if (showCamera) {//根据系统设置是否显示相机
                photoAdapter.setCameraListener(PhotoSelectorActivity.this);//显示相机
            } else {
                photoAdapter.setCameraListener(null);//隐藏相机
            }
            photoSelectorDomain.getReccent(photoListener);
        } else {
            photoAdapter.setCameraListener(null);//其他相册默认隐藏相机
            photoSelectorDomain.getAlbum(current.getName(), photoListener); // 异步获取相册列表
        }
    }

    /**
     * 获取相册所有照片回调
     */
    public interface OnLocalPhotoListener {
        void onPhotoLoaded(List<PhotoModel> photos);
    }

    /**
     * 异步获取读取相册回调
     */
    public interface OnLocalAlbumListener {
        void onAlbumLoaded(List<AlbumModel> albums);
    }
}
