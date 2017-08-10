/**
 * Copyright (C) 2016 fantianwen <twfan_09@hotmail.com>
 * <p>
 * also you can see {@link https://github.com/fantianwen/CircleProgressButton}
 */
package van.tian.wen.circleprogressbutton;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


/**
 * Round progress button
 */
public class CircleProgressButton extends View {

    private static final String TAG = "Milan";
    /**
     * Progress is increasing
     */
    private final static int PROGRESS_PLUS = 0;

    /**
     * Progress is decreasing
     */
    private final static int PROGRESS_REDUCE = 1;

    /**
     * The radius of the circle is reduced
     */
    private final static int RADIUS_PLUS = 2;

    /**
     * The radius of the circle increases
     */
    private final static int RADIUS_REDUCE = 3;

    private Context mContext;

    private static final long TIME_INTERVAL = 1;
    private Paint mPaint;
    private int mWidth, mHeight;
    private float sweepAngle;

    /**
     * Round color
     */
    private int mCircleColor;

    /**
     * The color of the progress bar
     */
    private int mProgressColor;

    /**
     * The width of the progress bar
     */
    private float mProgressWidth;

    private float mBouncedWidth;

    private float mAnimatedWidth;

    /**
     * The color of the text
     */
    private int mTextColor;

    /**
     * The size of the text
     */
    private float mTextSize;

    /**
     * End flag
     */
    private boolean isEnd;

    /**
     * After the hand is released, it is judged that there is no return to progress
     */
    private boolean isEndOk;

    /**
     * Press the animation end flag
     */
    private boolean isPressedOk;

    /**
     * Press to release the animation end flag
     */
    private boolean ifPressedBackOk;

    /**
     * The length of the long press
     */
    private float longTouchInterval;

    /**
     * The angle of the arc increases
     */
    private int everyIntervalAngle = 5;

    /**
     * Monitor the progress of the situation
     */
    private CircleProcessListener mCircleProcessListener;

    private int mSize;

    private int mRadius;
    private int mCircleRadius;
    private float timeInterval;
    private String mText;
    private Boolean mDrawArc = true;
    private BounceY mBounceY;

    int bouncedTime = 0;

    public float getLongTouchInterval() {
        return longTouchInterval;
    }


    public CircleProgressButton(Context context) {
        this(context, null);
    }

