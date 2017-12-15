package com.htsmart.wristband.sample.syncdata;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.htsmart.wristband.bean.SyncRawData;
import com.htsmart.wristband.sample.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * 睡眠日数据的图
 * Created by taowencong on 15-11-18.
 */
public class SleepDayView extends View {
    private static final int INVALID_POSITION = -1;
    private DrawParams mDrawParams;
    private SleepDayData[] mDayDatas;
    private ActiveRectParams mAnimRectParams;

    private int mAnimIndex;

    private GestureDetector mDetector;

    public SleepDayView(Context context) {
        super(context);
        init();
    }

    public SleepDayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SleepDayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SleepDayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mDrawParams = new DrawParams();
        // 创建手势检测器
        mDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                processClick(e);
                return super.onSingleTapUp(e);
            }

        });
        setLongClickable(true);
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    processClick(event);
                }
                return mDetector.onTouchEvent(event);
            }
        });
    }

    private void processClick(MotionEvent e) {
        if (!mDrawParams.isValid() || mDayDatas == null || mDayDatas.length == 0)
            return;//无效，和OnDraw判断一致

        float x = e.getX();
        float y = e.getY();
        if (x < mOffsetX || x > getWidth() - mOffsetX ||
                y < mOffsetY || y > getHeight() - mOffsetY) {//不在矩形区域内点击的
            return;
        }
        /*判断点击在哪一个矩形*/
        int clickIndex = -1;
        float centerX = 0;
        float width = 0;
        float startX = mOffsetX;
        for (int i = 0; i < mDayDatas.length; i++) {
            SleepDayData data = mDayDatas[i];
            width = mRectWidth * data.percent;
            if (x >= startX && x <= startX + width) {
                clickIndex = i;
                centerX = startX + width / 2;
                break;
            }
            startX += width;
        }
        if (clickIndex == INVALID_POSITION) return;//没有找到(容错判断)
        if (clickIndex == mAnimIndex) return;//和最后一次执行动画的Index一样，那么不用在执行了

        /*开始执行这一次的动画*/
        mAnimIndex = clickIndex;

        if (mAnimRectParams == null) {
            mAnimRectParams = new ActiveRectParams();
        }
        mAnimRectParams.mCenterX = centerX;
        mAnimRectParams.mRectColor = mDrawParams.getRectColorWithValue(mDayDatas[clickIndex].value);
        mAnimRectParams.mRectWidth = width;
        mAnimRectParams.mRectHeight = mActiveHeight;//最终高度为激活高度
        mAnimRectParams.mTextAlpha = 255;//最终的alpha为255
        mAnimRectParams.mStartTime = mDayDatas[clickIndex].startTime;
        mAnimRectParams.mEndTime = mDayDatas[clickIndex].endTime;

        invalidate();
    }

    private float mOffsetX;
    private float mOffsetY;
    private float mRectWidth;
    private float mRectHeight;
    private float mActiveHeight;

    private Date mTempDate = new Date();

    public static final SimpleDateFormat HourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!mDrawParams.isValid() || mDayDatas == null || mDayDatas.length == 0) return;

        /*计算X坐标绘制数据*/
        float offsetX = (getWidth() - mDrawParams.mActualWidth) / 2.0f;//矩形图在X坐标上的偏移量
        /*因为要绘制文字，保证文字不超出视图所以要调整这个偏移量*/
        float textMaxHalfWidth = mDrawParams.mSmallTextWidth > mDrawParams.mLargeTextWidth ? mDrawParams.mSmallTextWidth / 2.0f : mDrawParams.mLargeTextWidth / 2.0f;
        if (offsetX < textMaxHalfWidth) {
            offsetX = textMaxHalfWidth;
        }
        float rectWidth = getWidth() - offsetX * 2;//矩形的宽度

        /*计算Y坐标绘制数据*/
        float offsetY = (getHeight() - mDrawParams.mActualHeight) / 2.0f;
        float fitHeight = mDrawParams.mTextHeight * 2 + mDrawParams.mVerticalSpace * 2;//已固定元素的高度(上下文字，间距)
        float activeHeight = mDrawParams.mActualHeight - fitHeight;//剩下的高度，需要分配给激活的矩形区域，矩形图的按照比例缩小
        float rectHeight = activeHeight / DrawParams.ACTIVE_RATIO;////矩形的高度
        offsetY += ((mDrawParams.mActualHeight - rectHeight) / 2.0f);

        float startX = offsetX;
        float p = 0;
        for (int i = 0; i < mDayDatas.length; i++) {
            SleepDayData data = mDayDatas[i];
            float width = rectWidth * data.percent;
            canvas.drawRect(startX, offsetY, startX + width, offsetY + rectHeight, mDrawParams.getRectPaintWithValue(data.value));
            startX += width;
            p += data.percent;
        }

        //绘制起始时刻
        mTempDate.setTime(mDayDatas[0].startTime * 1000L);
        String startText = HourFormat.format(mTempDate);
        canvas.drawText(startText, offsetX - mDrawParams.mLargeTextWidth / 2.0f, (getHeight() - mDrawParams.mActualHeight) / 2 + mDrawParams.mActualHeight, mDrawParams.mPaintLarge);

        mTempDate.setTime(mDayDatas[mDayDatas.length - 1].endTime * 1000L);
        String endText = HourFormat.format(mTempDate);
        canvas.drawText(endText, offsetX + rectWidth - mDrawParams.mLargeTextWidth / 2.0f, (getHeight() - mDrawParams.mActualHeight) / 2 + mDrawParams.mActualHeight, mDrawParams.mPaintLarge);

        //赋值下，用于事件判断的全局变量(这些在其他地方都可以算的出来，直接赋值，免的其他地方算了)
        mOffsetX = offsetX;
        mOffsetY = offsetY;
        mRectWidth = rectWidth;
        mRectHeight = rectHeight;
        mActiveHeight = activeHeight;

        if (mAnimRectParams != null) {//绘制激活的矩阵
            canvas.drawRect(mAnimRectParams.mCenterX - mAnimRectParams.mRectWidth / 2,
                    getHeight() / 2 - mAnimRectParams.mRectHeight / 2,
                    mAnimRectParams.mCenterX + mAnimRectParams.mRectWidth / 2,
                    getHeight() / 2 + mAnimRectParams.mRectHeight / 2,
                    mDrawParams.getRectPaintWithColor(mAnimRectParams.mRectColor)
            );
            mTempDate.setTime(mAnimRectParams.mStartTime * 1000L);
            startText = HourFormat.format(mTempDate);
            mTempDate.setTime(mAnimRectParams.mEndTime * 1000L);
            endText = HourFormat.format(mTempDate);
            String timeText = startText + "-" + endText;

            mDrawParams.mPaintSmall.setAlpha(mAnimRectParams.mTextAlpha);
            canvas.drawText(timeText, mAnimRectParams.mCenterX - mDrawParams.mSmallTextWidth / 2, (getHeight() - mDrawParams.mActualHeight) / 2 + mDrawParams.mTextHeight, mDrawParams.mPaintSmall);
        }
    }


    private class ActiveRectParams {
        float mCenterX;
        float mRectWidth;
        float mRectHeight;
        int mRectColor;
        int mTextAlpha;
        int mStartTime;
        int mEndTime;

    }

    /**
     * 设置睡眠数据
     *
     * @param datas datas传入之前，必须要根据时间从小到大排序好
     */
    public void setSleepDayDatas(SleepDayData[] datas) {
        if (mDayDatas == datas) return;

        mAnimRectParams = null;
        mAnimIndex = INVALID_POSITION;

        mDayDatas = datas;

        postInvalidate();
    }


    private class DrawParams {

        private static final int MAX_WIDTH = 680;
        private static final int MAX_HEIGHT = 340;

        private static final int VERTICAL_SPACE = 5;//dp垂直方向上，几个元素的距离
        static final float ACTIVE_RATIO = 1.2f;//激活的矩阵与未激活的比例

        int mActualWidth;//防止图形过大，给予的限制，绘制在正中间
        int mActualHeight;//防止图形过大，给予的限制，绘制在正中间

        Paint mPaintLarge;
        Paint mPaintSmall;

        int mSmallTextWidth;
        int mLargeTextWidth;
        int mTextHeight;//因为除了两个文字大小不一样，图形几乎对称，所以这里将两个文字高度统一，作为对称处理，这样容易处理

        int mColor1;
        int mColor2;
        int mColor3;

        Paint mRectPaint;

        float mVerticalSpace;

        Paint getRectPaintWithValue(int value) {
            if (value == SyncRawData.SLEEP_STATUS_DEEP) {
                mRectPaint.setColor(mColor1);
            } else if (value == SyncRawData.SLEEP_STATUS_SHALLOW) {
                mRectPaint.setColor(mColor2);
            } else if (value == SyncRawData.SLEEP_STATUS_SOBER) {
                mRectPaint.setColor(mColor3);
            }
            return mRectPaint;
        }

        int getRectColorWithValue(int value) {
            if (value == SyncRawData.SLEEP_STATUS_DEEP) {
                return mColor1;
            } else if (value == SyncRawData.SLEEP_STATUS_SHALLOW) {
                return mColor2;
            } else if (value == SyncRawData.SLEEP_STATUS_SOBER) {
                return mColor3;
            }
            return mColor1;
        }

        Paint getRectPaintWithColor(int color) {
            mRectPaint.setColor(color);
            return mRectPaint;
        }

        public DrawParams() {
            mPaintLarge = new Paint();
            mPaintLarge.setAntiAlias(true);
            mPaintLarge.setDither(true);
            mPaintLarge.setColor(0xff676767);
            mPaintLarge.setTextSize(getResources().getDimensionPixelSize(R.dimen.text_size_small));
            mLargeTextWidth = (int) mPaintLarge.measureText("00:00");
            mTextHeight = (int) (-mPaintLarge.getFontMetrics().ascent);

            mPaintSmall = new Paint();
            mPaintSmall.setAntiAlias(true);
            mPaintSmall.setDither(true);
            mPaintSmall.setColor(0xff676767);
            mPaintSmall.setTextSize(getResources().getDimensionPixelSize(R.dimen.text_size_very_smallest));
            mSmallTextWidth = (int) mPaintSmall.measureText("00:00-00:00");
            //            mSmallTextHeight = (int) (mPaintSmall.getFontMetrics().descent - mPaintSmall.getFontMetrics().ascent);


            mColor1 = getResources().getColor(R.color.sleep_level_1_color);
            mColor2 = getResources().getColor(R.color.sleep_level_2_color);
            mColor3 = getResources().getColor(R.color.sleep_level_3_color);

            mRectPaint = new Paint();
            mRectPaint.setAntiAlias(true);
            mRectPaint.setDither(true);

            mVerticalSpace = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, VERTICAL_SPACE, getResources().getDisplayMetrics());
        }

        public void updateSize(int width, int height) {
            //            mActualWidth = width > MAX_WIDTH ? MAX_WIDTH : width;
            //            mActualHeight = height > MAX_HEIGHT ? MAX_HEIGHT : height;
            mActualWidth = width;
            mActualHeight = height;
        }

        boolean isValid() {
            return mActualWidth != 0 && mActualHeight != 0;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        mDrawParams.updateSize(width, height);
    }
}
