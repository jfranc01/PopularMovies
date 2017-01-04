package com.example.joel.popularmovies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

/**
 * Created by joel on 2016-12-28.
 */

public class TestUtilities extends AndroidTestCase {

    static ContentValues creteFavouriteValues() {
        ContentValues favouritesValues = new ContentValues();
        favouritesValues.put(PopularMoviesContract.FavouriteEntry.COLUMN_NAME_IMGURL,
                "https://www.google.ca/url?sa=i&rct=j&q=&esrc=s&source=images&cd=&cad=r" +
                        "ja&uact=8&ved=0ahUKEwjB_IHxpJjRAhUG44MKHfhRAS8QjRwIBw&url=http%3A%" +
                        "2F%2Fen.tintin.com%2Fpersonnages%2Fshow%2Fid%2F15%2Fpage%2F0%2F0%2Ftintin&bvm" +
                        "=bv.142059868,d.amc&psig=AFQjCNFnP40gRld-BWQqyDEKnsHgvHrqDg&ust=1483062605949650");
        favouritesValues.put(PopularMoviesContract.FavouriteEntry.COLUMN_MOVIE_ID, "11345");
        favouritesValues.put(PopularMoviesContract.FavouriteEntry.COLUMN_NAME_RATING, "8.1");
        favouritesValues.put(PopularMoviesContract.FavouriteEntry.COLUMN_NAME_RELEASE, "2016");
        favouritesValues.put(PopularMoviesContract.FavouriteEntry.COLUMN_NAME_SYNOPSIS,
                "My brother and I will order a pizza. What happens next is a thriller!" +
                " There will also be desert. Hopefully cheesecake. thi story is about two boys, their, pizza" +
                        "and desert");
        favouritesValues.put(PopularMoviesContract.FavouriteEntry.COLUMN_NAME_TITLE, "Pizza Night!");
        return favouritesValues;
    }

    static long insertPizzaNightValues(Context context) {
        // insert our test records into the database
        PopularMoviesDbHelper dbHelper = new PopularMoviesDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.creteFavouriteValues();

        long favRowId;
        favRowId = db.insert(PopularMoviesContract.FavouriteEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert North Pole Location Values", favRowId != -1);

        return favRowId;
    }

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
