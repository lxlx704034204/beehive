package com.gustz.beehive.config.cache.conf;

/**
 * ehcache cache conf
 *
 * @author zhangzhenfeng
 * @since 2016-10-13
 */
public class EhcacheCacheConfig extends BaseCacheConfig {

    private String diskStorePath;

    private boolean eternal;

    private boolean overflowToOffHeap;

    private long maxElementsInMemory;

    private long maxElementsOnDisk;

    private long timeToIdleSeconds;

    private long timeToLiveSeconds;

    private long diskExpiryThreadIntervalSeconds;

    public String getDiskStorePath() {
        return diskStorePath;
    }

    public void setDiskStorePath(String diskStorePath) {
        this.diskStorePath = diskStorePath;
    }

    public boolean isOverflowToOffHeap() {
        return overflowToOffHeap;
    }

    public void setOverflowToOffHeap(boolean overflowToOffHeap) {
        this.overflowToOffHeap = overflowToOffHeap;
    }

    public long getMaxElementsInMemory() {
        return maxElementsInMemory;
    }

    public void setMaxElementsInMemory(long maxElementsInMemory) {
        this.maxElementsInMemory = maxElementsInMemory;
    }

    public long getMaxElementsOnDisk() {
        return maxElementsOnDisk;
    }

    public void setMaxElementsOnDisk(long maxElementsOnDisk) {
        this.maxElementsOnDisk = maxElementsOnDisk;
    }

    public long getTimeToIdleSeconds() {
        return timeToIdleSeconds;
    }

    public void setTimeToIdleSeconds(long timeToIdleSeconds) {
        this.timeToIdleSeconds = timeToIdleSeconds;
    }

    public long getTimeToLiveSeconds() {
        return timeToLiveSeconds;
    }

    public void setTimeToLiveSeconds(long timeToLiveSeconds) {
        this.timeToLiveSeconds = timeToLiveSeconds;
    }

    public long getDiskExpiryThreadIntervalSeconds() {
        return diskExpiryThreadIntervalSeconds;
    }

    public void setDiskExpiryThreadIntervalSeconds(long diskExpiryThreadIntervalSeconds) {
        this.diskExpiryThreadIntervalSeconds = diskExpiryThreadIntervalSeconds;
    }

    public boolean isEternal() {
        return eternal;
    }

    public void setEternal(boolean eternal) {
        this.eternal = eternal;
    }
}
