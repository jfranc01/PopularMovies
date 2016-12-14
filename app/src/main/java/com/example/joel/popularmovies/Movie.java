package com.example.joel.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import java.net.URL;

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

    }
}
