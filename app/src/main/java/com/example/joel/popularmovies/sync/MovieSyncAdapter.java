package com.example.joel.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.example.joel.popularmovies.BuildConfig;
import com.example.joel.popularmovies.Constants;
import com.example.joel.popularmovies.MainActivity;
import com.example.joel.popularmovies.R;
import com.example.joel.popularmovies.Utility;
import com.example.joel.popularmovies.data.PopularMoviesContract;
import com.example.joel.popularmovies.model.Movie;
import com.example.joel.popularmovies.service.MovieService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static android.text.format.DateUtils.DAY_IN_MILLIS;

/**
 * Created by joel on 2017-01-16.
 */

public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {

    //log tag
    public final String LOG_TAG = MovieSyncAdapter.class.getSimpleName();
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    private static final int MOVIE_NOTIFICATION_ID = 3004;

    //Variable to hold the content resolver instance
    ContentResolver mContentResolver;

    //Constructor
    MovieSyncAdapter(Context context, boolean autoInitialize){
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    //This maintain compatability with older versions
    public MovieSyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,

                    authority, new Bundle(), syncInterval);
        }
    }



    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {

        Log.d(LOG_TAG, "onPerformSync Called.");

        String category = null;
        category = Utility.getCurrentCategory(getContext());
        String ADD_ON_SEGMENT = "";
        String jsonMovieString = null;
        HttpURLConnection httpURLConnection = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        if (category.equalsIgnoreCase("Popularity")) {
            ADD_ON_SEGMENT = Constants.ADD_ON_POPULAR_SEGMENT;
        } else {
            ADD_ON_SEGMENT = Constants.ADD_ON_RATING_SEGMENT;
        }
        try {

            Uri builtUri = Uri.parse(Constants.BASE_URL).buildUpon()
                    .appendEncodedPath(ADD_ON_SEGMENT)
                    .appendQueryParameter(Constants.API_KEY_PARAM,
                            BuildConfig.OPEN_MOVIE_DB_API_KEY)
                    .build();

            Log.i(LOG_TAG, " Built URI: " + builtUri.toString());

            //create the URL and open the connection
            URL url = new URL(builtUri.toString());
            httpURLConnection = (HttpURLConnection) url.openConnection();
            //set the request type
            httpURLConnection.setRequestMethod("GET");
            //call the connect method
            httpURLConnection.connect();
            //Create an InputStream reader;
            isr = new InputStreamReader(httpURLConnection.getInputStream());

            if (isr == null) {
                return;
            }

            //Create a string buffer
            StringBuffer buffer = new StringBuffer();
            //create a buffered reader
            br = new BufferedReader(isr);
            //variable to hold the content of the line being read
            String line;
            while ((line = br.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return;
            }

            jsonMovieString = buffer.toString();
            Log.i(LOG_TAG, " Content: " + jsonMovieString);

            //here we need to parse the json object to fetch the
            //data that we need
            getMovieDataFromJsonString(jsonMovieString);

        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, " Error: " + e.getMessage());
        } catch (IOException e) {
            Log.e(LOG_TAG, " Error: " + e.getMessage());
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }

            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, " Cannot close stream reader: " + e.getMessage());
                }
            }
        }

        return;

    }

    /**
     * Method that wil parse the json object and return a list
     * of Movie objects
     *
     * @param jsonMovieString
     * @return ArrayList<Movie>
     */
    private List<Movie> getMovieDataFromJsonString(String jsonMovieString) {

        final ArrayList<Movie> movieList = new ArrayList<>();
        Vector<ContentValues> cVVector;
        try {
            JSONObject jsonObj = new JSONObject(jsonMovieString);
            JSONArray jsonArray = jsonObj.getJSONArray("results");
            cVVector = new Vector<ContentValues>(jsonArray.length());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject movieObject = jsonArray.getJSONObject(i);
                ContentValues moviewValues =
                        Utility.createMovieContentValuesFromJSON(movieObject);
                cVVector.add(moviewValues);
            }

            //delete the older movies
            mContentResolver.delete(PopularMoviesContract.MovieEntry.CONTENT_URI,
                    null, null);
            int inserted = 0;
            // add to database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = mContentResolver.
                        bulkInsert(PopularMoviesContract.MovieEntry.CONTENT_URI, cvArray);
            }

            Log.d(LOG_TAG, "FetchMovieTask Complete. " + inserted + " Inserted");
            //return the list of movies
            return movieList;
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error creating JSON Object: " + e.getMessage());
        }

        return null;
    }


    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        MovieSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    private void notifyNewMovies(){
        Context context = getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));

        if ( displayNotifications ) {


            String lastNotificationKey = context.getString(R.string.pref_last_notification);
            long lastSync = prefs.getLong(lastNotificationKey, 0);

            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
                String title = context.getString(R.string.app_name);
                String contentText = context.getString(R.string.format_notification);

                //build the notification
                NotificationCompat.Builder mBuilder =
                        (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                                .setContentTitle(title)
                                .setContentText(contentText);

                // Make something interesting happen when the user clicks on the notification.
                // In this case, opening the app is sufficient.
                Intent resultIntent = new Intent(context, MainActivity.class);

                // The stack builder object will contain an artificial back stack for the
                // started Activity.
                // This ensures that navigating backward from the Activity leads out of
                // your application to the Home screen.
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                mBuilder.setContentIntent(resultPendingIntent);

                NotificationManager mNotificationManager =
                        (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                // WEATHER_NOTIFICATION_ID allows you to update the notification later on.
                mNotificationManager.notify(MOVIE_NOTIFICATION_ID, mBuilder.build());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(lastNotificationKey, System.currentTimeMillis());
                editor.commit();
            }
        }
    }
}
