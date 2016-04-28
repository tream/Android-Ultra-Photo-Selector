package com.photoselector.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.LruCache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author tream(cntream@gmail.com)
 * @version V1.0
 * @ClassName: NativeImageLoader
 * @Description: 本地图片加载器, 实现类似网络图片加载的三级缓存机制
 * @Date 2016/4/14 0014 10:26
 */
public class NativeImageLoader {
    //默认缓存文件路径
    private static String photoCacheDir = Environment.getExternalStorageDirectory() + "/temp/PhotoSelector/localPic/";
    private LruCache<String, Bitmap> mMemoryCache;//lru一级缓存
    private static NativeImageLoader mInstance = new NativeImageLoader();
    private Map<NativeImageCallBack, NativeImageLoaderOption> loadMap = new Hashtable<>();//使用hashMap快速滑动时出现线程同步异常
    private int currentTaskSize;
    private int maxTaskSize = 5;//最大加载图片线程数
    private int maxLruCache;//最大内存缓存容量
    private ExecutorService mImageThreadPool = Executors.newFixedThreadPool(5);//图片加载线程池,
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            NativeImageLoaderOption option = (NativeImageLoaderOption) msg.obj;
            if (option.callBack != null) {
                option.callBack.onImageLoader(option.bitmap, option.path);
            }
            return true;
        }
    });

    private NativeImageLoader() {
        setMaxLruCache(maxLruCache);
    }

    //设置最大内存缓存
    public NativeImageLoader setMaxLruCache(int maxLruCache) {
        this.maxLruCache = maxLruCache;
        //获取应用程序的最大内存
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);//kb
        if (maxLruCache < 2 * 1024 || maxLruCache > maxMemory) {
            //用最大内存的1/4来存储图片
            maxLruCache = maxMemory / 4;
        }
        mMemoryCache = new LruCache<String, Bitmap>(maxLruCache) {

            //获取每张图片的大小
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;//kb
            }
        };
        return this;
    }

    //设置同时加载任务数
    public NativeImageLoader setMaxTaskSize(int maxTaskSize) {
        this.maxTaskSize = maxTaskSize;
        return this;
    }

    /**
     * 通过此方法来获取NativeImageLoader的实例
     *
     * @return
     */
    public static NativeImageLoader getInstance() {
        return mInstance;
    }


    /**
     * 加载原图，对图片不进行裁剪
     *
     * @param path      文件路径(可以是网络路径及本地路径,网络路径以http开头,其他类型全部认为是本地路径)
     * @param mCallBack 回调
     */
    public void loadNativeImage(final String path, final NativeImageCallBack mCallBack) {
        this.loadNativeImage(path, null, false, mCallBack);
    }

    /**
     * 此方法来加载图片，这里的mPoint是用来封装ImageView的宽和高，我们会根据ImageView控件的大小来裁剪Bitmap
     * 如果你不想裁剪图片，调用loadNativeImage(final String path, final NativeImageCallBack mCallBack)来加载
     *
     * @param path      文件路径(可以是网络路径及本地路径,网络路径以http开头,其他类型全部认为是本地路径)
     * @param mPoint    图片截取的长宽高
     * @param saveCache 是否缓存图片
     * @param mCallBack 加载完成回调
     */
    public void loadNativeImage(String path, Point mPoint, boolean saveCache, NativeImageCallBack mCallBack) {
        NativeImageLoaderOption option = new NativeImageLoaderOption();
        option.path = path;
        option.point = mPoint;
        option.callBack = mCallBack;
        option.useCache = saveCache;

        //添加到任务队列
        loadMap.put(mCallBack, option);
        loadNext();
    }

    //继续加载下一张图片
    private synchronized void loadNext() {
        if (currentTaskSize > maxTaskSize) {
            return;
        }
        if (loadMap.size() > 0) {
            currentTaskSize++;
            final NativeImageCallBack callBack = (NativeImageCallBack) loadMap.keySet().toArray()[0];
            final NativeImageLoaderOption option = loadMap.get(callBack);
            loadMap.remove(callBack);
            final String path = option.path;
            final Point point = option.point;
            mImageThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    Bitmap mBitmap;
                    //获取缓存文件路径
                    String cacheFilePath = getCacheFilePath(path, point);
                    //先获取一级缓存内存中是否有缓存的Bitmap
                    Bitmap bitmap = getBitmapFromMemCache(cacheFilePath);
                    //若该Bitmap不在内存缓存中
                    if (bitmap == null) {
                        //检索二级缓存即SD卡中是否存在截取好的缩略图
                        mBitmap = getBitMapFromSDCache(cacheFilePath);
                    } else {
                        mBitmap = bitmap;
                    }

                    //不存在时获取三级缓存即源文件
                    if (mBitmap == null) {
                        Uri uri = Uri.parse(path);
                        //判断是网络图片还是本地图片
                        if (!TextUtils.isEmpty(uri.getScheme()) && uri.getScheme().contains("http")) {
                            mBitmap = getNetWorkBitmap(path);
                            option.useCache = true;//网络图片强制进行本地缓存
                        } else {//本地图片
                            mBitmap = decodeThumbBitmapForFile(path, point == null ? 0 : point.x, point == null ? 0 : point.y);
                        }
                        //允许创建缓存文件时保存到二级缓存
                        if (option.useCache && mBitmap != null) {
                            writeBitMapToSDCache(cacheFilePath, mBitmap);//三级缓存中取出时才添加到二级缓存
                        }
                    }

                    Message msg = mHandler.obtainMessage();
                    msg.obj = option;
                    option.bitmap = mBitmap;
                    mHandler.sendMessage(msg);

                    //将图片加入到内存缓存
                    if (bitmap == null) {
                        addBitmapToMemoryCache(cacheFilePath, mBitmap);
                    }
                    currentTaskSize--;
                    loadNext();
                }
            });
        }
    }

    /**
     * 一级缓存,往内存缓存中添加Bitmap
     *
     * @param key
     * @param bitmap
     */
    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null && bitmap != null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    /**
     * 一级缓存,根据key来获取内存中的图片
     *
     * @param key
     * @return
     */
    private Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    /**
     * 二级缓存,将压缩后的缩略图写如SD卡
     *
     * @param path
     * @param bitmap
     */
    private void writeBitMapToSDCache(String path, Bitmap bitmap) {
        try {
            CommonUtils.saveMyBitmap(path, bitmap, 50);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取缓存文件路径
     *
     * @param path  源文件路径
     * @param point 要压缩到的分辨率
     * @return 格式化后的缓存文件地址
     */
    public String getCacheFilePath(String path, Point point) {
        String cacheFileName = CommonUtils.getMd5Encode(path + point.hashCode());//获取MD5加密文件名
        File file = new File(photoCacheDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return photoCacheDir + cacheFileName;
    }

    /**
     * 二级缓存,读取SD卡中缓存的缩略图
     *
     * @param cachePath
     * @return
     */
    private Bitmap getBitMapFromSDCache(String cachePath) {
        //添加文件存在检测,避免打印fileNotFoundException
        try {
            return BitmapFactory.decodeStream(new FileInputStream(cachePath));
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    /**
     * 根据View(主要是ImageView)的宽和高来获取图片的缩略图
     *
     * @param path
     * @param viewWidth
     * @param viewHeight
     * @return
     */
    private Bitmap decodeThumbBitmapForFile(String path, int viewWidth, int viewHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        //设置为true,表示解析Bitmap对象，该对象不占内存
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        //计算设置缩放比例
        options.inSampleSize = computeScale(options, viewWidth, viewHeight);

        //设置为false,解析Bitmap对象加入到内存中
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * 根据View(主要是ImageView)的宽和高来计算Bitmap缩放比例。默认不缩放
     *
     * @param options
     * @param viewWidth
     * @param viewHeight
     */
    private int computeScale(BitmapFactory.Options options, int viewWidth, int viewHeight) {
        int inSampleSize = 1;
        if (viewWidth == 0 || viewWidth == 0) {
            return inSampleSize;
        }
        int bitmapWidth = options.outWidth;
        int bitmapHeight = options.outHeight;

        //假如Bitmap的宽度或高度大于我们设定图片的View的宽高，则计算缩放比例
        if (bitmapWidth > viewWidth || bitmapHeight > viewWidth) {
            int widthScale = Math.round((float) bitmapWidth / (float) viewWidth);
            int heightScale = Math.round((float) bitmapHeight / (float) viewWidth);

            //为了保证图片不缩放变形，我们取宽高比例最小的那个
            inSampleSize = widthScale < heightScale ? widthScale : heightScale;
        }
        return inSampleSize;
    }


    /**
     * 加载本地图片的回调接口
     *
     * @author xiaanming
     */
    public interface NativeImageCallBack {
        /**
         * 当子线程加载完了本地的图片，将Bitmap和图片路径回调在此方法中
         *
         * @param bitmap
         * @param path
         */
        void onImageLoader(Bitmap bitmap, String path);
    }

    /**
     * 用于handler中传递的对象
     */
    private class NativeImageLoaderOption {
        public String path;//图片路径
        public Point point;//图片宽高尺寸
        public NativeImageCallBack callBack;//回调对象
        public Bitmap bitmap;//加载的bitMap对象
        public boolean useCache = true;//是否缓存,默认开启缓存
    }

    /**
     * 加载网络图片
     *
     * @param urlString 图片url地址
     * @return BitMap
     */
    public Bitmap getNetWorkBitmap(String urlString) {
        URL imgUrl = null;
        Bitmap bitmap = null;
        try {
            imgUrl = new URL(urlString);
            // 使用HttpURLConnection打开连接
            HttpURLConnection urlConn = (HttpURLConnection) imgUrl
                    .openConnection();
            urlConn.setDoInput(true);
            urlConn.connect();
            // 将得到的数据转化成InputStream
            InputStream is = urlConn.getInputStream();
            // 将InputStream转换成Bitmap
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (MalformedURLException e) {
            System.out.println("[getNetWorkBitmap->]MalformedURLException");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("[getNetWorkBitmap->]IOException");
            e.printStackTrace();
        }
        return bitmap;
    }
}
