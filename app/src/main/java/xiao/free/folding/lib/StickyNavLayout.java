package xiao.free.folding.lib;

import android.content.Context;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Scroller;

import xiao.free.folding.R;
import xiao.free.folding.util.DensityUtils;


public class StickyNavLayout extends LinearLayout implements NestedScrollingParent {
    public static final String TAG = "StickyNavLayout";
    private static final int TOP_CHILD_FLING_THRESHOLD = 3;
    private static final int DEFAULT_TOP_PADDING = 0;
    private static final int DEFAULT_MAX_ANIMATION_DURATION = 600;//默认最大动画时间
    private static final int DEFAULT_MIN_ANIMATION_DURATION = 200;//默认最小动画时间
    public static final int SLIDING_ALL = 1;//整个布局整体上滑
    public static final int SLIDING_CONTAINER = 2;//只有底部container容器向上滑动，头部不动
    private int mType = SLIDING_CONTAINER;
    private int mTopMargin = 0;//滑动到顶部的padding
    private View mHeaderView;//头部View
    private View mContainer;//底部内容容器
    private View mNav;//固定导航栏
    private ViewGroup mViewPager;
    private RecyclerView mRecyclerView;
    private int mMaxScrollDistance;//最大滚动距离headerViewHeight-mTopMargin
    private ScrollListener mListener;
    private boolean enablePullDown = true;//头部折叠后，是否支持下拉展开
    private boolean isNestedScroll = true;//是否支持嵌套滑动
    private boolean isAutoScroll = true;//是否支持自动滚动
    private Scroller mScroller;
    private int mMaximumVelocity, mMinimumVelocity;

    public StickyNavLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(LinearLayout.VERTICAL);

