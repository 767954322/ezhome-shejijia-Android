package com.autodesk.shejijia.shared.components.common.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.autodesk.shejijia.shared.R;
import com.autodesk.shejijia.shared.components.common.utility.UIUtils;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * @author   xueqiudong .
 * @version  v1.0 .
 * @date       2016-6-13 .
 * @file          ImageUtils.java .
 * @brief        .
 */
public class ImageUtils {
    private static final String IMAGE_CACHE = "image_cache";

    public static final int DISPLAY_WIDTH = 720;
    public static final int DISPLAY_HEIGHT = 1280;

    private static final DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.common_case_icon)
            .showImageOnFail(R.drawable.common_case_icon)
            .showImageForEmptyUri(R.drawable.common_case_icon)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    private static final DisplayImageOptions userAvatarOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    private static final DisplayImageOptions fileHotspotOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(false)
            .cacheOnDisk(false)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .imageScaleType(ImageScaleType.NONE)
            .build();

    private static final DisplayImageOptions RoundOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .displayer(new RoundedBitmapDisplayer(360))
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
//            .imageScaleType(ImageScaleType.NONE)
            .build();


    private static final DisplayImageOptions FileOptions = new DisplayImageOptions.Builder()
//            .cacheInMemory(true)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .imageScaleType(ImageScaleType.NONE)
            .build();

    private static final DisplayImageOptions ListViewImageOptions = new DisplayImageOptions.Builder()
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .imageScaleType(ImageScaleType.EXACTLY)
            .build();

    public static void initImageLoader(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .memoryCacheExtraOptions(DISPLAY_WIDTH, DISPLAY_HEIGHT) // default = device screen dimensions
                .threadPoolSize(3) // default
                .threadPriority(Thread.NORM_PRIORITY - 1) // default
                .tasksProcessingOrder(QueueProcessingType.FIFO) // default
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .memoryCacheSizePercentage(13) // default
                .diskCache(new UnlimitedDiskCache(createDefaultCacheDir())) // default
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
                .imageDownloader(new BaseImageDownloader(context)) // default
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
                .memoryCache(new WeakMemoryCache())
                .build();
        ImageLoader.getInstance().init(config);
    }


    private static File createDefaultCacheDir() {
        File cache = new File(getCacheDir());
        if (!cache.exists()) {
            if (cache.mkdirs()) {
                Log.d("tag", "Cache files to create success");
            } else {
                Log.d("tag", "The cache file creation failed");
            }
        }
        return cache;
    }

    /**
     * SDcard cache root directory
     */
    public static String getCacheDir() {
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "demo/" + IMAGE_CACHE;
    }

    public static void loadImage(ImageView target, String imageUrl) {
        ImageLoader.getInstance().displayImage(imageUrl, target, options);
    }

    /**
     * 缩略图的设置
     */
    private static DisplayImageOptions iconOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.common_case_icon)
            .showImageOnFail(R.drawable.common_case_icon)
            .showImageForEmptyUri(R.drawable.common_case_icon)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .imageScaleType(ImageScaleType.IN_SAMPLE_INT)

            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();
    /**
     * 头像的设置
     */
    private static DisplayImageOptions avatarOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.icon_default_avator)
            .showImageOnFail(R.drawable.icon_default_avator)
            .showImageForEmptyUri(R.drawable.icon_default_avator)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .build();

    /**
     * 加载头像
     *
     * @param imageUrl
     * @param imageView
     */
    public static void displayAvatarImage(String imageUrl, ImageView imageView) {
        if (TextUtils.isEmpty(imageUrl) || "null".equals(imageUrl)) {
            imageView.setImageDrawable(UIUtils.getDrawable(R.drawable.icon_default_avator));
        }
        ImageLoader.getInstance().displayImage(imageUrl, imageView, avatarOptions);
    }

    /**
     * 显示缩略图
     *
     * @param imageUrl
     * @param imageView
     */
    public static void displayIconImage(String imageUrl, ImageView imageView) {
        ImageLoader.getInstance().displayImage(imageUrl, imageView, iconOptions);
    }

    public static void loadImageRound(ImageView target, String imageUrl) {
        ImageLoader.getInstance().displayImage(imageUrl, target, RoundOptions);
    }

    public static void loadUserAvatar(ImageView target, String imageUrl)
    {
        ImageLoader.getInstance().displayImage(imageUrl, target, userAvatarOptions);
    }

    public static void loadFileImage(ImageView target, String imageUrl) {
        ImageLoader.getInstance().displayImage(imageUrl, target, FileOptions);
    }

    /**
     * @param imageUrl
     * @param target
     * @param imageLoadingListener
     * @param imageLoadingProgressListener
     */
    public static void loadFileImageListenr(String imageUrl, ImageView target, ImageLoadingListener imageLoadingListener, ImageLoadingProgressListener imageLoadingProgressListener) {
        ImageLoader.getInstance().displayImage(imageUrl, target, FileOptions, imageLoadingListener, imageLoadingProgressListener);
    }

    public static void loadHotspotImage(String imageUrl, ImageView target, ImageLoadingListener imageLoadingListener, ImageLoadingProgressListener imageLoadingProgressListener) {
        ImageLoader.getInstance().displayImage(imageUrl, target, fileHotspotOptions, imageLoadingListener, imageLoadingProgressListener);
    }

    public static void loadListViewImage(String imageUrl, ImageView target, ImageLoadingListener imageLoadingListener, ImageLoadingProgressListener imageLoadingProgressListener) {
        ImageLoader.getInstance().displayImage(imageUrl, target, ListViewImageOptions, imageLoadingListener, imageLoadingProgressListener);
    }

    public static void loadImage(ImageView target, String imageUrl, ImageLoadingListener listener) {
        ImageLoader.getInstance().displayImage(imageUrl, target, options, listener);
    }


    public static void loadImage(ImageView target, String imageUrl, boolean wrapContent) {
        if (wrapContent) {
            ImageSize targetImageSize = new ImageSize(target.getWidth(), target.getHeight());
            ImageLoader.getInstance().displayImage(imageUrl, new ImageViewAware(target), options, targetImageSize, null, null);
        } else {
            loadImage(target, imageUrl);
        }
    }

    public static void loadFileImage(ImageView target, String imagePath, boolean wrapContent) {
        String imageUrl = ImageDownloader.Scheme.FILE.wrap(imagePath);
        loadImage(target, imageUrl, wrapContent);
    }


    public static Bitmap decodeBitmap(String path) {
        BitmapFactory.Options op = new BitmapFactory.Options();
        //inJustDecodeBounds
        //If set to true, the decoder will return null (no bitmap), but the out…
        op.inJustDecodeBounds = true;
        ///  Get the size information .
        BitmapFactory.decodeFile(path, op);
        ///The proportion of size using  .
        int wRatio = (int) Math.ceil(op.outWidth / DISPLAY_WIDTH);
        int hRatio = (int) Math.ceil(op.outHeight / DISPLAY_HEIGHT);
        ///  If beyond the specified size, reduce the corresponding proportion .
        if (wRatio > 1 && hRatio > 1) {
            if (wRatio > hRatio) {
                op.inSampleSize = wRatio;
            } else {
                op.inSampleSize = hRatio;
            }
        }
        op.inJustDecodeBounds = false;
        op.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(path, op);
        } catch (OutOfMemoryError e) {
        }
        return bitmap;
    }

    public static Bitmap loadImageSync(String imageUrl) {
        return ImageLoader.getInstance().loadImageSync(imageUrl);
    }


    public static void clearCache() {
        ImageLoader.getInstance().clearMemoryCache();
        ImageLoader.getInstance().clearDiscCache();
    }


    private static byte[] compressImageFileInternal(Bitmap image, int quality) {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            byte[] bytes = baos.toByteArray();
            int length = bytes.length;
            if (length / 1024 > 500) {
                return null;
            }
            return bytes;
        } catch (Exception e) {
            return null;
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    public static byte[] compressImageFile(Bitmap image) {
        int quality = 60;
        byte[] bytes;
        while ((bytes = compressImageFileInternal(image, quality)) == null) {
            quality -= 10;
            if (quality < 10) {
                break;
            }
        }
        return bytes;
    }
}