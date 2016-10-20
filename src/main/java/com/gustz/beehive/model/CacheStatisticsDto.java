package com.gustz.beehive.model;

import java.io.Serializable;

/**
 * cache statistics
 *
 * @author zhangzhenfeng
 * @since 2016-10-17
 */
public class CacheStatisticsDto implements Serializable {

    private String cacheName;

    private long evictedCount;

    private long expiredCount;

    private long hitCount;

    private long missCount;

    private long missExpiredCount;

    private long putAddedCount;

    private double hitRatio;

    private long localDiskSizeInBytes;

    private long localHeapSizeInBytes;

    private long localOffHeapSizeInBytes;

    @Override
    public String toString() {
        return "CacheStatisticsDto{" +
                "cacheName='" + cacheName + '\'' +
                ", evictedCount=" + evictedCount +
                ", expiredCount=" + expiredCount +
                ", hitCount=" + hitCount +
                ", missCount=" + missCount +
                ", missExpiredCount=" + missExpiredCount +
                ", putAddedCount=" + putAddedCount +
                ", hitRatio=" + hitRatio +
                ", localDiskSizeInBytes=" + localDiskSizeInBytes +
                ", localHeapSizeInBytes=" + localHeapSizeInBytes +
                ", localOffHeapSizeInBytes=" + localOffHeapSizeInBytes +
                '}';
    }

    public long getLocalDiskSizeInBytes() {
        return localDiskSizeInBytes;
    }

    public void setLocalDiskSizeInBytes(long localDiskSizeInBytes) {
        this.localDiskSizeInBytes = localDiskSizeInBytes;
    }

    public long getLocalOffHeapSizeInBytes() {
        return localOffHeapSizeInBytes;
    }

    public void setLocalOffHeapSizeInBytes(long localOffHeapSizeInBytes) {
        this.localOffHeapSizeInBytes = localOffHeapSizeInBytes;
    }

    public long getLocalHeapSizeInBytes() {
        return localHeapSizeInBytes;
    }

    public void setLocalHeapSizeInBytes(long localHeapSizeInBytes) {
        this.localHeapSizeInBytes = localHeapSizeInBytes;
    }

    public double getHitRatio() {
        return hitRatio;
    }

    public void setHitRatio(double hitRatio) {
        this.hitRatio = hitRatio;
    }

    public long getEvictedCount() {
        return evictedCount;
    }

    public void setEvictedCount(long evictedCount) {
        this.evictedCount = evictedCount;
    }

    public long getExpiredCount() {
        return expiredCount;
    }

    public void setExpiredCount(long expiredCount) {
        this.expiredCount = expiredCount;
    }

    public long getHitCount() {
        return hitCount;
    }

    public void setHitCount(long hitCount) {
        this.hitCount = hitCount;
    }

    public long getMissCount() {
        return missCount;
    }

    public void setMissCount(long missCount) {
        this.missCount = missCount;
    }

    public long getMissExpiredCount() {
        return missExpiredCount;
    }

    public void setMissExpiredCount(long missExpiredCount) {
        this.missExpiredCount = missExpiredCount;
    }

    public long getPutAddedCount() {
        return putAddedCount;
    }

    public void setPutAddedCount(long putAddedCount) {
        this.putAddedCount = putAddedCount;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    // local disk
    public static class LocalDisk {

        private long hitCount;

        private long missCount;

        private long putAddedCount;

        @Override
        public String toString() {
            return "LocalDisk{" +
                    "hitCount=" + hitCount +
                    ", missCount=" + missCount +
                    ", putAddedCount=" + putAddedCount +
                    '}';
        }

        public long getHitCount() {
            return hitCount;
        }

        public void setHitCount(long hitCount) {
            this.hitCount = hitCount;
        }

        public long getPutAddedCount() {
            return putAddedCount;
        }

        public void setPutAddedCount(long putAddedCount) {
            this.putAddedCount = putAddedCount;
        }

        public long getMissCount() {
            return missCount;
        }

        public void setMissCount(long missCount) {
            this.missCount = missCount;
        }
    }

    // local heap
    public static class LocalHeap {

        private long hitCount;

        private long missCount;

        private long putAddedCount;

        @Override
        public String toString() {
            return "LocalHeap{" +
                    "hitCount=" + hitCount +
                    ", missCount=" + missCount +
                    ", putAddedCount=" + putAddedCount +
                    '}';
        }

        public long getHitCount() {
            return hitCount;
        }

        public void setHitCount(long hitCount) {
            this.hitCount = hitCount;
        }

        public long getMissCount() {
            return missCount;
        }

        public void setMissCount(long missCount) {
            this.missCount = missCount;
        }

        public long getPutAddedCount() {
            return putAddedCount;
        }

        public void setPutAddedCount(long putAddedCount) {
            this.putAddedCount = putAddedCount;
        }
    }

}
