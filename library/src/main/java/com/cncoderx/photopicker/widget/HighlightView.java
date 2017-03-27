/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cncoderx.photopicker.widget;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.view.View;

// This class is used by CropImage to display a highlighted cropping rectangle
// overlayed with the image. There are two coordinate spaces in use. One is
// image, another is screen. computeLayout() uses mMatrix to map from image
// space to screen space.
public class HighlightView {

    @SuppressWarnings("unused")
    private static final String TAG = "HighlightView";
    View mContext;  // The View displaying the image.

    public static final int GROW_NONE        = (1 << 0);
    public static final int GROW_LEFT_EDGE   = (1 << 1);
    public static final int GROW_RIGHT_EDGE  = (1 << 2);
    public static final int GROW_TOP_EDGE    = (1 << 3);
    public static final int GROW_BOTTOM_EDGE = (1 << 4);
    public static final int MOVE             = (1 << 5);

    public HighlightView(View ctx) {
        mContext = ctx;
    }

    boolean mIsFocused;
    boolean mHidden;

    public boolean hasFocus() {
        return mIsFocused;
    }

    public void setFocus(boolean f) {
        mIsFocused = f;
    }

    public void setHidden(boolean hidden) {
        mHidden = hidden;
    }

    protected void draw(Canvas canvas) {
        if (mHidden) {
            return;
        }
        if (!hasFocus()) {
            mOutlinePaint.setColor(0xFF000000);
            canvas.drawRect(mDrawRect, mOutlinePaint);
        } else {
            drawFocusBounds(canvas);
            drawOutline(canvas);
            drawCorner(canvas);
        }
    }

    private void drawFocusBounds(Canvas canvas) {
        canvas.save();
        Path path = new Path();
        Rect viewDrawingRect = new Rect();
        mContext.getDrawingRect(viewDrawingRect);
        if (mCircle) {
            float width  = mDrawRect.width();
            float height = mDrawRect.height();
            path.addCircle(mDrawRect.left + (width  / 2),
                           mDrawRect.top + (height / 2),
                           width / 2,
                           Path.Direction.CW);
        } else {
            path.addRect(new RectF(mDrawRect), Path.Direction.CW);
        }
        canvas.clipPath(path, Region.Op.DIFFERENCE);
        canvas.drawRect(viewDrawingRect,
                hasFocus() ? mFocusPaint : mNoFocusPaint);
        canvas.restore();
    }

    private void drawOutline(Canvas canvas) {
        float width  = mDrawRect.width();
        float height = mDrawRect.height();
        mOutlinePaint.setColor(Color.WHITE);
        mOutlinePaint.setAlpha(100);
        canvas.drawLine(
                mDrawRect.left + (width / 3),
                mDrawRect.top,
                mDrawRect.left + (width / 3),
                mDrawRect.bottom,
                mOutlinePaint);
        canvas.drawLine(
                mDrawRect.left + (width * 2 / 3),
                mDrawRect.top,
                mDrawRect.left + (width * 2 / 3),
                mDrawRect.bottom,
                mOutlinePaint);
        canvas.drawLine(
                mDrawRect.left,
                mDrawRect.top + (height / 3),
                mDrawRect.right,
                mDrawRect.top + (height / 3),
                mOutlinePaint);
        canvas.drawLine(
                mDrawRect.left,
                mDrawRect.top + (height * 2 / 3),
                mDrawRect.right,
                mDrawRect.top + (height * 2 / 3),
                mOutlinePaint);
        mOutlinePaint.setAlpha(255);
        canvas.drawRect(mDrawRect, mOutlinePaint);
    }

