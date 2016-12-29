package com.example.joel.popularmovies.data;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.test.AndroidTestCase;

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
        assertEquals("Error: Records not deleted from Weather table during delete", 0, cursor.getCount());
        cursor.close();
    }

    public void testProviderRegistration(){
        //get a reference to the PackageManager
        PackageManager pm = mContext.getPackageManager();
        //Define a compnent
        ComponentName  cm = new ComponentName(mContext.getPackageName(),
                FavouritesProvider.class.getName());

        // "try" and fetch the component information
        try{
            ProviderInfo providerInfo = pm.getProviderInfo(cm, 0);

            assertEquals("Error: FavouritesProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + PopularMoviesContract.CONTENT_AUTHORITY,
                    providerInfo.authority, PopularMoviesContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            assertTrue("Error: FavouritesProvider not registered at " + mContext.getPackageName(),
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

    }

    public void testFavouriteQueries(){

    }

    public void testDeleteFavourite(){

    }

    public void testInsertFavourite(){

    }
}
