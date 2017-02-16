package com.soundlover.exoplayertest.seekbar;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.soundlover.exoplayertest.R;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Created by Sergiy on 25.01.2017.
 */

@Accessors(prefix = "m")
public class CircularSeekBar extends View {

    private RectF mSeekOval;

    private int mViewWidth;
    private int mViewHeight;

    @Getter
    @Setter
    private Drawable mThumb;

    @Getter
    @Setter
    private int mMax = 180;

    @Getter
    @Setter
    private float mProgress;

    private int mProgressWidth = 10;
    private int mArcWidth = 10;

    private int mStartAngle = 180;
    private int mSweepAngle = 180;

    @Getter
    @Setter
    private boolean mEnabled = true;

    private Paint mProgressPaint;
    private Paint mArcPaint;

    private float mThumbXPos;
    private float mThumbYPos;

    private Path mArcPath;
    private Path mProgressPath;

    @Getter
    @Setter
    private OnArcSeekChangeListener mOnSeekArcChangeListener;

    @Getter
    @Setter
    private OnMultipleTouchListener mOnMultipleTouchListener;

    private PathMeasure mPathMeasure;

    private ArrayList<ArcPoint> mArcPoints;

    private float mTouchCircleX;
    private float mTouchCircleY;
    private float mTouchCircleRadius;
    private boolean isCircleTouchEnable;
    private boolean enableViewTouch;
    private int updateIterator;
    private Paint mThumbPaint;

    public CircularSeekBar(Context context) {
        super(context);
        init(context, null, 0);
    }

    public CircularSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public CircularSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {

        final Resources res = getResources();

        int arcColor = res.getColor(R.color.default_violet);
//        int thumbHalfheight = 0;
//        int thumbHalfWidth = 0;
        mThumb = res.getDrawable(R.drawable.seek_arc_control_selector);
        int progressColor = res.getColor(R.color.default_blue_light);
        int thumbColor = res.getColor(R.color.default_blue_dark);

        if (attrs != null) {
            // Attribute initialization
            final TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.SeekArc, defStyle, 0);

            Drawable thumb = a.getDrawable(R.styleable.SeekArc_thumb);
            if (thumb != null) {
                mThumb = thumb;
            }

//            thumbHalfheight = (int) mThumb.getIntrinsicHeight() / 2;
//            thumbHalfWidth = (int) mThumb.getIntrinsicWidth() / 2;
//            mThumb.setBounds(-mThumb.getIntrinsicWidth(), -mThumb.getIntrinsicHeight(), mThumb.getIntrinsicWidth(),
//                    mThumb.getIntrinsicHeight());

            mMax = a.getInteger(R.styleable.SeekArc_max, mMax);
            mProgress = a.getFloat(R.styleable.SeekArc_progress, mProgress);

            mArcWidth = a.getInt(R.styleable.SeekArc_seekArcWidth, mArcWidth);
            mProgressWidth = a.getInt(R.styleable.SeekArc_progressArcWidth, mProgressWidth);

            mStartAngle = a.getInt(R.styleable.SeekArc_startAngle, mStartAngle);
            mSweepAngle = a.getInt(R.styleable.SeekArc_sweepAngle, mSweepAngle);
            mEnabled = a.getBoolean(R.styleable.SeekArc_enabled, mEnabled);

            arcColor = a.getColor(R.styleable.SeekArc_arcColor, arcColor);
            progressColor = a.getColor(R.styleable.SeekArc_progressArcColor,
                    progressColor);

            a.recycle();
        }

        mProgress = (mProgress > mMax) ? mMax : mProgress;
        mProgress = (mProgress < 0) ? 0 : mProgress;

        mSweepAngle = (mSweepAngle > 360) ? 360 : mSweepAngle;
        mSweepAngle = (mSweepAngle < 0) ? 0 : mSweepAngle;

        mStartAngle = (mStartAngle > 360) ? 0 : mStartAngle;
        mStartAngle = (mStartAngle < 0) ? 0 : mStartAngle;

