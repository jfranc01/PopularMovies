package com.example.joel.popularmovies.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;

import com.example.joel.popularmovies.model.Movie;

/**
 * Created by joel on 2016-12-28.
 */

public class TestProvider extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    public void deleteAllRecords(){
        mContext.getContentResolver().delete(PopularMoviesContract.FavouriteEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                PopularMoviesContract.FavouriteEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Favourites table during delete", 0, cursor.getCount());
        cursor.close();

        mContext.getContentResolver().delete(PopularMoviesContract.MovieEntry.CONTENT_URI,
                null,
                null
        );
        cursor = mContext.getContentResolver().query(
                PopularMoviesContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Movies table during delete", 0, cursor.getCount());
        cursor.close();
    }

    public void testProviderRegistration(){
        //get a reference to the PackageManager
        PackageManager pm = mContext.getPackageManager();
        //Define a compnent
        ComponentName  cm = new ComponentName(mContext.getPackageName(),
                MovieContentProvider.class.getName());

        // "try" and fetch the component information
        try{
            ProviderInfo providerInfo = pm.getProviderInfo(cm, 0);

            assertEquals("Error: MovieContentProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + PopularMoviesContract.CONTENT_AUTHORITY,
                    providerInfo.authority, PopularMoviesContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            assertTrue("Error: MovieContentProvider not registered at " + mContext.getPackageName(),
                    false);
            e.printStackTrace();
        }
    }

    public void testGetType(){
        String type = mContext.getContentResolver().getType(PopularMoviesContract.FavouriteEntry.CONTENT_URI);
        assertEquals("Error: the Favourites CONTENT_URI should return FavouritesEntry.CONTENT_TYPE",
                PopularMoviesContract.FavouriteEntry.CONTENT_TYPE, type);

        long recordID = 10L;
        type =  mContext.getContentResolver().getType(PopularMoviesContract.FavouriteEntry.buildPopularMoviesUri(recordID));
        assertEquals("Error: the Favourites WITH ID should return FavouritesEntry.CONTENT_TYPE_ITEM",
                PopularMoviesContract.FavouriteEntry.CONTENT_TYPE_ITEM, type);

        type = mContext.getContentResolver().getType(PopularMoviesContract.MovieEntry.CONTENT_URI);
        assertEquals("Error: the Movies CONTENT_URI should return FavouritesEntry.CONTENT_TYPE",
                PopularMoviesContract.MovieEntry.CONTENT_TYPE, type);

        recordID = 10L;
        type =  mContext.getContentResolver().getType(PopularMoviesContract.MovieEntry.buildPopularMoviesUri(recordID));
        assertEquals("Error: the Movies WITH ID should return FavouritesEntry.CONTENT_TYPE_ITEM",
                PopularMoviesContract.MovieEntry.CONTENT_TYPE_ITEM, type);

    }

    public void testFavouriteQueries() {
        //insert records into the database
        PopularMoviesDbHelper dbHelper = new PopularMoviesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = TestUtilities.creteFavouriteValues();
        long recordID = TestUtilities.insertPizzaNightValues(mContext);

        // Test the basic content provider query
        Cursor favCursor = mContext.getContentResolver().query(
                PopularMoviesContract.FavouriteEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testFavouriteQueries", favCursor, values);
        if ( Build.VERSION.SDK_INT >= 19 ) {
            assertEquals("Error: Favourites Query did not properly set NotificationUri",
                    favCursor.getNotificationUri(), PopularMoviesContract.FavouriteEntry.CONTENT_URI);
        }

        final String TITLE = "Pizza Night!";
        Cursor titleCursor = mContext.getContentResolver()
                .query(PopularMoviesContract.FavouriteEntry.CONTENT_URI,
                null,
                PopularMoviesContract.FavouriteEntry.COLUMN_NAME_TITLE + " = ?",
                new String[]{TITLE},
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testFavouriteQueries", titleCursor, values);
        if ( Build.VERSION.SDK_INT >= 19 ) {
            assertEquals("Error: Favourites Query did not properly set NotificationUri",
                    titleCursor.getNotificationUri(), PopularMoviesContract.FavouriteEntry.CONTENT_URI);
        }
        //Test the query by title

    }

    public void testMovieQueries(){
        //insert records into the database
        PopularMoviesDbHelper dbHelper = new PopularMoviesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = TestUtilities.createMovieValues();
        long recordID = TestUtilities.insertMoviePizzaNightValues(mContext);

        // Test the basic content provider query
        Cursor movCursor = mContext.getContentResolver().query(
                PopularMoviesContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testFavouriteQueries", movCursor, values);
        if ( Build.VERSION.SDK_INT >= 19 ) {
            assertEquals("Error: Favourites Query did not properly set NotificationUri",
                    movCursor.getNotificationUri(), PopularMoviesContract.MovieEntry.CONTENT_URI);
        }

        final String TITLE = "Pizza Night!";
        Cursor titleCursor = mContext.getContentResolver()
                .query(PopularMoviesContract.MovieEntry.CONTENT_URI,
                        null,
                        PopularMoviesContract.MovieEntry.COLUMN_NAME_TITLE + " = ?",
                        new String[]{TITLE},
                        null
                );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testFavouriteQueries", titleCursor, values);
        if ( Build.VERSION.SDK_INT >= 19 ) {
            assertEquals("Error: Favourites Query did not properly set NotificationUri",
                    titleCursor.getNotificationUri(), PopularMoviesContract.MovieEntry.CONTENT_URI);
        }
    }

    public void testDeleteFavourite(){

        //create some values
        ContentValues contentValues = TestUtilities.creteFavouriteValues();
        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().
                registerContentObserver(PopularMoviesContract.
                        FavouriteEntry.CONTENT_URI, true, tco);
        Uri favouriteUri =
                mContext.getContentResolver()
                        .insert(PopularMoviesContract
                                .FavouriteEntry.CONTENT_URI, contentValues);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        String favRowId = String.valueOf(ContentUris.parseId(favouriteUri));

        final String selection = PopularMoviesContract.FavouriteEntry.TABLE_NAME +
                "." + PopularMoviesContract.FavouriteEntry._ID + " = ? ";

        //delete the item
        int rowsDeleted = mContext.getContentResolver().delete(PopularMoviesContract.FavouriteEntry.CONTENT_URI,
                selection, new String[]{favRowId});
        assertEquals("Error: no rows deleted", 1, rowsDeleted);

    }
    public void testInsertFavourite(){
        //create some values
        ContentValues contentValues = TestUtilities.creteFavouriteValues();
        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().
                registerContentObserver(PopularMoviesContract.
                        FavouriteEntry.CONTENT_URI, true, tco);
        Uri favouriteUri =
                mContext.getContentResolver()
                        .insert(PopularMoviesContract
                                .FavouriteEntry.CONTENT_URI, contentValues);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long favRowId = ContentUris.parseId(favouriteUri);

        // Verify we got a row back.
        assertTrue(favRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                PopularMoviesContract.FavouriteEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertFavourite. Error validating FavouritesEntry.",
                cursor, contentValues);


    }

    public void  testInserMovie(){
        //create some values
        ContentValues contentValues = TestUtilities.createMovieValues();
        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().
                registerContentObserver(PopularMoviesContract.
                        MovieEntry.CONTENT_URI, true, tco);
        Uri favouriteUri =
                mContext.getContentResolver()
                        .insert(PopularMoviesContract
                                .MovieEntry.CONTENT_URI, contentValues);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long favRowId = ContentUris.parseId(favouriteUri);

        // Verify we got a row back.
        assertTrue(favRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                PopularMoviesContract.MovieEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertFavourite. Error validating FavouritesEntry.",
                cursor, contentValues);
    }

    public void testDeleteMovie(){
        //create some values
        ContentValues contentValues = TestUtilities.createMovieValues();
        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().
                registerContentObserver(PopularMoviesContract.
                        MovieEntry.CONTENT_URI, true, tco);
        Uri favouriteUri =
                mContext.getContentResolver()
                        .insert(PopularMoviesContract
                                .MovieEntry.CONTENT_URI, contentValues);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        String favRowId = String.valueOf(ContentUris.parseId(favouriteUri));

        final String selection = PopularMoviesContract.MovieEntry.TABLE_NAME +
                "." + PopularMoviesContract.MovieEntry._ID + " = ? ";

        //delete the item
        int rowsDeleted = mContext.getContentResolver().delete(PopularMoviesContract.MovieEntry.CONTENT_URI,
                selection, new String[]{favRowId});
        assertEquals("Error: no rows deleted", 1, rowsDeleted);
    }

    public void testBulkInsert(){
        ContentValues[] bulkInsertContentValues = TestUtilities.createBulkInsertValues();

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver movieObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(PopularMoviesContract.MovieEntry.CONTENT_URI, true, movieObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(PopularMoviesContract.MovieEntry.CONTENT_URI, bulkInsertContentValues);

        // Students:  If this fails, it means that you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
        // ContentProvider method.
        movieObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(movieObserver);

        assertEquals(insertCount, TestUtilities.BULK_INSERT_RECORDS_TO_INSERT);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                PopularMoviesContract.MovieEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order == by DATE ASCENDING
        );

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), TestUtilities.BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for ( int i = 0; i < TestUtilities.BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating WeatherEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }
}
