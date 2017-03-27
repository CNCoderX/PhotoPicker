/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.cncoderx.photopicker.io;

import android.graphics.Bitmap;
import android.graphics.Point;

/**
 * Pool allowing the efficient reuse of bitmaps in order to avoid long
 * garbage collection pauses.
 */
public class GalleryBitmapPool {

    private static final int CAPACITY_BYTES = 20971520;

    // We found that Gallery uses bitmaps that are either square (for example,
    // tiles of large images or square thumbnails), match one of the common
    // photo aspect ratios (4x3, 3x2, or 16x9), or, less commonly, are of some
    // other aspect ratio. Taking advantage of this information, we use 3
    // SparseArrayBitmapPool instances to back the GalleryBitmapPool, which affords
    // O(1) lookups for square bitmaps, and average-case - but *not* asymptotically -
    // O(1) lookups for common photo aspect ratios and other miscellaneous aspect
    // ratios. Beware of the pathological case where there are many bitmaps added
    // to the pool with different non-square aspect ratios but the same width, as
    // performance will degrade and the average case lookup will approach
    // O(# of different aspect ratios).
    private static final int POOL_INDEX_NONE = -1;
    private static final int POOL_INDEX_SQUARE = 0;
    private static final int POOL_INDEX_PHOTO = 1;
    private static final int POOL_INDEX_MISC = 2;

    private static final Point[] COMMON_PHOTO_ASPECT_RATIOS =
        { new Point(4, 3), new Point(3, 2), new Point(16, 9) };

    private int mCapacityBytes;
    private SparseArrayBitmapPool [] mPools;
    private Pools.Pool<SparseArrayBitmapPool.Node> mSharedNodePool = new Pools.SynchronizedPool<SparseArrayBitmapPool.Node>(128);

    private GalleryBitmapPool(int capacityBytes) {
        mPools = new SparseArrayBitmapPool[3];
        mPools[POOL_INDEX_SQUARE] = new SparseArrayBitmapPool(capacityBytes / 3, mSharedNodePool);
        mPools[POOL_INDEX_PHOTO] = new SparseArrayBitmapPool(capacityBytes / 3, mSharedNodePool);
        mPools[POOL_INDEX_MISC] = new SparseArrayBitmapPool(capacityBytes / 3, mSharedNodePool);
        mCapacityBytes = capacityBytes;
    }

    private static GalleryBitmapPool sInstance = new GalleryBitmapPool(CAPACITY_BYTES);

    public static GalleryBitmapPool getInstance() {
        return sInstance;
    }

    private SparseArrayBitmapPool getPoolForDimensions(int width, int height) {
        int index = getPoolIndexForDimensions(width, height);
        if (index == POOL_INDEX_NONE) {
            return null;
        } else {
            return mPools[index];
        }
    }

    private int getPoolIndexForDimensions(int width, int height) {
        if (width <= 0 || height <= 0) {
            return POOL_INDEX_NONE;
        }
        if (width == height) {
            return POOL_INDEX_SQUARE;
        }
        int min, max;
        if (width > height) {
            min = height;
            max = width;
        } else {
            min = width;
            max = height;
        }
        for (Point ar : COMMON_PHOTO_ASPECT_RATIOS) {
            if (min * ar.x == max * ar.y) {
                return POOL_INDEX_PHOTO;
            }
        }
        return POOL_INDEX_MISC;
    }

    /**
     * @return Capacity of the pool in bytes.
     */
    public synchronized int getCapacity() {
        return mCapacityBytes;
    }

    /**
     * @return Approximate total size in bytes of the bitmaps stored in the pool.
     */
    public int getSize() {
        // Note that this only returns an approximate size, since multiple threads
        // might be getting and putting Bitmaps from the pool and we lock at the
        // sub-pool level to avoid unnecessary blocking.
        int total = 0;
        for (SparseArrayBitmapPool p : mPools) {
            total += p.getSize();
        }
        return total;
    }

    /**
     * @return Bitmap from the pool with the desired height/width or null if none available.
     */
    public Bitmap get(int width, int height) {
        SparseArrayBitmapPool pool = getPoolForDimensions(width, height);
        if (pool == null) {
            return null;
        } else {
            return pool.get(width, height);
        }
    }

    /**
     * Adds the given bitmap to the pool.
     * @return Whether the bitmap was added to the pool.
     */
    public boolean put(Bitmap b) {
        if (b == null || b.getConfig() != Bitmap.Config.ARGB_8888) {
            return false;
        }
        SparseArrayBitmapPool pool = getPoolForDimensions(b.getWidth(), b.getHeight());
        if (pool == null) {
            b.recycle();
            return false;
        } else {
            return pool.put(b);
        }
    }

    /**
     * Empty the pool, recycling all the bitmaps currently in it.
     */
    public void clear() {
        for (SparseArrayBitmapPool p : mPools) {
            p.clear();
        }
    }
}
