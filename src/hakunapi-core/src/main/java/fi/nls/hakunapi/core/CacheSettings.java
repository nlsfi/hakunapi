package fi.nls.hakunapi.core;

public class CacheSettings {

    private final int maximumSize;
    private final long refreshAfterMs;
    private final long expireAfterMs;

    public CacheSettings(int maximumSize, long refreshAfterMs, long expireAfterMs) {
        this.maximumSize = maximumSize;
        this.refreshAfterMs = refreshAfterMs;
        this.expireAfterMs = expireAfterMs;
    }

    public int getMaximumSize() {
        return maximumSize;
    }

    public long getRefreshAfterMs() {
        return refreshAfterMs;
    }

    public long getExpireAfterMs() {
        return expireAfterMs;
    }

}