    private void drawCorner(Canvas canvas) {
        final int cornerWidth = 10;
        final int cornerLength = 50;
        mOutlinePaint.setStrokeWidth(6F);
        Path lines = new Path();
        lines.moveTo(
                mDrawRect.left - cornerWidth,
                mDrawRect.top + cornerLength);
        lines.lineTo(
                mDrawRect.left - cornerWidth,
                mDrawRect.top - cornerWidth);
        lines.lineTo(
                mDrawRect.left + cornerLength,
                mDrawRect.top - cornerWidth);
        lines.moveTo(
                mDrawRect.right - cornerLength,
                mDrawRect.top - cornerWidth);
        lines.lineTo(
                mDrawRect.right + cornerWidth,
                mDrawRect.top - cornerWidth);
        lines.lineTo(
                mDrawRect.right + cornerWidth,
                mDrawRect.top + cornerLength);
        lines.moveTo(
                mDrawRect.right + cornerWidth,
                mDrawRect.bottom - cornerLength);
        lines.lineTo(
                mDrawRect.right + cornerWidth,
                mDrawRect.bottom + cornerWidth);
        lines.lineTo(
                mDrawRect.right - cornerLength,
                mDrawRect.bottom + cornerWidth);
        lines.moveTo(
                mDrawRect.left - cornerWidth,
                mDrawRect.bottom - cornerLength);
        lines.lineTo(
                mDrawRect.left - cornerWidth,
                mDrawRect.bottom + cornerWidth);
        lines.lineTo(
                mDrawRect.left + cornerLength,
                mDrawRect.bottom + cornerWidth);
        canvas.drawPath(lines, mOutlinePaint);
        mOutlinePaint.setStrokeWidth(3F);
    }

    public void setMode(ModifyMode mode) {
        if (mode != mMode) {
            mMode = mode;
            mContext.invalidate();
        }
    }

    // Determines which edges are hit by touching at (x, y).
    public int getHit(float x, float y) {
        Rect r = computeLayout();
        final float hysteresis = 50F;//20F;
        int retval = GROW_NONE;
        if (x > r.left - hysteresis &&
                x < r.left + hysteresis &&
                y > r.top - hysteresis &&
                y < r.top + hysteresis) {
            retval |= GROW_LEFT_EDGE;
            retval |= GROW_TOP_EDGE;
        } else if (x > r.right - hysteresis &&
                x < r.right + hysteresis &&
                y > r.top - hysteresis &&
                y < r.top + hysteresis) {
            retval |= GROW_RIGHT_EDGE;
            retval |= GROW_TOP_EDGE;
        } else if (x > r.right - hysteresis &&
                x < r.right + hysteresis &&
                y > r.bottom - hysteresis &&
                y < r.bottom + hysteresis) {
            retval |= GROW_RIGHT_EDGE;
            retval |= GROW_BOTTOM_EDGE;
        } else if (x > r.left - hysteresis &&
                x < r.left + hysteresis &&
                y > r.bottom - hysteresis &&
                y < r.bottom + hysteresis) {
            retval |= GROW_LEFT_EDGE;
            retval |= GROW_BOTTOM_EDGE;
        }else if (r.contains((int) x, (int) y)) {
            retval = MOVE;
        }
        return retval;
    }

    // Handles motion (dx, dy) in screen space.
    // The "edge" parameter specifies which edges the user is dragging.
    void handleMotion(int edge, float dx, float dy) {
        Rect r = computeLayout();
        if (edge == GROW_NONE) {
            return;
        } else if (edge == MOVE) {
            // Convert to image space before sending to moveBy().
            moveBy(dx * (mCropRect.width() / r.width()),
                   dy * (mCropRect.height() / r.height()));
        } else {
            if (((GROW_LEFT_EDGE | GROW_RIGHT_EDGE) & edge) == 0) {
                dx = 0;
            }

            if (((GROW_TOP_EDGE | GROW_BOTTOM_EDGE) & edge) == 0) {
                dy = 0;
            }

            // Convert to image space before sending to growBy().
            float xDelta = dx * (mCropRect.width() / r.width());
            float yDelta = dy * (mCropRect.height() / r.height());
            growBy((((edge & GROW_LEFT_EDGE) != 0) ? -1 : 1) * xDelta,
                    (((edge & GROW_TOP_EDGE) != 0) ? -1 : 1) * yDelta);
        }
    }

    // Grows the cropping rectange by (dx, dy) in image space.
    void moveBy(float dx, float dy) {
        Rect invalRect = new Rect(mDrawRect);

        mCropRect.offset(dx, dy);

        // Put the cropping rectangle inside image rectangle.
        mCropRect.offset(
                Math.max(0, mImageRect.left - mCropRect.left),
                Math.max(0, mImageRect.top  - mCropRect.top));

        mCropRect.offset(
                Math.min(0, mImageRect.right  - mCropRect.right),
                Math.min(0, mImageRect.bottom - mCropRect.bottom));

        mDrawRect = computeLayout();
        invalRect.union(mDrawRect);
        invalRect.inset(-10, -10);
        mContext.invalidate(invalRect);
    }

