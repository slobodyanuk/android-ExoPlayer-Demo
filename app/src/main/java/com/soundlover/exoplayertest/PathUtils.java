package com.soundlover.exoplayertest;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.File;

/**
 * Created by Sergiy on 06.02.2017.
 */

public class PathUtils {

    private static final int MAX_CACHE_SIZE = 250_000_000;

    public static CacheDataSourceFactory getCacheDataSource(File cacheDir, String userAgent) {
        Cache cache = new SimpleCache(cacheDir, new LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE));
        DataSource.Factory upstream = new DefaultHttpDataSourceFactory(userAgent, null,
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS, true);
        return new CacheDataSourceFactory(cache, upstream,
                CacheDataSource.FLAG_IGNORE_CACHE_FOR_UNSET_LENGTH_REQUESTS,
                CacheDataSource.DEFAULT_MAX_CACHE_FILE_SIZE);
    }


}
