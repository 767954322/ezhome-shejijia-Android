package com.autodesk.shejijia.consumer.tools.watch.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;

import com.autodesk.shejijia.consumer.R;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.applyDimension;

/**
 * @author he.liu .
 * @version v1.0 .
 * @date 2016-8-4 .
 * @file CustomRatingBar.java .
 * @brief A simple RatingBar for Android. .
 */
public class CustomRatingBar extends View {

    /**
     * Represents gravity of the fill in the bar.
     */
    public enum Gravity {
        /**
         * Left gravity is default: the bar will be filled starting from left to right.
         */
        Left(0),
        /**
         * Right gravity: the bar will be filled starting from right to left.
         */
        Right(1);

        int id;

        Gravity(int id) {
            this.id = id;
        }

        static Gravity fromId(int id) {
            for (Gravity f : values()) {
                if (f.id == id) return f;
            }
            // default value
            Log.w("SimpleRatingBar", String.format("Gravity chosen is neither 'left' nor 'right', I will set it to Left"));
            return Left;
        }
    }

    // Configurable variables
    private
    @ColorInt
    int borderColor;
    private
    @ColorInt
    int fillColor;
    private
    @ColorInt
    int backgroundColor;
    private int numberOfStars;
    private float starsSeparation;
    private float starSize;
    private float maxStarSize;
    private float stepSize;
    private float rating;
    private boolean isIndicator;
    private Gravity gravity_1;
    private float starBorderWidth;

    // Internal variables
    private Paint paintStarBorder;
    private Paint paintStarFill;
    private Paint paintBackground;
    private Path path;
    private float defaultStarSize;
    private ValueAnimator ratingAnimator;
    private OnRatingBarChangeListener listener;
    private boolean touchInProgress;
    private float[] starVertex;
    private RectF starsDrawingSpace;
    private RectF starsTouchSpace;

    // in order to delete some drawing, and keep transparency
    // http://stackoverflow.com/a/21865858/2271834
    private Canvas internalCanvas;
    private Bitmap internalBitmap;

    public CustomRatingBar(Context context) {
        super(context);
        initView();
    }