        initPaints(arcColor, progressColor, thumbColor);
    }

    private void initPaints(int arcColor, int progressColor, int thumbColor) {
        mArcPaint = new Paint();
        mArcPaint.setColor(arcColor);
        mArcPaint.setAntiAlias(true);
        mArcPaint.setMaskFilter(new BlurMaskFilter(mArcWidth * 1.5f, BlurMaskFilter.Blur.INNER));
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(mArcWidth);

        mProgressPaint = new Paint();
        mProgressPaint.setColor(progressColor);
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setMaskFilter(new BlurMaskFilter(mProgressWidth * 1.5f, BlurMaskFilter.Blur.SOLID));
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(mProgressWidth);

        mThumbPaint = new Paint();
        mThumbPaint.setColor(thumbColor);
        mThumbPaint.setMaskFilter(new BlurMaskFilter(15, BlurMaskFilter.Blur.SOLID));
        mThumbPaint.setAntiAlias(true);
        mThumbPaint.setStrokeWidth(10);
        mThumbPaint.setStyle(Paint.Style.FILL);
    }

    private void initTouchArea() {
        int c = mViewHeight / mViewWidth;
        isCircleTouchEnable = (c >= 1.5f);
        mTouchCircleX = mViewWidth / 2;
        mTouchCircleY = (c >= 2.3f) ? mArcPoints.get(0).getY() - (mViewWidth - getPaddingTop()) / 3.5f
                : mArcPoints.get(0).getY();

        mTouchCircleRadius = (mViewWidth + getPaddingLeft() + getPaddingRight()) / 3.5f;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h * 2;

        mSeekOval = new RectF(mArcWidth + getPaddingLeft(), mArcWidth + getPaddingTop(),
                mViewWidth - mArcWidth - getPaddingRight(), mViewHeight - mArcWidth - getPaddingBottom() * 3);

        mArcPath = new Path();
        mProgressPath = new Path();
        mArcPath.addArc(mSeekOval, mStartAngle, mSweepAngle);
        mPathMeasure = new PathMeasure(mArcPath, false);

        mArcPoints = new ArrayList<>();
        for (int i = 0; i < mPathMeasure.getLength(); i++) {
            mArcPoints.add(new ArcPoint(getPoint((float) i / mPathMeasure.getLength())[0],
                    getPoint((float) i / mPathMeasure.getLength())[1], (float) i / mPathMeasure.getLength()));
        }

        initTouchArea();

        mThumbXPos = getPoint((float) mProgress / getMax())[0];
        mThumbYPos = getPoint((float) mProgress / getMax())[1];

        setProgress(mProgress);

        invalidate();
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mThumb != null && mThumb.isStateful()) {
            int[] state = getDrawableState();
            mThumb.setState(state);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(mSeekOval, mStartAngle, mSweepAngle, false, mArcPaint);
        canvas.drawPath(mProgressPath, mProgressPaint);

        if (mEnabled) {
            canvas.drawCircle(mThumbXPos, mThumbYPos, 10, mThumbPaint);
        }
    }

    private void calcMinDistance(MotionEvent event) {
        float aX = getPoint(0f)[0];
        float aY = getPoint(0f)[1];

        float bX = getPoint(1f)[0];
        float bY = getPoint(1f)[1];

        boolean isSearch = true;

        int iterator = 0;
        int aPosition = 0;
        int bPosition = mArcPoints.size();
        float currentX;
        float currentY;

        while (isSearch) {
            float distanceX1 = (float) MathUtils.calcDistance(aX, aY, event.getX(), event.getY());
            float distanceX2 = (float) MathUtils.calcDistance(bX, bY, event.getX(), event.getY());

            iterator = (aPosition + bPosition) / 2;
            currentX = mArcPoints.get(iterator).getX();
            currentY = mArcPoints.get(iterator).getY();

            if (distanceX1 < distanceX2) {
                bX = currentX;
                bY = currentY;
                bPosition = iterator;
            } else if (Math.round(distanceX1) <= Math.round(distanceX2) + 2
                    && Math.round(distanceX1) >= Math.round(distanceX2) - 2) {
                mThumbXPos = currentX;
                mThumbYPos = currentY;
                updateProgress(mArcPoints.get(iterator).getPercent() * getMax(), true);
                isSearch = false;
            } else {
                aX = currentX;
                aY = currentY;
                aPosition = iterator;
            }
        }
        invalidate();
    }

    private float[] getPoint(float percent) {
        float aCoordinates[] = {0f, 0f};
        mPathMeasure.getPosTan(mPathMeasure.getLength() * percent, aCoordinates, null);
        return aCoordinates;
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mEnabled) {
            this.getParent().requestDisallowInterceptTouchEvent(true);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    onStartTrackingTouch();
                    setPressed(true);
                    enableViewTouch = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    boolean inCircle = MathUtils.hasTouchInCircle(event, mTouchCircleX, mTouchCircleY, mTouchCircleRadius);
                    if (isCircleTouchEnable && !inCircle && event.getY() <= mViewHeight) {
                        calcMinDistance(event);
                    } else if (!isCircleTouchEnable && event.getY() <= mViewHeight) {
                        calcMinDistance(event);
                    }
                    mOnMultipleTouchListener.onProgressTouched(OnMultipleTouchListener.ViewType.SEEK, mProgress / getMax());

                    break;
                case MotionEvent.ACTION_UP:
                    onStopTrackingTouch();
                    setPressed(false);
                    enableViewTouch = false;
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

    private void updateProgress(float progress, boolean fromUser) {

        progress = (progress > mMax) ? mMax : progress;
        progress = (progress < 0) ? 0 : progress;
        mProgress = progress;

        if (!fromUser && mArcPoints != null) {
            updateIterator = (int) (mProgress * mArcPoints.size() / getMax());
            if (updateIterator >= mArcPoints.size()) {
                return;
            }
            mThumbXPos = mArcPoints.get(updateIterator).getX();
            mThumbYPos = mArcPoints.get(updateIterator).getY();

        }

        if (mOnSeekArcChangeListener != null
                && mOnMultipleTouchListener != null) {
            mOnSeekArcChangeListener
                    .onProgressChanged(this, true, mProgress, fromUser);
            if (!fromUser) {
                mOnMultipleTouchListener.onProgressTouched(OnMultipleTouchListener.ViewType.SEEK, mProgress / getMax());
            }
        }

        if (mProgressPath != null) {
            mProgressPath.reset();
            mPathMeasure.setPath(mArcPath, false);
            mPathMeasure.getSegment(0, mPathMeasure.getLength() * progress / getMax(), mProgressPath, true);
            mProgressPath.rLineTo(0, 0);
        }

        invalidate();
    }

    public void setProgress(float progress) {
        updateProgress(progress, false);
    }

    public void setArcWidth(int mArcWidth) {
        this.mArcWidth = mArcWidth;
        mArcPaint.setStrokeWidth(mArcWidth);
    }

    public void setProgressWidth(int mProgressWidth) {
        this.mProgressWidth = mProgressWidth;
        mProgressPaint.setStrokeWidth(mProgressWidth);
    }

    public void setStartAngle(int mStartAngle) {
        this.mStartAngle = mStartAngle;
//        updateThumbPosition();
    }

    public void setSweepAngle(int mSweepAngle) {
        this.mSweepAngle = mSweepAngle;
//        updateThumbPosition();
    }

    public void setProgressColor(int color) {
        mProgressPaint.setColor(color);
        invalidate();
    }

    public void setArcColor(int color) {
        mArcPaint.setColor(color);
        invalidate();
    }

    public void setTouchEvent(float progress) {
        enableViewTouch = false;
        mThumbXPos = getPoint(progress / getMax())[0];
        mThumbYPos = getPoint(progress / getMax())[1];
        updateProgress(progress, true);
    }
}
