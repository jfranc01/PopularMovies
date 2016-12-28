package com.example.joel.popularmovies.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

/**
 * Created by joel on 2016-12-28.
 */

public class TestDb extends AndroidTestCase {
    //create a LOG_TAG
    public static final String LOG_TAG = TestDb.class.getSimpleName();

    /**
     * Start with a clean slate each time and delete the exists
     * database
     */
    public void deleteDatabase(){
        mContext.deleteDatabase(PopularMoviesDbHelper.DATABASE_NAME);
    }

    /**
     * This method is called before each test case
     */
    public void setUp(){
        deleteDatabase();
    }

    public void testCreateDb()throws Throwable{

        //Create a hashset of the table name that we want to search for
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(PopularMoviesContract.FavouriteEntry.TABLE_NAME);

        //delete the existing database
        mContext.deleteDatabase(PopularMoviesDbHelper.DATABASE_NAME);
        //create a reference to the database
        SQLiteDatabase db = new PopularMoviesDbHelper(this.mContext).getWritableDatabase();

        //Write a query to select from the master table  that contains the names
        assertEquals(true, db.isOpen());
        //query the tables that we want
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: There were no table names that were returned", cursor.moveToFirst());
        //verify that the tables have been created
        do{
            tableNameHashSet.remove(cursor.getString(0));
        }while(cursor.moveToNext());

        assertTrue("Error: All the tables were not created!", tableNameHashSet.isEmpty());

        //check if the table contains the correct columns
        cursor = db.rawQuery("PRAGMA table_info(" + PopularMoviesContract.FavouriteEntry.TABLE_NAME + ")", null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                cursor.moveToFirst());

        //create a hashset of the columns names
        final HashSet<String> columnNameHashSet = new HashSet<String>();
        columnNameHashSet.add(PopularMoviesContract.FavouriteEntry.COLUMN_NAME_IMGURL);
        columnNameHashSet.add(PopularMoviesContract.FavouriteEntry.COLUMN_NAME_RATING);
        columnNameHashSet.add(PopularMoviesContract.FavouriteEntry.COLUMN_NAME_RELEASE);
        columnNameHashSet.add(PopularMoviesContract.FavouriteEntry.COLUMN_NAME_SYNOPSIS);
        columnNameHashSet.add(PopularMoviesContract.FavouriteEntry.COLUMN_NAME_TITLE);
        columnNameHashSet.add(PopularMoviesContract.FavouriteEntry._ID);

        int columnNameIndex = cursor.getColumnIndex("name");
        do {
            String columnName = cursor.getString(columnNameIndex);
            columnNameHashSet.remove(columnName);
        } while(cursor.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required favourite entry columns",
                columnNameHashSet.isEmpty());
        db.close();

    }

}