    public CustomRatingBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttrs(attrs);
        initView();
    }

    public CustomRatingBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parseAttrs(attrs);
        initView();
    }

    /**
     * Inits paint objects and default values.
     */
    private void initView() {
        paintStarBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        path = new Path();
        paintStarBorder.setAntiAlias(true);
        paintStarBorder.setDither(true);
        paintStarBorder.setStyle(Paint.Style.STROKE);
        paintStarBorder.setStrokeJoin(Paint.Join.ROUND);
        paintStarBorder.setStrokeCap(Paint.Cap.ROUND);
        paintStarBorder.setPathEffect(new CornerPathEffect(6));
        paintStarBorder.setStrokeWidth(starBorderWidth);
        paintStarBorder.setColor(borderColor);

        paintBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBackground.setStyle(Paint.Style.FILL_AND_STROKE);
        paintBackground.setStrokeWidth(1);
        paintBackground.setColor(backgroundColor);
        if (backgroundColor == Color.TRANSPARENT) {
            paintBackground.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }

        paintStarFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintStarFill.setStyle(Paint.Style.FILL_AND_STROKE);
        paintStarFill.setStrokeWidth(0);
        paintStarFill.setColor(fillColor);

        defaultStarSize = applyDimension(COMPLEX_UNIT_DIP, 25, getResources().getDisplayMetrics());
    }

    /**
     * Parses attributes defined in XML.
     */
    private void parseAttrs(AttributeSet attrs) {
        TypedArray arr = getContext().obtainStyledAttributes(attrs, R.styleable.SimpleRatingBar);

        borderColor = arr.getColor(R.styleable.SimpleRatingBar_borderColor, getResources().getColor(R.color.golden_stars));
        fillColor = arr.getColor(R.styleable.SimpleRatingBar_fillColor, borderColor);
        backgroundColor = arr.getColor(R.styleable.SimpleRatingBar_backgroundColor, Color.TRANSPARENT);
        numberOfStars = arr.getInteger(R.styleable.SimpleRatingBar_numberOfStars, 5);

        float starsSeparationDp = arr.getDimension(R.styleable.SimpleRatingBar_starsSeparation, 4);
        starsSeparation = applyDimension(COMPLEX_UNIT_DIP, starsSeparationDp, getResources().getDisplayMetrics());
        maxStarSize = arr.getDimensionPixelSize(R.styleable.SimpleRatingBar_maxStarSize, Integer.MAX_VALUE);
        starSize = arr.getDimensionPixelSize(R.styleable.SimpleRatingBar_starSize, Integer.MAX_VALUE);
        stepSize = arr.getFloat(R.styleable.SimpleRatingBar_stepSize, Float.MAX_VALUE);
        starBorderWidth = arr.getInteger(R.styleable.SimpleRatingBar_starBorderWidth, 5);

        rating = normalizeRating(arr.getFloat(R.styleable.SimpleRatingBar_rating, 0f));
        isIndicator = arr.getBoolean(R.styleable.SimpleRatingBar_isIndicator, false);
        gravity_1 = Gravity.fromId(arr.getInt(R.styleable.SimpleRatingBar_gravity_1, Gravity.Left.id));

        arr.recycle();

        validateAttrs();
    }

    /**
     * Validates parsed attributes. It will throw IllegalArgumentException if severe inconsistency is found.
     * Warnings will be logged to LogCat.
     */
    private void validateAttrs() {
        if (numberOfStars <= 0) {
            throw new IllegalArgumentException(String.format("SimpleRatingBar initialized with invalid value for numberOfStars. Found %d, but should be greater than 0", numberOfStars));
        }
        if (starSize != Integer.MAX_VALUE && maxStarSize != Integer.MAX_VALUE && starSize > maxStarSize) {
            Log.w("SimpleRatingBar", String.format("Initialized with conflicting values: starSize is greater than maxStarSize (%f > %f). I will ignore maxStarSize", starSize, maxStarSize));
        }
        if (stepSize <= 0) {
            throw new IllegalArgumentException(String.format("SimpleRatingBar initialized with invalid value for stepSize. Found %f, but should be greater than 0", stepSize));
        }
        if (starBorderWidth <= 0) {
            throw new IllegalArgumentException(String.format("SimpleRatingBar initialized with invalid value for starBorderWidth. Found %f, but should be greater than 0",
                    starBorderWidth));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        Drawable d = getCurrentDrawable();
//
//        int thumbHeight = mThumb == null ? 0 : mThumb.getIntrinsicHeight();
//        int dw = 0;
//        int dh = 0;
//        if (d != null) {
//            dw = Math.max(mMinWidth, Math.min(mMaxWidth, d.getIntrinsicWidth()));
//            dh = Math.max(mMinHeight, Math.min(mMaxHeight, d.getIntrinsicHeight()));
//            dh = Math.max(thumbHeight, dh);
//        }
//        dw += mPaddingLeft + mPaddingRight;
//        dh += mPaddingTop + mPaddingBottom;
//
//        setMeasuredDimension(resolveSizeAndState(dw, widthMeasureSpec, 0),
//                resolveSizeAndState(dh, heightMeasureSpec, 0));

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            if (starSize != Integer.MAX_VALUE) {
                // user specified a specific star size, so there is a desired width
                int desiredWidth = calculateTotalWidth(starSize, numberOfStars, starsSeparation, true);
                width = Math.min(desiredWidth, widthSize);
            } else if (maxStarSize != Integer.MAX_VALUE) {
                // user specified a max star size, so there is a desired width
                int desiredWidth = calculateTotalWidth(maxStarSize, numberOfStars, starsSeparation, true);
                width = Math.min(desiredWidth, widthSize);
            } else {
                // using defaults
                int desiredWidth = calculateTotalWidth(defaultStarSize, numberOfStars, starsSeparation, true);
                width = Math.min(desiredWidth, widthSize);
            }
        } else {
            //Be whatever you want
            if (starSize != Integer.MAX_VALUE) {
                // user specified a specific star size, so there is a desired width
                int desiredWidth = calculateTotalWidth(starSize, numberOfStars, starsSeparation, true);
                width = desiredWidth;
            } else if (maxStarSize != Integer.MAX_VALUE) {
                // user specified a max star size, so there is a desired width
                int desiredWidth = calculateTotalWidth(maxStarSize, numberOfStars, starsSeparation, true);
                width = desiredWidth;
            } else {
                // using defaults
                int desiredWidth = calculateTotalWidth(defaultStarSize, numberOfStars, starsSeparation, true);
                width = desiredWidth;
            }
        }

        float tentativeStarSize = (width - getPaddingLeft() - getPaddingRight() - starsSeparation * (numberOfStars - 1)) / numberOfStars;

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            if (starSize != Integer.MAX_VALUE) {
                // user specified a specific star size, so there is a desired width
                int desiredHeight = calculateTotalHeight(starSize, numberOfStars, starsSeparation, true);
                height = Math.min(desiredHeight, heightSize);
            } else if (maxStarSize != Integer.MAX_VALUE) {
                // user specified a max star size, so there is a desired width
                int desiredHeight = calculateTotalHeight(maxStarSize, numberOfStars, starsSeparation, true);
                height = Math.min(desiredHeight, heightSize);
            } else {
                // using defaults
                int desiredHeight = calculateTotalHeight(tentativeStarSize, numberOfStars, starsSeparation, true);
                height = Math.min(desiredHeight, heightSize);
            }
        } else {
            //Be whatever you want
            if (starSize != Integer.MAX_VALUE) {
                // user specified a specific star size, so there is a desired width
                int desiredHeight = calculateTotalHeight(starSize, numberOfStars, starsSeparation, true);
                height = desiredHeight;
            } else if (maxStarSize != Integer.MAX_VALUE) {
                // user specified a max star size, so there is a desired width
                int desiredHeight = calculateTotalHeight(maxStarSize, numberOfStars, starsSeparation, true);
                height = desiredHeight;
            } else {
                // using defaults
                int desiredHeight = calculateTotalHeight(tentativeStarSize, numberOfStars, starsSeparation, true);
                height = desiredHeight;
            }
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        int width = getWidth();
        int height = getHeight();
        if (starSize == Integer.MAX_VALUE) {
            starSize = calculateBestStarSize(width, height);
        }
        performStarSizeAssociatedCalculations(width, height);
    }

    /**
     * Calculates largest possible star size, based on chosen width and height.
     * If maxStarSize is present, it will be considered and star size will not be greater than this value.
     *
     * @param width
     * @param height
     */
    private float calculateBestStarSize(int width, int height) {
        if (maxStarSize != Integer.MAX_VALUE) {
            float desiredTotalWidth = calculateTotalWidth(maxStarSize, numberOfStars, starsSeparation, true);
            float desiredTotalHeight = calculateTotalHeight(maxStarSize, numberOfStars, starsSeparation, true);
            if (desiredTotalWidth >= width || desiredTotalHeight >= height) {
                // we need to shrink the size of the stars
                float sizeBasedOnWidth = (width - getPaddingLeft() - getPaddingRight() - starsSeparation * (numberOfStars - 1)) / numberOfStars;
                float sizeBasedOnHeight = height - getPaddingTop() - getPaddingBottom();
                return Math.min(sizeBasedOnWidth, sizeBasedOnHeight);
            } else {
                return maxStarSize;
            }
        } else {
            // expand the most we can
            float sizeBasedOnWidth = (width - getPaddingLeft() - getPaddingRight() - starsSeparation * (numberOfStars - 1)) / numberOfStars;
            float sizeBasedOnHeight = height - getPaddingTop() - getPaddingBottom();
            return Math.min(sizeBasedOnWidth, sizeBasedOnHeight);
        }
    }

    /**
     * Performs auxiliary calculations to later speed up drawing phase.
     *
     * @param width
     * @param height
     */
    private void performStarSizeAssociatedCalculations(int width, int height) {
        float totalStarsWidth = calculateTotalWidth(starSize, numberOfStars, starsSeparation, false);
        float totalStarsHeight = calculateTotalHeight(starSize, numberOfStars, starsSeparation, false);
        float startingX = (width - getPaddingLeft() - getPaddingRight()) / 2 - totalStarsWidth / 2 + getPaddingLeft();
        float startingY = (height - getPaddingTop() - getPaddingBottom()) / 2 - totalStarsHeight / 2 + getPaddingTop();
        starsDrawingSpace = new RectF(startingX, startingY, startingX + totalStarsWidth, startingY + totalStarsHeight);
        float aux = starsDrawingSpace.width() * 0.05f;
        starsTouchSpace = new RectF(starsDrawingSpace.left - aux, starsDrawingSpace.top, starsDrawingSpace.right + aux, starsDrawingSpace.bottom);

        float bottomFromMargin = starSize * 0.2f;
        float triangleSide = starSize * 0.35f;
        float half = starSize * 0.5f;
        float tipVerticalMargin = starSize * 0.05f;
        float tipHorizontalMargin = starSize * 0.03f;
        float innerUpHorizontalMargin = starSize * 0.38f;
        float innerBottomHorizontalMargin = starSize * 0.32f;
        float innerBottomVerticalMargin = starSize * 0.55f;
        float innerCenterVerticalMargin = starSize * 0.27f;

        starVertex = new float[]{
                tipHorizontalMargin, innerUpHorizontalMargin, // top left
                tipHorizontalMargin + triangleSide, innerUpHorizontalMargin, half, tipVerticalMargin, // top tip
                starSize - tipHorizontalMargin - triangleSide, innerUpHorizontalMargin,
                starSize - tipHorizontalMargin, innerUpHorizontalMargin, // top right
                starSize - innerBottomHorizontalMargin, innerBottomVerticalMargin,
                starSize - bottomFromMargin, starSize - tipVerticalMargin, // bottom right
                half, starSize - innerCenterVerticalMargin, bottomFromMargin, starSize - tipVerticalMargin, // bottom left
                innerBottomHorizontalMargin, innerBottomVerticalMargin, tipHorizontalMargin,
                innerUpHorizontalMargin, // top left
        };
    }

    /**
     * Calculates total width to occupy based on several parameters
     *
     * @param starSize
     * @param numberOfStars
     * @param starsSeparation
     * @param padding
     * @return
     */
    private int calculateTotalWidth(float starSize, int numberOfStars, float starsSeparation, boolean padding) {
        return Math.round(starSize * numberOfStars + starsSeparation * (numberOfStars - 1))
                + (padding ? getPaddingLeft() + getPaddingRight() : 0);
    }

    /**
     * Calculates total height to occupy based on several parameters
     *
     * @param starSize
     * @param numberOfStars
     * @param starsSeparation
     * @param padding
     * @return
     */
    private int calculateTotalHeight(float starSize, int numberOfStars, float starsSeparation, boolean padding) {
        return Math.round(starSize) + (padding ? getPaddingTop() + getPaddingBottom() : 0);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (internalBitmap != null) {
            // avoid leaking memory after losing the reference
            internalBitmap.recycle();
        }

        if (w > 0 && h > 0) {
            // if width == 0 or height == 0 we don't need internal bitmap, cause view won't be drawn anyway.
            internalBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            internalBitmap.eraseColor(Color.TRANSPARENT);
            internalCanvas = new Canvas(internalBitmap);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int height = getHeight();
        int width = getWidth();

        if (width == 0 || height == 0) {
            // don't draw view with width or height equal zero.
            return;
        }

        // reset internal canvas
        internalCanvas.drawColor(Color.argb(0, 0, 0, 0));

        if (gravity_1 == Gravity.Left) {
            drawFromLeftToRight(internalCanvas);
        } else {
            drawFromRightToLeft(internalCanvas);
        }

        // draw internal bitmap to definite canvas
        canvas.drawBitmap(internalBitmap, 0, 0, null);
    }

    /**
     * Draws the view when gravity is Left
     *
     * @param internalCanvas
     */
    private void drawFromLeftToRight(Canvas internalCanvas) {
        float remainingTotalRating = getRatingToDraw();
        float startingX = starsDrawingSpace.left;
        float startingY = starsDrawingSpace.top;
        for (int i = 0; i < numberOfStars; i++) {
            if (remainingTotalRating >= 1) {
                drawStar(internalCanvas, startingX, startingY, 1f, Gravity.Left);
                remainingTotalRating -= 1;
            } else {
                drawStar(internalCanvas, startingX, startingY, remainingTotalRating, Gravity.Left);
                remainingTotalRating = 0;
            }
            startingX += starSize;
            if (i < numberOfStars - 1) {
                drawSeparator(internalCanvas, startingX, startingY);
                startingX += starsSeparation;
            }
        }
    }

    /**
     * Draws the view when gravity is Right
     *
     * @param internalCanvas
     */
    private void drawFromRightToLeft(Canvas internalCanvas) {
        float remainingTotalRating = getRatingToDraw();
        float startingX = starsDrawingSpace.right - starSize;
        float startingY = starsDrawingSpace.top;
        for (int i = 0; i < numberOfStars; i++) {
            if (remainingTotalRating >= 1) {
                drawStar(internalCanvas, startingX, startingY, 1f, Gravity.Right);
                remainingTotalRating -= 1;
            } else {
                drawStar(internalCanvas, startingX, startingY, remainingTotalRating, Gravity.Right);
                remainingTotalRating = 0;
            }
            if (i < numberOfStars - 1) {
                startingX -= starsSeparation;
                drawSeparator(internalCanvas, startingX, startingY);
            }
            startingX -= starSize;
        }
    }

    /**
     * Calculates rating to draw based on current step size.
     *
     * @return
     */
    private float getRatingToDraw() {
        if (stepSize != Float.MAX_VALUE) {
            return rating - (rating % stepSize);
        } else {
            return rating;
        }
    }

    /**
     * Draws a star in the provided canvas.
     *
     * @param canvas
     * @param x       left of the star
     * @param y       top of the star
     * @param filled  between 0 and 1
     * @param gravity Left or Right
     */
    private void drawStar(Canvas canvas, float x, float y, float filled, Gravity gravity) {
        // draw fill of star
        float fill = starSize * filled;
        if (gravity == Gravity.Left) {
            canvas.drawRect(x, y, x + fill, y + starSize, paintStarFill);
            canvas.drawRect(x + fill, y, x + starSize, y + starSize, paintBackground);
        } else {
            canvas.drawRect(x, y, x + starSize - fill, y + starSize, paintBackground);
            canvas.drawRect(x + starSize - fill, y, x + starSize, y + starSize, paintStarFill);
        }

        // clean outside of star
        path.reset();
        path.moveTo(x + starVertex[0], y + starVertex[1]);
        for (int i = 2; i < starVertex.length - 2; i = i + 2) {
            path.lineTo(x + starVertex[i], y + starVertex[i + 1]);
        }
        path.lineTo(x, y + starVertex[starVertex.length - 3]); // reach the closest border
        path.lineTo(x, y + starSize); // bottom left corner
        path.lineTo(x + starSize, y + starSize); // bottom right corner
        path.lineTo(x + starSize, y); // top right corner
        path.lineTo(x, y); // top left corner
        path.lineTo(x, y + starVertex[1]);
        path.close();
        canvas.drawPath(path, paintBackground);

        // finish clean up outside
        path.reset();
        path.moveTo(x + starVertex[0], y + starVertex[1]);
        path.lineTo(x, y + starVertex[1]);
        path.lineTo(x, y + starVertex[starVertex.length - 5]);
        path.lineTo(x + starVertex[starVertex.length - 4], y + starVertex[starVertex.length - 3]);
        path.close();
        canvas.drawPath(path, paintBackground);

        // draw star border on top
        path.reset();
        path.moveTo(x + starVertex[0], y + starVertex[1]);
        for (int i = 2; i < starVertex.length; i = i + 2) {
            path.lineTo(x + starVertex[i], y + starVertex[i + 1]);
        }
        path.close();
        canvas.drawPath(path, paintStarBorder);
    }

    /**
     * Draws separator in the provided canvas.
     *
     * @param canvas
     * @param x
     * @param y
     */
    private void drawSeparator(Canvas canvas, float x, float y) {
        canvas.drawRect(x, y, x + starsSeparation, y + starSize, paintBackground);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isIndicator || (ratingAnimator != null && ratingAnimator.isRunning())) {
            return false;
        }

        int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
//                if (isInScrollingContainer()) {
//                    mTouchDownX = event.getX();
//                } else {
//                    setPressed(true);
//                    if (mThumb != null) {
//                        invalidate(mThumb.getBounds()); // This may be within the padding region
//                    }
//                    onStartTrackingTouch();
//                    trackTouchEvent(event);
//                    attemptClaimDrag();
//                }
//                break;

            case MotionEvent.ACTION_MOVE:
                // check if action is performed on stars
                if (starsTouchSpace.contains(event.getX(), event.getY())) {
                    touchInProgress = true;
                    setNewRatingFromTouch(event.getX(), event.getY());
                } else {
                    if (touchInProgress && listener != null) {
                        listener.onRatingChanged(this, rating, false);
                    }
                    touchInProgress = false;
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                setNewRatingFromTouch(event.getX(), event.getY());
            case MotionEvent.ACTION_CANCEL:
                if (listener != null) {
                    listener.onRatingChanged(this, rating, false);
                }
                touchInProgress = false;
                break;

        }

        invalidate();
        return true;
    }

    /**
     * Assigns a rating to the touch event.
     *
     * @param x
     * @param y
     */
    private void setNewRatingFromTouch(float x, float y) {
        // normalize x to inside starsDrawinSpace
        if (gravity_1 != Gravity.Left) {
            x = getWidth() - x;
        }

        // we know that touch was inside starsTouchSpace, but it might be outside starsDrawingSpace
        if (x < starsDrawingSpace.left) {
            rating = 0;
            return;
        } else if (x > starsDrawingSpace.right) {
            rating = numberOfStars;
            return;
        }

        x = x - starsDrawingSpace.left;
        // reduce the width to allow the user reach the top and bottom values of rating (0 and numberOfStars)
        rating = (float) numberOfStars / starsDrawingSpace.width() * x;

        // correct rating in case step size is present
        if (stepSize != Float.MAX_VALUE) {
            rating -= rating % stepSize;
        }
    }

  /* ----------- GETTERS AND SETTERS ----------- */

    public float getRating() {
        return rating;
    }

    /**
     * Sets rating.
     * If provided value is less than 0, rating will be set to 0.
     * * If provided value is greater than numberOfStars, rating will be set to numberOfStars.
     *
     * @param rating
     */
    public void setRating(float rating) {
        this.rating = normalizeRating(rating);
        if (stepSize != Float.MAX_VALUE && (ratingAnimator == null || !ratingAnimator.isRunning())) {
            rating -= rating % stepSize;
        }
        // request redraw of the view
        invalidate();
        if (listener != null && (ratingAnimator == null || !ratingAnimator.isRunning())) {
            listener.onRatingChanged(this, rating, false);
        }
    }

    public boolean isIndicator() {
        return isIndicator;
    }

    /**
     * Sets indicator property.
     * If provided value is true, touch events will be deactivated, and thus user interaction will be deactivated.
     *
     * @param indicator
     */
    public void setIndicator(boolean indicator) {
        isIndicator = indicator;
        touchInProgress = false;
    }

    public float getMaxStarSize() {
        return maxStarSize;
    }

    /**
     * Sets maximum star size in pixels.
     * If current star size is less than provided value, this has no effect on the view.
     *
     * @param maxStarSize
     */
    public void setMaxStarSize(float maxStarSize) {
        this.maxStarSize = maxStarSize;
        if (starSize > maxStarSize) {
            // force re-calculating the layout dimension
            requestLayout();
            // request redraw of the view
            invalidate();
        }
    }

    public float getStarSize() {
        return starSize;
    }

    /**
     * Sets exact star size.
     *
     * @param starSize
     */
    public void setStarSize(float starSize) {
        this.starSize = starSize;
        if (starSize != Integer.MAX_VALUE && maxStarSize != Integer.MAX_VALUE && starSize > maxStarSize) {
            Log.w("SimpleRatingBar", String.format("Initialized with conflicting values: starSize is greater than maxStarSize (%f > %f). I will ignore maxStarSize", starSize, maxStarSize));
        }
        // force re-calculating the layout dimension
        requestLayout();
        // request redraw of the view
        invalidate();
    }

    public float getStepSize() {
        return stepSize;
    }

    /**
     * Sets step size of rating.
     * Throws IllegalArgumentException if provided value is less or equal than zero.
     *
     * @param stepSize
     */
    public void setStepSize(float stepSize) {
        this.stepSize = stepSize;
        if (stepSize <= 0) {
            throw new IllegalArgumentException(String.format("SimpleRatingBar initialized with invalid value for stepSize. Found %f, but should be greater than 0", stepSize));
        }
        // request redraw of the view
        invalidate();
    }

    public float getStarsSeparation() {
        return starsSeparation;
    }

    /**
     * Sets separation between stars in pixels.
     *
     * @param starsSeparation
     */
    public void setStarsSeparation(float starsSeparation) {
        this.starsSeparation = starsSeparation;
        // force re-calculating the layout dimension
        requestLayout();
        // request redraw of the view
        invalidate();
    }

    public int getNumberOfStars() {
        return numberOfStars;
    }

    /**
     * Sets number of stars.
     * Throws IllegalArgumentException if provided value is less or equal than zero.
     *
     * @param numberOfStars
     */
    public void setNumberOfStars(int numberOfStars) {
        this.numberOfStars = numberOfStars;
        if (numberOfStars <= 0) {
            throw new IllegalArgumentException(String.format("SimpleRatingBar initialized with invalid value for numberOfStars. Found %d, but should be greater than 0", numberOfStars));
        }
        // force re-calculating the layout dimension
        requestLayout();
        // request redraw of the view
        invalidate();
    }

    public
    @ColorInt
    int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets background color of view.
     *
     * @param backgroundColor
     */
    @Override
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        paintBackground.setColor(backgroundColor);
        // request redraw of the view
        invalidate();
    }

    public
    @ColorInt
    int getBorderColor() {
        return borderColor;
    }

    /**
     * Sets border color of stars.
     *
     * @param borderColor
     */
    public void setBorderColor(@ColorInt int borderColor) {
        this.borderColor = borderColor;
        paintStarBorder.setColor(borderColor);
        // request redraw of the view
        invalidate();
    }

    public float getStarBorderWidth() {
        return starBorderWidth;
    }

    /**
     * Sets border width of stars in pixels.
     *
     * @param starBorderWidth
     */
    public void setStarBorderWidth(float starBorderWidth) {
        this.starBorderWidth = starBorderWidth;
        if (starBorderWidth <= 0) {
            throw new IllegalArgumentException(String.format("SimpleRatingBar initialized with invalid value for starBorderWidth. Found %f, but should be greater than 0",
                    starBorderWidth));
        }
        paintStarBorder.setStrokeWidth(starBorderWidth);
        // request redraw of the view
        invalidate();
    }

    public
    @ColorInt
    int getFillColor() {
        return fillColor;
    }

    /**
     * Sets fill color of stars.
     *
     * @param fillColor
     */
    public void setFillColor(@ColorInt int fillColor) {
        this.fillColor = fillColor;
        paintStarFill.setColor(fillColor);
        // request redraw of the view
        invalidate();
    }

    public Gravity getGravity() {
        return gravity_1;
    }

    /**
     * Sets gravity of fill.
     *
     * @param gravity
     */
    public void setGravity(Gravity gravity) {
        this.gravity_1 = gravity;
        // request redraw of the view
        invalidate();
    }

    /**
     * Sets rating with animation.
     *
     * @param builder
     */
    private void animateRating(AnimationBuilder builder) {
        builder.ratingTarget = normalizeRating(builder.ratingTarget);
        ratingAnimator = ValueAnimator.ofFloat(0, builder.ratingTarget);
        ratingAnimator.setDuration(builder.duration);
        ratingAnimator.setRepeatCount(builder.repeatCount);
        ratingAnimator.setRepeatMode(builder.repeatMode);

        // Callback that executes on animation steps.
        ratingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) (animation.getAnimatedValue())).floatValue();
                setRating(value);
            }
        });

        if (builder.interpolator != null) {
            ratingAnimator.setInterpolator(builder.interpolator);
        }
        if (builder.animatorListener != null) {
            ratingAnimator.addListener(builder.animatorListener);
        }
        ratingAnimator.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (listener != null) {
                    listener.onRatingChanged(CustomRatingBar.this, rating, false);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                if (listener != null) {
                    listener.onRatingChanged(CustomRatingBar.this, rating, false);
                }
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                if (listener != null) {
                    listener.onRatingChanged(CustomRatingBar.this, rating, false);
                }
            }
        });
        ratingAnimator.start();
    }

    /**
     * Returns a new AnimationBuilder.
     *
     * @return
     */
    public AnimationBuilder getAnimationBuilder() {
        return new AnimationBuilder(this);
    }

    /**
     * Normalizes rating passed by argument between 0 and numberOfStars.
     *
     * @param rating
     * @return
     */
    private float normalizeRating(float rating) {
        if (rating < 0) {
            Log.w("SimpleRatingBar", String.format("Assigned rating is less than 0 (%f < 0), I will set it to exactly 0", rating));
            return 0;
        } else if (rating > numberOfStars) {
            Log.w("SimpleRatingBar", String.format("Assigned rating is greater than numberOfStars (%f > %d), I will set it to exactly numberOfStars", rating, numberOfStars));
            return numberOfStars;
        } else {
            return rating;
        }
    }

    /**
     * Sets OnRatingBarChangeListener.
     *
     * @param listener
     */
    public void setOnRatingBarChangeListener(OnRatingBarChangeListener listener) {
        this.listener = listener;
    }

    public interface OnRatingBarChangeListener {

        /**
         * Notification that the rating has changed. Clients can use the
         * fromUser parameter to distinguish user-initiated changes from those
         * that occurred programmatically. This will not be called continuously
         * while the user is dragging, only when the user finalizes a rating by
         * lifting the touch.
         *
         * @param customRatingBar The RatingBar whose rating has changed.
         * @param rating          The current rating. This will be in the range
         *                        0..numStars.
         * @param fromUser        True if the rating change was initiated by a user's
         *                        touch gesture or arrow key/horizontal trackbell movement.
         */
        void onRatingChanged(CustomRatingBar customRatingBar, float rating, boolean fromUser);

    }

    /**
     * Helper class to build rating animation.
     * Provides good defaults:
     * - Target rating: numberOfStars
     * - Animation: Bounce
     * - Duration: 2s
     */
    public class AnimationBuilder {
        private CustomRatingBar ratingBar;
        private long duration;
        private Interpolator interpolator;
        private float ratingTarget;
        private int repeatCount;
        private int repeatMode;
        private AnimatorListener animatorListener;

        private AnimationBuilder(CustomRatingBar ratingBar) {
            this.ratingBar = ratingBar;
            this.duration = 2000;
            this.interpolator = new BounceInterpolator();
            this.ratingTarget = ratingBar.getNumberOfStars();
            this.repeatCount = 1;
            this.repeatMode = ValueAnimator.REVERSE;
        }

        /**
         * Sets duration of animation.
         *
         * @param duration
         * @return
         */
        public AnimationBuilder setDuration(long duration) {
            this.duration = duration;
            return this;
        }

        /**
         * Sets interpolator for animation.
         *
         * @param interpolator
         * @return
         */
        public AnimationBuilder setInterpolator(Interpolator interpolator) {
            this.interpolator = interpolator;
            return this;
        }

        /**
         * Sets rating after animation has ended.
         *
         * @param ratingTarget
         * @return
         */
        public AnimationBuilder setRatingTarget(float ratingTarget) {
            this.ratingTarget = ratingTarget;
            return this;
        }

        /**
         * Sets repeat count for animation.
         *
         * @param repeatCount
         * @return
         */
        public AnimationBuilder setRepeatCount(int repeatCount) {
            this.repeatCount = repeatCount;
            return this;
        }

        /**
         * Sets repeat mode for animation.
         * Possible values are: ValueAnimator.INFINITE, ValueAnimator.RESTART or ValueAnimator.REVERSE
         *
         * @param repeatMode
         * @return
         */
        public AnimationBuilder setRepeatMode(int repeatMode) {
            this.repeatMode = repeatMode;
            return this;
        }

        /**
         * Sets AnimatorListener.
         *
         * @param animatorListener
         * @return
         */
        public AnimationBuilder setAnimatorListener(AnimatorListener animatorListener) {
            this.animatorListener = animatorListener;
            return this;
        }

        /**
         * Starts animation.
         */
        public void start() {
            ratingBar.animateRating(this);
        }
    }
}
