package com.lxs.simpleloopbanner;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

import java.lang.ref.WeakReference;
import java.util.List;

public class SimpleLoopBanner extends RelativeLayout {

    public SimpleLoopBanner(Context context) {
        this(context, null);
    }

    public SimpleLoopBanner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleLoopBanner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mIsAutoLoop && !mIsSingleImg) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    stopAutoPlay();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_OUTSIDE:
                    startAutoPlay();
                    break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAutoPlayHandler != null) {
            mAutoPlayHandler.removeCallbacksAndMessages(null);
        }
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SimpleLoopBanner);
        mPointsIsVisible = a.getBoolean(R.styleable.SimpleLoopBanner_points_visibility, true);
        mPointLayoutPosition = a.getInt(R.styleable.SimpleLoopBanner_points_position, CENTER);
        mPointContainerBackgroundDrawable = a.getDrawable(R.styleable.SimpleLoopBanner_points_container_background);
        a.recycle();
        setLayout(context);
    }

    private void setLayout(Context context) {
        // 去掉ViewPager两侧的over scroll效果
        setOverScrollMode(OVER_SCROLL_NEVER);
        //设置指示器背景
        if (mPointContainerBackgroundDrawable == null) {
            //noinspection SpellCheckingInspection
            mPointContainerBackgroundDrawable = new ColorDrawable(Color.parseColor("#00AAAAAA"));
        }
        //添加ViewPager
        mViewPager = new ViewPager(context);
        addView(mViewPager, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        //设置指示器背景容器
        RelativeLayout pointContainerRl = new RelativeLayout(context);
        if (Build.VERSION.SDK_INT >= 16) {
            pointContainerRl.setBackground(mPointContainerBackgroundDrawable);
        } else {
            pointContainerRl.setBackgroundDrawable(mPointContainerBackgroundDrawable);
        }
        //设置内边距
        int dp8 = dp2px(8);
        int dp4 = dp2px(4);
        int dp16 = dp2px(16);
        pointContainerRl.setPadding(dp8, dp4, dp16, dp4);
        //设定指示器容器布局及位置
        LayoutParams pointContainerLp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        pointContainerLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        addView(pointContainerRl, pointContainerLp);
        //设置指示器容器
        mPointRealContainerLl = new LinearLayout(context);
        mPointRealContainerLl.setOrientation(LinearLayout.HORIZONTAL);
        mPointRealContainerLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mPointRealContainerLp.bottomMargin = dp2px(7);
        pointContainerRl.addView(mPointRealContainerLl, mPointRealContainerLp);
        //设置指示器容器是否可见
        if (mPointRealContainerLl != null) {
            if (mPointsIsVisible) {
                mPointRealContainerLl.setVisibility(View.VISIBLE);
            } else {
                mPointRealContainerLl.setVisibility(View.GONE);
            }
        }
        //设置指示器布局位置
        if (mPointLayoutPosition == CENTER) {
            mPointRealContainerLp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        } else if (mPointLayoutPosition == LEFT) {
            mPointRealContainerLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        } else if (mPointLayoutPosition == RIGHT) {
            mPointRealContainerLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }
    }

    // 设置本地图片
    public void setImages(List<Integer> images) {
        //加载本地图片
        mIsNetImage = false;
        this.mImages = images;
        if (images.size() <= 1)
            mIsSingleImg = true;
        //初始化ViewPager
        initViewPager();
    }

    // 设置网络图片
    public void setImagesUrl(List<String> urls) {
        //加载网络图片
        mIsNetImage = true;
        this.mImageUrls = urls;
        if (urls.size() <= 1)
            mIsSingleImg = true;
        //初始化ViewPager
        initViewPager();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    private void initViewPager() {
        //当图片多于1张时添加指示点
        if (!mIsSingleImg) {
            addPoints();
        }
        //设置ViewPager
        mViewPager.setAdapter(new BannerPageAdapter());
        mViewPager.addOnPageChangeListener(mOnPageChangeListener);
        //跳转到首页
        mViewPager.setCurrentItem(1, false);
        //当图片多于1张时开始轮播
        if (!mIsSingleImg) {
            startAutoPlay();
        }
    }

    // 添加指示点
    private void addPoints() {
        mPointRealContainerLl.removeAllViews();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        int dp8 = dp2px(8);
        lp.setMargins(dp8, dp8, dp8, dp8);
        ImageView imageView;
        int length = mIsNetImage ? mImageUrls.size() : mImages.size();
        for (int i = 0; i < length; i++) {
            imageView = new ImageView(getContext());
            imageView.setLayoutParams(lp);
            imageView.setImageResource(R.drawable.simple_loop_banner_selector);
            mPointRealContainerLl.addView(imageView);
        }

        switchToPoint(0);
    }

    public void setPointsIsVisible(boolean isVisible) {
        if (mPointRealContainerLl != null) {
            if (isVisible) {
                mPointRealContainerLl.setVisibility(View.VISIBLE);
            } else {
                mPointRealContainerLl.setVisibility(View.GONE);
            }
        }
    }

    // 切换指示器
    private void switchToPoint(final int currentPoint) {
        for (int i = 0; i < mPointRealContainerLl.getChildCount(); i++) {
            mPointRealContainerLl.getChildAt(i).setEnabled(false);
        }
        mPointRealContainerLl.getChildAt(currentPoint).setEnabled(true);

    }

    public void setPointsPosition(int position) {
        //设置指示器布局位置
        if (position == CENTER) {
            mPointRealContainerLp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        } else if (position == LEFT) {
            mPointRealContainerLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        } else if (position == RIGHT) {
            mPointRealContainerLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }
    }

    // 开始播放
    public void startAutoPlay() {
        if (mIsAutoLoop && !mIsLooping) {
            mIsLooping = true;
            mAutoPlayHandler.sendEmptyMessageDelayed(HANDLER_MSG_WHAT, mLoopInterval);
        }
    }

    // 停止播放
    public void stopAutoPlay() {
        if (mIsAutoLoop && mIsLooping) {
            mIsLooping = false;
            mAutoPlayHandler.removeMessages(HANDLER_MSG_WHAT);
        }
    }

    // 返回真实的位置
    private int toRealPosition(int position) {
        int realPosition;
        if (mIsNetImage) {
            realPosition = (position - 1) % mImageUrls.size();
            if (realPosition < 0)
                realPosition += mImageUrls.size();
        } else {
            realPosition = (position - 1) % mImages.size();
            if (realPosition < 0)
                realPosition += mImages.size();
        }

        return realPosition;
    }

    private int dp2px(int dp) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private static class RefHandler extends Handler {
        private WeakReference<SimpleLoopBanner> bannerRef;

        RefHandler(SimpleLoopBanner banner) {
            bannerRef = new WeakReference<>(banner);
        }

        @Override
        public void handleMessage(Message msg) {
            SimpleLoopBanner banner = bannerRef.get();
            if (banner == null) {
                return;
            }
            banner.mCurrentPosition++;
            banner.mViewPager.setCurrentItem(banner.mCurrentPosition);
            banner.mAutoPlayHandler.sendEmptyMessageDelayed(HANDLER_MSG_WHAT, banner.mLoopInterval);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private class BannerPageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            //当只有一张图片时返回1
            if (mIsSingleImg) {
                return 1;
            }
            //当为网络图片，返回网页图片长度
            if (mIsNetImage)
                return mImageUrls.size() + 2;
            //当为本地图片，返回本地图片长度
            return mImages.size() + 2;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {
            ImageView imageView = new ImageView(getContext());
            imageView.setOnClickListener(view -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(toRealPosition(position));
                }
            });
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if (mIsNetImage) {
                Glide.with(getContext()).load(mImageUrls.get(toRealPosition(position))).into(imageView);
            } else {
                imageView.setImageResource(mImages.get(toRealPosition(position)));
            }
            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

    private final static int HANDLER_MSG_WHAT = 1;
    private final static int DEFAULT_LOOP_INTERVAL_MILLIS = 3500;

    // Point布局位置
    public static final int CENTER = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;

    // 指示器点的容器，水平的线性布局
    private LinearLayout mPointRealContainerLl;
    // ViewPager
    private ViewPager mViewPager;
    //本地图片资源
    private List<Integer> mImages;
    //网络图片资源
    private List<String> mImageUrls;
    //是否是网络图片
    private boolean mIsNetImage = false;
    //是否只有一张图片
    private boolean mIsSingleImg = false;
    //是否自动播放
    private boolean mIsAutoLoop = true;
    //是否播放中
    private boolean mIsLooping = false;
    // 播放时间间隔
    private int mLoopInterval = DEFAULT_LOOP_INTERVAL_MILLIS;
    //当前页面位置
    private int mCurrentPosition;
    //指示点位置
    private int mPointLayoutPosition = CENTER;
    //指示容器背景
    private Drawable mPointContainerBackgroundDrawable;
    //指示容器布局规则
    private LayoutParams mPointRealContainerLp;
    //指示点是否可见
    private boolean mPointsIsVisible = true;

    private Handler mAutoPlayHandler = new RefHandler(this);

    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset,
                                   int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            if (mIsNetImage) {
                mCurrentPosition = position % (mImageUrls.size() + 2);
            } else {
                mCurrentPosition = position % (mImages.size() + 2);
            }
            switchToPoint(toRealPosition(mCurrentPosition));
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                int current = mViewPager.getCurrentItem();
                int lastReal = mViewPager.getAdapter().getCount() - 2;
                if (current == 0) {
                    mViewPager.setCurrentItem(lastReal, false);
                } else if (current == lastReal + 1) {
                    mViewPager.setCurrentItem(1, false);
                }
            }
        }
    };

    private OnItemClickListener mOnItemClickListener;

}