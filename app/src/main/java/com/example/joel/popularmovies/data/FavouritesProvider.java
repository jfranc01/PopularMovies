package com.example.joel.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by joel on 2016-12-28.
 */

public class FavouritesProvider extends ContentProvider {
    /*
        Adding integer constants for the URIs
     */
    public static final int FAVOURITES = 100; // to get all favourites
    public static final int FAVOURITES_ID = 101; // to view a favourite or delete a faovurite
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    static UriMatcher buildUriMatcher(){
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PopularMoviesContract.CONTENT_AUTHORITY.toString();
        matcher.addURI(authority, PopularMoviesContract.PATH_FAVOURITES, 100 );
        matcher.addURI(authority, PopularMoviesContract.PATH_FAVOURITES + "/#" , 101);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
