package com.sdklite.gesture;

import android.content.Context;
import android.view.MotionEvent;

/**
 * Scale gesture detector
 */
public class ScaleGestureDetector {

    public interface OnScaleGestureListener {
        public boolean onScale(ScaleGestureDetector detector);
        public boolean onScaleBegin(ScaleGestureDetector detector);
        public void onScaleEnd(ScaleGestureDetector detector, boolean cancel);
    }

    public static class SimpleOnScaleGestureListener implements OnScaleGestureListener {
        public boolean onScale(ScaleGestureDetector detector) {
            return true;
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        public void onScaleEnd(ScaleGestureDetector detector, boolean cancel) {
        }
    }

    private static final float PRESSURE_THRESHOLD = 0.67f;

    private final Context mContext;
    private final OnScaleGestureListener mListener;
    private boolean mGestureInProgress;
    private MotionEvent mPrevEvent;
    private MotionEvent mCurrEvent;
    private float mFocusX;
    private float mFocusY;
    private float mPrevFingerDiffX;
    private float mPrevFingerDiffY;
    private float mCurrFingerDiffX;
    private float mCurrFingerDiffY;
    private float mCurrLen;
    private float mPrevLen;
    private float mScaleFactor;
    private float mCurrPressure;
    private float mPrevPressure;
    private long mTimeDelta;
    // Tracking individual fingers.
    private float mTopFingerBeginX;
    private float mTopFingerBeginY;
    private float mBottomFingerBeginX;
    private float mBottomFingerBeginY;
    private float mTopFingerCurrX;
    private float mTopFingerCurrY;
    private float mBottomFingerCurrX;
    private float mBottomFingerCurrY;
    private boolean mTopFingerIsPointer1;
    private boolean mPointerOneUp;
    private boolean mPointerTwoUp;

    public ScaleGestureDetector(final Context context, final OnScaleGestureListener listener) {
        this.mContext = context;
        this.mListener = listener;
    }

    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();

