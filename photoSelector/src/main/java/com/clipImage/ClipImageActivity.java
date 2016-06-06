package com.clipImage;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;

import com.photoselector.R;
import com.photoselector.util.CommonUtils;
import com.photoselector.util.NativeImageLoader;

import java.io.File;
import java.io.IOException;

/**
 * @author tream(cntream@gmail.com)
 * @version V1.0
 * @ClassName: ClipImageActivity
 * @Description: 对图像进行裁剪, 返回裁剪后的图片
 * @Date 2016/6/1 0001 19:22
 */
public class ClipImageActivity extends Activity implements View.OnClickListener {
    private ClipImageLayout mClipImageLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clip_image_activity);
        mClipImageLayout = (ClipImageLayout) findViewById(R.id.clipImageLayout);
        String path = getIntent().getStringExtra("path");
        if (TextUtils.isEmpty(path)) {
            finish();
        }
        findViewById(R.id.bv_back_lh).setOnClickListener(this); // 返回
        findViewById(R.id.btn_right_lh).setOnClickListener(this); // 完成
        NativeImageLoader.getInstance(getApplicationContext()).loadImageBitmap(path, new Point(600, 600), false, 0, new NativeImageLoader.NativeImageCallBack() {
            @Override
            public void onImageLoad(Bitmap bitmap, String path) {
                if (bitmap != null) {
                    mClipImageLayout.setImageBitmap(bitmap);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bv_back_lh) {
            setResult(RESULT_CANCELED);
        } else if (v.getId() == R.id.btn_right_lh) {
            Bitmap bitmap = mClipImageLayout.clip();
            //保存到SD卡
            File dir = new File(Environment.getExternalStorageDirectory() + "/temp/clipImage/");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String path = dir.getAbsolutePath() + "/" + System.currentTimeMillis();
            try {
                CommonUtils.saveMyBitmap(path, bitmap, 80);
                Intent intent = getIntent();
                intent.putExtra("path", path);
                setResult(RESULT_OK, intent);
            } catch (IOException e) {
                e.printStackTrace();
                setResult(RESULT_CANCELED);
            }
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//            byte[] datas = baos.toByteArray();
//            Intent intent = getIntent();
//            intent.putExtra("bitmap", datas);
//            setResult(RESULT_OK, intent);
        }
        finish();
    }
}
