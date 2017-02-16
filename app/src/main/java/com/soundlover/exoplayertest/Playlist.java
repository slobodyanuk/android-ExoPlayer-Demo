package com.soundlover.exoplayertest;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

import java.util.LinkedList;

/**
 * Created by Sergiy on 10.02.2017.
 */

@Parcel(Parcel.Serialization.BEAN)
public class Playlist {

    private LinkedList<MediaModel> mTracks;

    private int mCurrentIndex;

    @ParcelConstructor
    public Playlist(int currentIndex, LinkedList<MediaModel> tracks) {
        this.mTracks = tracks;
        this.mCurrentIndex = currentIndex;
    }

    public void addToEnd(MediaModel track) {
        mTracks.addLast(track);
        if (mCurrentIndex == -1) {
            mCurrentIndex = 0;
        }
    }


    public void addToBeginning(MediaModel track) {
        mTracks.addFirst(track);
        if (mCurrentIndex == -1) {
            mCurrentIndex = 0;
        }
    }

    public boolean previous() {
        if (mCurrentIndex - 1 >= 0) {
            mCurrentIndex--;
            return true;
        }
        return false;
    }

    public boolean next() {
        if (mCurrentIndex + 1 < mTracks.size()) {
            mCurrentIndex++;
            return true;
        }
        return false;
    }

    public LinkedList<MediaModel> getTracks() {
        return mTracks;
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    public boolean moveTo(MediaModel track) {
        for (int i = 0; i < mTracks.size(); i++) {

            if (mTracks.get(i).equals(track)) {
                mCurrentIndex = i;
                return true;
            }
        }
        return false;
    }
}
