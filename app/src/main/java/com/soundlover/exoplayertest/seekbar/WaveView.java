package com.soundlover.exoplayertest.seekbar;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.soundlover.exoplayertest.R;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Created by Sergiy on 27.01.2017.
 */

@Accessors(prefix = "m")
public class WaveView extends View {

    private Paint mWavePaint;
    private Rect mWaveProgress;

    @Getter
    @Setter
    private float mWaveProgressWidth = 0;

    @Getter
    @Setter
    private int mProgressColor;

    @Getter
    @Setter
    private int mMax = 100;

    @Getter
    @Setter
    private float mProgress;

    @Getter
    @Setter
    private OnArcSeekChangeListener mOnSeekArcChangeListener;

    private int mViewWidth;
    private int mViewHeight;

    @Getter
    @Setter
    private boolean isEnabled;

    @Getter
    @Setter
    private Bitmap mWaveBitmap;

    @Getter
    @Setter
    private Drawable mWaveDrawable;

    @Getter
    @Setter
    private OnMultipleTouchListener mOnMultipleTouchListener;

    private boolean enableViewTouch;

    public WaveView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public WaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        final Resources res = getResources();

        mProgressColor = res.getColor(R.color.default_blue_transparent);

        mWaveDrawable = getResources().getDrawable(R.drawable.soundwave);
        mWaveBitmap = ((BitmapDrawable) mWaveDrawable).getBitmap();

        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.WaveView, defStyle, 0);

            mMax = a.getInteger(R.styleable.WaveView_max, mMax);
            mWaveDrawable = a.getDrawable(R.styleable.WaveView_waveSrc);
            mProgress = a.getFloat(R.styleable.WaveView_progress, mProgress);
            mWaveProgressWidth = a.getFloat(R.styleable.WaveView_progressWidth, mWaveProgressWidth);

            mProgressColor = a.getColor(R.styleable.WaveView_progressColor, mProgressColor);

            isEnabled = a.getBoolean(R.styleable.WaveView_enabled, true);

            a.recycle();
        }

        initPaints();
    }

    private void initPaints() {
        mWavePaint = new Paint();
        mWavePaint.setColor(mProgressColor);
        mWavePaint.setAntiAlias(true);
        mWavePaint.setStyle(Paint.Style.FILL);
        mWavePaint.setStrokeWidth(0);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;

        mWaveProgress = new Rect(0, 0, (int) (mProgress * mViewWidth / getMax()), mViewHeight);
        mWaveDrawable.setBounds(0, 0, mViewWidth, mViewHeight);
        setProgress(mProgress / getMax());

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mWaveDrawable.draw(canvas);
        canvas.drawRect(mWaveProgress, mWavePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isEnabled) {
            this.getParent().requestDisallowInterceptTouchEvent(true);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    onStartTrackingTouch();
                    setPressed(true);
                    enableViewTouch = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    updateProgress(event, event.getX() / mViewWidth, true);
                    mOnMultipleTouchListener.onProgressTouched(OnMultipleTouchListener.ViewType.WAVE, mProgress);
                    break;
                case MotionEvent.ACTION_UP:
                    onStopTrackingTouch();
                    enableViewTouch = false;
                    setPressed(false);
                    this.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    onStopTrackingTouch();
                    enableViewTouch = false;
                    setPressed(false);
                    this.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return enableViewTouch;
        }
        return false;
    }

    private void updateProgress(MotionEvent event, float progress, boolean fromUser) {

        progress = (progress > mMax) ? mMax : progress;
        progress = (progress < 0) ? 0 : progress;
        mProgress = progress * getMax();
        if (mOnSeekArcChangeListener != null
                && mOnMultipleTouchListener != null) {
            mOnSeekArcChangeListener
                    .onProgressChanged(this, false, mProgress, fromUser);
            if (!fromUser) {
                mOnMultipleTouchListener.onProgressTouched(OnMultipleTouchListener.ViewType.WAVE, mProgress);
            }
        }

        if (mWaveProgress != null) {
            if (event != null) {
                mWaveProgress.set(0, 0, (int) (event.getX()), mViewHeight);
            } else {
                mWaveProgress.set(0, 0, (int) (mProgress * mViewWidth / getMax()), mViewHeight);
            }
        }

        invalidate();
    }

    private void onStartTrackingTouch() {
        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener.onStartTrackingTouch(this);
        }
    }

    private void onStopTrackingTouch() {
        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener.onStopTrackingTouch(this);
        }
    }

    public void setProgress(float progress) {
        updateProgress(null, progress, false);
    }

    public void setProgressWidth(int width) {
        this.mWaveProgressWidth = width;
        mWavePaint.setStrokeWidth(mWaveProgressWidth);
    }

    public void setProgressColor(int color) {
        mWavePaint.setColor(color);
        invalidate();
    }

    public void setTouchEvent(float progress) {
        enableViewTouch = false;
        updateProgress(null, progress, true);
    }
}
