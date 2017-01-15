package com.example.joel.popularmovies.data;

/**
 * Created by joel on 2016-12-28.
 */

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Define the table and the columns for the Popular movie database
 */
public class PopularMoviesContract {

    public static final String CONTENT_AUTHORITY = "com.example.joel.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" +  CONTENT_AUTHORITY);
    public static final String PATH_FAVOURITES = "favourites";
    public static final String PATH_MOVIES = "movies";

    //We create one static inner class for each table


    public static final class MovieEntry implements BaseColumns{
        //Table Name
        public static final String TABLE_NAME = "movies";

        //Constants for the columns
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_IMGURL = "imageurl";
        public static final String COLUMN_NAME_SYNOPSIS = "synopsis";
        public static final String COLUMN_NAME_RATING = "rating";
        public static final String COLUMN_NAME_RELEASE = "release";
        public static final String COLUMN_MOVIE_ID = "movieid";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIES).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        public static final String CONTENT_TYPE_ITEM = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        public static Uri buildPopularMoviesUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getMovieIDFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

    }

    public static final class FavouriteEntry implements BaseColumns{

        //Table Name
        public static final String TABLE_NAME = "favourites";

        //Constants for the columns
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_IMGURL = "imageurl";
        public static final String COLUMN_NAME_SYNOPSIS = "synopsis";
        public static final String COLUMN_NAME_RATING = "rating";
        public static final String COLUMN_NAME_RELEASE = "release";
        public static final String COLUMN_MOVIE_ID = "movieid";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_FAVOURITES).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_FAVOURITES;

        public static final String CONTENT_TYPE_ITEM = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_FAVOURITES;

        public static Uri buildPopularMoviesUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

}
