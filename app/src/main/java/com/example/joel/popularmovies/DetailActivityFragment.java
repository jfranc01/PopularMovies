package com.example.joel.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.joel.popularmovies.adapters.TrailerAdapter;
import com.example.joel.popularmovies.data.PopularMoviesContract;
import com.example.joel.popularmovies.model.Movie;
import com.example.joel.popularmovies.model.Review;
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
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private Movie mMovie = null;
    private LayoutInflater mInflator;
    private String mMovieID;
    private Uri mUri;
    private ImageView mPoster;
    private TextView mTitle;
    private TextView mRelease_date;
    private TextView mRating;
    private TextView mSynopsis;
    private Trailer mFirstTrailer;
    private ImageButton startButton;
    private ShareActionProvider mShareActionProvider;
    private TrailerAdapter mTrailerAdapter;
    private LinearLayout mReviewLayout = null;
    private ListView mTrailerListView = null;
    private final String POPULAR_MOVIES_SHARE_TAG = "#Check out the movie link below: ";
    public final String LOG_TAG = this.getClass().getSimpleName();
    public final String FAV_ADDED = "Movie saved as favourite!!!!!";
    public final String ALREADY_A_FAV = "Already a favourite!!";
    static final String DETAIL_URI = "uri";

    public static final int MOVIE_LOADER = 3;
    public static final int FAV_LOADER = 4;

    public static final String[] DETAIL_COLUMNS = {
            PopularMoviesContract.MovieEntry._ID,
            PopularMoviesContract.MovieEntry.COLUMN_MOVIE_ID,
            PopularMoviesContract.MovieEntry.COLUMN_NAME_TITLE,
            PopularMoviesContract.MovieEntry.COLUMN_NAME_SYNOPSIS,
            PopularMoviesContract.MovieEntry.COLUMN_NAME_RELEASE,
            PopularMoviesContract.MovieEntry.COLUMN_NAME_RATING,
            PopularMoviesContract.MovieEntry.COLUMN_NAME_IMGURL
    };

    public static final int COL_ID = 0;
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_TITLE = 2;
    public static final int COL_SYNOPSIS = 3;
    public static final int COL_RELEASE = 4;
    public static final int COL_RATING = 5;
    public static final int COL_IMGURL = 6;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    /*
        Interface
     */
    public interface Callbacks {
        public void onItemClicked(Uri uri);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mInflator = getLayoutInflater(savedInstanceState);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = null;
        rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailActivityFragment.DETAIL_URI);
            mMovieID = mUri.getPathSegments().get(1);
            Log.i(LOG_TAG, "Passed in URI: " + mUri.toString());
        } else {
            rootView.setVisibility(View.INVISIBLE);
            return rootView;
        }
        rootView.setVisibility(View.VISIBLE);
        mPoster = (ImageView) rootView.findViewById(R.id.detail_poster);
        mTitle = (TextView) rootView.findViewById(R.id.detail_title);
        mRelease_date = (TextView) rootView.findViewById(R.id.detail_release_date);
        mRating = (TextView) rootView.findViewById(R.id.detail_rating);
        mSynopsis = (TextView) rootView.findViewById(R.id.detail_sysnopsis);
        mTrailerListView = (ListView) rootView.findViewById(R.id.traier_list);
        mTrailerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(LOG_TAG, "Item at position " + String.valueOf(position) + " clicked");
                //here we need to create an implicit intent
                Trailer trailer = (Trailer) parent.getItemAtPosition(position);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri youtubeUri = Uri.parse(Constants.YOUTBE_BASE_URI).buildUpon()
                        .appendQueryParameter(Constants.YOUTUBE_PARAM_V, trailer.getmKey()).build();
                intent.setData(youtubeUri);
                startActivity(intent);
            }
        });
        //get a reference to the star button
        startButton = (ImageButton) rootView.findViewById(R.id.starButtton);
        //set on onlicklistener
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(LOG_TAG, "Star button was pressed");
                if (mMovieID != null){
                    //first check if this movie is already a favourite
                    Cursor returnCursor = getContext().getContentResolver().
                            query(
                                    PopularMoviesContract.FavouriteEntry.CONTENT_URI,
                                    MainActivityFragment.FAVOURITE_COLUMNS,
                                    PopularMoviesContract.FavouriteEntry.COLUMN_MOVIE_ID + " = ? ",
                                    new String[]{mMovieID},
                                    null);

                    if (returnCursor != null && returnCursor.moveToFirst()) {
                        if (returnCursor.getString(COL_MOVIE_ID)
                                .equalsIgnoreCase(mMovieID)) {
                            Toast toast = Toast.makeText(getContext(), ALREADY_A_FAV, Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    } else {
                        //here we get access to the content resolver and perform an insert into the database
                        Uri returnUri = getContext().getContentResolver().insert
                                (PopularMoviesContract.FavouriteEntry.CONTENT_URI, Utility.createFavContentValues(mMovie));
                        if (returnUri != null) {
                            Toast.makeText(getContext(), FAV_ADDED, Toast.LENGTH_SHORT).show();
                            startButton.setBackgroundResource(R.drawable.on__star);
                        }
                    }
                }
            }
        });
        //here we query the movie and see if it is a favourite and set the star logo
        Cursor returnCursor = getContext().getContentResolver().
                query(
                        PopularMoviesContract.FavouriteEntry.CONTENT_URI,
                        MainActivityFragment.FAVOURITE_COLUMNS,
                        PopularMoviesContract.FavouriteEntry.COLUMN_MOVIE_ID + " = ? ",
                        new String[]{mMovieID},
                        null);
        if (returnCursor != null && returnCursor.moveToFirst()) {
            if (returnCursor.getString(COL_MOVIE_ID)
                    .equalsIgnoreCase(mMovieID)) {
                startButton.setBackgroundResource(R.drawable.on__star);
            }
        }

        mReviewLayout = (LinearLayout) rootView.findViewById(R.id.review_layout);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        //start the loader to read from the database
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        //retrieve the movie id from the uri
        if (mUri != null) {
            String movieID = PopularMoviesContract.MovieEntry.getMovieIDFromUri(mUri);
            if (movieID != null) {
                //call the task to get the trailers and the reviews
                FetchTrailers fetchTrailers = new FetchTrailers();
                fetchTrailers.execute(movieID);
                //here we fetch the reviews
                FetchReviewsTask fetchReviewsTask = new FetchReviewsTask();
                fetchReviewsTask.execute(movieID);
            }
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.share_menu, menu);
        //Retrieve the menu item
        MenuItem shareItem = menu.findItem(R.id.action_share);
        //get the action Provider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        if (mTrailerAdapter != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            return new CursorLoader(getActivity(), mUri, DETAIL_COLUMNS, null, null, null);
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            mMovie = new Movie();
            mMovie.setmMovieID(data.getString(COL_MOVIE_ID));
            mMovie.setmTtile(data.getString(COL_TITLE));
            mMovie.setmRating(data.getString(COL_RATING));
            mMovie.setmReleaseDate(data.getString(COL_RELEASE));
            mMovie.setmSynopsis(data.getString(COL_SYNOPSIS));
            mMovie.setmImageUrl(data.getString(COL_IMGURL));
            mTitle.setText(mMovie.getmTtile());
            mRating.setText(mMovie.getmRating() + "/10");
            mRelease_date.setText(Utility.formatDate(mMovie.getmReleaseDate()));
            mSynopsis.setText(mMovie.getmSynopsis());
            Picasso.with(getActivity()).load(data.getString(COL_IMGURL)).into(mPoster);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, POPULAR_MOVIES_SHARE_TAG + getFirstTrailerLink());
        return shareIntent;
    }

    private String getFirstTrailerLink() {

        if (mTrailerAdapter != null && mFirstTrailer != null) {
            Uri link = Uri.parse(Constants.YOUTBE_BASE_URI).buildUpon()
                    .appendQueryParameter(Constants.YOUTUBE_PARAM_V,
                            mFirstTrailer.getmKey()).build();

            URL url = null;
            try {
                url = new URL(link.toString());
                return url.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.i(LOG_TAG, "Error getting share link for video: ");
                return null;
            }
        }

        return null;
    }

    /****
     * Method for Setting the Height of the ListView dynamically.
     * *** Hack to fix the issue of not showing all the items of the ListView
     * *** when placed inside a ScrollView
     ****/
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    public class FetchTrailers extends AsyncTask<String, Void, List<Trailer>> {

        private final String LOG_TAG = getClass().getSimpleName();

        @Override
        protected List<Trailer> doInBackground(String... params) {
            //get the movie name
            String movieId = params[0];
            HttpURLConnection httpUrlConn = null;
            InputStreamReader isr;
            BufferedReader br = null;
            String jsonTrailerString = null;

            try {
                Uri builtUri = Uri.parse(Constants.BASE_URL).buildUpon()
                        .appendEncodedPath(movieId)
                        .appendEncodedPath(Constants.ADD_ON_VIDEOS_SEGMENT)
                        .appendQueryParameter(Constants.API_KEY_PARAM,
                                BuildConfig.OPEN_MOVIE_DB_API_KEY).build();

                //Log.i(LOG_TAG, "Trailer URI: " + builtUri.toString());

                //create the url and open the connection
                URL url = new URL(builtUri.toString());
                //open the connection
                httpUrlConn = (HttpURLConnection) url.openConnection();
                //set the request type
                httpUrlConn.setRequestMethod("GET");
                //connect
                httpUrlConn.connect();
                isr = new InputStreamReader(httpUrlConn.getInputStream());
                if (isr == null) {
                    return null;
                }
                StringBuffer buffer = new StringBuffer();
                //cretae a new buffered reader
                br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                jsonTrailerString = buffer.toString();
                //Log.i(LOG_TAG, "JSON Trailer String: " + jsonTrailerString);
                //here we need to parse the json object and fetch the data
                //that we need
                return getTrailerDataFromJsonString(jsonTrailerString);

            } catch (MalformedURLException e) {
                Log.i(LOG_TAG, "Error: Malformed URI ");
                e.printStackTrace();
            } catch (IOException e) {
                Log.i(LOG_TAG, "Error: I/O Exception ");
                e.printStackTrace();
            } finally {
                if (httpUrlConn != null) {
                    httpUrlConn.disconnect();
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

        public List<Trailer> getTrailerDataFromJsonString(String trailerString) {
            final List<Trailer> trailerList = new ArrayList<>();
            try {
                JSONObject jsonObject = new JSONObject(trailerString);
                JSONArray jsonArray = jsonObject.getJSONArray("results");
                for (int i = 0; i < jsonArray.length(); i++) {
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
            if (trailers != null && trailers.size() > 0 && getActivity() != null) {
                mFirstTrailer = trailers.get(0);
                mTrailerAdapter = new TrailerAdapter(getActivity(), trailers);
                mTrailerListView.setAdapter(mTrailerAdapter);
                setListViewHeightBasedOnChildren(mTrailerListView);
                mTrailerListView.setOnTouchListener(new View.OnTouchListener() {
                    // Setting on Touch Listener for handling the touch inside ScrollView
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        // Disallow the touch request for parent scroll on touch of child view
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        return false;
                    }
                });
            }
        }
    }

    public class FetchReviewsTask extends AsyncTask<String, Void, List<Review>> {

        final String LOG_TAG = getClass().getSimpleName();


        @Override
        protected List<Review> doInBackground(String... params) {

            HttpURLConnection httpURLConnection = null;
            BufferedReader br = null;
            InputStreamReader isr = null;
            String movieID = params[0];

            try {
                Uri builtUri = Uri.parse(Constants.BASE_URL)
                        .buildUpon().appendEncodedPath(movieID)
                        .appendEncodedPath(Constants.ADD_ON_REVIEWS_SEGMENT)
                        .appendQueryParameter(Constants.API_KEY_PARAM,
                                BuildConfig.OPEN_MOVIE_DB_API_KEY).build();

                //Log.i(LOG_TAG, "Built Uri: " + builtUri.toString());

                URL url = new URL(builtUri.toString());
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();
                isr = new InputStreamReader(httpURLConnection.getInputStream());
                if (isr == null) {
                    return null;
                }
                br = new BufferedReader(isr);
                String line;
                StringBuffer buffer = new StringBuffer();
                while ((line = br.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                String jsonReviewString = buffer.toString();
                // Log.i(LOG_TAG, "Json review string: " + jsonReviewString);
                return getReviewsFromJsonString(jsonReviewString);

            } catch (MalformedURLException e) {
                Log.i(LOG_TAG, "Error creating url: " + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                Log.i(LOG_TAG, "Error can't open connection: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        public List<Review> getReviewsFromJsonString(String jsonString) {
            final List<Review> reviewList = new ArrayList<>();
            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray jsonArray = jsonObject.getJSONArray("results");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject reviewObj = jsonArray.getJSONObject(i);
                    Review review = new Review();
                    review.setId(reviewObj.getString(Constants.REVIEW_ID_KEY));
                    review.setAuthor(reviewObj.getString(Constants.REVIEW_AUTHOR_KEY));
                    review.setContent(reviewObj.getString(Constants.REVIEW_CONTENT_KEY));
                    review.setUrl(reviewObj.getString(Constants.REVIEW_URL_KEY));
                    reviewList.add(review);
                }
                return reviewList;
            } catch (JSONException e) {
                Log.i(LOG_TAG, "Error can't open create JSON object " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Review> reviews) {
            if (reviews != null && reviews.size() > 0) {
                TextView contentView;
                for (int i = 0; i < reviews.size(); i++) {
                    View review_item = mInflator.inflate(R.layout.review_item, null);
                    contentView = (TextView) review_item.findViewById(R.id.review_content);
                    contentView.setText(reviews.get(i).getContent());
                    mReviewLayout.addView(review_item);
                }
            }
        }
    }
}
