package com.example.joel.popularmovies.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.example.joel.popularmovies.BuildConfig;
import com.example.joel.popularmovies.Constants;
import com.example.joel.popularmovies.R;
import com.example.joel.popularmovies.Utility;
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
 * Created by joel on 2017-01-16.
 */

public class MovieService extends IntentService {

    private final String LOG_TAG = this.getClass().getSimpleName();
    public static final String CATEGORY_QUERY = "catgegoty_query";

    public MovieService(){
        super("MovieService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String category = null;

        if(intent !=  null && intent.hasExtra(CATEGORY_QUERY)){
            category = intent.getStringExtra(CATEGORY_QUERY);
        }

        String ADD_ON_SEGMENT = "";
        String jsonMovieString = null;
        HttpURLConnection httpURLConnection = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        if (category.equalsIgnoreCase(getString(R.string.sort_order_choice_popularity_value))) {
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
            int inserted = 0;
            // add to database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = this.getContentResolver().
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

    static public class AlarmReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent sendIntent = new Intent(context, MovieService.class);
            sendIntent.putExtra(CATEGORY_QUERY, intent.getStringExtra(CATEGORY_QUERY));
            context.startService(sendIntent);
        }
    }

}
