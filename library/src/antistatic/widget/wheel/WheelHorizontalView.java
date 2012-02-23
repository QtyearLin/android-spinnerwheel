/*
 * android-spinnerwheel
 * https://github.com/ai212983/android-spinnerwheel
 *
 * based on
 *
 * Android Wheel Control.
 * https://code.google.com/p/android-wheel/
 *
 * Copyright 2011 Yuri Kanivets
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package antistatic.widget.wheel;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import antistatic.widget.R;

/**
 * Spinner wheel horizontal view.
 *
 * @author Yuri Kanivets
 * @author Dimitri Fedorov
 */
public class WheelHorizontalView extends AbstractWheelView {

    private static int itemID = -1;

    @SuppressWarnings("unused")
    private final String LOG_TAG = WheelHorizontalView.class.getName() + " #" + (++itemID);

    /**
     * The width of the selection divider.
     */
    protected int mSelectionDividerWidth;

    // Item width
    private int itemWidth = 0;

    //--------------------------------------------------------------------------
    //
    //  Constructors
    //
    //--------------------------------------------------------------------------

    /**
     * Create a new wheel horizontal view.
     *
     * @param context The application environment.
     */
    public WheelHorizontalView(Context context) {
        this(context, null);
    }

    /**
     * Create a new wheel horizontal view.
     *
     * @param context The application environment.
     * @param attrs A collection of attributes.
     */
    public WheelHorizontalView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.abstractWheelViewStyle);
    }

    /**
     * Create a new wheel horizontal view.
     *
     * @param context the application environment.
     * @param attrs a collection of attributes.
     * @param defStyle The default style to apply to this view.
     */
    public WheelHorizontalView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    //--------------------------------------------------------------------------
    //
    //  Debugging stuff
    //
    //--------------------------------------------------------------------------
    @Override
    protected void onScrollTouchedUp() {
        super.onScrollTouchedUp();
        // See http://developer.android.com/guide/topics/ui/how-android-draws.html
        Log.e(LOG_TAG, " --- dumping items of layout " + this.getMeasuredWidth() +"x" + this.getMeasuredHeight() + " // "  + this.getLeft() + ", " + this.getTop() + ", " + this.getRight() + ", " + this.getBottom());
        for (int i = 0; i < this.getChildCount(); i++) {
            View v = this.getChildAt(i);
            Log.e(LOG_TAG, "Item #" + i + ", " + v.getMeasuredWidth() + "x" + v.getMeasuredHeight() + " // "  + v.getLeft() + ", " + v.getTop() + ", " + v.getRight() + ", " + v.getBottom());
        }
        Log.e(LOG_TAG, " --- end of dumping items");
    }

    //--------------------------------------------------------------------------
    //
    //  Initiating assets and setter for selector paint
    //
    //--------------------------------------------------------------------------

    @Override
    protected void initAttributes(AttributeSet attrs, int defStyle) {
        super.initAttributes(attrs, defStyle);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.WheelHorizontalView, defStyle, 0);
        mSelectionDividerWidth = a.getDimensionPixelSize(R.styleable.WheelHorizontalView_selectionDividerWidth, DEF_SELECTION_DIVIDER_SIZE);
        a.recycle();
    }

    @Override
    public void setSelectorPaintCoeff(float coeff) {
        LinearGradient shader;

        int w = getMeasuredWidth();
        int iw = getItemDimension();
        float p1 = (1 - iw/(float) w)/2;
        float p2 = (1 + iw/(float) w)/2;
        float z = mItemsDimmedAlpha * (1 - coeff);
        float c1f = z + 255 * coeff;

        if (mVisibleItems == 2) {
            int c1 = Math.round( c1f ) << 24;
            int c2 = Math.round( z ) << 24;
            int[] colors =      {c2, c1, 0xff000000, 0xff000000, c1, c2};
            float[] positions = { 0, p1,     p1,         p2,     p2,  1};
            shader = new LinearGradient(0, 0, w, 0, colors, positions, Shader.TileMode.CLAMP);
        } else {
            float p3 = (1 - iw*3/(float) w)/2;
            float p4 = (1 + iw*3/(float) w)/2;

            float s = 255 * p3/p1;
            float c3f = s * coeff ; // here goes some optimized stuff
            float c2f = z + c3f;

            int c1 = Math.round( c1f ) << 24;
            int c2 = Math.round( c2f ) << 24;
            int c3 = Math.round( c3f ) << 24;

            int[] colors =      {0, c3, c2, c1, 0xff000000, 0xff000000, c1, c2, c3, 0};
            float[] positions = {0, p3, p3, p1,     p1,         p2,     p2, p4, p4, 1};
            shader = new LinearGradient(0, 0, w, 0, colors, positions, Shader.TileMode.CLAMP);
        }
        mSelectorWheelPaint.setShader(shader);
        invalidate();
    }


    //--------------------------------------------------------------------------
    //
    //  Scroller-specific methods
    //
    //--------------------------------------------------------------------------

    @Override
    protected WheelScroller createScroller(WheelScroller.ScrollingListener scrollingListener) {
        return new WheelHorizontalScroller(getContext(), scrollingListener);
    }

    @Override
    protected float getMotionEventPosition(MotionEvent event) {
        return event.getY();
    }


    //--------------------------------------------------------------------------
    //
    //  Base measurements
    //
    //--------------------------------------------------------------------------

    @Override
    protected int getBaseDimension() {
        return getWidth();
    }

    /**
     * Returns height of widget item
     * @return the item height
     */
    @Override
    protected int getItemDimension() {
        if (itemWidth != 0) {
            return itemWidth;
        }

        if (this.getChildAt(0) != null) {
            itemWidth = this.getChildAt(0).getMeasuredWidth();
            return itemWidth;
        }

        return getBaseDimension() / mVisibleItems;
    }


    //--------------------------------------------------------------------------
    //
    //  Layout creation and measurement operations
    //
    //--------------------------------------------------------------------------

    @Override
    protected void initData(Context context) {
        super.initData(context);
        this.setOrientation(LinearLayout.HORIZONTAL);
    }

