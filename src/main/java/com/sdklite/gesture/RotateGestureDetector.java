package com.sdklite.gesture;

import android.content.Context;
import android.graphics.PointF;
import android.view.MotionEvent;

/**
 * Rotate gesture detector
 */
public class RotateGestureDetector {

    public interface OnRotateListener {
        public boolean onRotateBegin(final RotateGestureDetector detector);
        public boolean onRotate(final RotateGestureDetector detector);
        public void onRotateEnd(final RotateGestureDetector detector);
    }

    private final Context mContext;
    private final OnRotateListener mListener;

    private PointF mPivot;
    private boolean mFingersReady;
    private float mRotation0;
    private float mRotation1;
    private boolean mGestureInProgress;
    private boolean mGestureAccepted;

    public RotateGestureDetector(final Context context, final OnRotateListener listener) {
        this.mContext = context;
        this.mListener = listener;
    }

    public float getDeltaRotation() {
        if (!this.mGestureInProgress) {
            return 0;
        }

        return this.mRotation1 - this.mRotation0;
    }

    public PointF getPivot() {
        if (!this.mGestureInProgress) {
            return null;
        }

        return this.mPivot;
    }

    public boolean onTouchEvent(final MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_CANCEL:
                return this.cancel();
            case MotionEvent.ACTION_DOWN:
                this.reset();
                break;
            case MotionEvent.ACTION_UP:
                this.reset();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                this.mFingersReady = 2 == event.getPointerCount();
                if (this.mFingersReady) {
                    this.mRotation0 = this.mRotation1 = getRotation(event);
                    this.mPivot = getPivot(event);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                this.mFingersReady = 3 == event.getPointerCount();
                if (this.mFingersReady) {
                    this.mRotation0 = this.mRotation1 = getRotation(event);
                    this.mPivot = getPivot(event);
                }
                break;
            case MotionEvent.ACTION_MOVE: {
                if (!this.mFingersReady) {
                    return false;
                }

                this.mRotation1 = getRotation(event);

                if (!this.mGestureInProgress) {
                    this.mGestureInProgress = true;
                    this.mGestureAccepted = this.mListener.onRotateBegin(this);
                } else if (this.mGestureAccepted) {
                    this.mGestureAccepted = this.mListener.onRotate(this);
                }

                if (this.mGestureAccepted) {
                    this.mRotation0 = this.mRotation1;
                }

                return true;
            }
        }

        return false;
    }

    private static PointF getPivot(final MotionEvent event) {
        return new PointF((event.getX(0) + event.getX(1)) / 2f, (event.getY(0) + event.getY(1)) / 2f);
    }

    private boolean cancel() {
        if (!this.mGestureInProgress) {
            return false;
        }

        this.mGestureInProgress = false;

        if (this.mGestureAccepted) {
            this.mListener.onRotateEnd(this);
            this.mGestureAccepted = false;
        }

        this.reset();
        return true;
    }

    private void reset() {
        this.mFingersReady = false;
        this.mGestureInProgress = false;
        this.mGestureAccepted = false;
        this.mRotation0 = 0;
        this.mRotation1 = 0;
        this.mPivot = null;
    }

    private static float getRotation(final MotionEvent event) {
        final double deltaX = (event.getX(0) - event.getX(1));
        final double deltaY = (event.getY(0) - event.getY(1));
        final double radians = Math.atan2(deltaY, deltaX);
        return (float) Math.toDegrees(radians);
    }
}

