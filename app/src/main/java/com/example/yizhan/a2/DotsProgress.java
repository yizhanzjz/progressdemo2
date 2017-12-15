package com.example.yizhan.a2;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by yizhan on 2017/12/13.
 */

public class DotsProgress extends View {

    private Paint mPaint;

    private int mDotColor = 0xFF091FB1;//默认的画笔颜色
    private int mDotAlpha = 128;//默认的点的最浅的颜色
    private float mMinRadius = 5;//点最小时的半径，默认为5
    private float mMaxRadius = 8;//点最大时的半径，默认为8
    private float mSpace = 3;//两个点之间的间距

    //屏幕的宽高
    private int mWidth;
    private int mHeight;

    //动画相关变量
    private ValueAnimator mValueAnimator;
    private float mCurrentValue;//动画变化时某一刻的值
    private int mDuration = 1000;

    //为了简化处理，动画正在运行时不允许更改参数
    private boolean isAnimationRunning = false;//当前动画是否在运行

    //画笔
    private Paint mTextPaint;
    private int mTextColor = Color.BLACK;
    private String mText = "";
    private int mTextSize = 14;

    //点与文本之间的间距
    private int mDivider = 10;

    public DotsProgress(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {

        //初始化点的画笔
        mPaint = new Paint();
        //画笔模式这里设置成填充，因为画的是点，不是圆形
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);//抗锯齿
        mPaint.setColor(mDotColor);//设置画笔颜色，也就是点的颜色

        //初始化画文字的画笔
        mTextPaint = new Paint();
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(getResources().getDisplayMetrics().scaledDensity * mTextSize);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        mValueAnimator = getValueAnimator();
    }

    private ValueAnimator getValueAnimator() {
        //初始化一个点的动画
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 3);
        //设置动画值变化的监听
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrentValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(mDuration);

