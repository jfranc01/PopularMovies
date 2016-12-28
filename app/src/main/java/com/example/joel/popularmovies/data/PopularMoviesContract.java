package com.example.joel.popularmovies.data;

/**
 * Created by joel on 2016-12-28.
 */

import android.provider.BaseColumns;

/**
 * Define the table and the columns for the Popular movie database
 */
public class PopularMoviesContract {

    //We create one static inner class for each table

    public static final class FavouriteEntry implements BaseColumns{

        //Table Name
        public static final String TABLE_NAME = "favourites";

        //Constants for the columns
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_IMGURL = "imageurl";
        public static final String COLUMN_NAME_SYNOPSIS = "synopsis";
        public static final String COLUMN_NAME_RATING = "rating";
        public static final String COLUMN_NAME_RELEASE = "release";

    }

}
