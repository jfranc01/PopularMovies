package com.example.joel.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import java.net.URL;


/***
 * NOTE:
 * You should be able to this from Android studio. The easiest way is going to the bottom
 * right corner of the Android Studio window where you should see the text "Git: branch name",
 * in your case it should say "Git: master". Click on and it will show a small menu consisting
 * of the different branches available both locally and remotely, also there should be an option
 * "+ New Branch" which will create a new branch for you and switch you to it. Now you should be
 * able to change some code, commit it and push it to remote.Merging and checking out branches can
 * also be done from that same menu. The same thing can als be done from the menubar option "VCS"
 */

/**
 * Created by joel on 2016-12-13.
 * This Movie class will hold details about
 * each movie
 */

public class Movie implements Parcelable{

    private String mTtile;
    private String mImageUrl;
    private String mSynopsis;
    private String mRating;
    private String mReleaseDate;
    private String mId;
    private boolean mIsFav = false;

    public Movie(){

    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public String getmTtile() {
        return mTtile;
    }

    public String getmImageUrl() {
        return mImageUrl;
    }

    public String getmSynopsis() {
        return mSynopsis;
    }

    public String getmReleaseDate(){
        return mReleaseDate;
    }

    public String getmRating() {
        return mRating;
    }

    public void setmTtile(String mTtile) {
        this.mTtile = mTtile;
    }

    public void setmImageUrl(String mImageUrl) {
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

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public boolean ismIsFav() {
        return mIsFav;
    }

    public void setmIsFav(boolean mIsFav) {
        this.mIsFav = mIsFav;
    }

    /**
     * Read back the fields from the parcel based on the order
     * in which they were written - title, rating, synopsis, release date, image url
     * @param parcel
     */
    public Movie (Parcel parcel){

        mTtile = parcel.readString();
        mRating = parcel.readString();
        mSynopsis = parcel.readString();
        mReleaseDate = parcel.readString();
        mImageUrl = parcel.readString();
        mIsFav = parcel.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTtile);
        dest.writeString(mRating);
        dest.writeString(mSynopsis);
        dest.writeString(mReleaseDate);
        dest.writeString(mImageUrl);
        dest.writeByte((byte)(mIsFav? 1: 0));
    }
}
