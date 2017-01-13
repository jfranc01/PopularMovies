package com.example.joel.popularmovies.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by joel on 2016-12-28.
 */

public class TestUriMatcher extends AndroidTestCase {
    private static final long TEST_RECORD_ID = 10L;
    //test the directory
    private static final Uri TEST_FAVOURITES_DIR = PopularMoviesContract.FavouriteEntry.CONTENT_URI;
    //test by passing a record id
    public static final Uri TEST_FAVOURITES_WITH_ID =
            PopularMoviesContract.FavouriteEntry.buildPopularMoviesUri(TEST_RECORD_ID);

    public void testUriMatcher(){
        UriMatcher testMatcher= MovieContentProvider.buildUriMatcher();
        assertEquals("Error: The FAVOURITES URI was matched incorrectly.",
                testMatcher.match(TEST_FAVOURITES_DIR), MovieContentProvider.FAVOURITES);
        assertEquals("Error: The FAVOURITES WITH ID URI was matched incorrectly.",
                testMatcher.match(TEST_FAVOURITES_WITH_ID), MovieContentProvider.FAVOURITES_ID);
    }

}
