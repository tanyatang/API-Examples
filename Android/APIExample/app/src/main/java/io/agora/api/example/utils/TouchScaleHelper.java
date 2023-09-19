package io.agora.api.example.utils;

import android.content.Context;
import android.graphics.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

/**
 * The type Touch scale helper.
 */
public class TouchScaleHelper implements View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener, View.OnLayoutChangeListener {

    private TouchScaleListener touchScaleListener;
    private final ScaleGestureDetector mScaleGestureDetector;

    private float mPreScaleY = -1;
    private float mPreScaleX = -1;
    private float mCurrScaleY = -1;
    private float mCurrScaleX = -1;

    private float mCurScale = 1.0f;
    private float mPreviewScale = mCurScale;
    private boolean mMovable = true;
    private float mCurrTransX;
    private float mPreTransX = -1;
    private float mCurrTransY;
    private float mPreTransY = -1;
    private WeakReference<View> viewRef;
    private float mScaleMin = 1.0f;
    private float mScaleMax = 8.0f;
    private float mMoveFactor = 0.01f;

    /**
     * With touch scale helper.
     *
     * @param context the context
     * @return the touch scale helper
     */
    public static TouchScaleHelper with(Context context){
        return new TouchScaleHelper(context);
    }

    private TouchScaleHelper(Context context){
        mScaleGestureDetector = new ScaleGestureDetector(context.getApplicationContext(), this);
        touchScaleListener = new DefaultTouchScaleListener();
    }

    /**
     * Set touch scale listener touch scale helper.
     *
     * @param listener the listener
     * @return the touch scale helper
     */
    public TouchScaleHelper setTouchScaleListener(TouchScaleListener listener){
        touchScaleListener = listener;
        return this;
    }

    /**
     * Bind view touch scale helper.
     *
     * @param view the view
     * @return the touch scale helper
     */
    public TouchScaleHelper bindView(@NonNull View view){
        if (viewRef != null) {
            View oView = viewRef.get();
            if (oView != view) {
                oView.setOnTouchListener(null);
                oView.removeOnLayoutChangeListener(this);
            }
        }
        viewRef = new WeakReference<>(view);
        view.setOnTouchListener(this);
        view.addOnLayoutChangeListener(this);
        notifyScaleUpdate();
        return this;
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        notifyScaleUpdate();
    }

    /**
     * Set scale max touch scale helper.
     *
     * @param scale the scale
     * @return the touch scale helper
     */
    public TouchScaleHelper setScaleMax(float scale){
        if(mScaleMax != scale){
            mScaleMax = scale;
            notifyScaleUpdate();
        }
        return this;
    }

    /**
     * Set scale min touch scale helper.
     *
     * @param scale the scale
     * @return the touch scale helper
     */
    public TouchScaleHelper setScaleMin(float scale){
        if(mScaleMin != scale){
            mScaleMin = scale;
            notifyScaleUpdate();
        }
        return this;
    }

    /**
     * Set move factor touch scale helper.
     *
     * @param factor the factor
     * @return the touch scale helper
     */
    public TouchScaleHelper setMoveFactor(float factor){
        if(mMoveFactor != factor){
            mMoveFactor = factor;
        }
        return this;
    }

    /**
     * Set scale touch scale helper.
     *
     * @param scale the scale
     * @param px    the px
     * @param py    the py
     * @return the touch scale helper
     */
    public TouchScaleHelper setScale(float scale, float px, float py){
        if(mCurScale != scale){
            mPreviewScale = mCurScale = scale;
            mCurrScaleX = px;
            mCurrScaleY = py;
            notifyScaleUpdate();
        }
        return this;
    }

    /**
     * Set translate touch scale helper.
     *
     * @param dx the dx
     * @param dy the dy
     * @return the touch scale helper
     */
    public TouchScaleHelper setTranslate(float dx, float dy){
        if(mCurrTransX != dx || mCurrTransY != dy){
            mCurrTransX = dx;
            mCurrTransY = dy;
            notifyScaleUpdate();
        }
        return this;
    }

