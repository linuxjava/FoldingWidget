package xiao.free.folding.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
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
    private static final int DEFAULT_TOP_PADDING = 0;
    private static final int TYPE_ALL = 1;
    private static final int TYPE_CONTAINER = 2;
    private int mType = TYPE_CONTAINER;
    private boolean isFling = false;//是否快速滑动
    private int mTopPadding = 0;//滑动到顶部的padding
    private View mHeaderView;//头部View
    private View mContainer;//底部内容容器
    private View mNav;//固定导航栏
    private ViewGroup mViewPager;
    private int mMaxScrollDistance;//最大滚动距离headerViewHeight-mTopPadding
    private ValueAnimator mOffsetAnimator;
    private Interpolator mInterpolator;
    private ScrollListener mListener;

    public StickyNavLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(LinearLayout.VERTICAL);

        mInterpolator = new AccelerateInterpolator();
    }

    public void setListener(ScrollListener mListener) {
        this.mListener = mListener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHeaderView = findViewById(R.id.id_stickynavlayout_topview);
        mContainer = findViewById(R.id.container);
        mNav = findViewById(R.id.id_stickynavlayout_indicator);
        View view = findViewById(R.id.id_stickynavlayout_viewpager);
        if (!(view instanceof ViewPager)) {
            throw new RuntimeException(
                    "id_stickynavlayout_viewpager show used by ViewPager !");
        }
        mViewPager = (ViewPager) view;

        mTopPadding = dp2px(getContext(), DEFAULT_TOP_PADDING);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //不限制顶部的高度
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        getChildAt(0).measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

        ViewGroup.LayoutParams params = mContainer.getLayoutParams();
        params.height = getMeasuredHeight() - mTopPadding;

        params = mViewPager.getLayoutParams();
        params.height = getMeasuredHeight() - mNav.getMeasuredHeight() - mTopPadding;

        setMeasuredDimension(getMeasuredWidth(), mHeaderView.getMeasuredHeight() +
                mNav.getMeasuredHeight() + mViewPager.getMeasuredHeight());
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

            if (mType == TYPE_ALL) {
                autoScroll();
            } else {
                autoScroll2();
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
            if (mType == TYPE_ALL) {
                scrollBy(0, dy);
            } else {
                mContainer.setTranslationY(mContainer.getTranslationY() - dy);
                //修改透明度
                mHeaderView.setAlpha(getScrollRatio());
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
        if (mType == TYPE_ALL) {
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
        if (mType == TYPE_ALL) {
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
        if (mType == TYPE_ALL) {
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
        if (mType == TYPE_ALL) {
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

        if (mType == TYPE_ALL) {
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
        final int distance;
        if (velocityY > 0) {
            distance = Math.abs(mHeaderView.getHeight() - getScrollY());
        } else {
            distance = Math.abs(mHeaderView.getHeight() - (mHeaderView.getHeight() - getScrollY()));
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

    private void autoScroll() {
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
        mOffsetAnimator.setDuration(300);

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

    private void autoScroll2() {
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
                            mHeaderView.setAlpha(getScrollRatio());//修改透明度
                            callListener();
                        }
                    }
                }
            });
        } else {
            mOffsetAnimator.cancel();
        }
        mOffsetAnimator.setDuration(300);

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
                            mHeaderView.setAlpha(getScrollRatio());//修改透明度
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
        if (mType == TYPE_ALL) {
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
        void onScroll(float ratio);
    }
}
