package com.example.joel.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.joel.popularmovies.adapters.TrailerAdapter;
import com.example.joel.popularmovies.data.PopularMoviesContract;
import com.example.joel.popularmovies.model.Movie;
import com.example.joel.popularmovies.model.Trailer;
import com.squareup.picasso.Picasso;

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
public class DetailActivityFragment extends Fragment {

    Intent intent;
    public final String LOG_TAG = this.getClass().getSimpleName();
    public final String FAV_ADDED = "Movie saved as favourite!!!!!";
    public final String ALREADY_A_FAV = "Already a favourite!!";
    Movie movie =  null;
    ListView mTrailerListView = null;

    public DetailActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        intent = getActivity().getIntent();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(intent != null){
            if(intent.hasExtra("movie")){
                movie = intent.getParcelableExtra("movie");
                //here we create the task to fetch the trailers
                FetchTrailers fetchTrailers = new FetchTrailers();
                fetchTrailers.execute(movie.getmId());
            }
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ImageView poster = (ImageView)rootView.findViewById(R.id.detail_poster);
        TextView title = (TextView)rootView.findViewById(R.id.detail_title);
        TextView release_date = (TextView)rootView.findViewById(R.id.detail_release_date);
        TextView rating  = (TextView)rootView.findViewById(R.id.detail_rating);
        TextView synopsis = (TextView)rootView.findViewById(R.id.detail_sysnopsis);
        mTrailerListView = (ListView)rootView.findViewById(R.id.traier_list);
        title.setText(movie.getmTtile());
        rating.setText(movie.getmRating() + "/10");
        release_date.setText(Utility.formatDate(movie.getmReleaseDate()));
        synopsis.setText(movie.getmSynopsis());
        Picasso.with(getActivity()).load(movie.getmImageUrl()).into(poster);

        //get a reference to the star button
        ImageButton startButton = (ImageButton)rootView.findViewById(R.id.starButtton);
        if(movie.ismIsFav()){
            startButton.setBackgroundResource(R.drawable.on__star);
        }
        //set on onlicklistener
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(LOG_TAG, "Star button was pressed");

                //first check if this movie is already a favourite
                Cursor returnCursor = getContext().getContentResolver().
                        query(
                                PopularMoviesContract.FavouriteEntry.CONTENT_URI,
                                MainActivityFragment.FAVOURITE_COLUMNS,
                                PopularMoviesContract.FavouriteEntry.COLUMN_NAME_TITLE + " = ? ",
                                new String[]{movie.getmTtile()},
                                null);

                if(returnCursor!= null && returnCursor.moveToFirst()){
                    if(returnCursor.getString(MainActivityFragment.COL_FAV_TITLE)
                            .equalsIgnoreCase(movie.getmTtile())) {
                            Toast toast = Toast.makeText(getContext(), ALREADY_A_FAV, Toast.LENGTH_SHORT);
                            toast.show();
                    }
                }
                else {
                    //here we get access to the content resolver and perform an insert into the database
                    Uri returnUri = getContext().getContentResolver().insert
                            (PopularMoviesContract.FavouriteEntry.CONTENT_URI, Utility.createContentValues(movie));
                    if (returnUri != null) {
                        Toast.makeText(getContext(), FAV_ADDED, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        return rootView;
    }

    public class FetchTrailers extends AsyncTask<String, Void, List<Trailer>>{

        private final String LOG_TAG = getClass().getSimpleName();

        @Override
        protected List<Trailer> doInBackground(String... params) {
            //get the movie name
            String movieId =  params[0];
            HttpURLConnection httpUrlConn = null;
            InputStreamReader isr;
            BufferedReader br = null;
            String jsonTrailerString = null;

            try{
                Uri builtUri = Uri.parse(Constants.BASE_URL).buildUpon()
                        .appendEncodedPath(movieId)
                        .appendEncodedPath(Constants.ADD_ON_VIDEOS_SEGMENT)
                        .appendQueryParameter(Constants.API_KEY_PARAM,
                                BuildConfig.OPEN_MOVIE_DB_API_KEY).build();

                Log.i(LOG_TAG, "Trailer URI: " +  builtUri.toString());

                //create the url and open the connection
                URL url = new URL(builtUri.toString());
                //open the connection
                httpUrlConn =  (HttpURLConnection)url.openConnection();
                //set the request type
                httpUrlConn.setRequestMethod("GET");
                //connect
                httpUrlConn.connect();
                isr = new InputStreamReader(httpUrlConn.getInputStream());
                if(isr ==  null){
                    return null;
                }
                StringBuffer buffer = new StringBuffer();
                //cretae a new buffered reader
                br = new BufferedReader(isr);
                String line;
                while((line = br.readLine()) != null){
                    buffer.append(line + "\n");
                }

                if(buffer.length() == 0){
                    return null;
                }

                jsonTrailerString = buffer.toString();
                Log.i(LOG_TAG, "JSON Trailer String: " +  jsonTrailerString);
                //here we need to parse the json object and fetch the data
                //that we need
                return getTrailerDataFromJsonString(jsonTrailerString);

            } catch (MalformedURLException e) {
                Log.i(LOG_TAG, "Error: Malformed URI ");
                e.printStackTrace();
            } catch (IOException e) {
                Log.i(LOG_TAG, "Error: I/O Exception ");
                e.printStackTrace();
            }finally{
                if(httpUrlConn != null){
                    httpUrlConn.disconnect();
                }
                if(br != null){
                    try{
                        br.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, " Cannot close stream reader: " + e.getMessage());
                    }
                }
            }
            return null;
        }

        public List<Trailer> getTrailerDataFromJsonString(String trailerString){
            final List<Trailer> trailerList = new ArrayList<>();
            try{
                JSONObject jsonObject = new JSONObject(trailerString);
                JSONArray jsonArray = jsonObject.getJSONArray("results");
                for(int i = 0; i<jsonArray.length(); i++){
                    JSONObject trailerObj = jsonArray.getJSONObject(i);
                    Trailer trailer = new Trailer();
                    trailer.setmTrailerId(trailerObj.getString(Constants.TRAILER_ID_KEY));
                    trailer.setmKey(trailerObj.getString(Constants.TRAILER_KEY));
                    trailer.setName(trailerObj.getString(Constants.TRAILER_NAME_KEY));
                    trailer.setSite(trailerObj.getString(Constants.TRAILER_SITE_KEY));
                    trailerList.add(trailer);
                }
                //return the movie list
                return trailerList;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error creating JSON Object: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Trailer> trailers) {
            TrailerAdapter adapter = new TrailerAdapter(getActivity(), trailers);
            mTrailerListView.setAdapter(adapter);
        }
    }
}