    private void notifyScaleUpdate() {
        if (viewRef != null) {
            View v = viewRef.get();
            if (v != null) {
                limitTranslate(v);
                if (touchScaleListener != null) {
                    touchScaleListener.onScaleUpdated(v, mCurScale, mCurrScaleX / v.getWidth(), mCurrScaleY / v.getHeight(), mCurrTransX / v.getWidth(), mCurrTransY / v.getHeight());
                }
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_MOVE) {
            if (e.getPointerCount() == 2) {
                float centerX = e.getX(0) + (e.getX(1) - e.getX(0)) / 2;
                float centerY = e.getY(0) + (e.getY(1) - e.getY(0)) / 2;

                if (mCurrScaleX < 0 && mCurrScaleY < 0) {
                    if (mPreScaleX < 0 && mPreScaleY < 0) {
                        mPreScaleX = centerX;
                        mPreScaleY = centerY;
                    }

                    float scaleX;
                    float scaleY;
                    if (centerX > mPreScaleX) {
                        scaleX = Math.max(0, Math.min(mPreScaleX + (centerX - mPreScaleX) / mCurScale, v.getWidth()));
                    } else {
                        scaleX = Math.max(0, Math.min(mPreScaleX - (mPreScaleX - centerX) / mCurScale, v.getWidth()));
                    }

                    if (centerY > mPreScaleY) {
                        scaleY = Math.max(0, Math.min(mPreScaleY + (centerY - mPreScaleY) / mCurScale, v.getHeight()));
                    } else {
                        scaleY = Math.max(0, Math.min(mPreScaleY - (mPreScaleY - centerY) / mCurScale, v.getHeight()));
                    }

                    mPreScaleX = mCurrScaleX = scaleX;
                    mPreScaleY = mCurrScaleY = scaleY;
                } else if (mMovable) {
                    if (mPreTransX < 0 && mPreTransY < 0) {
                        mPreTransX = centerX;
                        mPreTransY = centerY;
                    }

                    mCurrTransX -= (mPreTransX - centerX);
                    mCurrTransY -= (mPreTransY - centerY);
                    limitTranslate(v);
                    mPreTransX = centerX;
                    mPreTransY = centerY;

                    notifyScaleUpdate();
                }
            }
        } else if (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_CANCEL) {
            mPreviewScale = mCurScale;
            mCurrScaleX = -1;
            mCurrScaleY = -1;
            mPreTransX = -1;
            mPreTransY = -1;
            mMovable = true;
        }
        mScaleGestureDetector.onTouchEvent(e);
        return true;
    }

    private void limitTranslate(View v) {
        mCurrTransX = Math.max(Math.max(0, (v.getWidth() - mCurrScaleX) * (mCurScale - 1) ) * -1, Math.min(mCurrTransX, Math.max(0, mCurrScaleX * (mCurScale - 1))));
        mCurrTransY = Math.max(Math.max(0, (v.getHeight() - mCurrScaleY) * (mCurScale - 1)) * -1, Math.min(mCurrTransY, Math.max(0, mCurrScaleY * (mCurScale - 1))));
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float preSpan = detector.getPreviousSpan();
        float curSpan = detector.getCurrentSpan();
        float currScale = mCurScale;
        if (curSpan < preSpan) {
            currScale = mPreviewScale - (preSpan - curSpan) / 200;
        } else {
            currScale = mPreviewScale + (curSpan - preSpan) / 200;
        }
        currScale = Math.max(mScaleMin, Math.min(currScale, mScaleMax));
        mMovable = Math.abs(currScale - mCurScale) <= mMoveFactor;
        //mMovable = true;
        if (mMovable) {
            return false;
        }

        mCurScale = currScale;
        notifyScaleUpdate();
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        if(viewRef != null){
            View view = viewRef.get();
            if (touchScaleListener != null && view != null) {
                touchScaleListener.onScaleBegin(view);
            }
        }

        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        if(viewRef != null){
            View view = viewRef.get();
            if (touchScaleListener != null && view != null) {
                touchScaleListener.onScaleEnd(view);
            }
        }
    }

    /**
     * The interface Touch scale listener.
     */
    public interface TouchScaleListener {
        /**
         * On scale begin.
         *
         * @param view the view
         */
        default void onScaleBegin(@NonNull View view) {
        }

        /**
         * On scale updated.
         *
         * @param view  the view
         * @param scale the scale
         * @param spx   the spx
         * @param spy   the spy
         * @param tx    the tx
         * @param ty    the ty
         */
        void onScaleUpdated(@NonNull View view, float scale, float spx, float spy, float tx, float ty);

        /**
         * On scale end.
         *
         * @param view the view
         */
        default void onScaleEnd(@NonNull View view) {
        }
    }


    private static class DefaultTouchScaleListener implements TouchScaleListener {

        private final Matrix matrix = new Matrix();

        @Override
        public void onScaleUpdated(@NonNull View view, float scale, float spx, float spy, float tx, float ty) {
            if (view instanceof TextureView) {
                matrix.reset();
                matrix.preScale(scale, scale, spx * view.getWidth(), spy * view.getHeight());
                matrix.postTranslate(tx * view.getWidth(), ty * view.getHeight());
                ((TextureView) view).setTransform(matrix);
            }
            else if(view instanceof ImageView){
                matrix.reset();
                matrix.preScale(scale, scale, spx * view.getWidth(), spy * view.getHeight());
                matrix.postTranslate(tx * view.getWidth() , ty * view.getHeight());
                ((ImageView) view).setScaleType(ImageView.ScaleType.MATRIX);
                ((ImageView) view).setImageMatrix(matrix);
            }
            else {
                Log.e("DefaultTouchScale", "onScaleUpdated >> not support view type. scale=" + scale + ", spx=" + spx + ", spy=" + spy + ", tx=" + tx + ", ty=" + ty);
            }
        }
    }

}
