package com.example.joel.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.joel.popularmovies.data.PopularMoviesContract;
import com.example.joel.popularmovies.model.Movie;
import com.example.joel.popularmovies.sync.MovieSyncAdapter;

import org.json.JSONException;
import org.json.JSONObject;

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
     *
     * @param dateString
     * @return
     */
    public static String formatDate(String dateString) {
        String expectedPattern = DATE_FORMAT;
        SimpleDateFormat formatter = new SimpleDateFormat(expectedPattern);
        try {
            Date date = formatter.parse(dateString);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return (String.valueOf(cal.get(Calendar.YEAR)));
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Error parsing the date " + e.getMessage());
        }
        return null;
    }

    /**
     * This method will create a ContentValues object
     * based on the Movie object that is passed in to the
     * database
     *
     * @param movie
     * @return
     */
    public static ContentValues createFavContentValues(Movie movie) {
        ContentValues cv = new ContentValues();
        cv.put(PopularMoviesContract.FavouriteEntry.COLUMN_NAME_TITLE, movie.getmTtile());
        cv.put(PopularMoviesContract.FavouriteEntry.COLUMN_NAME_SYNOPSIS, movie.getmSynopsis());
        cv.put(PopularMoviesContract.FavouriteEntry.COLUMN_NAME_RELEASE, movie.getmReleaseDate());
        cv.put(PopularMoviesContract.FavouriteEntry.COLUMN_NAME_IMGURL, movie.getmImageUrl());
        cv.put(PopularMoviesContract.FavouriteEntry.COLUMN_NAME_RATING, movie.getmRating());
        cv.put(PopularMoviesContract.FavouriteEntry.COLUMN_MOVIE_ID, movie.getmMovieID());
        return cv;
    }

    /**
     * Method will create ContentValues by passing in a JSONObject
     *
     * @param movieObject
     * @return
     * @throws JSONException
     */
    public static ContentValues createMovieContentValuesFromJSON(JSONObject movieObject)
            throws JSONException {
        ContentValues movieValues = new ContentValues();
        movieValues.put(PopularMoviesContract.MovieEntry.COLUMN_NAME_TITLE,
                movieObject.getString(Constants.TITLE_KEY));
        movieValues.put(PopularMoviesContract.MovieEntry.COLUMN_NAME_RATING,
                movieObject.getString(Constants.RATING_KEY));
        movieValues.put(PopularMoviesContract.MovieEntry.COLUMN_NAME_SYNOPSIS,
                movieObject.getString(Constants.SYNOPSIS_KEY));
        movieValues.put(PopularMoviesContract.MovieEntry.COLUMN_NAME_RELEASE,
                Constants.RELEASE_DATE_KEY);
        Uri builtUri = Uri.parse(Constants.BASE_IMAGE_URL).buildUpon()
                .appendEncodedPath(movieObject.getString(Constants.IMAGE_URL_KEY)).build();
        movieValues.put(PopularMoviesContract.MovieEntry.COLUMN_NAME_IMGURL, builtUri.toString());
        movieValues.put(PopularMoviesContract.MovieEntry.COLUMN_MOVIE_ID,
                movieObject.getString(Constants.ID));

        return movieValues;
    }

    /**
     * Method will return the default category of the movie choice
     * by retrieving the shared Preferences. If one hasn't been set then
     * the default gets saved!
     *
     * @param context
     * @return
     */
    public static String getCurrentCategory(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        String category = sharedPrefs.getString(context.getString(R.string.sort_order_key),
                context.getString(R.string.sort_order_default));

        return category;
    }

    /**
     * Method will check the network info status and return
     * true or false
     * @param context
     * @return
     */
    public static Boolean isNetworkConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @SuppressWarnings("ResourceType")
    static public @MovieSyncAdapter.ServerStatus int getServerStatusFromPreferences(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(context.getString(R.string.pref_server_status_key),
                MovieSyncAdapter.MOVIE_UNKNOWN_ERROR);
    }
}