        if (!mGestureInProgress) {
            // Track individual fingers.
            if (action == MotionEvent.ACTION_POINTER_1_DOWN) {
            }

            if (action == MotionEvent.ACTION_POINTER_2_DOWN) {
            }

            if ((action == MotionEvent.ACTION_POINTER_1_DOWN || action == MotionEvent.ACTION_POINTER_2_DOWN) && event.getPointerCount() >= 2) {
                // We have a new multi-finger gesture
                mBottomFingerBeginX = event.getX(0);
                mBottomFingerBeginY = event.getY(0);
                mTopFingerBeginX = event.getX(1);
                mTopFingerBeginY = event.getY(1);

                mTopFingerCurrX = mTopFingerBeginX;
                mTopFingerCurrY = mTopFingerBeginY;
                mBottomFingerCurrX = mBottomFingerBeginX;
                mBottomFingerCurrY = mBottomFingerBeginY;
                mPointerOneUp = false;
                mPointerTwoUp = false;
                // Be paranoid in case we missed an event
                reset();
                // We decide which finger should be designated as the top finger
                if (mTopFingerBeginY > mBottomFingerBeginY) {
                    mTopFingerIsPointer1 = false;
                } else {
                    mTopFingerIsPointer1 = true;
                }
                mPrevEvent = MotionEvent.obtain(event);
                mTimeDelta = 0;
                setContext(event);
                mGestureInProgress = mListener.onScaleBegin(this);
            }
        } else {
            // Transform gesture in progress - attempt to handle it
            switch (action) {
                case MotionEvent.ACTION_UP:
                    mPointerOneUp = true;
                    mPointerTwoUp = true;
                case MotionEvent.ACTION_POINTER_1_UP:
                    if (mPointerOneUp) {
                        mPointerTwoUp = true;
                    }
                    mPointerOneUp = true;
                case MotionEvent.ACTION_POINTER_2_UP:
                    if (action == MotionEvent.ACTION_POINTER_2_UP) {
                        if (mPointerTwoUp == true) {
                            mPointerOneUp = true;
                        }
                        mPointerTwoUp = true;
                    }
                    // Gesture ended
                    if (mPointerOneUp || mPointerTwoUp) {
                        setContext(event);
                        // Set focus point to the remaining finger
                        int id = (((action & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT) == 0) ? 1 : 0;
                        mFocusX = event.getX(id);
                        mFocusY = event.getY(id);
                        mListener.onScaleEnd(this, false);
                        mGestureInProgress = false;
                        reset();
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    mListener.onScaleEnd(this, true);
                    mGestureInProgress = false;
                    reset();
                    break;
                case MotionEvent.ACTION_MOVE:
                    setContext(event);
                    // Only accept the event if our relative pressure is within
                    // a certain limit - this can help filter shaky data as a
                    // finger is lifted.
                    if (mCurrPressure / mPrevPressure > PRESSURE_THRESHOLD) {
                        final boolean updatePrevious = mListener.onScale(this);
                        if (updatePrevious) {
                            mPrevEvent.recycle();
                            mPrevEvent = MotionEvent.obtain(event);
                        }
                    }
                    break;
            }
        }

        return false;
    }

    private void setContext(MotionEvent curr) {
        if (mCurrEvent != null) {
            mCurrEvent.recycle();
        }
        mCurrEvent = MotionEvent.obtain(curr);
        mCurrLen = -1;
        mPrevLen = -1;
        mScaleFactor = -1;
        final MotionEvent prev = mPrevEvent;
        final float px0 = prev.getX(0);
        final float py0 = prev.getY(0);
        final float px1 = prev.getX(1);
        final float py1 = prev.getY(1);
        final float cx0 = curr.getX(0);
        final float cy0 = curr.getY(0);
        final float cx1 = curr.getX(1);
        final float cy1 = curr.getY(1);
        final float pvx = px1 - px0;
        final float pvy = py1 - py0;
        final float cvx = cx1 - cx0;
        final float cvy = cy1 - cy0;
        mPrevFingerDiffX = pvx;
        mPrevFingerDiffY = pvy;
        mCurrFingerDiffX = cvx;
        mCurrFingerDiffY = cvy;
        mFocusX = cx0 + cvx * 0.5f;
        mFocusY = cy0 + cvy * 0.5f;
        mTimeDelta = curr.getEventTime() - prev.getEventTime();
        mCurrPressure = curr.getPressure(0) + curr.getPressure(1);
        mPrevPressure = prev.getPressure(0) + prev.getPressure(1);
        // Update the correct finger.
        mBottomFingerCurrX = cx0;
        mBottomFingerCurrY = cy0;
        mTopFingerCurrX = cx1;
        mTopFingerCurrY = cy1;
    }

    private void reset() {
        if (mPrevEvent != null) {
            mPrevEvent.recycle();
            mPrevEvent = null;
        }
        if (mCurrEvent != null) {
            mCurrEvent.recycle();
            mCurrEvent = null;
        }
    }

    public boolean isInProgress() {
        return mGestureInProgress;
    }

    public float getFocusX() {
        return mFocusX;
    }

    public float getFocusY() {
        return mFocusY;
    }

    public float getCurrentSpan() {
        if (mCurrLen == -1) {
            final float cvx = mCurrFingerDiffX;
            final float cvy = mCurrFingerDiffY;
            mCurrLen = (float) Math.sqrt(cvx * cvx + cvy * cvy);
        }
        return mCurrLen;
    }

    public float getPreviousSpan() {
        if (mPrevLen == -1) {
            final float pvx = mPrevFingerDiffX;
            final float pvy = mPrevFingerDiffY;
            mPrevLen = (float) Math.sqrt(pvx * pvx + pvy * pvy);
        }
        return mPrevLen;
    }

    public float getScaleFactor() {
        if (mScaleFactor == -1) {
            mScaleFactor = getCurrentSpan() / getPreviousSpan();
        }
        return mScaleFactor;
    }

    public long getTimeDelta() {
        return mTimeDelta;
    }

    public long getEventTime() {
        return mCurrEvent.getEventTime();
    }

    public float getTopFingerX() {
        return (mTopFingerIsPointer1) ? mTopFingerCurrX : mBottomFingerCurrX;
    }

    public float getTopFingerY() {
        return (mTopFingerIsPointer1) ? mTopFingerCurrY : mBottomFingerCurrY;
    }

    public float getTopFingerDeltaX() {
        return (mTopFingerIsPointer1) ? mTopFingerCurrX - mTopFingerBeginX : mBottomFingerCurrX - mBottomFingerBeginX;
    }

    public float getTopFingerDeltaY() {
        return (mTopFingerIsPointer1) ? mTopFingerCurrY - mTopFingerBeginY : mBottomFingerCurrY - mBottomFingerBeginY;
    }

    public float getBottomFingerX() {
        return (!mTopFingerIsPointer1) ? mTopFingerCurrX : mBottomFingerCurrX;
    }

    public float getBottomFingerY() {
        return (!mTopFingerIsPointer1) ? mTopFingerCurrY : mBottomFingerCurrY;
    }

    public float getBottomFingerDeltaX() {
        return (!mTopFingerIsPointer1) ? mTopFingerCurrX - mTopFingerBeginX : mBottomFingerCurrX - mBottomFingerBeginX;
    }

    public float getBottomFingerDeltaY() {
        return (!mTopFingerIsPointer1) ? mTopFingerCurrY - mTopFingerBeginY : mBottomFingerCurrY - mBottomFingerBeginY;
    }
}

