package com.photoselector.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.ImageColumns;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.photoselector.ui.PhotoSelectorActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 通用工具类
 *
 * @author chenww
 */
public class CommonUtils {

    /**
     * 开启activity
     */
    public static void launchActivity(Context context, Class<?> activity) {
        Intent intent = new Intent(context, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        context.startActivity(intent);
    }

    /**
     * 开启activity(带参数)
     */
    public static void launchActivity(Context context, Class<?> activity, Bundle bundle) {
        Intent intent = new Intent(context, activity);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        context.startActivity(intent);
    }

    /**
     * 开启activity(带参数)
     */
    public static void launchActivity(Context context, Class<?> activity, String key, int value) {
        Bundle bundle = new Bundle();
        bundle.putInt(key, value);
        launchActivity(context, activity, bundle);
    }

    public static void launchActivity(Context context, Class<?> activity, String key, String value) {
        Bundle bundle = new Bundle();
        bundle.putString(key, value);
        launchActivity(context, activity, bundle);
    }

    public static void launchActivityForResult(Activity context, Class<?> activity, int requestCode) {
        Intent intent = new Intent(context, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        context.startActivityForResult(intent, requestCode);
    }

    public static void launchActivityForResult(Activity context, Class<?> activity, int requestCode, int maxImage) {
        Intent intent = new Intent(context, activity);
        intent.putExtra(PhotoSelectorActivity.KEY_MAX, maxImage);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        context.startActivityForResult(intent, requestCode);
    }

    public static void launchActivityForResult(Activity activity, Intent intent, int requestCode) {
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 启动一个服务
     */
    public static void launchService(Context context, Class<?> service) {
        Intent intent = new Intent(context, service);
        context.startService(intent);
    }

    public static void stopService(Context context, Class<?> service) {
        Intent intent = new Intent(context, service);
        context.stopService(intent);
    }

    /**
     * 判断字符串是否为空
     *
     * @param text
     * @return true null false !null
     */
    public static boolean isNull(CharSequence text) {
        if (text == null || "".equals(text.toString().trim()) || "null".equals(text))
            return true;
        return false;
    }

    /**
     * 获取屏幕宽度
     */
    public static int getWidthPixels(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    /**
     * 获取屏幕高度
     */
    public static int getHeightPixels(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    /**
     * 通过Uri获取图片路径
     */
    public static String query(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, new String[]{ImageColumns.DATA}, null, null, null);
        cursor.moveToNext();
        return cursor.getString(cursor.getColumnIndex(ImageColumns.DATA));
    }

    /**
     * md5加密
     *
     * @param password
     * @return
     */
    public static String getMd5Encode(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] result = digest.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                int number = b & 0xff;
                //int number = b&0xff-3;//加减一个数字就成为加盐,能使算法更加难以被猜透

                String hex = Integer.toHexString(number);
                if (hex.length() == 1) {
                    sb.append("0");
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 保存bitMap文件到sd卡(格式为JPEG)
     *
     * @param filePath 文件全路径
     * @param mBitmap  BitMap对象
     * @param quality  压缩质量(范围:0-100)
     * @throws IOException
     */
    public static void saveMyBitmap(String filePath, Bitmap mBitmap, int quality) throws IOException {
        if (TextUtils.isEmpty(filePath) || mBitmap == null) {
            return;
        }
        File f = new File(filePath);
        f.createNewFile();
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (quality < 0) {
            quality = 0;//最小体积
        } else if (quality > 100) {
            quality = 100;//最高质量
        }
        mBitmap.compress(Bitmap.CompressFormat.JPEG, quality, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否能上网
     *
     * @param context
     * @return 是否能上网
     */
    public static boolean hasConnectedNetwork(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
