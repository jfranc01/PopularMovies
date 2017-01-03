package com.example.joel.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by joel on 2016-12-28.
 */

public class PopularMoviesDbHelper extends SQLiteOpenHelper {

    public static int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "popularmovies.db";

    public PopularMoviesDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a table called Favourites that will hold the "favouritied" movies
        // along with their details.
        final String CREATE_FAVORITES_TABLE = "CREATE TABLE " +
                PopularMoviesContract.FavouriteEntry.TABLE_NAME + " ( " +
                PopularMoviesContract.FavouriteEntry._ID + " INTEGER PRIMARY KEY," +
                PopularMoviesContract.FavouriteEntry.COLUMN_NAME_TITLE + " TEXT UNIQUE NOT NULL," +
                PopularMoviesContract.FavouriteEntry.COLUMN_NAME_SYNOPSIS + " TEXT NOT NULL," +
                PopularMoviesContract.FavouriteEntry.COLUMN_NAME_RELEASE + " TEXT NOT NULL," +
                PopularMoviesContract.FavouriteEntry.COLUMN_NAME_IMGURL + " TEXT NOT NULL," +
                PopularMoviesContract.FavouriteEntry.COLUMN_NAME_RATING + " TEXT NOT NULL" + " ) ";

        //create the table by calling the execute method
        db.execSQL(CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //For a BD Upgrade, we decide to drop the table and create it again
        db.execSQL("DROP TABLE IF EXISTS " + PopularMoviesContract.FavouriteEntry.TABLE_NAME);
        //call the onCreate
        onCreate(db);
    }

    /**to get the list of videos,
     {
     "id": "577bd56b9251411df60006f9",
     "iso_639_1": "en",
     "iso_3166_1": "US",
     "key": "UnlAXQze1Qg",
     "name": "A Look Inside",
     "site": "YouTube",
     "size": 720,
     "type": "Featurette"
     },
    https://www.youtube.com/watch?v=SUXWAEX2jlg*/
}