    // Grows the cropping rectange by (dx, dy) in image space.
    void growBy(float dx, float dy) {
        if (mMaintainAspectRatio) {
            if (dx != 0) {
                dy = dx / mInitialAspectRatio;
            } else if (dy != 0) {
                dx = dy * mInitialAspectRatio;
            }
        }

        // Don't let the cropping rectangle grow too fast.
        // Grow at most half of the difference between the image rectangle and
        // the cropping rectangle.
        RectF r = new RectF(mCropRect);
        if (dx > 0F && r.width() + 2 * dx > mImageRect.width()) {
            float adjustment = (mImageRect.width() - r.width()) / 2F;
            dx = adjustment;
            if (mMaintainAspectRatio) {
                dy = dx / mInitialAspectRatio;
            }
        }
        if (dy > 0F && r.height() + 2 * dy > mImageRect.height()) {
            float adjustment = (mImageRect.height() - r.height()) / 2F;
            dy = adjustment;
            if (mMaintainAspectRatio) {
                dx = dy * mInitialAspectRatio;
            }
        }

        r.inset(-dx, -dy);

        // Don't let the cropping rectangle shrink too fast.
        final float widthCap = 50F;
        if (r.width() < widthCap) {
            r.inset(-(widthCap - r.width()) / 2F, 0F);
        }
        float heightCap = mMaintainAspectRatio
                ? (widthCap / mInitialAspectRatio)
                : widthCap;
        if (r.height() < heightCap) {
            r.inset(0F, -(heightCap - r.height()) / 2F);
        }

        // Put the cropping rectangle inside the image rectangle.
        if (r.left < mImageRect.left) {
            r.offset(mImageRect.left - r.left, 0F);
        } else if (r.right > mImageRect.right) {
            r.offset(-(r.right - mImageRect.right), 0);
        }
        if (r.top < mImageRect.top) {
            r.offset(0F, mImageRect.top - r.top);
        } else if (r.bottom > mImageRect.bottom) {
            r.offset(0F, -(r.bottom - mImageRect.bottom));
        }

        mCropRect.set(r);
        mDrawRect = computeLayout();
        mContext.invalidate();
    }

    // Returns the cropping rectangle in image space.
    public Rect getCropRect() {
        return new Rect((int) mCropRect.left, (int) mCropRect.top,
                        (int) mCropRect.right, (int) mCropRect.bottom);
    }

    // Maps the cropping rectangle from image space to screen space.
    private Rect computeLayout() {
        RectF r = new RectF(mCropRect.left, mCropRect.top,
                            mCropRect.right, mCropRect.bottom);
        mMatrix.mapRect(r);
        return new Rect(Math.round(r.left), Math.round(r.top),
                        Math.round(r.right), Math.round(r.bottom));
    }

    public void invalidate() {
        mDrawRect = computeLayout();
    }

    public void setup(Matrix m, Rect imageRect, RectF cropRect, boolean circle,
                      boolean maintainAspectRatio) {
        if (circle) {
            maintainAspectRatio = true;
        }
        mMatrix = new Matrix(m);

        mCropRect = cropRect;
        mImageRect = new RectF(imageRect);
        mMaintainAspectRatio = maintainAspectRatio;
        mCircle = circle;

        mInitialAspectRatio = mCropRect.width() / mCropRect.height();
        mDrawRect = computeLayout();

        mFocusPaint.setARGB(125, 50, 50, 50);
        mNoFocusPaint.setARGB(125, 50, 50, 50);
        mOutlinePaint.setStrokeWidth(3F);
        mOutlinePaint.setStyle(Paint.Style.STROKE);
        mOutlinePaint.setAntiAlias(true);

        mMode = ModifyMode.None;
    }

    enum ModifyMode { None, Move, Grow }

    private ModifyMode mMode = ModifyMode.None;

    Rect mDrawRect;  // in screen space
    private RectF mImageRect;  // in image space
    RectF mCropRect;  // in image space
    Matrix mMatrix;

    private boolean mMaintainAspectRatio = false;
    private float mInitialAspectRatio;
    private boolean mCircle = false;

    private final Paint mFocusPaint = new Paint();
    private final Paint mNoFocusPaint = new Paint();
    private final Paint mOutlinePaint = new Paint();
}
