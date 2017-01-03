package com.example.joel.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.joel.popularmovies.adapters.FavouritesAdapter;
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

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener{

    GridView mGridView;
    public final String LOG_TAG = getClass().getSimpleName();
    private static final int FAVOURTIES_LOADER = 1;
    private FavouritesAdapter mFavouritesAdapter;
    private MovieAdapter mMovieAdapter;
    private List<Movie> mCurrentMovieList;
    private String mCurrentSortOrder;


    public MainActivityFragment() {
        //notify thar the fragment has settings options
    }

    //projection columns
    public static final String[] FAVOURITE_COLUMNS = {
            PopularMoviesContract.FavouriteEntry._ID,
            PopularMoviesContract.FavouriteEntry.COLUMN_NAME_TITLE,
            PopularMoviesContract.FavouriteEntry.COLUMN_NAME_SYNOPSIS,
            PopularMoviesContract.FavouriteEntry.COLUMN_NAME_RELEASE,
            PopularMoviesContract.FavouriteEntry.COLUMN_NAME_RATING,
            PopularMoviesContract.FavouriteEntry.COLUMN_NAME_IMGURL
    };

    public static final int COL_FAV_ID = 0;
    public static final int COL_FAV_TITLE = 1;
    public static final int COL_FAV_SYNOPSIS = 2;
    public static final int COL_FAV_RELEASE = 3;
    public static final int COL_FAV_RATING = 4;
    public static final int COL_FAV_IMGURL = 5;

    @Override
    public void onStart() {
        super.onStart();
        //Read from the shared preferences and pass the value appropriately
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        //set the preference change listener
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);
        //get the key and also set the default value
        mCurrentSortOrder = sharedPrefs.getString(getString(R.string.sort_order_key),
                getString(R.string.sort_order_default));

        mCurrentMovieList = new ArrayList();
        if(mCurrentSortOrder.equalsIgnoreCase("Favourites")){
            //here we use a AsyncTaskLoader to load items form the database
            getLoaderManager().initLoader(FAVOURTIES_LOADER,null, this);
            mMovieAdapter = new MovieAdapter(getActivity(), mCurrentMovieList);
            mGridView.setAdapter(mMovieAdapter);
        }
        else{
            FetchMovieList fetchMovieList = new FetchMovieList();
            //pass the value of the key to the task
            fetchMovieList.execute(mCurrentSortOrder);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if(mCurrentSortOrder.equalsIgnoreCase("Favourites")){
            getLoaderManager().restartLoader(FAVOURTIES_LOADER, null, this);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mGridView = (GridView) rootView.findViewById(R.id.flavours_grid);

        //mFavouritesAdapter = new FavouritesAdapter(getActivity(), null, 0);
        MovieAdapter adapter = new MovieAdapter(getActivity(), null);
        mGridView.setAdapter(mMovieAdapter);

        //create the listener for the gridview
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = (Movie)parent.getItemAtPosition(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                //place the parceable in the intent
                intent.putExtra("movie", movie);
                //start the intent
                startActivity(intent);
            }
        });


        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //we query the content provider
        //here we don't care about projects or the sort order
        //simply query and retrieve all records from the database

        return new CursorLoader(
                getActivity(),
                PopularMoviesContract.FavouriteEntry.CONTENT_URI,
                FAVOURITE_COLUMNS,
                null,
                null,
                null
        );

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //iterate over the cursor and create a list of movies
        List<Movie> favList = new ArrayList();
        if(data != null && data.moveToFirst()){
            do{
                Movie movie = new Movie();
                movie.setmId(data.getString(COL_FAV_ID));
                movie.setmTtile(data.getString(COL_FAV_TITLE));//title
                movie.setmSynopsis(data.getString(COL_FAV_SYNOPSIS)); //synopsis
                movie.setmReleaseDate(data.getString(COL_FAV_RELEASE)); //release
                movie.setmRating(data.getString(COL_FAV_RATING)); //rating
                movie.setmImageUrl(data.getString(COL_FAV_IMGURL)); //img url
                movie.setmIsFav(true); //is it a favourite
                favList.add(movie);
            }while(data.moveToNext());
        }
        //save it to the global movie list
        mCurrentMovieList = favList;
        mMovieAdapter = new MovieAdapter(getActivity(), mCurrentMovieList);
        mGridView.setAdapter(mMovieAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //m.swapCursor(null);
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
    public class FetchMovieList extends AsyncTask<String, String, List<Movie>>{

        private final String LOG_TAG = FetchMovieList.class.getSimpleName();


        @Override
        protected List<Movie> doInBackground(String... params) {

            String ADD_ON_SEGMENT = "";
            String jsonMovieString =  null;
            HttpURLConnection httpURLConnection =  null;
            InputStreamReader isr = null;
            BufferedReader br = null;

            if(params[0].equalsIgnoreCase(getString(R.string.sort_order_choice_popularity_value))){
                ADD_ON_SEGMENT = Constants.ADD_ON_POPULAR_SEGMENT;
            }
            else{
                ADD_ON_SEGMENT = Constants.ADD_ON_RATING_SEGMENT;
            }


            try{

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

                if(isr == null){
                    return null;
                }

                //Create a string buffer
                StringBuffer buffer = new StringBuffer();
                //create a buffered reader
                br = new BufferedReader(isr);
                //variable to hold the content of the line being read
                String line;
                while((line  = br.readLine()) != null){
                    buffer.append(line + "\n");
                }

                if(buffer.length() == 0){
                    return null;
                }

                jsonMovieString =  buffer.toString();
                Log.i(LOG_TAG,  " Content: " + jsonMovieString);

                //here we need to parse the json object to fetch the
                //data that we need
                return getMovieDataFromJsonString(jsonMovieString);

            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, " Error: " + e.getMessage());
            } catch (IOException e) {
                Log.e(LOG_TAG, " Error: " + e.getMessage());
            }finally {
                if(httpURLConnection != null){
                    httpURLConnection.disconnect();
                }

                if(br !=  null){
                    try{
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
         * @param jsonMovieString
         * @return ArrayList<Movie>
         */
        private List<Movie> getMovieDataFromJsonString(String jsonMovieString) {

            final ArrayList<Movie>  movieList = new ArrayList<>();

            try {
                JSONObject jsonObj = new JSONObject(jsonMovieString);
                JSONArray  jsonArray = jsonObj.getJSONArray("results");

                for(int i = 0; i<jsonArray.length(); i++ ){

                    JSONObject movieObject = jsonArray.getJSONObject(i);
                    Movie movie = new Movie();
                    movie.setmTtile(movieObject.getString(Constants.TITLE_KEY));
                    movie.setmRating(movieObject.getString(Constants.RATING_KEY));
                    movie.setmSynopsis(movieObject.getString(Constants.SYNOPSIS_KEY));
                    movie.setmReleaseDate(movieObject.getString(Constants.RELEASE_DATE_KEY));
                    Uri builtUri = Uri.parse(Constants.BASE_IMAGE_URL).buildUpon()
                            .appendEncodedPath(movieObject.getString(Constants.IMAGE_URL_KEY)).build();

                    movie.setmImageUrl(builtUri.toString());
                    movie.setmId(movieObject.getString(Constants.ID));
                    movieList.add(movie);
                }
                //return the list of movies
                return movieList;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error creating JSON Object: " + e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            //save it to the global variable!
            mCurrentMovieList =  movies;
            //initialize the adapter
            mMovieAdapter = new MovieAdapter(getActivity(), mCurrentMovieList);
            //set the adapter
            mGridView.setAdapter(mMovieAdapter);
        }
    }
}
