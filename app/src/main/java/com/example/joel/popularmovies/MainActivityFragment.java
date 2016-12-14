package com.example.joel.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    GridView gridView;

    public MainActivityFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        FetchMovieList fetchMovieList = new FetchMovieList();
        fetchMovieList.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        gridView = (GridView) rootView.findViewById(R.id.flavours_grid);

        //create the listener for the gridview
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

    /**
     * This class will retrieve the list of movies
     */
    public class FetchMovieList extends AsyncTask<Void, String, List<Movie>>{

        private final String LOG_TAG = FetchMovieList.class.getSimpleName();


        @Override
        protected List<Movie> doInBackground(Void... params) {

            final String BASE_URL = "http://api.themoviedb.org/3/movie";
            final String ADD_ON_SEGMENT = "popular";
            final String API_KEY_PARAM= "api_key";
            String jsonMovieString =  null;
            HttpURLConnection httpURLConnection =  null;
            InputStreamReader isr = null;
            BufferedReader br = null;

            try{

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendEncodedPath(ADD_ON_SEGMENT)
                        .appendQueryParameter(API_KEY_PARAM,
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

            final String TITLE_KEY = "original_title";
            final String SYNOPSIS_KEY= "overview";
            final String RELEASE_DATE_KEY = "release_date";
            final String RATING_KEY = "vote_average";
            final String IMAGE_URL_KEY = "poster_path";
            final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/w185";

            final ArrayList<Movie>  movieList = new ArrayList<>();

            try {
                JSONObject jsonObj = new JSONObject(jsonMovieString);
                JSONArray  jsonArray = jsonObj.getJSONArray("results");

                for(int i = 0; i<jsonArray.length(); i++ ){

                    JSONObject movieObject = jsonArray.getJSONObject(i);
                    Movie movie = new Movie();
                    movie.setmTtile(movieObject.getString(TITLE_KEY));
                    movie.setmRating(movieObject.getString(RATING_KEY));
                    movie.setmSynopsis(movieObject.getString(SYNOPSIS_KEY));
                    movie.setmReleaseDate(movieObject.getString(RELEASE_DATE_KEY));
                    Uri builtUri = Uri.parse(BASE_IMAGE_URL).buildUpon()
                            .appendEncodedPath(movieObject.getString(IMAGE_URL_KEY)).build();

                    movie.setmImageUrl(builtUri.toString());
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
            MovieAdapter adapter = new MovieAdapter(getActivity(), movies);
            gridView.setAdapter(adapter);
        }
    }
}
