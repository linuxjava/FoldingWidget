package xiao.free.folding.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

import xiao.free.folding.R;


public class StickyNavLayout extends LinearLayout implements NestedScrollingParent {
    private static final String TAG = "StickyNavLayout";
    private static final int TOP_CHILD_FLING_THRESHOLD = 3;
    private static final int DEFAULT_TOP_PADDING = 40;
    private static final int SLIDING_ALL = 1;//整个布局整体上滑
    private static final int SLIDING_CONTAINER = 2;//只有底部container容器向上滑动，头部不动
    private int mType = SLIDING_ALL;
    private boolean isFling = false;//是否快速滑动
    private int mTopPadding = 0;//滑动到顶部的padding
    private View mHeaderView;//头部View
    private View mContainer;//底部内容容器
    private View mNav;//固定导航栏
    private ViewGroup mViewPager;
    private RecyclerView mRecycView;
    private int mMaxScrollDistance;//最大滚动距离headerViewHeight-mTopPadding
    private ValueAnimator mOffsetAnimator;
    private Interpolator mInterpolator;
    private ScrollListener mListener;
    private boolean enablePullDown = true;//头部折叠后，是否支持下拉展开

    public StickyNavLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(LinearLayout.VERTICAL);

        mInterpolator = new AccelerateInterpolator();
    }

    public void setListener(ScrollListener mListener) {
        this.mListener = mListener;
    }

    /**
     * 控制头部折叠后是否还能展开
     * @param enablePullDown
     */
    public void setEnablePullDown(boolean enablePullDown) {
        this.enablePullDown = enablePullDown;
    }

    /**
     * 展开头部
     * @param duration
     */
    public void expand(int duration){
        if(mHeaderView == null){
            return;
        }

        if (mType == SLIDING_ALL) {
            if (isFulledHideHeader()) {
                if(mListener != null){
                    mListener.onStartScroll();
                }

                final int currentOffset = getScrollY();
                //显示头部
                if (mOffsetAnimator == null) {
                    mOffsetAnimator = new ValueAnimator();
                    mOffsetAnimator.setInterpolator(mInterpolator);
                    mOffsetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            if (animation.getAnimatedValue() instanceof Integer) {
                                int y = (Integer) animation.getAnimatedValue();
                                if (y != getScrollY()) {
                                    scrollTo(0, y);
                                    callListener();
                                }

                            }
                        }
                    });
                } else {
                    mOffsetAnimator.cancel();
                }
                mOffsetAnimator.setDuration(duration);
                mOffsetAnimator.setIntValues(currentOffset, 0);
                mOffsetAnimator.start();
            }
        } else {
            if (isFulledHideHeader()) {
                if(mListener != null && enablePullDown){
                    mListener.onStartScroll();
                }

                final float currentOffset = mContainer.getTranslationY();
                //显示头部
                if (mOffsetAnimator == null) {
                    mOffsetAnimator = new ValueAnimator();
                    mOffsetAnimator.setInterpolator(mInterpolator);
                    mOffsetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            if (animation.getAnimatedValue() instanceof Float) {
                                float y = (Float) animation.getAnimatedValue();
                                if (y != mContainer.getTranslationY()) {
                                    mContainer.setTranslationY(y);
                                    setAlpha();//修改透明度
                                    callListener();
                                }
                            }
                        }
                    });
                } else {
                    mOffsetAnimator.cancel();
                }
                mOffsetAnimator.setDuration(duration);
                mOffsetAnimator.setFloatValues(currentOffset, 0);
                mOffsetAnimator.start();
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHeaderView = findViewById(R.id.id_stickynavlayout_topview);
        mContainer = findViewById(R.id.container);
        mNav = findViewById(R.id.id_stickynavlayout_indicator);
        mViewPager = findViewById(R.id.id_stickynavlayout_viewpager);
        mRecycView = findViewById(R.id.id_stickynavlayout_recycview);

        mTopPadding = dp2px(getContext(), DEFAULT_TOP_PADDING);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //不限制顶部的高度
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        getChildAt(0).measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

        //设置Container布局高度
        ViewGroup.LayoutParams params = mContainer.getLayoutParams();
        params.height = getMeasuredHeight() - mTopPadding;

        //固定导航头高度
        int navHeight = 0;
        if(mNav != null){
            navHeight = mNav.getMeasuredHeight();
        }

        if(mViewPager != null) {
            params = mViewPager.getLayoutParams();
            params.height = getMeasuredHeight() - navHeight - mTopPadding;
        }else if(mRecycView != null){
            params = mRecycView.getLayoutParams();
            params.height = getMeasuredHeight() - navHeight - mTopPadding;

        }

        setMeasuredDimension(getMeasuredWidth(), mHeaderView.getMeasuredHeight() +
                mContainer.getMeasuredHeight());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMaxScrollDistance = mHeaderView.getMeasuredHeight() - mTopPadding;
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
        if(mListener != null && !isFulledHideHeader()){
            mListener.onStartScroll();
        }
        return true;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        Log.e(TAG, "onNestedScrollAccepted");
    }

    @Override
    public void onStopNestedScroll(View target) {
        //如果不是快速滑动则执行自动滚动
        if (!isFling) {
            /**
             * 1.头部已完全隐藏时继续向上快速滑动
             * 2.头部已完全显示时继续向下快速滑动
             */
            if (isFulledHideHeader() || isFulledShowHeader()) {
                return;
            }

            if (mType == SLIDING_ALL) {
                autoScroll(300);
            } else {
                autoScroll2(300);
            }
        } else {
            isFling = false;
        }
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        Log.e(TAG, "onNestedScroll");
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        boolean hiddenTop = isHiddenHeader(dy);
        boolean showTop = isShowHeader(target, dy);

        Log.e(TAG, hiddenTop + "::" + showTop);
        if (hiddenTop || showTop) {

            //头部折叠后不支持下拉展开头部
            if(showTop && !enablePullDown){
                return;
            }

            if (mType == SLIDING_ALL) {
                /**
                 * bug1：向上超快速滑动
                 * 检测targetY不能超过mMaxScrollDistance
                 * bug2：按住向上滑动，然后突然快速下滑
                 * 检测targetY不能小于0
                 */
                float targetY = getScrollY() + dy;
                if(targetY > mMaxScrollDistance){
                    targetY = mMaxScrollDistance;
                }
                if(targetY < 0){
                    targetY = 0;
                }
                scrollTo(0, (int) targetY);
            } else {
                /**
                 * 处理同上场景的bug
                 */
                float targetY = mContainer.getTranslationY() - dy;
                if(-targetY > mMaxScrollDistance) {
                    targetY = -mMaxScrollDistance;
                }
                if(targetY > 0){
                    targetY = 0;
                }
                mContainer.setTranslationY(targetY);
                //修改透明度
                setAlpha();
            }
            callListener();
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
    private boolean isFulledHideHeader() {
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
    private boolean isFulledShowHeader() {
        if (mType == SLIDING_ALL) {
            return getScrollY() <= 0;
        } else {
            return mContainer.getTranslationY() >= 0;
        }
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        isFling = true;

        /**
         * 1.头部已完全隐藏时继续向上快速滑动
         * 2.头部已完全显示时继续向下快速滑动
         */
        if (isFulledHideHeader() || isFulledShowHeader()) {
            return false;
        }

        if (target instanceof RecyclerView && velocityY < 0) {
            //如果是recyclerView 根据判断第一个元素是哪个位置可以判断是否消耗
            //这里判断如果第一个元素的位置是大于TOP_CHILD_FLING_THRESHOLD的
            //认为已经被消耗，在animateScroll里不会对velocityY<0时做处理
            final RecyclerView recyclerView = (RecyclerView) target;
            final View firstChild = recyclerView.getChildAt(0);
            final int childAdapterPosition = recyclerView.getChildAdapterPosition(firstChild);
            consumed = childAdapterPosition > TOP_CHILD_FLING_THRESHOLD;
        }

        if (mType == SLIDING_ALL) {
            if (!consumed) {
                flingScroll(velocityY, computeDuration(0), consumed);
            } else {
                flingScroll(velocityY, computeDuration(velocityY), consumed);
            }
        } else {
            if (!consumed) {
                flingScroll2(velocityY, computeDuration(0), consumed);
            } else {
                flingScroll2(velocityY, computeDuration(velocityY), consumed);
            }
        }

        return false;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        //不做拦截 可以传递给子View
        return false;
    }

    @Override
    public int getNestedScrollAxes() {
        Log.e(TAG, "getNestedScrollAxes");
        return 0;
    }

    /**
     * 根据速度计算滚动动画持续时间
     *
     * @param velocityY
     * @return
     */
    private int computeDuration(float velocityY) {
        int distance = 0;
        if(mType == SLIDING_ALL) {
            if (velocityY > 0) {//向上滑
                distance = Math.abs(mMaxScrollDistance - getScrollY());
            } else {//向下滑
                distance = Math.abs(getScrollY());
            }
        }else if(mType == SLIDING_CONTAINER){
            if (velocityY > 0) {
                distance = (int) Math.abs(mMaxScrollDistance + mContainer.getTranslationY());
            } else {
                distance = (int) Math.abs(mContainer.getTranslationY());
            }
        }

        final int duration;
        velocityY = Math.abs(velocityY);
        if (velocityY > 0) {
            duration = 3 * Math.round(1000 * (distance / velocityY));
        } else {
            final float distanceRatio = (float) distance / getHeight();
            duration = (int) ((distanceRatio + 1) * 150);
        }

        return duration;

    }

    private void autoScroll(int duration) {
        final int currentOffset = getScrollY();
        final int headerHeight = mHeaderView.getHeight();
        if (mOffsetAnimator == null) {
            mOffsetAnimator = new ValueAnimator();
            mOffsetAnimator.setInterpolator(mInterpolator);
            mOffsetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (animation.getAnimatedValue() instanceof Integer) {
                        int y = (Integer) animation.getAnimatedValue();
                        if (y != getScrollY()) {
                            scrollTo(0, y);
                            callListener();
                        }

                    }
                }
            });
        } else {
            mOffsetAnimator.cancel();
        }
        mOffsetAnimator.setDuration(duration);

        if (currentOffset < headerHeight / 2) {
            //显示头部
            mOffsetAnimator.setIntValues(currentOffset, 0);
            mOffsetAnimator.start();
        } else {
            //隐藏头部
            mOffsetAnimator.setIntValues(currentOffset, headerHeight - mTopPadding);
            mOffsetAnimator.start();
        }
    }

    private void autoScroll2(int duration) {
        final float currentOffset = mContainer.getTranslationY();
        final int headerHeight = mHeaderView.getHeight();
        if (mOffsetAnimator == null) {
            mOffsetAnimator = new ValueAnimator();
            mOffsetAnimator.setInterpolator(mInterpolator);
            mOffsetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (animation.getAnimatedValue() instanceof Float) {
                        float y = (Float) animation.getAnimatedValue();
                        if (y != mContainer.getTranslationY()) {
                            mContainer.setTranslationY(y);
                            setAlpha();//修改透明度
                            callListener();
                        }
                    }
                }
            });
        } else {
            mOffsetAnimator.cancel();
        }
        mOffsetAnimator.setDuration(duration);

        if (Math.abs(currentOffset) < headerHeight / 2) {
            //显示头部
            mOffsetAnimator.setFloatValues(currentOffset, 0);
            mOffsetAnimator.start();
        } else {
            //隐藏头部
            mOffsetAnimator.setFloatValues(currentOffset, -(headerHeight - mTopPadding));
            mOffsetAnimator.start();
        }
    }

    private void flingScroll(float velocityY, final int duration, boolean consumed) {
        final int currentOffset = getScrollY();
        final int headerHeight = mHeaderView.getHeight();
        if (mOffsetAnimator == null) {
            mOffsetAnimator = new ValueAnimator();
            mOffsetAnimator.setInterpolator(mInterpolator);
            mOffsetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (animation.getAnimatedValue() instanceof Integer) {
                        int y = (Integer) animation.getAnimatedValue();
                        if (y != getScrollY()) {
                            scrollTo(0, y);
                        }
                        callListener();
                    }
                }
            });
        } else {
            mOffsetAnimator.cancel();
        }
        mOffsetAnimator.setDuration(Math.min(duration, 400));

        if (velocityY >= 0) {
            //隐藏头部
            mOffsetAnimator.setIntValues(currentOffset, headerHeight - mTopPadding);
            mOffsetAnimator.start();
        } else {
            //如果子View没有消耗down事件 那么就让自身滑倒0位置
            if (!consumed) {
                //显示头部
                mOffsetAnimator.setIntValues(currentOffset, 0);
                mOffsetAnimator.start();
            }

        }
    }

    private void flingScroll2(float velocityY, final int duration, boolean consumed) {
        final float currentOffset = mContainer.getTranslationY();
        final int headerHeight = mHeaderView.getHeight();
        if (mOffsetAnimator == null) {
            mOffsetAnimator = new ValueAnimator();
            mOffsetAnimator.setInterpolator(mInterpolator);
            mOffsetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (animation.getAnimatedValue() instanceof Float) {
                        float y = (Float) animation.getAnimatedValue();
                        if (y != mContainer.getTranslationY()) {
                            mContainer.setTranslationY(y);
                            setAlpha();//修改透明度
                            callListener();
                        }
                    }
                }
            });
        } else {
            mOffsetAnimator.cancel();
        }
        mOffsetAnimator.setDuration(Math.min(duration, 400));

        if (velocityY >= 0) {
            //隐藏头部
            mOffsetAnimator.setFloatValues(currentOffset, -(headerHeight - mTopPadding));
            mOffsetAnimator.start();
        } else {
            //如果子View没有消耗down事件 那么就让自身滑倒0位置
            if (!consumed) {
                //显示头部
                mOffsetAnimator.setFloatValues(currentOffset, 0);
                mOffsetAnimator.start();
            }

        }
    }

    private void setAlpha(){
        float ratio = getScrollRatio();
        if(ratio < 0.5f){
            ratio = 0.5f;
        }
        //mHeaderView.setAlpha(ratio);//修改透明度
    }

    private void callListener() {
        if (mListener != null) {
            mListener.onScroll(getScrollRatio());
        }
    }

    /**
     * 获取滑动比率
     *
     * @return
     */
    private float getScrollRatio() {
        if (mType == SLIDING_ALL) {
            return 1.0f - getScrollY() * 1.0f / mMaxScrollDistance;
        } else {
            return 1.0f - Math.abs(mContainer.getTranslationY()) / mMaxScrollDistance;
        }
    }

    private int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }

    public interface ScrollListener {
        /**
         * 开始滚动
         */
        void onStartScroll();
        /**
         * 头部滚动回调
         * @param percentage 1:头部完全隐藏；0头部完全显示
         */
        void onScroll(float percentage);
    }
}