/*
    @Override
    protected void measureLayout() {
        this.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        this.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(getHeight() - 2 * mItemPadding, MeasureSpec.EXACTLY)
        );
    }
*/
    //XXX: Most likely, measurements of mItemsLayout or/and its children are done inconrrectly.
    // Investigate and fix it

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        rebuildItems(); // rebuilding before measuring

        // int height = calculateLayoutHeight(heightSize, heightMode); XXX: This is causing stack overflow
        int height = 50;

        int width;
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = Math.max(
                    getItemDimension() * (mVisibleItems - mItemOffsetPercent / 100),
                    getSuggestedMinimumWidth()
            );

            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(width, widthSize);
            }
        }
        setMeasuredDimension(width, height);
    }


    /**
     * Calculates control height and creates text layouts
     * @param heightSize the input layout height
     * @param mode the layout mode
     * @return the calculated control height
     */
    private int calculateLayoutHeight(int heightSize, int mode) {
        this.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        this.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.UNSPECIFIED)
        );
        int height = this.getMeasuredHeight();

        if (mode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height += 2 * mItemPadding;

            // Check against our minimum width
            height = Math.max(height, getSuggestedMinimumHeight());

            if (mode == MeasureSpec.AT_MOST && heightSize < height) {
                height = heightSize;
            }
        }
        // forcing recalculating
        this.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(height - 2 * mItemPadding, MeasureSpec.EXACTLY)
        );

        return height;
    }


    //--------------------------------------------------------------------------
    //
    //  Drawing items
    //
    //--------------------------------------------------------------------------

    @Override
    protected void drawItems(Canvas canvas) {
        /*
        canvas.save();
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        int iw = getItemDimension();

        // resetting intermediate bitmap and recreating canvases
        mSpinBitmap.eraseColor(0);
        Canvas c = new Canvas(mSpinBitmap);
        Canvas cSpin = new Canvas(mSpinBitmap);

        int left = (mCurrentItemIdx - mFirstItemIdx) * iw + (iw - getWidth()) / 2;
        c.translate(- left + mScrollingOffset, mItemPadding);
        Log.e(LOG_TAG, "Items total " + this.getChildCount());
        super.draw(c);
        for (int i = 0; i < this.getChildCount(); i++) {
            View v = this.getChildAt(i);
            v.draw(c);
            Log.e(LOG_TAG, "Item #" + i + ", " + v.getMeasuredWidth() + "x" + v.getMeasuredHeight() + " // "  + v.getLeft() + ", " + v.getTop() + ", " + v.getRight() + ", " + v.getBottom());
        }

        mSeparatorsBitmap.eraseColor(0);
        Canvas cSeparators = new Canvas(mSeparatorsBitmap);

        if (mSelectionDivider != null) {
            // draw the top divider
            int leftOfLeftDivider = (getWidth() - iw - mSelectionDividerWidth) / 2;
            int rightOfLeftDivider = leftOfLeftDivider + mSelectionDividerWidth;
            mSelectionDivider.setBounds(leftOfLeftDivider, 0, rightOfLeftDivider, getHeight());
            mSelectionDivider.draw(cSeparators);

            // draw the bottom divider
            int leftOfRightDivider =  leftOfLeftDivider + iw;
            int rightOfRightDivider = rightOfLeftDivider + iw;
            mSelectionDivider.setBounds(leftOfRightDivider, 0, rightOfRightDivider, getHeight());
            mSelectionDivider.draw(cSeparators);
        }

        cSpin.drawRect(0, 0, w, h, mSelectorWheelPaint);
        cSeparators.drawRect(0, 0, w, h, mSeparatorsPaint);

        canvas.drawBitmap(mSpinBitmap, 0, 0, null);
        canvas.drawBitmap(mSeparatorsBitmap, 0, 0, null);
        canvas.restore();
        */
    }

}