    public CircleProgressButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;

        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.CircleProgressButton);
        mCircleColor = typedArray.getColor(R.styleable.CircleProgressButton_circleColor, Color.WHITE);
        mProgressColor = typedArray.getColor(R.styleable.CircleProgressButton_progressColor, Color.BLUE);
        mProgressWidth = typedArray.getDimension(R.styleable.CircleProgressButton_progressWidth, CommonUtils.dip2px(mContext, 1f));
        mTextColor = typedArray.getColor(R.styleable.CircleProgressButton_textColor, Color.BLACK);
        mTextSize = typedArray.getDimension(R.styleable.CircleProgressButton_textSize, CommonUtils.sp2px(mContext, 1f));

        typedArray.recycle();

        init();
    }

    public void setText(String text) {
        this.mText = text;
        invalidate();
    }

    public void setDrawArc(Boolean drawArc) {
        this.mDrawArc = drawArc;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        mSize = (width > height ? height : width);
        mRadius = mSize / 2 - 30;
        mCircleRadius = mRadius - 20;

        setMeasuredDimension(mSize, mSize);

    }

    private Handler mLongPressedHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PROGRESS_PLUS:
                    isEnd = sweepAngle == 360;
                    if (isEnd) {
                        if (mCircleProcessListener != null) {
                            mCircleProcessListener.onFinished();
                        }
                        removeMessages(PROGRESS_PLUS);

                    }
                    sweepAngle += everyIntervalAngle;
                    invalidate();
                    sendEmptyMessageDelayed(PROGRESS_PLUS, TIME_INTERVAL);
                    break;
                case PROGRESS_REDUCE:
                    isEndOk = sweepAngle == 0;
                    if (!isEndOk) {
                        sweepAngle -= everyIntervalAngle;
                        invalidate();
                        sendEmptyMessageDelayed(PROGRESS_REDUCE, TIME_INTERVAL);
                    } else {
                        if (mCircleProcessListener != null) {
                            mCircleProcessListener.onCancelOk();
                        }
                        removeMessages(PROGRESS_REDUCE);
                    }

                    break;
                case RADIUS_PLUS:
                    isPressedOk = mBouncedWidth - mAnimatedWidth <= 0;
                    if (!isPressedOk) {
                        mAnimatedWidth += 0.5;
                        invalidate();
                        sendEmptyMessageDelayed(RADIUS_PLUS, 1);
                    } else {
                        removeMessages(RADIUS_PLUS);
                    }

                    break;
                case RADIUS_REDUCE:
                    ifPressedBackOk = mAnimatedWidth <= 0;

                    if (!ifPressedBackOk) {
                        mAnimatedWidth -= 0.5;
                        invalidate();
                        sendEmptyMessageDelayed(RADIUS_REDUCE, 1);
                    } else {
                        removeMessages(RADIUS_REDUCE);
                    }

                    break;
                default:
                    break;
            }

        }
    };


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        /**
         * Press the animation
         */
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (isEnd) {
                    sweepAngle = 0;
                }

                if (ifPressedBackOk) {
                    mLongPressedHandler.sendEmptyMessage(RADIUS_PLUS);
                }
                mLongPressedHandler.sendEmptyMessage(RADIUS_REDUCE);

                if (!isEndOk) {
                    if (mCircleProcessListener != null) {
                        mCircleProcessListener.onReStart();
                    }
                    mLongPressedHandler.removeMessages(PROGRESS_REDUCE);
                }

                mLongPressedHandler.sendEmptyMessage(PROGRESS_PLUS);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (isPressedOk) {
                    mLongPressedHandler.sendEmptyMessage(RADIUS_REDUCE);
                }
                mLongPressedHandler.sendEmptyMessage(RADIUS_PLUS);

                if (!isEnd) {
                    if (mCircleProcessListener != null) {
                        mCircleProcessListener.onCancel();
                    }
                    mLongPressedHandler.sendEmptyMessage(PROGRESS_REDUCE);
                }

                mLongPressedHandler.removeMessages(PROGRESS_PLUS);
                break;
        }

        return true;

    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mBouncedWidth = mProgressWidth / 2;

        mBounceY = new BounceY(mBouncedWidth);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(mWidth / 2, mHeight / 2);

        /**
         * Draw the progress bar
         */
        if (mDrawArc) {
            mPaint.setColor(mProgressColor);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mProgressWidth);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            RectF rectF = new RectF(-mRadius + mBouncedWidth + mAnimatedWidth, -mRadius + mBouncedWidth + mAnimatedWidth, mRadius - mBouncedWidth - mAnimatedWidth, mRadius - mBouncedWidth - mAnimatedWidth);
            canvas.drawArc(rectF, -90, sweepAngle, false, mPaint);
        }

        /**
         * Draw a circle
         */
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mCircleColor);
        canvas.drawCircle(0, 0, mCircleRadius - mAnimatedWidth, mPaint);

        /**
         * Painting text
         */
        mPaint.setColor(mTextColor);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(mTextSize);
        mPaint.setStrokeWidth(1f);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        Rect bounds = new Rect();
        mPaint.getTextBounds(mText, 0, mText.length(), bounds);
        Paint.FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
        int baseline = -(fontMetrics.ascent + fontMetrics.descent) / 2;

        /**
         * Where the second argument x is the coordinates of the midpoint of the text
         */
        canvas.drawText(mText, 0, baseline, mPaint);

    }

    public void setCircleProcessListener(CircleProcessListener circleProcessListener) {
        this.mCircleProcessListener = circleProcessListener;
    }

    public interface CircleProcessListener {

        /**
         * On progress finish
         */
        void onFinished();

        /**
         * On progress cancel
         */
        void onCancel();

        /**
         * Cancel the progress to 0
         */
        void onCancelOk();

        /**
         * On Progress restart
         */
        void onReStart();
    }


}

