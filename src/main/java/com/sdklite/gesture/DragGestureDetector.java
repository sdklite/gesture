package com.sdklite.gesture;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Drag gesture detector
 */
public class DragGestureDetector {

    public interface OnDragGestureListener {
        boolean onDragBegin(final DragGestureDetector detector);
        boolean onDrag(final DragGestureDetector detector);
        void onDragEnd(final DragGestureDetector detector, final boolean cancel);
    }

    private final Context mContext;
    private final OnDragGestureListener mListener;

    private PointF mP0;
    private PointF mP1;
    private boolean mPointerDown;
    private boolean mGestureAcepted;
    private boolean mGestureInProgress;

    public DragGestureDetector(final Context context, final OnDragGestureListener listener) {
        this.mContext = context;
        this.mListener = listener;
    }

    public boolean onTouchEvent(final MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_CANCEL:
                this.cancelDrag();
                break;
            case MotionEvent.ACTION_UP:
                this.cancelDrag();
                break;
            case MotionEvent.ACTION_DOWN: {
                this.cancelDrag();
                this.mPointerDown = true;
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                this.mP0 = getFocusPoint(event);
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                final int index = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                this.mP0 = this.mP1 = getFocusPoint(event, index);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (this.mPointerDown) {
                    this.mP1 = getFocusPoint(event);

                    if (!this.mGestureInProgress) {
                        this.mGestureInProgress = true;
                        this.mGestureAcepted = this.mListener.onDragBegin(this);
                        if (this.mGestureAcepted) {
                            this.mP0 = getFocusPoint(event);
                        }
                    } else if (this.mGestureAcepted) {
                        this.mGestureAcepted = this.mListener.onDrag(this);
                        if (this.mGestureAcepted) {
                            this.mP0 = getFocusPoint(event);
                        }
                    }
                }
                break;
            }
        }

        return false;
    }

    private PointF getFocusPoint(final MotionEvent event, final int ignore) {
        float x = 0;
        float y = 0;

        final int n = event.getPointerCount();
        for (int i = 0; i < n; i++) {
            if (i == ignore) {
                continue;
            }
            x += event.getX(i);
            y += event.getY(i);
        }

        return new PointF(x / (n - 1), y / (n - 1));
    }

    private PointF getFocusPoint(final MotionEvent event) {
        float x = 0;
        float y = 0;

        final int n = event.getPointerCount();
        for (int i = 0; i < n; i++) {
            x += event.getX(i);
            y += event.getY(i);
        }

        return new PointF(x / n, y / n);
    }

    public float getDeltaY() {
        if (!this.mGestureInProgress) {
            return 0;
        }

        return this.mP1.y - this.mP0.y;
    }

    public float getDeltaX() {
        if (!this.mGestureInProgress) {
            return 0;
        }

        return this.mP1.x - this.mP0.x;
    }

    private void cancelDrag() {
        if (this.mGestureInProgress) {
            this.mGestureInProgress = false;
            if (this.mGestureAcepted) {
                this.mGestureAcepted = false;
                this.mListener.onDragEnd(this, true);
            }
        }

        this.mP0 = null;
        this.mP1 = null;
    }

}

