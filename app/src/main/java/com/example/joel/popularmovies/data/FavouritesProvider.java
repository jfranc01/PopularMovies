package com.example.joel.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
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
    public static final int FAVOURITES_TITLE = 102; //query for a movie with the title
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private PopularMoviesDbHelper dbHelpher;

    static UriMatcher buildUriMatcher(){
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PopularMoviesContract.CONTENT_AUTHORITY.toString();
        matcher.addURI(authority, PopularMoviesContract.PATH_FAVOURITES, 100 );
        matcher.addURI(authority, PopularMoviesContract.PATH_FAVOURITES + "/#" , 101);
        matcher.addURI(authority, PopularMoviesContract.PATH_FAVOURITES + "/*" , 102);


        return matcher;
    }

    @Override
    public boolean onCreate() {
        dbHelpher = new PopularMoviesDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection,
                        String selection, String[] selectionArgs, String sortOrder) {

        int match = sUriMatcher.match(uri);
        Cursor retCursor;
        switch (match){
            case FAVOURITES:
                retCursor = dbHelpher.getReadableDatabase().query(
                        PopularMoviesContract.FavouriteEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case FAVOURITES_ID:
                //get the ID that was passed in
                String id = String.valueOf(ContentUris.parseId(uri));
                //create the selection statement
                final String favWithIDSelection = PopularMoviesContract.FavouriteEntry.TABLE_NAME +
                        "." + PopularMoviesContract.FavouriteEntry._ID + " = ? ";
                //return the cursor
                retCursor =  dbHelpher.getReadableDatabase().query(
                        PopularMoviesContract.FavouriteEntry.TABLE_NAME,
                        projection,
                        favWithIDSelection,
                        new String[]{id},
                        null,
                        null,
                        sortOrder
                );
            break;

            case FAVOURITES_TITLE:
                retCursor = dbHelpher.getReadableDatabase()
                        .query(
                                PopularMoviesContract.FavouriteEntry.TABLE_NAME,
                                projection,
                                selection,
                                selectionArgs,
                                null,
                                null,
                                sortOrder
                        );
                break;
            default: throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;

    }

    @Nullable
    @Override
    /**
     * This method will help the Provider decide whethere a DIR
     *  or a SINGLE_ITEM was queried
     */
    public String getType(Uri uri) {
        int match = sUriMatcher.match(uri);
        switch(match){
            case FAVOURITES: {
                return  PopularMoviesContract.FavouriteEntry.CONTENT_TYPE;

            }

            case FAVOURITES_ID: {
                return  PopularMoviesContract.FavouriteEntry.CONTENT_TYPE_ITEM;
            }

            case FAVOURITES_TITLE: {
                return PopularMoviesContract.FavouriteEntry.CONTENT_TYPE_ITEM;
            }

            default: throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match){
            case FAVOURITES : {
                long _id = dbHelpher.getWritableDatabase().insert(
                        PopularMoviesContract.FavouriteEntry.TABLE_NAME,
                        null,
                        values
                        );
                if(_id>0){
                    returnUri = PopularMoviesContract.FavouriteEntry.buildPopularMoviesUri(_id);
                }
                else{
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        //nofity the resolver of the change
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        int match = sUriMatcher.match(uri);
        int rowsDeleted;

        switch(match){
            case FAVOURITES:{
                rowsDeleted =  dbHelpher.getWritableDatabase()
                        .delete(PopularMoviesContract.FavouriteEntry.TABLE_NAME,
                        selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        if(rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
