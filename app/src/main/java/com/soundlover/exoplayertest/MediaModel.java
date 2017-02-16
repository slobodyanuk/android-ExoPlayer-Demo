package com.soundlover.exoplayertest;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

/**
 * Created by Sergiy on 06.02.2017.
 */

@Parcel(Parcel.Serialization.BEAN)
public class MediaModel {

    public String id;
    public String mUrl;
    private long mDuration;
    private long mProgress;
    private int mDownloadStatus;
    private String mLocalMediaUrl;
    private int mStatus;
    private String mTitle;

    @ParcelConstructor
    public MediaModel(String url) {
        mUrl = url;
    }

    public MediaModel(MediaModel currentMedia) {
        this.id = currentMedia.getId();
        this.mUrl = currentMedia.getUrl();
        this.mDuration = currentMedia.getDuration();
        this.mProgress = currentMedia.getProgress();
        this.mLocalMediaUrl = currentMedia.getLocalMediaUrl();
        this.mDownloadStatus = currentMedia.getDownloadStatus();
    }

    public String getUrl() {
        return mUrl;
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }

    public long getProgress() {
        return mProgress;
    }

    public void setProgress(long progress) {
        mProgress = progress;
    }

    public int getDownloadStatus() {
        return mDownloadStatus;
    }

    public void setDownloadStatus(int downloadStatus) {
        mDownloadStatus = downloadStatus;
    }

    public String getLocalMediaUrl() {
        return mLocalMediaUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setStatus(int status) {
        mStatus = status;
    }

    public int getStatus() {
        return mStatus;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }
}
