package xiao.free.folding.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Field;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

//跟App相关的辅助类
public class AppUtils {
    private final static int STATUS_BAR_DEFAULT_HEIGHT_DP = 25;
    private static String channel = "unknown";

    private AppUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");

    }

    public static String getPackageName(Context context) {
        if(context == null) {
            return "";
        }

        return context.getApplicationContext().getPackageName();
    }

    public static String getManufacturer() {
        return Build.MANUFACTURER;
    }

    public static String getProduct() {
        return Build.PRODUCT;
    }

    public static boolean isRootSystem() {
        File f = null;
        final String kSuSearchPaths[] = {"/system/bin/", "/system/xbin/", "/system/sbin/", "/sbin/", "/vendor/bin/"};
        try {
            for (int i = 0; i < kSuSearchPaths.length; i++) {
                f = new File(kSuSearchPaths[i] + "su");
                if (f != null && f.exists()) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 获取应用程序名称
     */
    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * [获取应用程序版本名称信息]
     *
     * @param context
     * @return 当前应用的版本名称
     */
    public static String getVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.versionName;

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static int getVersionCode(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.versionCode;

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static Drawable getAppIcon(Context context) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;

        try {
            packageManager = context.getApplicationContext()
                    .getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(
                    context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            applicationInfo = null;
        }

        Drawable d = packageManager.getApplicationIcon(applicationInfo); //xxx根据自己的情况获取drawable
        //BitmapDrawable bd = (BitmapDrawable) d;

        return d;
    }

    public static Drawable getAppIconByPackageName(Context context, String packageName) {
        if(context == null){
            return null;
        }

        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;

        try {
            packageManager = context.getApplicationContext()
                    .getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            applicationInfo = null;
        }

        Drawable d = null;
        if (applicationInfo != null) {
            //packageManager.getApplicationIcon(packageName);
            d = packageManager.getApplicationIcon(applicationInfo); //xxx根据自己的情况获取drawable
        }
        //BitmapDrawable bd = (BitmapDrawable) d;

        return d;
    }

    /**
     * 获得屏幕高度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * 获得屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    /**
     * 获取设备分辨率
     *
     * @param context
     * @return
     */
    public static float getDensity(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.density;
    }

    /**
     * Android SDK版本
     *
     * @return
     */
    public static int getSDKVersion() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * 获得状态栏的高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        Class<?> clazz = null;
        Object obj = null;
        Field field = null;
        int statusBarHeight = DensityUtils.dp2px(context,
                STATUS_BAR_DEFAULT_HEIGHT_DP);
        try {
            clazz = Class.forName("com.android.internal.R$dimen");
            obj = clazz.newInstance();
            if (isMeizu()) {
                try {
                    field = clazz.getField("status_bar_height_large");
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            if (field == null) {
                field = clazz.getField("status_bar_height");
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        if (field != null && obj != null) {
            try {
                int id = Integer.parseInt(field.get(obj).toString());
                statusBarHeight = context.getResources().getDimensionPixelSize(
                        id);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        if (statusBarHeight <= 0
                || statusBarHeight > DensityUtils.dp2px(context,
                STATUS_BAR_DEFAULT_HEIGHT_DP * 2)) {
            // 安卓默认状态栏高度为25dp，如果获取的状态高度大于2倍25dp的话，这个数值可能有问题，用回默认值。出现这种可能性较低，只有小部分手机出现
            float density = context.getResources().getDisplayMetrics().density;
            if (density == -1) {
                statusBarHeight = DensityUtils.dp2px(context,
                        STATUS_BAR_DEFAULT_HEIGHT_DP);
            } else {
                statusBarHeight = (int) (STATUS_BAR_DEFAULT_HEIGHT_DP * density + 0.5f);
            }
        }
        return statusBarHeight;
    }

    public static boolean isMeizu() {
        String brand = Build.BRAND.toLowerCase();
        if (brand.contains("meizu")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取当前屏幕截图，包含状态栏
     *
     * @param activity
     * @return
     */
    public static Bitmap snapShotWithStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        int width = getScreenWidth(activity);
        int height = getScreenHeight(activity);
        Bitmap bp = null;
        bp = Bitmap.createBitmap(bmp, 0, 0, width, height);
        view.destroyDrawingCache();
        return bp;

    }

    /**
     * 获取当前屏幕截图，不包含状态栏
     *
     * @param activity
     * @return
     */
    public static Bitmap snapShotWithoutStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;

        int width = getScreenWidth(activity);
        int height = getScreenHeight(activity);
        Bitmap bp = null;
        bp = Bitmap.createBitmap(bmp, 0, statusBarHeight, width, height
                - statusBarHeight);
        view.destroyDrawingCache();
        return bp;
    }

    public static boolean isInstallApp(Context context, String packageName) {
        try {
            PackageManager mPackageManager = context.getPackageManager();
            mPackageManager.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public boolean isAppInstalled(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        List<String> pName = new ArrayList<>();
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                pName.add(pn);
            }
        }

        return pName.contains(packageName);
    }

    /**
     * 获取手机内部剩余存储空间
     *
     * @return
     */
    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    /**
     * 获取手机内部总的存储空间
     *
     * @return
     */
    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    public static void startApp(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent();
        intent = packageManager.getLaunchIntentForPackage(packageName);
        if (intent != null) {
            context.startActivity(intent);
        }
    }

    /**
     * 获取当前安装包的渠道号
     */
    public static String getChannel(Context context) {
        if (channel.contentEquals("unknown")) {
            try {
                PackageManager packageManager = context.getApplicationContext()
                        .getPackageManager();
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(
                        context.getPackageName(), PackageManager.GET_META_DATA);
                channel = applicationInfo.metaData.getString("CHANNEL");
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return channel;
    }

    public static String getSignatureInfo(Context context, String packageName) {
        try {

            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            Signature[] signs = packageInfo.signatures;
            Signature sign = signs[0];
            return parseSignature(sign.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private static String parseSignature(byte[] signature) {
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(signature));
            String pubKey = cert.getPublicKey().toString();
            String signNumber = cert.getSerialNumber().toString();
//            System.out.println("signName:" + cert.getSigAlgName());
//            System.out.println("pubKey:" + pubKey);
//            System.out.println("signNumber:" + signNumber);
//            System.out.println("subjectDN:" + cert.getSubjectDN().toString());
            return cert.getSigAlgName();
        } catch (CertificateException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static long getAppSize(Context context, String packageName) {
        if(context == null || TextUtils.isEmpty(packageName)){
            return 0;
        }

        PackageManager mPackageManager = context.getPackageManager();
        List<ApplicationInfo> installList = mPackageManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (int i = 0; i < installList.size(); i++) {
            ApplicationInfo info = installList.get(i);
            if (info.packageName.equals(packageName)) {
                String dir = info.publicSourceDir;
                if(!TextUtils.isEmpty(dir)) {
                    File file = new File(dir);
                    if(file != null) {
                        return file.length();
                    }
                }
                break;
            }
        }

        return 0;
    }
}
