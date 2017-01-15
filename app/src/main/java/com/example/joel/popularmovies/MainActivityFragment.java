package com.example.joel.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.joel.popularmovies.adapters.CursorAdaptor;
import com.example.joel.popularmovies.adapters.MovieAdapter;
import com.example.joel.popularmovies.data.PopularMoviesContract;
import com.example.joel.popularmovies.model.Movie;

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
        String category = Utility.getCurrentCategory(getActivity());
        FetchMovieList moviesTask = new FetchMovieList();
        moviesTask.execute(category);
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
        super.onResume();
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
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = (Movie) parent.getItemAtPosition(position);
                //call the call back interface's implementation
                ((DetailActivityFragment.Callbacks) getActivity()).onItemClicked(movie);
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
    }

    /**
     * This class will retrieve the list of movies
     */
    public class FetchMovieList extends AsyncTask<String, String, List<Movie>> {

        private final String LOG_TAG = FetchMovieList.class.getSimpleName();


        @Override
        protected List<Movie> doInBackground(String... params) {

            String ADD_ON_SEGMENT = "";
            String jsonMovieString = null;
            HttpURLConnection httpURLConnection = null;
            InputStreamReader isr = null;
            BufferedReader br = null;

            if (params[0].equalsIgnoreCase(getString(R.string.sort_order_choice_popularity_value))) {
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
                    return null;
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
                    return null;
                }

                jsonMovieString = buffer.toString();
                Log.i(LOG_TAG, " Content: " + jsonMovieString);

                //here we need to parse the json object to fetch the
                //data that we need
                return getMovieDataFromJsonString(jsonMovieString);

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

            return null;
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
                int inserted = 0;
                // add to database
                if (cVVector.size() > 0) {
                    ContentValues[] cvArray = new ContentValues[cVVector.size()];
                    cVVector.toArray(cvArray);
                    inserted = getActivity().getContentResolver().
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
    }
}