        return valueAnimator;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //获取控件的宽高
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mMinRadius <= 0) {
            mMinRadius = 5;
        }

        if (mMaxRadius <= 0) {
            mMaxRadius = 8;
        }

        //将当前控件坐标系原点移到控件的中心位置
        canvas.translate(mWidth / 2, mHeight / 2);

        //3个点及其间距的总长度
        float dotsLength = 3 * (mMaxRadius * 2 + mSpace) - mSpace;
        //文本的长度
        Rect rect = new Rect();
        mTextPaint.getTextBounds(mText, 0, mText.length(), rect);
        float rectLength = rect.right - rect.left;
        //3个点及文本的总长度
        float length = dotsLength + rectLength + mDivider;

        //以此画三个点
        for (int i = 0; i < 3; i++) {

            float currentValue = 0f;

            switch (i) {
                case 0:
                    if (mCurrentValue >= 0 && mCurrentValue < 1) {
                        currentValue = mCurrentValue;
                    } else if (mCurrentValue >= 1 && mCurrentValue <= 2) {
                        currentValue = 1 - 0.5f * mCurrentValue;
                    } else {
                        currentValue = 0;
                    }

                    mPaint.setAlpha((int) (mDotAlpha + (255 - mDotAlpha) * currentValue + 0.5));
                    canvas.drawCircle(-length / 2 + (2 * i + 1) * mMaxRadius + i * mSpace, 0,
                            mMinRadius + (mMaxRadius - mMinRadius) * currentValue, mPaint);

                    break;
                case 1:
                    if (mCurrentValue >= 0.5 && mCurrentValue < 1.5) {
                        currentValue = mCurrentValue - 0.5f;
                    } else if (mCurrentValue >= 1.5 && mCurrentValue <= 2.5) {
                        currentValue = 1 - 0.5f * (mCurrentValue - 0.5f);
                    } else {
                        currentValue = 0;
                    }

                    mPaint.setAlpha((int) (mDotAlpha + (255 - mDotAlpha) * currentValue + 0.5));
                    canvas.drawCircle(-length / 2 + (2 * i + 1) * mMaxRadius + i * mSpace, 0,
                            mMinRadius + (mMaxRadius - mMinRadius) * currentValue, mPaint);

                    break;
                case 2:
                    if (mCurrentValue >= 1 && mCurrentValue < 2) {
                        currentValue = mCurrentValue - 1f;
                    } else if (mCurrentValue >= 2 && mCurrentValue <= 3) {
                        currentValue = 1 - 0.5f * (mCurrentValue - 1f);
                    } else {
                        currentValue = 0;
                    }

                    mPaint.setAlpha((int) (mDotAlpha + (255 - mDotAlpha) * currentValue + 0.5));
                    canvas.drawCircle(-length / 2 + (2 * i + 1) * mMaxRadius + i * mSpace, 0,
                            mMinRadius + (mMaxRadius - mMinRadius) * currentValue, mPaint);
                    break;
            }
        }

        if (!TextUtils.isEmpty(mText)) {
            Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
            canvas.drawText(mText, -length / 2 + dotsLength + mDivider, (-fontMetrics.top - fontMetrics.bottom) / 2, mTextPaint);
        }
    }


    /**
     * 设置的点的颜色
     * 需在调用startProgress之前调用
     */
    public void setDotsColor(int dotsColor) {
        if (!isAnimationRunning) {
            this.mDotColor = dotsColor;
            mPaint.setColor(dotsColor);
        }
    }

    /**
     * 设置点最浅的颜色，0~255，默认128
     * 需在调用startProgress之前调用
     */
    public void setDotAlpha(int dotAlpha) {
        if (!isAnimationRunning) {
            this.mDotAlpha = dotAlpha;
        }
    }

    /**
     * 设置点最小时的半径，单位为px，默认为5
     * 需在调用startProgress之前调用
     */
    public void setMinRadius(float minRadius) {
        if (!isAnimationRunning) {
            this.mMinRadius = minRadius;
        }
    }

    /**
     * 设置点最大时的半径，单位为px，默认为8
     * 需在调用startProgress之前调用
     */
    public void setMaxRadius(float maxRadius) {
        if (!isAnimationRunning) {
            this.mMaxRadius = maxRadius;
        }
    }

    /**
     * 设置两个点之间的间距，单位为px，默认为0
     * 需在调用startProgress之前调用
     */
    public void setSpace(float space) {
        if (!isAnimationRunning) {
            this.mSpace = space;
        }
    }


    /**
     * 设置单个点从小点到大点变化的时间，这个时间也是单个点从大点到小点变化的时间
     * 需在调用startProgress之前调用
     */
    public void setAnimationDuration(int duration) {
        if (!isAnimationRunning) {
            this.mDuration = duration;
            mValueAnimator.setDuration(duration);
        }
    }

    /**
     * 设置文本颜色
     * 需在调用startProgress之前调用
     */
    public void setTextColor(int textColor) {
        if (!isAnimationRunning) {
            this.mTextColor = textColor;
            mTextPaint.setColor(textColor);
        }
    }


    /**
     * 设置文本
     * 需在调用startProgress之前调用
     */
    public void setText(String text) {
        if (!isAnimationRunning) {
            this.mText = text;
        }
    }


    /**
     * 设置文本，单位为sp
     * 需在调用startProgress之前调用
     */
    public void setTextSize(int textSize) {
        if (!isAnimationRunning) {
            this.mTextSize = textSize;
            mTextPaint.setTextSize(getResources().getDisplayMetrics().scaledDensity * textSize);
        }
    }

    /**
     * 设置文本与点之间的间距，单位为px
     * 需在调用startProgress之前调用
     */
    public void setDivider(int divider) {

        //异常处理
        if (divider < 0) {
            divider = 0;
        }

        if (!isAnimationRunning) {
            this.mDivider = divider;
        }
    }


    /**
     * 开启
     */
    public void startProgress() {
        isAnimationRunning = true;
        mValueAnimator.start();
    }

    /**
     * 结束
     */
    public void endProgress() {
        isAnimationRunning = false;
        mValueAnimator.end();
    }

}
