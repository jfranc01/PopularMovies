package com.example.joel.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.joel.popularmovies.data.PopularMoviesContract;
import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    Intent intent;
    public final String LOG_TAG = this.getClass().getSimpleName();
    public final String FAV_ADDED = "Movie saved as favourite!!!!!";
    public final String ALREADY_A_FAV = "Already a favourite!!";
    Movie movie =  null;

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
            }
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ImageView poster = (ImageView)rootView.findViewById(R.id.detail_poster);
        TextView title = (TextView)rootView.findViewById(R.id.detail_title);
        TextView release_date = (TextView)rootView.findViewById(R.id.detail_release_date);
        TextView rating  = (TextView)rootView.findViewById(R.id.detail_rating);
        TextView synopsis = (TextView)rootView.findViewById(R.id.detail_sysnopsis);
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
}
