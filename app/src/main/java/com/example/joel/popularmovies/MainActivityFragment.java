package com.example.joel.popularmovies;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.example.joel.popularmovies.adapters.CursorAdaptor;
import com.example.joel.popularmovies.adapters.MovieAdapter;
import com.example.joel.popularmovies.data.PopularMoviesContract;
import com.example.joel.popularmovies.model.Movie;
import com.example.joel.popularmovies.service.MovieService;
import com.example.joel.popularmovies.sync.MovieSyncAdapter;

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

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

    public static GridView mGridView;
    public final String LOG_TAG = getClass().getSimpleName();
    private static final int FAVOURTIES_LOADER = 1;
    private static final int MOVIES_LOADER = 2;
    private CursorAdaptor mCursorAdapter;
    private MovieAdapter mMovieAdapter;
    private List<Movie> mCurrentMovieList;
    private String mCurrentSortOrder;


    public MainActivityFragment() {
        //notify thar the fragment has settings options
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_refresh) {
            //here we call the FetchMovies task
            fetchMovies();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void fetchMovies() {

//        String category = Utility.getCurrentCategory(getActivity());
//        Intent alarmIntent = new Intent(getActivity(),
//                MovieService.AlarmReceiver.class);
//        alarmIntent.putExtra(MovieService.CATEGORY_QUERY, category);
//
//        PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0,
//                alarmIntent, PendingIntent.FLAG_ONE_SHOT);
//
//        AlarmManager alarmManager = (AlarmManager)
//                getActivity().getSystemService(Context.ALARM_SERVICE);
//        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000 , pi);
//        Intent movieService = new Intent(getActivity(), MovieService.class);
//        movieService.putExtra(MovieService.CATEGORY_QUERY, category);
        //getLoaderManager().restartLoader(MOVIES_LOADER, null, MainActivityFragment.this);
        //FetchMovieList moviesTask = new FetchMovieList();
        //moviesTask.execute(category);
        MovieSyncAdapter.syncImmediately(getActivity());
    }

    public void fetchFavourites() {
        getLoaderManager().restartLoader(FAVOURTIES_LOADER, null, this);
    }


    //projection columns
    public static final String[] FAVOURITE_COLUMNS = {
            PopularMoviesContract.FavouriteEntry._ID,
            PopularMoviesContract.FavouriteEntry.COLUMN_MOVIE_ID,
            PopularMoviesContract.FavouriteEntry.COLUMN_NAME_TITLE,
            PopularMoviesContract.FavouriteEntry.COLUMN_NAME_SYNOPSIS,
            PopularMoviesContract.FavouriteEntry.COLUMN_NAME_RELEASE,
            PopularMoviesContract.FavouriteEntry.COLUMN_NAME_RATING,
            PopularMoviesContract.FavouriteEntry.COLUMN_NAME_IMGURL
    };

    public static final int COL_FAV_ID = 0;
    public static final int COL_FAV_MOVIE_ID = 1;
    public static final int COL_FAV_TITLE = 2;
    public static final int COL_FAV_SYNOPSIS = 3;
    public static final int COL_FAV_RELEASE = 4;
    public static final int COL_FAV_RATING = 5;
    public static final int COL_FAV_IMGURL = 6;

    @Override
    public void onResume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        //once the activity has been created, we need to start the loader
        //but we need to know what the current category was set to
        String category = Utility.getCurrentCategory(getActivity());
        //if it is the favourites read from the favourites table
        if (category.equals("Favourites")) {
            getLoaderManager().initLoader(FAVOURTIES_LOADER, null, this);
        }
        //otherwise just read from the movies db
        else {
            getLoaderManager().initLoader(MOVIES_LOADER, null, this);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mCurrentSortOrder = Utility.getCurrentCategory(getActivity());
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mCursorAdapter = new CursorAdaptor(getContext(), null, 0);
        mGridView = (GridView) rootView.findViewById(R.id.flavours_grid);
        mGridView.setAdapter(mCursorAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                Uri contentUri;
                if (cursor != null) {
                    //first find out what the current category is
                    String category = Utility.getCurrentCategory(getActivity());
                    if (category.equals("Favourites")) {
                        contentUri = PopularMoviesContract.FavouriteEntry
                                .buildPopularMoviesUri(cursor.getLong(COL_FAV_MOVIE_ID));
                    } else {
                        contentUri = PopularMoviesContract.MovieEntry
                                .buildPopularMoviesUri(cursor.getLong(COL_FAV_MOVIE_ID));
                    }

                    //call the call back interface's implementation
                    ((DetailActivityFragment.Callbacks) getActivity()).onItemClicked(contentUri);
                }
            }
        });


        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //depending on the loadet id, we query either the movies tables
        //or the favourites table
        if (FAVOURTIES_LOADER == id) {

            return new CursorLoader(
                    getActivity(),
                    PopularMoviesContract.FavouriteEntry.CONTENT_URI,
                    FAVOURITE_COLUMNS,
                    null,
                    null,
                    null
            );
        }

        return new CursorLoader(
                getActivity(),
                PopularMoviesContract.MovieEntry.CONTENT_URI,
                FAVOURITE_COLUMNS,
                null,
                null,
                null
        );

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
        updateEmptyView();

//        mGridView.post(new Runnable() {
//            @Override
//            public void run() {
//                if(mGridView.getAdapter() != null) {
//                    mGridView.performItemClick(mGridView, 0, mGridView.getAdapter().getItemId(0));
//                }
//            }
//        })
    }

    private void updateEmptyView(){
        if (mCursorAdapter.getCount() == 0) {
            View viewParent = (View) getView().getParent();
            AppCompatTextView tv = (AppCompatTextView) viewParent.findViewById(R.id.empty_movie_list);
            if (tv != null) {
                mGridView.setEmptyView(tv);
                int message = R.string.empty_movie_list;
                @MovieSyncAdapter.ServerStatus int serverStatus =
                        Utility.getServerStatusFromPreferences(getActivity());
                switch (serverStatus) {

                    case MovieSyncAdapter.MOVIE_SERVER_DOWN:
                        message = R.string.empty_movie_list_server_down;
                        break;
                    case MovieSyncAdapter.MOVIE_SERVER_INVALID:
                        message = R.string.empty_movie_list_server_error;
                        break;
                    default:
                        if (!Utility.isNetworkConnected(getActivity())) {
                                message = R.string.empty_movie_list_due_to_network;
                        }
                }
                tv.setText(message);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i(LOG_TAG, "Settings key changed: " + key);
        mCurrentSortOrder = sharedPreferences.getString(key, "Popularity");
        Log.i(LOG_TAG, "Current Sort Order: " + mCurrentSortOrder);

        if(key.equals(getString(R.string.pref_server_status_key))){
            //update the view
            updateEmptyView();
        }
    }
}
