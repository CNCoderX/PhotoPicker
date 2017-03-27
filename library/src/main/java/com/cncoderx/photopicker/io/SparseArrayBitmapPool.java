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
import android.util.SparseArray;

/**
 * Bitmap pool backed by a sparse array indexing linked lists of bitmaps
 * sharing the same width. Performance will degrade if using this to store
 * many bitmaps with the same width but many different heights.
 */
public class SparseArrayBitmapPool {

    private int mCapacityBytes;
    private SparseArray<Node> mStore = new SparseArray<Node>();
    private int mSizeBytes = 0;

    private Pools.Pool<Node> mNodePool;
    private Node mPoolNodesHead = null;
    private Node mPoolNodesTail = null;

    protected static class Node {
        Bitmap bitmap;

        // Each node is part of two doubly linked lists:
        // - A pool-level list (accessed by mPoolNodesHead and mPoolNodesTail)
        //   that is used for FIFO eviction of nodes when the pool gets full.
        // - A bucket-level list for each index of the sparse array, so that
        //   each index can store more than one item.
        Node prevInBucket;
        Node nextInBucket;
        Node nextInPool;
        Node prevInPool;
    }

    /**
     * @param capacityBytes Maximum capacity of the pool in bytes.
     * @param nodePool Shared pool to use for recycling linked list nodes, or null.
     */
    public SparseArrayBitmapPool(int capacityBytes, Pools.Pool<Node> nodePool) {
        mCapacityBytes = capacityBytes;
        if (nodePool == null) {
            mNodePool = new Pools.SimplePool<Node>(32);
        } else {
            mNodePool = nodePool;
        }
    }

    /**
     * Set the maximum capacity of the pool, and if necessary trim it down to size.
     */
    public synchronized void setCapacity(int capacityBytes) {
        mCapacityBytes = capacityBytes;

        // No-op unless current size exceeds the new capacity.
        freeUpCapacity(0);
    }

    private void freeUpCapacity(int bytesNeeded) {
        int targetSize = mCapacityBytes - bytesNeeded;
        // Repeatedly remove the oldest node until we have freed up at least bytesNeeded.
        while (mPoolNodesTail != null && mSizeBytes > targetSize) {
            unlinkAndRecycleNode(mPoolNodesTail, true);
        }
    }

    private void unlinkAndRecycleNode(Node n, boolean recycleBitmap) {
        // Unlink the node from its sparse array bucket list.
        if (n.prevInBucket != null) {
            // This wasn't the head, update the previous node.
            n.prevInBucket.nextInBucket = n.nextInBucket;
        } else {
            // This was the head of the bucket, replace it with the next node.
            mStore.put(n.bitmap.getWidth(), n.nextInBucket);
        }
        if (n.nextInBucket != null) {
            // This wasn't the tail, update the next node.
            n.nextInBucket.prevInBucket = n.prevInBucket;
        }

        // Unlink the node from the pool-wide list.
        if (n.prevInPool != null) {
            // This wasn't the head, update the previous node.
            n.prevInPool.nextInPool = n.nextInPool;
        } else {
            // This was the head of the pool-wide list, update the head pointer.
            mPoolNodesHead = n.nextInPool;
        }
        if (n.nextInPool != null) {
            // This wasn't the tail, update the next node.
            n.nextInPool.prevInPool = n.prevInPool;
        } else {
            // This was the tail, update the tail pointer.
            mPoolNodesTail = n.prevInPool;
        }

        // Recycle the node.
        n.nextInBucket = null;
        n.nextInPool = null;
        n.prevInBucket = null;
        n.prevInPool = null;
        mSizeBytes -= n.bitmap.getByteCount();
        if (recycleBitmap) n.bitmap.recycle();
        n.bitmap = null;
        mNodePool.release(n);
    }

    /**
     * @return Capacity of the pool in bytes.
     */
    public synchronized int getCapacity() {
        return mCapacityBytes;
    }

    /**
     * @return Total size in bytes of the bitmaps stored in the pool.
     */
    public synchronized int getSize() {
        return mSizeBytes;
    }

    /**
     * @return Bitmap from the pool with the desired height/width or null if none available.
     */
    public synchronized Bitmap get(int width, int height) {
        Node cur = mStore.get(width);

        // Traverse the list corresponding to the width bucket in the
        // sparse array, and unlink and return the first bitmap that
        // also has the correct height.
        while (cur != null) {
            if (cur.bitmap.getHeight() == height) {
                Bitmap b = cur.bitmap;
                unlinkAndRecycleNode(cur, false);
                return b;
            }
            cur = cur.nextInBucket;
        }
        return null;
    }

    /**
     * Adds the given bitmap to the pool.
     * @return Whether the bitmap was added to the pool.
     */
    public synchronized boolean put(Bitmap b) {
        if (b == null) {
            return false;
        }

        // Ensure there is enough room to contain the new bitmap.
        int bytes = b.getByteCount();
        freeUpCapacity(bytes);

        Node newNode = mNodePool.acquire();
        if (newNode == null) {
            newNode = new Node();
        }
        newNode.bitmap = b;

        // We append to the head, and freeUpCapacity clears from the tail,
        // resulting in FIFO eviction.
        newNode.prevInBucket = null;
        newNode.prevInPool = null;
        newNode.nextInPool = mPoolNodesHead;
        mPoolNodesHead = newNode;

        // Insert the node into its appropriate bucket based on width.
        int key = b.getWidth();
        newNode.nextInBucket = mStore.get(key);
        if (newNode.nextInBucket != null) {
            // The bucket already had nodes, update the old head.
            newNode.nextInBucket.prevInBucket = newNode;
        }
        mStore.put(key, newNode);

        if (newNode.nextInPool == null) {
            // This is the only node in the list, update the tail pointer.
            mPoolNodesTail = newNode;
        } else {
            newNode.nextInPool.prevInPool = newNode;
        }
        mSizeBytes += bytes;
        return true;
    }

    /**
     * Empty the pool, recycling all the bitmaps currently in it.
     */
    public synchronized void clear() {
        // Clearing is equivalent to ensuring all the capacity is available.
        freeUpCapacity(mCapacityBytes);
    }
}
