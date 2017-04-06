package com.newind.core;

/**
 * Created by newind on 17-4-6.
 */

public class NObject {
    public static native long createCache();
    public static native void destroyCache(long id);
    public static native int fetchAll(long cacheID,int type,long[] idList);
}
