# RefreshLayout

项目特点
* 支持RecycView和ViewPage嵌套滑动；
* headerview支持固定不动和上滑两种类型；
* 支持上滑设置margin；
* 支持自动回弹和上滑；
* 支持控制头部折叠后是否还能展开；
* 控制是否支持嵌套滑动；
* 支持方法调用自动展开或隐藏headerview；
* 支持滑动回调listener；

##  APK下载
[Download](https://github.com/linuxjava/FoldingWidget/raw/master/apk/app-debug.apk)

## XML配置
参考demo中的使用，注意StickyNavLayout需包含在RelativeLayout布局内，否则在有虚拟导航的设备上有bug；参考代码如下：
```xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <xiao.free.folding.lib.StickyNavLayout
        android:id="@+id/stickynavlayout"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

    </xiao.free.folding.lib.StickyNavLayout>

</RelativeLayout>
```
## 方法

```
/**
 * 设置顶部Margin
 *
 * @param topMargin 单位dp
 */
public void setTopPadding(int topMargin)

/**
 * 是否支持停留在headview的中间位置
 *
 * @param autoScroll
 */
public void setAutoScroll(boolean autoScroll)

/**
 * 控制头部折叠后是否还能展开
 *
 * @param enablePullDown
 */
public void setEnablePullDown(boolean enablePullDown)

/**
 * 设置是否支持嵌套滑动
 *
 * @param nestedScroll
 */
public void setNestedScroll(boolean nestedScroll)

/**
 * 展开或隐藏头部
 *
 * @param isExpand true:展开;false:隐藏
 * @param duration
 */
public void expandFold(boolean isExpand, int duration)

/**
 * 头部是否已完全隐藏
 *
 * @return
 */
public boolean isFulledHideHeader()

/**
 * 头部是否已完全显示
 *
 * @return
 */
public boolean isFulledShowHeader()
```

## 滑动回调监听

```
public interface ScrollListener {
    /**
     * 头部滚动回调
     *
     * @param percentage 0~1:头部隐藏；1~0头部显示
     */
    void onScroll(float percentage);
}
```

## 效果图
### 两种头部滑动效果
![image](https://github.com/linuxjava/FoldingWidget/raw/master/gif/1.gif)
![image](https://github.com/linuxjava/FoldingWidget/raw/master/gif/2.gif)
### 支持单个Recycview
![image](https://github.com/linuxjava/FoldingWidget/raw/master/gif/3.gif)
### 支持ViewPage
![image](https://github.com/linuxjava/FoldingWidget/raw/master/gif/4.gif)




