package com.example.joel.popularmovies;

import java.net.URL;

/**
 * Created by joel on 2016-12-13.
 * This Movie class will hold details about
 * each movie
 */

public class Movie {

    private String mTtile;
    private URL mImageUrl;
    private String mSynopsis;
    private String mRating;
    private String mReleaseDate;

    public String getmTtile() {
        return mTtile;
    }

    public URL getmImageUrl() {
        return mImageUrl;
    }

    public String getmSynopsis() {
        return mSynopsis;
    }

    public String getmRating() {
        return mRating;
    }

    public void setmTtile(String mTtile) {
        this.mTtile = mTtile;
    }

    public void setmImageUrl(URL mImageUrl) {
        this.mImageUrl = mImageUrl;
    }

    public void setmSynopsis(String mSynopsis) {
        this.mSynopsis = mSynopsis;
    }

    public void setmRating(String mRating) {
        this.mRating = mRating;
    }

    public void setmReleaseDate(String mReleaseDate) {
        this.mReleaseDate = mReleaseDate;
    }
}
