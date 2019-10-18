package com.github.rkhusainov.speedometerview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SpeedometerView extends View {

    public static final float STROKE_WIDTH = 64;
    public static final int DEFAULT_MAX_SPEED = 360;
    private static final int DEFAULT_COLOR_INDICATOR = Color.GRAY;
    private static final int DEFAULT_COLOR_LOW = Color.BLUE;
    private static final int DEFAULT_COLOR_MIDDLE = Color.GREEN;
    private static final int DEFAULT_COLOR_HIGH = Color.RED;
    private static final int DEFAULT_COLOR_TEXT = Color.DKGRAY;

    private int mCurrentSpeed;
    private int mMaxSpeed;
    private int mLowSpeedColor;
    private int mMiddleSpeedColor;
    private int mHighSpeedColor;
    private int mIndicatorColor;
    private int mTextSize;

    private RectF mProgressRect = new RectF();
    private Path mBackgroundScalePath = new Path();
    private Path mIndicatorScalePath = new Path();

    private Paint mBackScalePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Rect mTextBounds = new Rect();

    private Paint mArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public SpeedometerView(Context context) {
        super(context);
        init(context, null);
    }

    public SpeedometerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SpeedometerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        final Resources.Theme theme = context.getTheme();
        final TypedArray typedArray = theme.obtainStyledAttributes(attrs,
                R.styleable.SpeedometerView, 0, 0);

        // Default values
//        final TypedArray typedArray = theme.obtainStyledAttributes(attrs,
//                R.styleable.SpeedometerView, R.attr.speedometerProgressStyle, R.style.SpeedometerDefaultProgressStyle);

        try {
            mMaxSpeed = typedArray.getInteger(R.styleable.SpeedometerView_max_speed, DEFAULT_MAX_SPEED);
            mLowSpeedColor = typedArray.getColor(R.styleable.SpeedometerView_color, DEFAULT_COLOR_LOW);
            mMiddleSpeedColor = typedArray.getColor(R.styleable.SpeedometerView_color, DEFAULT_COLOR_MIDDLE);
            mHighSpeedColor = typedArray.getColor(R.styleable.SpeedometerView_color, DEFAULT_COLOR_HIGH);
            mIndicatorColor = typedArray.getColor(R.styleable.SpeedometerView_color_indicator, DEFAULT_COLOR_INDICATOR);

            mTextSize = typedArray.getDimensionPixelSize(R.styleable.SpeedometerView_textSize,
                    getResources().getDimensionPixelSize(R.dimen.default_text_size));

        } finally {
            typedArray.recycle();
        }
        initDrawingTools();
    }

    private void initDrawingTools() {
        mBackScalePaint.setStrokeWidth(STROKE_WIDTH);
        mBackScalePaint.setStyle(Paint.Style.STROKE);
        mBackScalePaint.setColor(mIndicatorColor);

        mIndicatorPaint.setStrokeWidth(STROKE_WIDTH);
        mIndicatorPaint.setStyle(Paint.Style.STROKE);
        mIndicatorPaint.setColor(currentColor());

        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(DEFAULT_COLOR_TEXT);

        mArrowPaint.setStrokeWidth(10f);
        mArrowPaint.setStyle(Paint.Style.STROKE);
        mArrowPaint.setColor(currentColor());
    }


    private int currentColor() {
        if (mCurrentSpeed >= 0 && mCurrentSpeed <= 60) {
            return mLowSpeedColor;
        } else if (mCurrentSpeed > 60 && mCurrentSpeed <= 140) {
            return mMiddleSpeedColor;
        } else {
            return mHighSpeedColor;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(STROKE_WIDTH/2, STROKE_WIDTH/2);

        mProgressRect.left = getPaddingLeft();
        mProgressRect.top = getPaddingTop();
        mProgressRect.right = getWidth() - STROKE_WIDTH - getPaddingRight();
        mProgressRect.bottom = getHeight() - STROKE_WIDTH - getPaddingBottom();

        // scale
        drawScaleBackground(canvas);
        // indicator
        drawScaleIndicator(canvas);
        // text
        drawText(canvas);
        // arrow
        drawArrow(canvas);
    }

    private void drawArrow(Canvas canvas) {
        double radius = mProgressRect.height() / 2;
        double angle = (2 * Math.PI) / mMaxSpeed * mCurrentSpeed + 2 * Math.PI / 2;
        float x = (float) (radius * Math.cos(angle));
        float y = (float) (radius * Math.sin(angle));
        canvas.drawLine(mProgressRect.centerX(), mProgressRect.centerY(), x + mProgressRect.centerX(), y + mProgressRect.centerY(), mArrowPaint);
    }

    private void drawScaleBackground(Canvas canvas) {
        for (int i = -180; i < 0; i += 4) {
            mBackgroundScalePath.addArc(mProgressRect, i, 2f);
        }
        canvas.drawPath(mBackgroundScalePath, mBackScalePaint);
    }

    private void drawScaleIndicator(Canvas canvas) {
        mIndicatorScalePath.reset();
        for (int i = -180; i < mCurrentSpeed - 180; i += 4) {
            mIndicatorScalePath.addArc(mProgressRect, i, 2f);
        }
        canvas.drawPath(mIndicatorScalePath, mIndicatorPaint);
    }

    public void changeSpeed(int currentSpeed) {
        mCurrentSpeed = currentSpeed;
        mIndicatorPaint.setColor(currentColor());
        mArrowPaint.setColor(currentColor());
        invalidate();
    }

    private void drawText(Canvas canvas) {
        final String speedString = String.format(getResources().getString(R.string.speed_template), mCurrentSpeed * 2);
        final String maxSpeedString = String.format(getResources().getString(R.string.speed_template), mMaxSpeed);
        mTextPaint.getTextBounds(speedString, 0, speedString.length(), mTextBounds);
        float x = mProgressRect.width() / 2f - mTextBounds.width() / 2f - mTextBounds.left+ mProgressRect.left;
        float y = mProgressRect.height() / 2f + mTextBounds.height() / 2f - mTextBounds.bottom+ mProgressRect.top;
        canvas.drawText(speedString, x, y + 50, mTextPaint);
        canvas.drawText(maxSpeedString, x, y + 150, mTextPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final String maxSpeedString = String.format(getResources().getString(R.string.speed_template), mMaxSpeed);
        mTextPaint.getTextBounds(maxSpeedString, 0, maxSpeedString.length(), mTextBounds);

        int requestedSize = (int) (Math.max(mTextBounds.width(), mTextBounds.height()) + Math.PI * STROKE_WIDTH);
        final int suggestedMinimumSize = Math.max(getSuggestedMinimumWidth(), getSuggestedMinimumHeight());
        requestedSize = Math.max(requestedSize, suggestedMinimumSize);

        final int desireWidth = requestedSize + getPaddingLeft() + getPaddingRight();
        final int desireHeight = requestedSize + getPaddingTop() + getPaddingBottom();
        final int resolveWidth = resolveSize(desireWidth, widthMeasureSpec);
        final int resolveHeight = resolveSize(desireHeight, heightMeasureSpec);

        final int resolvedSize = Math.min(resolveHeight, resolveWidth);

        setMeasuredDimension(resolvedSize, resolvedSize);
    }
}
