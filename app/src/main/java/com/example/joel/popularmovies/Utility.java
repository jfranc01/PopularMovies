package com.example.joel.popularmovies;

import android.content.ContentValues;
import android.util.Log;

import com.example.joel.popularmovies.data.PopularMoviesContract;
import com.example.joel.popularmovies.model.Movie;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by joel on 2016-12-15.
 */

public class Utility {

    final static String LOG_TAG = Utility.class.getSimpleName();
    final static String DATE_FORMAT = "yyyy-mm-dd";
    /**
     * Method will take in a date string and pass only the
     * YEAR back as a String
     * @param dateString
     * @return
     */
    public static String formatDate (String dateString){
        String expectedPattern = DATE_FORMAT;
        SimpleDateFormat formatter = new SimpleDateFormat(expectedPattern);
        try {
            Date date = formatter.parse(dateString);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return(String.valueOf(cal.get(Calendar.YEAR)));
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Error parsing the date " + e.getMessage() );
        }
        return null;
    }

    /**
     * This method will create a ContentValues object
     * based on the Movie object that is passed in to the
     * database
     * @param movie
     * @return
     */
    public static ContentValues createContentValues(Movie movie){
        ContentValues cv  = new ContentValues();
        cv.put(PopularMoviesContract.FavouriteEntry.COLUMN_NAME_TITLE, movie.getmTtile());
        cv.put(PopularMoviesContract.FavouriteEntry.COLUMN_NAME_SYNOPSIS, movie.getmSynopsis());
        cv.put(PopularMoviesContract.FavouriteEntry.COLUMN_NAME_RELEASE, movie.getmReleaseDate());
        cv.put(PopularMoviesContract.FavouriteEntry.COLUMN_NAME_IMGURL, movie.getmImageUrl());
        cv.put(PopularMoviesContract.FavouriteEntry.COLUMN_NAME_RATING, movie.getmRating());
        cv.put(PopularMoviesContract.FavouriteEntry.COLUMN_MOVIE_ID, movie.getmMovieID());
        return cv;
    }
}
