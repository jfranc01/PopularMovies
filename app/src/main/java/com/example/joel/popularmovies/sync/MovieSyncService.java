package com.example.joel.popularmovies.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by joel on 2017-01-16.
 */

public class MovieSyncService extends Service {

    // Storage for an instance of the sync adapter
    private static MovieSyncAdapter sMovieSyncAdapter = null;
    //Object to use a thread safe lock
    private static final Object sSyncAdapterLock = new Object();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return sMovieSyncAdapter.getSyncAdapterBinder();
    }

    @Override
    public void onCreate() {
        /*
         * Create the sync adapter as a singleton.
         * Set the sync adapter as syncable
         * Disallow parallel syncs
         */
        synchronized (sSyncAdapterLock) {
            if (sMovieSyncAdapter == null) {
                sMovieSyncAdapter = new MovieSyncAdapter(getApplicationContext(), true);
            }
        }
    }
}