        mScroller = new Scroller(getContext());
        mTopMargin = DensityUtils.dp2px(getContext(), DEFAULT_TOP_PADDING);
        mMaximumVelocity = ViewConfiguration.get(context)
                .getScaledMaximumFlingVelocity();
        mMinimumVelocity = ViewConfiguration.get(context)
                .getScaledMinimumFlingVelocity();
        Log.d(TAG, mMaximumVelocity + ":" + mMinimumVelocity);
    }

    public void setType(int mType) {
        this.mType = mType;
    }

    /**
     * 设置顶部Margin
     *
     * @param topMargin 单位dp
     */
    public void setTopPadding(int topMargin) {
        mTopMargin = DensityUtils.dp2px(getContext(), topMargin);
    }

    public void setListener(ScrollListener mListener) {
        this.mListener = mListener;
    }

    /**
     * 是否支持停留在headview的中间位置
     *
     * @param autoScroll
     */
    public void setAutoScroll(boolean autoScroll) {
        isAutoScroll = autoScroll;
    }

    /**
     * 控制头部折叠后是否还能展开
     *
     * @param enablePullDown
     */
    public void setEnablePullDown(boolean enablePullDown) {
        this.enablePullDown = enablePullDown;
    }

    /**
     * 设置是否支持嵌套滑动
     *
     * @param nestedScroll
     */
    public void setNestedScroll(boolean nestedScroll) {
        isNestedScroll = nestedScroll;
    }

    /**
     * 展开或隐藏头部
     *
     * @param isExpand true:展开;false:隐藏
     * @param duration
     */
    public void expandFold(boolean isExpand, int duration) {
        if (mHeaderView == null) {
            return;
        }

        if (!mScroller.isFinished()) {
            return;
        }

        if (isExpand) {
            scrollShowHeader(duration);
        } else {
            scrollHideHeader(duration);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHeaderView = findViewById(R.id.id_stickynavlayout_headerview);
        mContainer = findViewById(R.id.id_stickynavlayout_container);
        mNav = findViewById(R.id.id_stickynavlayout_indicator);
        mViewPager = findViewById(R.id.id_stickynavlayout_viewpager);
        mRecyclerView = findViewById(R.id.id_stickynavlayout_recycview);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //不限制顶部的高度
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //设置Container布局高度
        ViewGroup.LayoutParams params = mContainer.getLayoutParams();
        params.height = getMeasuredHeight() - mTopMargin;
        mContainer.setLayoutParams(params);

        //固定导航头高度
        int navHeight = 0;
        if (mNav != null) {
            navHeight = mNav.getMeasuredHeight();
        }

        //设置mViewPager或mViewPager的高度
        if (mViewPager != null) {
            ViewGroup.LayoutParams params1 = mViewPager.getLayoutParams();
            params1.height = params.height - navHeight;
        } else if (mRecyclerView != null) {
            ViewGroup.LayoutParams params2 = mRecyclerView.getLayoutParams();
            params2.height = params.height - navHeight;
            mRecyclerView.setLayoutParams(params2);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMaxScrollDistance = mHeaderView.getMeasuredHeight() - mTopMargin;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            if (mType == SLIDING_ALL) {
                scrollTo(0, mScroller.getCurrY());
            } else if (mType == SLIDING_CONTAINER) {
                mContainer.setTranslationY(mScroller.getCurrY());
            }
            callScroll();
            invalidate();
        }
    }

    @Override
    public void scrollTo(int x, int y) {
        if (y < 0) {
            y = 0;
        }
        if (y > mMaxScrollDistance) {
            y = mMaxScrollDistance;
        }
        if (y != getScrollY()) {
            super.scrollTo(x, y);
        }
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        Log.e(TAG, "onStartNestedScroll");
        return isNestedScroll;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        Log.e(TAG, "onNestedScrollAccepted");
    }

    @Override
    public void onStopNestedScroll(View target) {
        Log.e(TAG, "onStopNestedScroll");
        if (!mScroller.isFinished()) {
            Log.e(TAG, "onStopNestedScroll_isFinished");
            return;
        }

        /**
         * 1.头部已完全隐藏时继续向上快速滑动
         * 2.头部已完全显示时继续向下快速滑动
         */
        if (isFulledHideHeader() || isFulledShowHeader()) {
            return;
        }


        autoScroll(DEFAULT_MAX_ANIMATION_DURATION);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        Log.e(TAG, "onNestedScroll");
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        Log.d(TAG, "onNestedPreScroll");
        if (!mScroller.isFinished()) {
            return;
        }

        boolean hiddenTop = isHiddenHeader(dy);
        boolean showTop = isShowHeader(target, dy);

        //头部折叠后是否支持下拉展开头部
        if (showTop && !enablePullDown) {
            //此处返回时并未设置消耗consumed[1]，所以后续的onNestedFling和onStopNestedScroll方法不会被调用
            return;
        }

        if (hiddenTop || showTop) {
            if (mType == SLIDING_ALL) {
                /**
                 * bug1：向上超快速滑动
                 * 检测targetY不能超过mMaxScrollDistance
                 * bug2：按住向上滑动，然后突然快速下滑
                 * 检测targetY不能小于0
                 */
                float targetY = getScrollY() + dy;
                if (targetY > mMaxScrollDistance) {
                    targetY = mMaxScrollDistance;
                }
                if (targetY < 0) {
                    targetY = 0;
                }
                scrollTo(0, (int) targetY);
            } else {
                /**
                 * 处理同上场景的bug
                 */
                float targetY = mContainer.getTranslationY() - dy;
                if (-targetY > mMaxScrollDistance) {
                    targetY = -mMaxScrollDistance;
                }
                if (targetY > 0) {
                    targetY = 0;
                }

                mContainer.setTranslationY(targetY);
            }
            callScroll();
            consumed[1] = dy;
        }
    }

    /**
     * 是否隐藏header
     *
     * @return
     */
    private boolean isHiddenHeader(int dy) {
        if (mType == SLIDING_ALL) {
            return dy > 0 && getScrollY() < mMaxScrollDistance;
        } else {
            return dy > 0 && -mContainer.getTranslationY() < mMaxScrollDistance;
        }
    }

    /**
     * 是否显示header
     *
     * @return
     */
    private boolean isShowHeader(View target, int dy) {
        if (mType == SLIDING_ALL) {
            return dy < 0 && getScrollY() > 0 && !ViewCompat.canScrollVertically(target, -1);
        } else {
            /**
             * 1.手指向下滑动
             * 2.target是否可以向下滑动(负数表示手指向下滑动，正数表示手指向上滑动)
             * 3.头部是否完全显示
             */
            return dy < 0 && mContainer.getTranslationY() < 0 && !ViewCompat.canScrollVertically(target, -1);
        }
    }

    /**
     * 头部是否已完全隐藏
     *
     * @return
     */
    public boolean isFulledHideHeader() {
        if (mType == SLIDING_ALL) {
            return getScrollY() >= mMaxScrollDistance;
        } else {
            return mContainer.getTranslationY() <= -mMaxScrollDistance;
        }
    }

    /**
     * 头部是否已完全显示
     *
     * @return
     */
    public boolean isFulledShowHeader() {
        if (mType == SLIDING_ALL) {
            return getScrollY() <= 0;
        } else {
            return mContainer.getTranslationY() >= 0;
        }
    }

    /**
     * 获取RecyclerView最小可见高度
     *
     * @return
     */
    public int getRecyclerViewMinVisibleHeight() {
        return mRecyclerView.getMeasuredHeight() - (mHeaderView.getMeasuredHeight() - mTopMargin);
    }

    /**
     * @param target
     * @param velocityX
     * @param velocityY Vertical velocity in pixels per second(正数：向上滚动;负数：向下滚动)
     * @param consumed
     * @return
     */
    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        Log.d(TAG, "onNestedFling.velocityY=" + velocityY);
//        if (!mScroller.isFinished()) {
//            return false;
//        }
//
//        /**
//         * 1.头部已完全隐藏时继续向上快速滑动
//         * 2.头部已完全显示时继续向下快速滑动
//         */
//        if (isFulledHideHeader() || isFulledShowHeader()) {
//            return false;
//        }
//
//        /**
//         * 控制处理如下下滑动场景：头部已折叠，快速惯性下滑时
//         * 如果recyclerView第一个可见child的位置>TOP_CHILD_FLING_THRESHOLD，惯性滑动不可以将头部展开；否则，惯性滑动可以将头部展开
//         */
//        if (target instanceof RecyclerView && velocityY < 0) {
//            //如果是recyclerView 根据判断第一个元素是哪个位置可以判断是否消耗
//            //这里判断如果第一个元素的位置是大于TOP_CHILD_FLING_THRESHOLD的
//            //认为已经被消耗，在animateScroll里不会对velocityY<0时做处理
//            final RecyclerView recyclerView = (RecyclerView) target;
//            final View firstChild = recyclerView.getChildAt(0);
//            final int childAdapterPosition = recyclerView.getChildAdapterPosition(firstChild);
//            consumed = childAdapterPosition > TOP_CHILD_FLING_THRESHOLD;
//        }
//
//        int durarion = 0;
//        if(consumed){
//            durarion = computeDuration(velocityY);
//        }else {
//            durarion = computeDuration(0);
//        }
//
//        if (mType == SLIDING_ALL) {
//            flingScroll(velocityY, durarion, consumed);
//        } else {
//            flingScroll2(velocityY, durarion, consumed);
//        }

        return false;
    }

    /**
     * 父view是否拦截Fling事件
     *
     * @param target
     * @param velocityX
     * @param velocityY
     * @return true:拦截target事件;false:不拦截
     */
    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        Log.d(TAG, "onNestedPreFling");
        if (!mScroller.isFinished()) {
            return true;
        }

        if(target instanceof RecyclerView) {
            final RecyclerView recyclerView = (RecyclerView) target;
            final View firstChild = recyclerView.getChildAt(0);
            final int firstVisibleItemPosition = recyclerView.getChildAdapterPosition(firstChild);
            if(firstVisibleItemPosition == 0) {
                if (velocityY > 0) {//向上
                    if (!isFulledHideHeader()) {
                        scrollHideHeader(computeDuration(velocityY));
                        return true;
                    } else {
                        return false;
                    }
                } else {//向下
                    if (!isFulledHideHeader()) {
                        scrollShowHeader(computeDuration(velocityY));
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public int getNestedScrollAxes() {
        return 0;
    }

    private void autoScroll(int duration) {
        if(!isAutoScroll){
            return;
        }

        if (mType == SLIDING_ALL) {
            final int currentOffset = getScrollY();
            final int headerHeight = mHeaderView.getHeight();

            if (currentOffset < headerHeight / 2) {
                scrollShowHeader(duration);
            } else {
                scrollHideHeader(duration);
            }
        } else if (mType == SLIDING_CONTAINER) {
            final float currentOffset = mContainer.getTranslationY();
            final int headerHeight = mHeaderView.getHeight();
            if (Math.abs(currentOffset) < headerHeight / 2) {
                //显示头部
                scrollShowHeader(duration);
            } else {
                //隐藏头部
                scrollHideHeader(duration);
            }
        }
    }

    private void flingScroll(float velocityY, final int duration, boolean consumed) {
        if (velocityY >= 0) {
            scrollHideHeader(duration);
        } else {
            if (!consumed) {
                scrollShowHeader(duration);
            }
        }
    }

    private void flingScroll2(float velocityY, int duration, boolean consumed) {
        if (velocityY >= 0) {
            scrollHideHeader(duration);
        } else {
            //如果子View没有消耗down事件 那么就让自身滑倒0位置
            if (!consumed) {
                scrollShowHeader(duration);
            }
        }
    }

    /**
     * 滚动隐藏HeaderView
     */
    private void scrollHideHeader(int duration) {
        int startY = 0, dy = 0;
        final int headerHeight = mHeaderView.getHeight();

        if (mType == SLIDING_ALL) {
            final int currentOffset = getScrollY();

            startY = currentOffset;
            dy = headerHeight - mTopMargin - currentOffset;
        } else if (mType == SLIDING_CONTAINER) {
            final float currentOffset = mContainer.getTranslationY();

            startY = (int) currentOffset;
            dy = (int) (-(headerHeight - mTopMargin) - currentOffset);
        }

        duration = Math.abs(dy) * duration / mMaxScrollDistance;
        smoothScroll(startY, dy, duration);
    }

    /**
     * 滚动显示HeaderView
     */
    private void scrollShowHeader(int duration) {
        int startY = 0, dy = 0;

        if (mType == SLIDING_ALL) {
            final int currentOffset = getScrollY();

            startY = currentOffset;
            dy = -currentOffset;
        } else if (mType == SLIDING_CONTAINER) {
            final float currentOffset = mContainer.getTranslationY();

            startY = (int) currentOffset;
            dy = (int) -currentOffset;
        }

        duration = Math.abs(dy) * duration / mMaxScrollDistance;
        smoothScroll(startY, dy, duration);
    }

    private void smoothScroll(int startY, int dy, int duration) {
        mScroller.startScroll(0, startY, 0, dy, duration);
        invalidate();
    }

    /**
     * 根据速度计算滚动动画持续时间
     *
     * @param velocityY
     * @return
     */
    private int computeDuration(float velocityY) {
        int distance = 0;
        if (mType == SLIDING_ALL) {
            if (velocityY > 0) {//向上滑
                distance = Math.abs(mMaxScrollDistance - getScrollY());
            } else {//向下滑
                distance = Math.abs(getScrollY());
            }
        } else if (mType == SLIDING_CONTAINER) {
            if (velocityY > 0) {//向上滑
                distance = (int) Math.abs(mMaxScrollDistance + mContainer.getTranslationY());
            } else {
                distance = (int) Math.abs(mContainer.getTranslationY());
            }
        }

        int duration;
        velocityY = Math.abs(velocityY);
        if (velocityY > 0) {
            duration = 3 * Math.round(1000 * (distance / velocityY));
        } else {
            final float distanceRatio = (float) distance / getHeight();
            duration = (int) ((distanceRatio + 1) * 150);
        }
        duration = duration > DEFAULT_MAX_ANIMATION_DURATION ? DEFAULT_MAX_ANIMATION_DURATION : duration;
        duration = duration < DEFAULT_MIN_ANIMATION_DURATION ? DEFAULT_MIN_ANIMATION_DURATION : duration;

        return duration;
    }

    private float lastPercentage = -1;//防止percentage重复回调

    private void callScroll() {
        if (mListener != null) {
            float percentage = getScrollRatio();
            if (lastPercentage != percentage) {
                lastPercentage = percentage;
                mListener.onScroll(percentage);
            }
        }
    }

    /**
     * 获取滑动比率
     *
     * @return
     */
    private float getScrollRatio() {
        //保留两位小数
        if (mType == SLIDING_ALL) {
            return (getScrollY() * 100 / mMaxScrollDistance) * 1.0f / 100;
        } else {
            return ((int) Math.abs(mContainer.getTranslationY()) * 100) / mMaxScrollDistance * 1.0f / 100;
        }
    }

    public interface ScrollListener {
        /**
         * 开始滚动
         *
         * @param direction 1:向上滚动；-1向上滚动
         */
        //void onStartScroll(int direction);

        /**
         * 头部滚动回调
         *
         * @param percentage 0~1:头部隐藏；1~0头部显示
         */
        void onScroll(float percentage);
    }
}
